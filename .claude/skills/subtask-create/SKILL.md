---
name: subtask-create
description: Utility to create subtasks in Convex from a task plan. Handles bulk creation with phase ordering and dependencies.
user-invocable: false
---

# Subtask Create Utility

Internal utility to sync subtasks from a task plan to Convex. Used by `/task-dev` after planner generates an implementation plan.

## Usage (Internal)

This skill is not user-invocable. It's called by other skills:

```
Called by /task-dev:
→ /subtask-create FT-001 with subtasks JSON

Called internally by task-dev planner:
→ /subtask-create with implementation_plan.json subtasks
```

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `CONVEX_SITE_URL` | Convex HTTP site URL | `https://xxx-yyy.convex.site` |
| `CLAUDE_API_KEY` | API key for authentication | `sk-...` |

## Input Format

Subtasks from implementation plan:

```json
{
  "taskId": "FT-001",
  "subtasks": [
    {
      "subtaskId": "backend-auth-1",
      "phaseNumber": 1,
      "description": "Create auth middleware for JWT validation",
      "domain": "backend",
      "dependsOn": [],
      "filesToCreate": ["src/middleware/auth.ts"],
      "filesToModify": [],
      "patternsFrom": ["src/middleware/cors.ts"],
      "verification": {
        "type": "command",
        "command": "npm test -- middleware/auth.test.ts"
      }
    },
    {
      "subtaskId": "backend-auth-2",
      "phaseNumber": 1,
      "description": "Add login/logout endpoints",
      "domain": "backend",
      "dependsOn": ["backend-auth-1"],
      "filesToCreate": ["src/routes/auth.ts"],
      "filesToModify": ["src/routes/index.ts"],
      "patternsFrom": ["src/routes/users.ts"],
      "verification": {
        "type": "command",
        "command": "npm test -- routes/auth.test.ts"
      }
    }
  ]
}
```

## API Endpoint

### POST /api/tasks/{customId}/subtasks

Create subtasks in bulk for a task.

```bash
curl -s -X POST "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "subtasks": [
      {
        "subtaskId": "backend-auth-1",
        "phaseNumber": 1,
        "description": "Create auth middleware",
        "domain": "backend",
        "dependsOn": [],
        "filesToCreate": ["src/middleware/auth.ts"],
        "verification": {
          "type": "command",
          "command": "npm test"
        }
      }
    ]
  }'
```

**Response:**

```json
{
  "created": 5,
  "subtasks": [
    {"_id": "abc123", "subtaskId": "backend-auth-1", "status": "pending"},
    {"_id": "def456", "subtaskId": "backend-auth-2", "status": "pending"}
  ]
}
```

## Process

### Step 1: Validate Input

```bash
validate_subtasks() {
  local subtasks="$1"

  # Check required fields
  for subtask in $(echo "$subtasks" | jq -c '.[]'); do
    subtaskId=$(echo "$subtask" | jq -r '.subtaskId')
    description=$(echo "$subtask" | jq -r '.description')

    if [ -z "$subtaskId" ] || [ "$subtaskId" = "null" ]; then
      echo "Error: subtaskId is required"
      return 1
    fi

    if [ -z "$description" ] || [ "$description" = "null" ]; then
      echo "Error: description is required for $subtaskId"
      return 1
    fi
  done

  return 0
}
```

### Step 2: Check for Existing Subtasks

```bash
# Get existing subtasks
EXISTING=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

EXISTING_COUNT=$(echo "$EXISTING" | jq '.subtasks | length')

if [ "$EXISTING_COUNT" -gt 0 ]; then
  echo "Task already has $EXISTING_COUNT subtasks"
  # Ask user: overwrite or append?
fi
```

### Step 3: Create Subtasks

```bash
create_subtasks() {
  local task_id="$1"
  local subtasks="$2"

  RESPONSE=$(curl -s -X POST "${CONVEX_SITE_URL}/api/tasks/${task_id}/subtasks" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "{\"subtasks\": ${subtasks}}")

  if echo "$RESPONSE" | grep -q '"error"'; then
    echo "Error creating subtasks: $(echo $RESPONSE | jq -r '.error')"
    return 1
  fi

  CREATED=$(echo "$RESPONSE" | jq -r '.created')
  echo "Created $CREATED subtasks"

  return 0
}
```

### Step 4: Save Local Copy

```bash
# Save implementation_plan.json locally for reference
BUILD_DIR=".claude/builds/${TASK_ID}"
mkdir -p "$BUILD_DIR"

echo "$IMPLEMENTATION_PLAN" > "${BUILD_DIR}/implementation_plan.json"
echo "Saved plan to ${BUILD_DIR}/implementation_plan.json"
```

## Subtask Schema

```typescript
interface Subtask {
  subtaskId: string;           // Human-readable ID (e.g., "backend-auth-1")
  phaseNumber: number;         // Execution order (1, 2, 3...)
  description: string;         // What the subtask does
  domain: 'frontend' | 'backend' | 'database' | 'fullstack';
  status: 'pending' | 'in_progress' | 'completed' | 'failed' | 'blocked';
  dependsOn: string[];         // Other subtaskIds this depends on
  filesToModify?: string[];    // Existing files to change
  filesToCreate?: string[];    // New files to create
  patternsFrom?: string[];     // Example files to follow
  verification?: {
    type: 'command' | 'visual' | 'functional' | 'code-review';
    command?: string;
    description?: string;
  };
  attemptCount?: number;       // Recovery tracking
  blockedReason?: string;      // Why blocked
  notes?: string;              // Implementation notes
}
```

## Dependency Validation

Before creating, validate dependencies exist:

```bash
validate_dependencies() {
  local subtasks="$1"

  # Build list of all subtaskIds
  ALL_IDS=$(echo "$subtasks" | jq -r '.[].subtaskId')

  # Check each dependency exists
  for subtask in $(echo "$subtasks" | jq -c '.[]'); do
    ID=$(echo "$subtask" | jq -r '.subtaskId')
    DEPS=$(echo "$subtask" | jq -r '.dependsOn[]')

    for dep in $DEPS; do
      if ! echo "$ALL_IDS" | grep -q "^${dep}$"; then
        echo "Error: $ID depends on non-existent subtask: $dep"
        return 1
      fi
    done
  done

  return 0
}
```

## Output Format

```
=== Subtask Creation Summary ===

Task: FT-001 - Add user authentication

SUBTASKS CREATED:
┌────────────────────────────────────────────────────────────────────┐
│ Phase │ ID              │ Description                   │ Domain   │
├───────┼─────────────────┼───────────────────────────────┼──────────┤
│   1   │ backend-auth-1  │ Create auth middleware        │ backend  │
│   1   │ backend-auth-2  │ Add login/logout endpoints    │ backend  │
│   1   │ backend-auth-3  │ Add password hashing utility  │ backend  │
│   2   │ frontend-auth-1 │ Create LoginForm component    │ frontend │
│   2   │ frontend-auth-2 │ Add auth state management     │ frontend │
│   2   │ frontend-auth-3 │ Connect form to backend API   │ frontend │
└────────────────────────────────────────────────────────────────────┘

DEPENDENCY GRAPH:
Phase 1:
  backend-auth-1 ──► backend-auth-2
  backend-auth-3 (independent)

Phase 2:
  frontend-auth-1 ──► frontend-auth-2 ──► frontend-auth-3

Phase 1 must complete before Phase 2.

LOCAL FILES:
  .claude/builds/FT-001/implementation_plan.json

NEXT STEPS:
Run /subtask FT-001 --next to start execution
```

## Error Handling

| Scenario | Action |
|----------|--------|
| Task not found | Report error |
| Invalid subtaskId format | Suggest valid format |
| Duplicate subtaskId | Add suffix or error |
| Missing dependencies | Report missing deps |
| API error | Retry once |

## Clearing Existing Subtasks

If task already has subtasks:

```bash
# Option 1: Clear and recreate
clear_subtasks() {
  local task_id="$1"

  curl -s -X DELETE "${CONVEX_SITE_URL}/api/tasks/${task_id}/subtasks" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}"
}

# Option 2: Append new subtasks
# Just POST new subtasks, API handles merge
```

## Integration

Called by:
- `/task-dev` - After planner creates implementation plan
- `/task-dev --auto` - During planning phase

Returns control to calling skill with subtask count and first subtask for execution.
