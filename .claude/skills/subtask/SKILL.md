---
name: subtask
description: Execute a single subtask with focused context. Used by the autonomous build loop to implement individual atomic units of work.
---

# Subtask Execution Skill

Executes a single subtask from an implementation plan with focused context. This skill is the workhorse of the autonomous build pipeline.

## Key Principles

1. **One subtask per session** - Fresh context, no accumulated confusion
2. **Focused prompt** - ~100-200 tokens, not 900
3. **Immediate status update** - Python bookkeeping, not agent memory
4. **Dependency respect** - Only execute if dependencies are complete

## Usage

```bash
/subtask FT-001 backend-auth-1
```

Or to get the next pending subtask:

```bash
/subtask FT-001 --next
```

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `CONVEX_SITE_URL` | Convex HTTP site URL | `https://xxx-yyy.convex.site` |
| `CLAUDE_API_KEY` | API key for authentication | `sk-...` |

## API Endpoints

### GET /api/tasks/{customId}/subtasks

List all subtasks for a task.

```bash
curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Response:**
```json
{
  "subtasks": [
    {
      "_id": "abc123",
      "taskId": "xyz789",
      "subtaskId": "backend-auth-1",
      "phaseNumber": 1,
      "description": "Create user authentication models",
      "domain": "backend",
      "status": "completed",
      "dependsOn": [],
      "filesToModify": [],
      "filesToCreate": ["src/models/auth.ts"],
      "verification": {
        "type": "command",
        "command": "npm run test:auth"
      },
      "attemptCount": 1,
      "startedAt": 1705000000000,
      "completedAt": 1705001000000
    }
  ]
}
```

### GET /api/tasks/{customId}/subtasks/next

Get the next pending subtask (respecting dependencies).

```bash
curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks/next" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Response:**
```json
{
  "subtask": {
    "subtaskId": "backend-auth-2",
    "description": "Create authentication middleware",
    "domain": "backend",
    "status": "pending",
    "dependsOn": ["backend-auth-1"],
    "filesToCreate": ["src/middleware/auth.ts"]
  }
}
```

Or if no subtasks are available:
```json
{
  "subtask": null,
  "message": "No pending subtasks available"
}
```

### GET /api/tasks/{customId}/subtasks/progress

Get progress statistics.

```bash
curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks/progress" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Response:**
```json
{
  "total": 8,
  "completed": 5,
  "inProgress": 1,
  "pending": 2,
  "failed": 0,
  "blocked": 0,
  "percentComplete": 62
}
```

### POST /api/tasks/{customId}/subtasks

Create subtasks (bulk).

```bash
curl -s -X POST "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "subtasks": [
      {
        "subtaskId": "backend-auth-1",
        "phaseNumber": 1,
        "description": "Create user authentication models",
        "domain": "backend",
        "filesToCreate": ["src/models/auth.ts"],
        "verification": {
          "type": "command",
          "command": "npm run test:auth"
        }
      }
    ]
  }'
```

### PATCH /api/tasks/{customId}/subtasks/{subtaskId}

Update subtask status.

```bash
curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks/backend-auth-1" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"status": "completed", "notes": "Implemented with bcrypt hashing"}'
```

**Request body:**
- `status` (required) - `pending`, `in_progress`, `completed`, `failed`, `blocked`
- `blockedReason` (optional) - Reason if status is `blocked`
- `notes` (optional) - Implementation notes

## Subtask Statuses

| Status | Description |
|--------|-------------|
| `pending` | Not yet started |
| `in_progress` | Currently being worked on |
| `completed` | Successfully finished |
| `failed` | Implementation failed (can be retried) |
| `blocked` | Cannot proceed (requires resolution) |

## Execution Workflow

### 1. Get Next Subtask

```bash
NEXT=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks/next" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

SUBTASK_ID=$(echo "$NEXT" | jq -r '.subtask.subtaskId')

if [ "$SUBTASK_ID" = "null" ]; then
  echo "All subtasks complete!"
  exit 0
fi
```

### 2. Mark In Progress

```bash
curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks/${SUBTASK_ID}" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"status": "in_progress"}'
```

### 3. Execute Implementation

Focus on ONLY the files listed in `filesToModify` and `filesToCreate`.

### 4. Run Verification (if specified)

```bash
VERIFICATION=$(echo "$NEXT" | jq -r '.subtask.verification')
if [ "$VERIFICATION" != "null" ]; then
  VERIFY_CMD=$(echo "$VERIFICATION" | jq -r '.command')
  if [ -n "$VERIFY_CMD" ]; then
    eval "$VERIFY_CMD"
  fi
fi
```

### 5. Mark Complete

```bash
curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks/${SUBTASK_ID}" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"status": "completed", "notes": "Implemented successfully"}'
```

## Focused Prompt Template

When executing a subtask, generate a focused prompt:

```markdown
## Subtask: {subtaskId}

**Description:** {description}

**Domain:** {domain}

### Files to Create
{filesToCreate}

### Files to Modify
{filesToModify}

### Patterns to Follow
Reference these files for patterns:
{patternsFrom}

### Verification
{verification.description || verification.command}

---

Implement this subtask. Focus ONLY on the files listed above.
Do not modify other files unless absolutely necessary.
```

## Bash Helper Functions

```bash
# Get next pending subtask
get_next_subtask() {
  local task_id="$1"
  curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${task_id}/subtasks/next" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}"
}

# Update subtask status
update_subtask_status() {
  local task_id="$1"
  local subtask_id="$2"
  local status="$3"
  local notes="${4:-}"

  local body="{\"status\": \"${status}\""
  if [ -n "$notes" ]; then
    body="${body}, \"notes\": \"${notes}\""
  fi
  body="${body}}"

  curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/${task_id}/subtasks/${subtask_id}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "$body"
}

# Get progress stats
get_progress() {
  local task_id="$1"
  curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${task_id}/subtasks/progress" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}"
}

# Check if all subtasks complete
is_all_complete() {
  local task_id="$1"
  local progress=$(get_progress "$task_id")
  local total=$(echo "$progress" | jq -r '.total')
  local completed=$(echo "$progress" | jq -r '.completed')
  [ "$total" -eq "$completed" ]
}
```

## Error Handling

| HTTP Status | Meaning |
|-------------|---------|
| 200 | Success |
| 400 | Invalid status or missing fields |
| 401 | Unauthorized |
| 404 | Task or subtask not found |

## Recovery Patterns

### On Failure

```bash
# Mark failed
update_subtask_status "FT-001" "backend-auth-1" "failed" "TypeError in middleware"

# Later: Reset failed subtasks
curl -s -X POST "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks/reset-failed" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

### On Block

```bash
# Mark blocked with reason
curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks/backend-auth-1" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"status": "blocked", "blockedReason": "Waiting for API key configuration"}'
```

## Example Complete Session

```bash
#!/bin/bash
source .env

TASK_ID="FT-001"

# Get next subtask
NEXT=$(get_next_subtask "$TASK_ID")
SUBTASK=$(echo "$NEXT" | jq -r '.subtask')

if [ "$SUBTASK" = "null" ]; then
  echo "All subtasks complete!"
  exit 0
fi

SUBTASK_ID=$(echo "$SUBTASK" | jq -r '.subtaskId')
DESCRIPTION=$(echo "$SUBTASK" | jq -r '.description')
DOMAIN=$(echo "$SUBTASK" | jq -r '.domain')

echo "=== Executing Subtask: $SUBTASK_ID ==="
echo "Description: $DESCRIPTION"
echo "Domain: $DOMAIN"

# Mark in progress
update_subtask_status "$TASK_ID" "$SUBTASK_ID" "in_progress"

# Generate focused prompt and execute...
# (Agent implementation happens here)

# On success:
update_subtask_status "$TASK_ID" "$SUBTASK_ID" "completed" "Implemented successfully"

# Check overall progress
PROGRESS=$(get_progress "$TASK_ID")
echo "Progress: $(echo $PROGRESS | jq -r '.percentComplete')% complete"
```
