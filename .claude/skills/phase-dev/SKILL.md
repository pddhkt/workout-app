---
name: phase-dev
description: Develop a phase (evolution) by creating tasks with proper ordering and dependencies. Entry point for phase-level development workflow.
allowed-tools:
  - Task
  - Bash
  - Read
  - AskUserQuestion
  - TodoWrite
---

# Phase Development Workflow

Develop a phase (evolution) by analyzing requirements, creating tasks with proper dependencies, and syncing to Convex. This is the entry point for phase-level development, similar to how `/task-dev` handles individual tasks.

## Understanding the Hierarchy

```
Phase (Evolution) - Big picture milestone (e.g., "v2.0 Authentication System")
    │
    ├── Task FT-001: Add user model & schema
    │   └── Subtasks (created by /task-dev)
    │
    ├── Task FT-002: Add login/logout API (depends on FT-001)
    │   └── Subtasks (created by /task-dev)
    │
    └── Task FT-003: Add login UI (depends on FT-002)
        └── Subtasks (created by /task-dev)
```

**Key Distinction:**
- `/phase-dev` creates **tasks** from a phase description
- `/task-dev` creates **subtasks** from a task

## Usage

```bash
/phase-dev "Add user authentication with email/password login"
```

**Options:**
- `--version N` - Specific phase version to work on
- `--dry-run` - Show plan without syncing to Convex
- `--yolo` - Skip approval, proceed after showing plan

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `CONVEX_SITE_URL` | Convex HTTP site URL | `https://xxx-yyy.convex.site` |
| `CLAUDE_API_KEY` | API key for authentication | `sk-...` |

## Workflow

```
/phase-dev "Description"
    ↓
1. Analyze phase requirements
    ↓
2. Run phase-planning (internal)
   → Creates tasks with ordering
    ↓
3. Display ASCII plan to user
    ↓
4. User approves (or --yolo)
    ↓
5. /task-create syncs to Convex
    ↓
6. Phase record created
    ↓
7. Ready for /task-dev on each task
```

## Phase 1: Analyze Requirements

Parse the phase description to understand:
- Core functionality being added
- Services/domains involved
- External integrations (APIs, SDKs)
- Acceptance criteria (implicit or explicit)

```bash
# Extract key signals from description
DESCRIPTION="$1"

# Detect domains
DOMAINS=""
if echo "$DESCRIPTION" | grep -qiE "ui|page|component|form|button"; then
  DOMAINS="${DOMAINS}frontend,"
fi
if echo "$DESCRIPTION" | grep -qiE "api|endpoint|auth|database|schema"; then
  DOMAINS="${DOMAINS}backend,"
fi
if echo "$DESCRIPTION" | grep -qiE "table|migration|index|query"; then
  DOMAINS="${DOMAINS}database,"
fi
```

## Phase 2: Run Phase Planning

Use the phase-planner agent to create tasks:

```
Use Task tool with subagent_type: "phase-planner"
Prompt:
## Phase Description

{description}

## Instructions

1. Analyze the phase requirements
2. Break into logical tasks (one per focused area)
3. Set task dependencies (database → backend → frontend)
4. Output ASCII task graph + JSON task list

## Expected Output

1. ASCII dependency graph showing task ordering
2. JSON array of tasks with:
   - customId placeholder (will be assigned by Convex)
   - title
   - description
   - type (feature/bugfix/refactor)
   - domain (frontend/backend/database/fullstack)
   - priority (1-4)
   - acceptanceCriteria (array)
   - dependencies (array of task titles or IDs)
```

## Phase 3: Display Plan

Show the plan in ASCII format:

```
=== PHASE PLAN: Authentication System ===

OVERVIEW
--------
Description: Implement complete user authentication with OAuth support
Tasks: 4

TASK DEPENDENCY GRAPH
---------------------
                    ┌─────────────────────────┐
                    │   FT-001: User Model    │
                    │   [database]            │
                    └───────────┬─────────────┘
                                │
              ┌─────────────────┼─────────────────┐
              │                 │                 │
              ▼                 │                 ▼
┌─────────────────────┐         │   ┌─────────────────────┐
│ FT-002: Backend API │         │   │ FT-003: JWT         │
│ [backend]           │         │   │ [backend]           │
│ depends: FT-001     │         │   │ depends: FT-001     │
└─────────┬───────────┘         │   └─────────┬───────────┘
          │                     │             │
          └──────────┬──────────┘─────────────┘
                     │
                     ▼
          ┌─────────────────────┐
          │ FT-004: Login UI    │
          │ [frontend]          │
          │ depends: FT-002,003 │
          └─────────────────────┘

TASK DETAILS
------------
FT-001: Add user model & schema
  Domain: database
  Depends: (none)
  Priority: 1
  Acceptance Criteria:
    - User table exists with email, password_hash, created_at
    - Indexes on email (unique)

FT-002: Add login/logout API endpoints
  Domain: backend
  Depends: FT-001
  Priority: 2
  Acceptance Criteria:
    - POST /api/auth/login returns JWT on valid credentials
    - POST /api/auth/logout invalidates session

FT-003: Add JWT middleware
  Domain: backend
  Depends: FT-001
  Priority: 2
  Acceptance Criteria:
    - Protected routes return 401 without valid token
    - Token payload includes user ID and expiration

FT-004: Add login UI components
  Domain: frontend
  Depends: FT-002, FT-003
  Priority: 3
  Acceptance Criteria:
    - Login form with email/password inputs
    - Error messages on invalid credentials
    - Redirect to dashboard on success

EXECUTION ORDER
---------------
1. FT-001 (database) - Foundation
2. FT-002, FT-003 (parallel) - Both depend only on FT-001
3. FT-004 (frontend) - Depends on backend tasks

=== END PHASE PLAN ===
```

## Phase 4: User Approval

**If not --yolo:**
- Wait for user confirmation
- User can request changes or approve

**If --yolo:**
- Show plan with "YOLO mode: Proceeding without approval..."
- Immediately proceed to Phase 5

## Phase 5: Sync to Convex

Create tasks in Convex using the task-create utility:

```bash
# For each task in the plan:
curl -s -X POST "${CONVEX_SITE_URL}/api/tasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Add user model & schema",
    "description": "Create user table with email, password_hash, and timestamps",
    "type": "feature",
    "domain": "database",
    "priority": 1,
    "status": "pending",
    "acceptanceCriteria": [
      "User table exists with email, password_hash, created_at",
      "Indexes on email (unique)"
    ],
    "dependencies": []
  }'
```

**Update dependencies after all tasks created:**
```bash
# Once all tasks have customIds, update dependencies
curl -s -X PUT "${CONVEX_SITE_URL}/api/tasks/FT-002" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"dependencies": ["FT-001"]}'
```

## Phase 6: Create Phase Record

Create the phase (evolution) record:

```bash
curl -s -X POST "${CONVEX_SITE_URL}/api/phases" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Authentication System",
    "phaseType": "implementation",
    "description": "Implement complete user authentication with email/password login",
    "status": "pending",
    "dependsOn": [],
    "summary": {
      "tasksCreated": 4,
      "domains": ["database", "backend", "frontend"]
    }
  }'
```

## Phase 7: Ready for Task Development

Output next steps:

```
=== Phase Created Successfully ===

Phase: Authentication System (v3)
Tasks Created: 4 (FT-001 through FT-004)

NEXT STEPS:

1. Start with FT-001 (no dependencies):
   /task-dev FT-001

2. After FT-001 completes, work on FT-002 and FT-003 (can be parallel):
   /task-dev FT-002
   /task-dev FT-003

3. Finally, work on FT-004:
   /task-dev FT-004

Or run each task autonomously:
   /task-dev FT-001 --auto
   /task-dev FT-002 --auto
   ...
```

## API Endpoints Used

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/tasks` | POST | Create individual task |
| `/api/tasks/{id}` | PUT | Update task (set dependencies) |
| `/api/phases` | POST | Create phase record |
| `/api/phases` | GET | List existing phases |

## Bash Helper Functions

```bash
# Create a task and return its customId
create_task() {
  local title="$1"
  local description="$2"
  local type="${3:-feature}"
  local domain="${4:-fullstack}"
  local priority="${5:-2}"

  RESPONSE=$(curl -s -X POST "${CONVEX_SITE_URL}/api/tasks" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "{
      \"title\": \"${title}\",
      \"description\": \"${description}\",
      \"type\": \"${type}\",
      \"domain\": \"${domain}\",
      \"priority\": ${priority},
      \"status\": \"pending\"
    }")

  echo "$RESPONSE" | jq -r '.customId'
}

# Update task dependencies
set_dependencies() {
  local task_id="$1"
  shift
  local deps="[$(printf '"%s",' "$@" | sed 's/,$//')]"

  curl -s -X PUT "${CONVEX_SITE_URL}/api/tasks/${task_id}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "{\"dependencies\": ${deps}}"
}

# Create phase record
create_phase() {
  local name="$1"
  local description="$2"
  local tasks_created="$3"

  curl -s -X POST "${CONVEX_SITE_URL}/api/phases" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"${name}\",
      \"phaseType\": \"implementation\",
      \"description\": \"${description}\",
      \"status\": \"pending\",
      \"summary\": {
        \"tasksCreated\": ${tasks_created}
      }
    }"
}
```

## Error Handling

| Scenario | Action |
|----------|--------|
| Invalid description | Ask for more details |
| Task creation fails | Rollback created tasks, report error |
| Dependency cycle detected | Report and ask user to resolve |
| Phase already exists | Offer to update or create new version |

## Integration with Other Skills

```
┌─────────────┐
│ /phase-dev  │  (this skill - creates tasks from phase)
└──────┬──────┘
       │ creates tasks
       ▼
┌─────────────┐
│ /task-dev   │  (for each task - creates subtasks)
└──────┬──────┘
       │ creates subtasks
       ▼
┌─────────────┐
│  /subtask   │  (execute individual subtasks)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ /qa-review  │  (validate completed task)
└─────────────┘
```

## Example Session

```bash
# User initiates phase development
/phase-dev "Add user authentication with email/password and session management"

# Claude runs phase-planner agent
# → Generates task breakdown

# Claude displays ASCII plan
# === PHASE PLAN: Authentication System ===
# [ASCII diagram shown]
# === END PHASE PLAN ===

# User: "Looks good, proceed"

# Claude syncs to Convex
# → Creates FT-001, FT-002, FT-003, FT-004
# → Sets dependencies
# → Creates phase record

# Claude outputs next steps
# Ready for /task-dev FT-001
```

## Related Skills

| Skill | Relationship |
|-------|--------------|
| `/phase-planning` | Internal planning logic (used by this skill) |
| `/task-dev` | Execute individual tasks after phase is created |
| `/task-create` | Utility to sync tasks to Convex |
| `/task-dev --auto` | Autonomous execution of individual tasks |
| `/ideation` | Generate phase ideas from brainstorming |
| `/analyze` | Market research to inform phase planning |
