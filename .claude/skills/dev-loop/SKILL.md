---
name: dev-loop
description: Automated execution loop that runs pre-planned tasks from Convex until completion criteria are met. The execution layer that implements work created by /phase-init.
allowed-tools:
  - Task
  - Bash
  - Read
  - AskUserQuestion
  - TodoWrite
---

# Dev-Loop Execution System

Automated execution loop that reads tasks from Convex and executes them via `/task-dev` until completion criteria are met. This is the **execution layer** - it does NOT plan, just executes pre-planned tasks.

## Two-Layer Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  LAYER 1: PLANNING (Creates things)                         │
│                                                             │
│  /phase-init "Add auth"     → Creates Phase + Tasks         │
│  /task-init "Add button"    → Creates single Task           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼ (Tasks ready in Convex)
┌─────────────────────────────────────────────────────────────┐
│  LAYER 2: EXECUTION (Implements things)                     │
│                                                             │
│  /task-dev FT-001           → Implements single task        │
│  /dev-loop --until-phase 2  → Loops task-dev until done     │
│  /ad-hoc "Quick fix"        → Implement without tracking    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Usage

```bash
/dev-loop --until-phase 2        # Execute through phase 2
/dev-loop --until-task FT-010    # Execute until specific task done
/dev-loop --all-pending          # Execute all pending tasks
/dev-loop --max-cycles 20        # Safety limit (default: 50)
/dev-loop --batch-size 3         # Tasks per cycle (default: 1)
/dev-loop --resume               # Resume interrupted session
```

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `CONVEX_SITE_URL` | Convex HTTP site URL | `https://xxx-yyy.convex.site` |
| `CLAUDE_API_KEY` | API key for authentication | `sk-...` |

## Workflow Overview

```
/dev-loop --until-phase 2
    ↓
1. Check for existing session (--resume or active marker)
    ↓
2. Create loop session in Convex
    ↓
3. Write local marker (.claude/dev-loop-active)
    ↓
4. Fetch pending tasks from Convex
    ↓
5. Select batch based on dependencies and phase
    ↓
6. Execute batch via /task-dev
    ↓
7. Update session (cycle count, completed tasks)
    ↓
8. Check completion criteria
    ↓
9. If not complete → Loop back to step 4
    ↓
10. If complete → Clean up session and marker
```

---

## Completion Criteria Options

| Flag | Logic |
|------|-------|
| `--until-task FT-010` | Stop when FT-010.status == "completed" |
| `--until-phase 2` | Stop when all tasks with phaseNumber <= 2 are completed |
| `--all-pending` | Stop when no pending tasks remain |
| `--until-milestone "Auth"` | Stop when all tasks in milestone complete |
| `--max-cycles N` | Safety limit - stop after N cycles regardless |

---

## Phase 1: Initialize Session

### Check for Existing Session

```bash
source .env.local 2>/dev/null || source .env 2>/dev/null || true

# Check local marker
if [ -f ".claude/dev-loop-active" ]; then
  SESSION_ID=$(grep "session_id=" .claude/dev-loop-active | cut -d= -f2)

  if [ -n "$SESSION_ID" ]; then
    # Fetch session from Convex
    SESSION=$(curl -s -X GET "${CONVEX_SITE_URL}/api/loop-sessions/${SESSION_ID}" \
      -H "Authorization: Bearer ${CLAUDE_API_KEY}")

    if echo "$SESSION" | jq -e '.active' > /dev/null 2>&1; then
      echo "Found active session: ${SESSION_ID}"
      # Resume mode
    fi
  fi
fi
```

### Create New Session

```bash
# Parse completion criteria from arguments
COMPLETION_TYPE="all-pending"  # default
COMPLETION_VALUE=""

if [ -n "$UNTIL_PHASE" ]; then
  COMPLETION_TYPE="until-phase"
  COMPLETION_VALUE="$UNTIL_PHASE"
elif [ -n "$UNTIL_TASK" ]; then
  COMPLETION_TYPE="until-task"
  COMPLETION_VALUE="$UNTIL_TASK"
elif [ -n "$UNTIL_MILESTONE" ]; then
  COMPLETION_TYPE="until-milestone"
  COMPLETION_VALUE="$UNTIL_MILESTONE"
fi

# Create session in Convex
SESSION=$(curl -s -X POST "${CONVEX_SITE_URL}/api/loop-sessions" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "{
    \"active\": true,
    \"cycle\": 0,
    \"maxCycles\": ${MAX_CYCLES:-50},
    \"completionType\": \"${COMPLETION_TYPE}\",
    \"completionValue\": \"${COMPLETION_VALUE}\",
    \"batchSize\": ${BATCH_SIZE:-1},
    \"tasksCompleted\": [],
    \"tasksBlocked\": [],
    \"currentBatch\": []
  }")

SESSION_ID=$(echo "$SESSION" | jq -r '._id')

# Write local marker
mkdir -p .claude
echo "session_id=${SESSION_ID}" > .claude/dev-loop-active
```

---

## Phase 2: Batch Selection Algorithm

Select next batch of tasks to execute:

```bash
fetch_next_batch() {
  # 1. Fetch pending tasks
  TASKS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks?status=pending" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}")

  # 2. Filter to tasks with satisfied dependencies
  # 3. Sort by phaseNumber (lowest first)
  # 4. Group by domain within phase (efficiency)
  # 5. Select up to batch_size tasks

  BATCH=$(echo "$TASKS" | jq -c --arg batch "$BATCH_SIZE" '
    .tasks
    | map(select(.dependencies | all(. as $dep |
        ($tasks | map(select(.customId == $dep and .status == "completed")) | length > 0) or
        ($dep | length == 0)
      )))
    | sort_by(.phaseNumber)
    | .[0:($batch | tonumber)]
    | map(.customId)
  ')

  echo "$BATCH"
}
```

**Batch Selection Logic:**

1. Fetch all pending tasks from Convex
2. Filter to tasks where all dependencies are completed
3. Sort by phaseNumber (lowest first)
4. Group by domain within same phase for efficiency
5. Select up to `batch_size` tasks
6. Return as comma-separated IDs for `/task-dev`

**Example:**
```
Phase 1: FT-001 (db), FT-002 (backend) → Batch 1 (if deps satisfied)
Phase 2: FT-003, FT-004 (frontend, depend on phase 1) → Batch 2
```

---

## Phase 3: Execute Batch

Execute selected tasks via `/task-dev`:

```bash
execute_batch() {
  local BATCH="$1"  # e.g., "FT-001,FT-002"

  # Update session with current batch
  curl -s -X PATCH "${CONVEX_SITE_URL}/api/loop-sessions/${SESSION_ID}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "{\"currentBatch\": $(echo "$BATCH" | jq -R 'split(\",\")')}"

  # Execute via task-dev with --auto flag
  # Use Task tool with task-dev to implement
  echo "Executing batch: ${BATCH}"
}
```

**Execution Method:**

For each task in batch, use the Task tool:

```
Use Task tool with subagent_type: "task-dev"
Prompt:
Execute task ${TASK_ID} in autonomous mode (--auto).

The dev-loop is orchestrating this execution. After completion:
- Mark task as completed
- Return summary of what was implemented
- Report any blockers

Do NOT ask for user approval - this is automated execution.
```

---

## Phase 4: Update Session

After each cycle:

```bash
update_session() {
  local COMPLETED="$1"  # Comma-separated completed task IDs
  local BLOCKED="$2"    # Comma-separated blocked task IDs

  # Increment cycle count
  CYCLE=$((CYCLE + 1))

  # Update session
  curl -s -X PATCH "${CONVEX_SITE_URL}/api/loop-sessions/${SESSION_ID}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "{
      \"cycle\": ${CYCLE},
      \"tasksCompleted\": $(echo "$COMPLETED" | jq -R 'split(\",\")'),
      \"tasksBlocked\": $(echo "$BLOCKED" | jq -R 'split(\",\")'),
      \"currentBatch\": [],
      \"lastCycleAt\": $(date +%s000)
    }"
}
```

---

## Phase 5: Check Completion

Check if loop should continue or stop:

```bash
check_completion() {
  case "$COMPLETION_TYPE" in
    "until-task")
      # Check if specific task is completed
      TASK_STATUS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${COMPLETION_VALUE}" \
        -H "Authorization: Bearer ${CLAUDE_API_KEY}" | jq -r '.status')
      [ "$TASK_STATUS" == "completed" ]
      ;;

    "until-phase")
      # Check if all tasks in phases <= N are completed
      PENDING=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks?status=pending&maxPhase=${COMPLETION_VALUE}" \
        -H "Authorization: Bearer ${CLAUDE_API_KEY}" | jq '.tasks | length')
      [ "$PENDING" -eq 0 ]
      ;;

    "all-pending")
      # Check if any pending tasks remain
      PENDING=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks?status=pending" \
        -H "Authorization: Bearer ${CLAUDE_API_KEY}" | jq '.tasks | length')
      [ "$PENDING" -eq 0 ]
      ;;

    "until-milestone")
      # Check if all tasks in milestone are completed
      PENDING=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks?milestone=${COMPLETION_VALUE}&status=pending" \
        -H "Authorization: Bearer ${CLAUDE_API_KEY}" | jq '.tasks | length')
      [ "$PENDING" -eq 0 ]
      ;;
  esac
}
```

---

## Phase 6: Cleanup

When loop completes or is cancelled:

```bash
cleanup_session() {
  # Delete session from Convex
  curl -s -X DELETE "${CONVEX_SITE_URL}/api/loop-sessions/${SESSION_ID}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}"

  # Remove local marker
  rm -f .claude/dev-loop-active

  echo "Dev-loop session ended"
}
```

---

## Stop Hook Integration

The dev-loop uses a Stop hook (`core/hooks/dev-loop-stop.sh`) that:

1. Checks if `.claude/dev-loop-active` marker exists
2. If yes, fetches session state from Convex
3. Checks completion criteria
4. If complete → cleans up and allows exit
5. If not complete → outputs JSON to continue loop

**Hook Configuration** (`core/hooks/hooks.json`):

```json
{
  "hooks": {
    "Stop": [{
      "matcher": "",
      "hooks": [{
        "type": "command",
        "command": "${CLAUDE_PLUGIN_ROOT}/hooks/dev-loop-stop.sh"
      }]
    }]
  }
}
```

---

## Safety Features

### Max Cycles
```bash
if [ "$CYCLE" -ge "$MAX_CYCLES" ]; then
  echo "WARNING: Max cycles (${MAX_CYCLES}) reached. Stopping loop."
  cleanup_session
  exit 0
fi
```

### Blocked Task Handling
```bash
if [ "$BLOCKED_COUNT" -gt 0 ]; then
  echo "WARNING: ${BLOCKED_COUNT} tasks are blocked"
  # Continue with remaining tasks
  # Don't fail the whole loop
fi
```

### No Progress Detection
```bash
if [ "$COMPLETED_THIS_CYCLE" -eq 0 ] && [ "$BATCH_SIZE" -gt 0 ]; then
  NO_PROGRESS_COUNT=$((NO_PROGRESS_COUNT + 1))
  if [ "$NO_PROGRESS_COUNT" -ge 3 ]; then
    echo "ERROR: No progress for 3 cycles. Stopping to avoid infinite loop."
    cleanup_session
    exit 1
  fi
fi
```

---

## Resume Functionality

Resume an interrupted session:

```bash
/dev-loop --resume
```

**Resume Logic:**

1. Read session ID from `.claude/dev-loop-active`
2. Fetch session state from Convex
3. Get list of completed tasks
4. Continue from last cycle
5. Works across machines (session in Convex)

---

## Progress Display

During execution, display progress:

```
=== DEV-LOOP PROGRESS ===

Session: abc123
Completion: until-phase 2
Cycle: 3 / 50 (max)

Tasks Completed: 5
Tasks Blocked: 1
Tasks Remaining: 8

Current Batch:
  → FT-006: Add user settings page [in_progress]
  → FT-007: Add notification preferences [pending]

Phase Progress:
  Phase 1: ████████████████████ 100% (3/3)
  Phase 2: ████████░░░░░░░░░░░░  40% (2/5)
  Phase 3: ░░░░░░░░░░░░░░░░░░░░   0% (0/6)

=========================
```

---

## Error Handling

| Scenario | Action |
|----------|--------|
| Task fails | Mark as blocked, continue with others |
| API unavailable | Retry 3 times, then pause |
| No eligible tasks | Check for dependency cycles |
| Max cycles reached | Stop gracefully, report status |
| Session not found | Create new session |

---

## Example Session

```bash
# User creates phase (Layer 1 - Planning)
> /phase-init "Add user authentication with OAuth"

Phase created with tasks:
- FT-015: OAuth provider setup (Phase 2, backend)
- FT-016: OAuth callback handler (Phase 2, backend, deps: FT-015)
- FT-017: Login with Google button (Phase 3, frontend)
- FT-018: Login with GitHub button (Phase 3, frontend)
- FT-019: OAuth session management (Phase 3, frontend, deps: FT-016)

# User starts execution (Layer 2 - Execution)
> /dev-loop --until-phase 3

=== DEV-LOOP STARTED ===
Session: xyz789
Completion: until-phase 3
Max Cycles: 50

Cycle 1:
  Batch: FT-015 (OAuth provider setup)
  → Executing via /task-dev FT-015 --auto
  → Completed successfully

Cycle 2:
  Batch: FT-016 (OAuth callback handler)
  → Executing via /task-dev FT-016 --auto
  → Completed successfully

Cycle 3:
  Batch: FT-017, FT-018 (parallel, no deps between them)
  → Executing via /task-dev FT-017,FT-018 --auto
  → Both completed successfully

Cycle 4:
  Batch: FT-019 (OAuth session management)
  → Executing via /task-dev FT-019 --auto
  → Completed successfully

=== DEV-LOOP COMPLETE ===
Cycles: 4
Tasks Completed: 5
Tasks Blocked: 0

All tasks through phase 3 are complete!
```

---

## API Endpoints Used

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/loop-sessions` | POST | Create new session |
| `/api/loop-sessions/active` | GET | Get active session |
| `/api/loop-sessions/{id}` | GET | Get session by ID |
| `/api/loop-sessions/{id}` | PATCH | Update session |
| `/api/loop-sessions/{id}` | DELETE | End session |
| `/api/tasks` | GET | Fetch tasks with filters |
| `/api/tasks/{id}` | GET | Get single task |

---

## Convex Schema (loopSessions table)

```typescript
loopSessions: defineTable({
  projectId: v.id("projects"),
  active: v.boolean(),
  cycle: v.number(),
  maxCycles: v.number(),
  completionType: v.string(),  // "until-phase" | "until-task" | "all-pending" | "until-milestone"
  completionValue: v.string(), // phase number, task ID, or milestone name
  batchSize: v.number(),
  tasksCompleted: v.array(v.string()),
  tasksBlocked: v.array(v.string()),
  currentBatch: v.array(v.string()),
  startedAt: v.number(),
  lastCycleAt: v.number(),
})
```

---

## Related Skills

| Skill | Relationship |
|-------|--------------|
| `/phase-init` | Creates phases and tasks (planning layer) |
| `/task-init` | Creates single tasks (planning layer) |
| `/task-dev` | Implements individual tasks (called by dev-loop) |
| `/dev-loop-cancel` | Cancels active loop |
| `/ad-hoc` | Quick implementation without tracking |
