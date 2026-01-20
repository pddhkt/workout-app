---
name: task
description: Task API integration for fetching task data and updating status from Convex backend. Used by /task-dev command to load task context.
---

# Task API Skill

Provides functions to interact with the task manager's Convex API for loading task context and updating status.

## Environment Variables

Required environment variables (set in shell or `.env`):

| Variable          | Description                                                     | Example                       |
| ----------------- | --------------------------------------------------------------- | ----------------------------- |
| `CONVEX_SITE_URL` | Convex HTTP site URL (note: `.convex.site` not `.convex.cloud`) | `https://xxx-yyy.convex.site` |
| `CLAUDE_API_KEY`  | API key for authentication                                      | `sk-...`                      |

**Important:** HTTP endpoints use `.convex.site` domain, not `.convex.cloud`.

## API Endpoints

### GET /api/tasks/{customId}

Fetch task details by custom ID.

```bash
curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/FT-001" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json"
```

**Response fields:**

- `_id` - Internal Convex ID
- `customId` - Display ID (e.g., "FT-001")
- `projectId` - Parent project reference
- `type` - "feature" | "bugfix" | "refactor"
- `title` - Task title
- `description` - Task description (may be null)
- `status` - "pending" | "in_progress" | "completed" | "blocked"
- `priority` - 1 (critical) to 4 (low)
- `images` - Array of storage IDs (may be null)
- `blockedReason` - Why task is blocked (if status is blocked)
- `createdAt` - Creation timestamp
- `updatedAt` - Last update timestamp

### PATCH /api/tasks/{customId}/status

Update task status.

```bash
curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/FT-001/status" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"status": "in_progress"}'
```

**Request body:**

- `status` (required) - New status value
- `blockedReason` (required if status is "blocked") - Reason for blocking

## Task Type Prefixes

| Prefix | Type     | Workflow                                |
| ------ | -------- | --------------------------------------- |
| FT-xxx | feature  | Scout → Plan → Implement → Integrate    |
| BF-xxx | bugfix   | Scout → Diagnose → Fix → Test           |
| RF-xxx | refactor | Scout → Plan → Test → Refactor → Verify |

## Valid Statuses

| Status        | Description                             |
| ------------- | --------------------------------------- |
| `pending`     | Task not started                        |
| `in_progress` | Currently being worked on               |
| `completed`   | Successfully finished                   |
| `blocked`     | Cannot proceed (requires blockedReason) |

## Status Transitions

When using `/task` command:

1. **On start**: Status set to `in_progress`
2. **On success**: Status set to `completed`
3. **On failure**: Status remains `in_progress` (manual resolution needed)

## Bash Helper Functions

```bash
# Fetch task data by customId
fetch_task() {
  local task_id="$1"
  curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${task_id}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json"
}

# Update task status
update_task_status() {
  local task_id="$1"
  local status="$2"
  local blocked_reason="${3:-}"

  if [ -n "$blocked_reason" ]; then
    curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/${task_id}/status" \
      -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
      -H "Content-Type: application/json" \
      -d "{\"status\": \"${status}\", \"blockedReason\": \"${blocked_reason}\"}"
  else
    curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/${task_id}/status" \
      -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
      -H "Content-Type: application/json" \
      -d "{\"status\": \"${status}\"}"
  fi
}

# Parse task type from customId prefix
get_task_type() {
  local task_id="$1"
  case "${task_id:0:2}" in
    FT) echo "feature" ;;
    BF) echo "bugfix" ;;
    RF) echo "refactor" ;;
    *) echo "unknown" ;;
  esac
}
```

## Error Handling

| HTTP Status | Meaning                                             |
| ----------- | --------------------------------------------------- |
| 200         | Success                                             |
| 400         | Bad request (invalid status, missing blockedReason) |
| 401         | Unauthorized (invalid or missing API key)           |
| 404         | Task not found                                      |

## Example Usage

```bash
# Load environment
source .env 2>/dev/null || true

# Fetch task
TASK_JSON=$(fetch_task "FT-001")

# Check if task exists
if echo "$TASK_JSON" | grep -q '"error"'; then
  echo "Task not found"
  exit 1
fi

# Extract fields with jq
TITLE=$(echo "$TASK_JSON" | jq -r '.title')
DESCRIPTION=$(echo "$TASK_JSON" | jq -r '.description // "No description"')
TYPE=$(echo "$TASK_JSON" | jq -r '.type')

echo "Working on: $TITLE"
echo "Type: $TYPE"
echo "Description: $DESCRIPTION"

# Update status to in_progress
update_task_status "FT-001" "in_progress"
```
