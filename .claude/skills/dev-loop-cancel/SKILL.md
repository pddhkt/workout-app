---
name: dev-loop-cancel
description: Cancel an active dev-loop session and clean up resources.
---

# Dev-Loop Cancel

Cancel an active dev-loop session and clean up both local and Convex resources.

## Usage

```bash
/dev-loop-cancel              # Cancel active session
/dev-loop-cancel --force      # Force cancel even if tasks in progress
```

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `CONVEX_SITE_URL` | Convex HTTP site URL | `https://xxx-yyy.convex.site` |
| `CLAUDE_API_KEY` | API key for authentication | `sk-...` |

## Workflow

```
/dev-loop-cancel
    ↓
1. Check for local marker (.claude/dev-loop-active)
    ↓
2. Read session ID from marker
    ↓
3. Fetch session state from Convex
    ↓
4. If tasks in progress → ask for confirmation (unless --force)
    ↓
5. Delete session from Convex
    ↓
6. Remove local marker file
    ↓
7. Report cancellation summary
```

---

## Implementation

### Check for Active Session

```bash
source .env.local 2>/dev/null || source .env 2>/dev/null || true

MARKER_FILE=".claude/dev-loop-active"

if [ ! -f "$MARKER_FILE" ]; then
  echo "No active dev-loop session found."
  exit 0
fi

SESSION_ID=$(grep "session_id=" "$MARKER_FILE" | cut -d= -f2)

if [ -z "$SESSION_ID" ]; then
  echo "Invalid marker file. Cleaning up..."
  rm -f "$MARKER_FILE"
  exit 0
fi
```

### Fetch Session State

```bash
SESSION=$(curl -s -X GET "${CONVEX_SITE_URL}/api/loop-sessions/${SESSION_ID}" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

if [ -z "$SESSION" ] || echo "$SESSION" | jq -e '.error' > /dev/null 2>&1; then
  echo "Session not found in Convex. Cleaning up local marker..."
  rm -f "$MARKER_FILE"
  exit 0
fi

ACTIVE=$(echo "$SESSION" | jq -r '.active')
CYCLE=$(echo "$SESSION" | jq -r '.cycle')
TASKS_COMPLETED=$(echo "$SESSION" | jq -r '.tasksCompleted | length')
CURRENT_BATCH=$(echo "$SESSION" | jq -r '.currentBatch | length')
```

### Check for In-Progress Tasks

```bash
if [ "$CURRENT_BATCH" -gt 0 ] && [ "$FORCE" != "true" ]; then
  echo "WARNING: There are tasks currently in progress."
  echo ""
  echo "Current batch:"
  echo "$SESSION" | jq -r '.currentBatch[]' | sed 's/^/  - /'
  echo ""

  # Use AskUserQuestion to confirm
  # Options:
  # 1. "Cancel anyway" - Force cancel
  # 2. "Wait for batch to complete" - Don't cancel
  # 3. "Mark tasks as pending" - Reset in-progress tasks
fi
```

### Delete Session and Cleanup

```bash
# Delete session from Convex
curl -s -X DELETE "${CONVEX_SITE_URL}/api/loop-sessions/${SESSION_ID}" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"

# Remove local marker
rm -f "$MARKER_FILE"

# Report summary
cat << EOF

=== DEV-LOOP CANCELLED ===

Session: ${SESSION_ID}
Cycles Completed: ${CYCLE}
Tasks Completed: ${TASKS_COMPLETED}

The dev-loop has been cancelled. Any in-progress tasks remain in their current state.

To resume work manually:
  /task-list pending    View remaining tasks
  /task-dev FT-XXX      Continue with specific task

==========================

EOF
```

---

## Confirmation Flow

If tasks are in progress, ask user:

```
questions: [
  {
    question: "There are tasks currently in progress. How would you like to proceed?",
    header: "Cancel",
    multiSelect: false,
    options: [
      {label: "Cancel anyway", description: "Stop the loop, tasks remain in_progress"},
      {label: "Wait for completion", description: "Let current batch finish first"},
      {label: "Reset and cancel", description: "Reset in_progress tasks to pending, then cancel"}
    ]
  }
]
```

---

## Error Handling

| Scenario | Action |
|----------|--------|
| No marker file | Report no active session |
| Invalid session ID | Clean up marker, report |
| Session not in Convex | Clean up marker, report |
| API unavailable | Retry, then clean up local only |
| Tasks in progress | Ask for confirmation |

---

## Example Session

```bash
# User starts a dev-loop
> /dev-loop --until-phase 2

=== DEV-LOOP STARTED ===
Session: abc123
...

# User wants to cancel mid-execution
> /dev-loop-cancel

Current session state:
  Session: abc123
  Cycle: 3 / 50
  Tasks Completed: 5
  Tasks In Progress: 1 (FT-008)

There is a task currently in progress.

[AskUserQuestion: How to proceed?]

User: "Cancel anyway"

=== DEV-LOOP CANCELLED ===

Session: abc123
Cycles Completed: 3
Tasks Completed: 5

Note: FT-008 remains in_progress. You can:
  - Resume: /task-dev FT-008
  - Reset: /task-list reset FT-008

==========================
```

---

## Related Skills

| Skill | Relationship |
|-------|--------------|
| `/dev-loop` | Main execution loop (this cancels it) |
| `/task-dev` | Continue individual tasks after cancel |
| `/task-list` | View task states after cancel |
