---
name: task-create
description: Utility to create tasks in Convex from a phase plan. Handles bulk creation, dependency resolution, and ID assignment.
user-invocable: false
---

# Task Create Utility

Internal utility to sync tasks from a phase plan to Convex. Used by `/phase-dev`, `/ideation`, and `/analyze` after user approves a plan.

## Usage (Internal)

This skill is not user-invocable. It's called by other skills:

```
Called by /phase-dev:
→ /task-create with task list JSON

Called by /ideation:
→ /task-create with elaborated idea tasks

Called by /analyze:
→ /task-create with recommendation tasks
```

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `CONVEX_SITE_URL` | Convex HTTP site URL | `https://xxx-yyy.convex.site` |
| `CLAUDE_API_KEY` | API key for authentication | `sk-...` |

## Input Format

Tasks JSON array from phase planning:

```json
{
  "phaseName": "Authentication System",
  "phaseDescription": "Implement user auth with email/password",
  "tasks": [
    {
      "title": "Add user model & schema",
      "description": "Create user table with email, password_hash, timestamps",
      "type": "feature",
      "domain": "database",
      "priority": 1,
      "acceptanceCriteria": [
        "User table exists with required fields",
        "Indexes on email (unique)"
      ],
      "dependencies": []
    },
    {
      "title": "Add login/logout API",
      "description": "Create API endpoints for authentication",
      "type": "feature",
      "domain": "backend",
      "priority": 2,
      "acceptanceCriteria": [
        "POST /api/auth/login returns JWT",
        "POST /api/auth/logout invalidates session"
      ],
      "dependencies": ["Add user model & schema"]
    }
  ]
}
```

## Process

### Step 1: Validate Input

```bash
# Check required fields
for task in tasks:
  - title (required)
  - type (default: feature)
  - domain (default: fullstack)
  - priority (default: 2)
```

### Step 2: Create Tasks (Ordered)

Create tasks in dependency order to get customIds:

```bash
#!/bin/bash
source .env

# Track created task IDs
declare -A TASK_IDS

# Create tasks without dependencies first
for task in $(echo "$TASKS" | jq -c '.[] | select(.dependencies | length == 0)'); do
  TITLE=$(echo "$task" | jq -r '.title')

  RESPONSE=$(curl -s -X POST "${CONVEX_SITE_URL}/api/tasks" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "$task")

  CUSTOM_ID=$(echo "$RESPONSE" | jq -r '.customId')
  TASK_IDS["$TITLE"]="$CUSTOM_ID"

  echo "Created: $CUSTOM_ID - $TITLE"
done

# Create tasks with dependencies (resolving to customIds)
for task in $(echo "$TASKS" | jq -c '.[] | select(.dependencies | length > 0)'); do
  TITLE=$(echo "$task" | jq -r '.title')

  # Resolve dependencies to customIds
  DEPS=$(echo "$task" | jq -r '.dependencies[]')
  RESOLVED_DEPS="["
  for dep in $DEPS; do
    RESOLVED_DEPS="${RESOLVED_DEPS}\"${TASK_IDS[$dep]}\","
  done
  RESOLVED_DEPS="${RESOLVED_DEPS%,}]"

  # Update task with resolved dependencies
  task=$(echo "$task" | jq ".dependencies = $RESOLVED_DEPS")

  RESPONSE=$(curl -s -X POST "${CONVEX_SITE_URL}/api/tasks" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "$task")

  CUSTOM_ID=$(echo "$RESPONSE" | jq -r '.customId')
  TASK_IDS["$TITLE"]="$CUSTOM_ID"

  echo "Created: $CUSTOM_ID - $TITLE (deps: $RESOLVED_DEPS)"
done
```

### Step 3: Create Phase Record

```bash
# Get all created task IDs
ALL_TASK_IDS=$(printf '%s\n' "${TASK_IDS[@]}" | jq -R . | jq -s .)

# Create phase record
curl -s -X POST "${CONVEX_SITE_URL}/api/phases" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"${PHASE_NAME}\",
    \"phaseType\": \"implementation\",
    \"description\": \"${PHASE_DESCRIPTION}\",
    \"status\": \"pending\",
    \"taskIds\": ${ALL_TASK_IDS},
    \"summary\": {
      \"tasksCreated\": ${#TASK_IDS[@]},
      \"domains\": $(echo "$TASKS" | jq '[.[].domain] | unique')
    }
  }"
```

### Step 4: Return Results

```json
{
  "success": true,
  "phaseVersion": 3,
  "tasksCreated": [
    {"customId": "FT-001", "title": "Add user model & schema"},
    {"customId": "FT-002", "title": "Add login/logout API"},
    {"customId": "FT-003", "title": "Add JWT middleware"},
    {"customId": "FT-004", "title": "Add login UI"}
  ],
  "dependencyMap": {
    "FT-002": ["FT-001"],
    "FT-003": ["FT-001"],
    "FT-004": ["FT-002", "FT-003"]
  }
}
```

## API Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/tasks` | POST | Create single task |
| `/api/tasks/{id}` | PUT | Update task (set dependencies) |
| `/api/phases` | POST | Create phase record |

## Task Schema (Convex)

```typescript
interface Task {
  title: string;
  description: string;
  type: 'feature' | 'bugfix' | 'refactor';
  domain: 'frontend' | 'backend' | 'database' | 'fullstack';
  priority: 1 | 2 | 3 | 4;
  status: 'pending' | 'in_progress' | 'completed' | 'blocked';
  acceptanceCriteria?: string[];
  dependencies?: string[];  // Array of customIds
  tags?: string[];
  complexity?: 'simple' | 'standard' | 'complex';
}
```

## Dependency Resolution

Dependencies are specified by task title, then resolved to customIds:

```
Input:
  Task "Add login UI" depends on ["Add login API"]

After creation:
  "Add login API" → FT-001

Updated:
  Task "Add login UI" depends on ["FT-001"]
```

### Topological Sort

Tasks are created in topological order to ensure dependencies exist:

```
1. Find all tasks with no dependencies → Create first
2. Find tasks whose dependencies are all created → Create next
3. Repeat until all tasks created
```

### Circular Dependency Detection

```bash
# Detect cycles before creating
detect_cycles() {
  # Build adjacency list
  # Run DFS to detect back edges
  # Return error if cycle found
}
```

## Error Handling

| Scenario | Action |
|----------|--------|
| Task creation fails | Retry once, then report error |
| Duplicate title | Add suffix (-2, -3, etc.) |
| Invalid domain | Default to "fullstack" |
| Circular dependency | Report error, ask user to resolve |
| API rate limit | Back off and retry |

## Rollback on Failure

If task creation fails mid-way:

```bash
rollback_tasks() {
  # Delete all tasks created in this batch
  for id in "${CREATED_IDS[@]}"; do
    curl -s -X DELETE "${CONVEX_SITE_URL}/api/tasks/${id}" \
      -H "Authorization: Bearer ${CLAUDE_API_KEY}"
  done
  echo "Rolled back ${#CREATED_IDS[@]} tasks"
}
```

## Output Format

```
=== Task Creation Summary ===

Phase: Authentication System (v3)

TASKS CREATED:
┌────────────────────────────────────────────────────────────────────┐
│ ID      │ Title                    │ Domain   │ Depends On         │
├─────────┼──────────────────────────┼──────────┼────────────────────┤
│ FT-001  │ Add user model & schema  │ database │ -                  │
│ FT-002  │ Add login/logout API     │ backend  │ FT-001             │
│ FT-003  │ Add JWT middleware       │ backend  │ FT-001             │
│ FT-004  │ Add login UI components  │ frontend │ FT-002, FT-003     │
└────────────────────────────────────────────────────────────────────┘

EXECUTION ORDER:
1. FT-001 (no dependencies)
2. FT-002, FT-003 (can run in parallel)
3. FT-004 (after FT-002 and FT-003)

NEXT STEPS:
Run /task-dev FT-001 to start implementation
```

## Integration

Called by:
- `/phase-dev` - After user approves phase plan
- `/ideation` - After user selects idea
- `/analyze` - After user selects recommendation

Returns control to calling skill with task IDs for status updates.
