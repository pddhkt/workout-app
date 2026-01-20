---
name: phase-planning
description: Create and manage implementation phases and subtasks for a task. Bridges task requirements and subtask execution.
---

# Phase Planning Skill

Creates and manages implementation plans with phases and subtasks. This skill bridges the gap between high-level task requirements and atomic subtask execution.

## Usage

```bash
/phase-planning FT-001                           # Create new implementation plan
/phase-planning FT-001 --status                  # Show current plan status
/phase-planning FT-001 --analyze                 # Analyze phases, tasks, ordering
/phase-planning FT-001 --add-phase "Frontend UI" # Add a new phase
/phase-planning FT-001 --add-subtask backend-auth-1 --phase 1  # Add subtask to phase
/phase-planning FT-001 --reorder                 # Reorder phases based on dependencies
```

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `CONVEX_SITE_URL` | Convex HTTP site URL | `https://xxx-yyy.convex.site` |
| `CLAUDE_API_KEY` | API key for authentication | `sk-...` |

## Workflow

```
/phase-planning FT-001
    ↓
1. Fetch task from Convex API
    ↓
2. Run phase-planner agent to analyze requirements
    ↓
3. Create subtasks with phaseNumber for ordering:
   - phaseNumber 1: database/backend work
   - phaseNumber 2: worker/API integration
   - phaseNumber 3: frontend work
   - phaseNumber 4+: integration/testing
   (Note: Complexity is emergent from the subtask count)
    ↓
4. Set dependencies (subtask → subtask)
    ↓
5. Write subtasks to Convex (subtasks table)
    ↓
6. Sync implementation_plan.json locally
```

## Output: implementation_plan.json

The implementation plan uses a **flat subtasks array** with `phaseNumber` as an ordering field on each subtask (not nested phases).

```json
{
  "taskId": "FT-001",
  "title": "Add user authentication",
  "complexity": "standard",
  "createdAt": "2024-01-15T10:00:00Z",
  "subtasks": [
    {
      "subtaskId": "backend-auth-1",
      "phaseNumber": 1,
      "description": "Create auth middleware",
      "domain": "backend",
      "status": "pending",
      "dependsOn": [],
      "filesToCreate": ["src/middleware/auth.ts"],
      "filesToModify": [],
      "verification": {
        "type": "command",
        "command": "npm test -- auth.test.ts"
      }
    },
    {
      "subtaskId": "backend-auth-2",
      "phaseNumber": 1,
      "description": "Add login/logout endpoints",
      "domain": "backend",
      "status": "pending",
      "dependsOn": ["backend-auth-1"],
      "filesToCreate": ["src/routes/auth.ts"],
      "filesToModify": ["src/routes/index.ts"],
      "verification": {
        "type": "command",
        "command": "curl -X POST localhost:3000/api/auth/login"
      }
    },
    {
      "subtaskId": "frontend-auth-1",
      "phaseNumber": 2,
      "description": "Create login form component",
      "domain": "frontend",
      "status": "pending",
      "dependsOn": [],
      "filesToCreate": ["src/components/LoginForm.tsx"],
      "filesToModify": [],
      "verification": {
        "type": "visual",
        "description": "Login form renders correctly"
      }
    }
  ],
  "summary": {
    "totalSubtasks": 3,
    "completedSubtasks": 0,
    "percentComplete": 0
  }
}
```

**Key points:**
- `phaseNumber` is a field ON each subtask for ordering, NOT a nested container
- Subtasks are grouped logically by phaseNumber (1 = backend, 2 = frontend, etc.)
- Phase dependencies are implicit via subtask dependencies (e.g., frontend-auth-1 depends on backend being done)

## API Endpoints Used

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/tasks/{id}` | GET | Fetch task details |
| `/api/tasks/{id}/complexity` | PATCH | Update task complexity |
| `/api/phases` | POST | Create phase record |
| `/api/phases` | GET | List phases for project |
| `/api/tasks/{id}/subtasks` | POST | Create subtasks (bulk) |
| `/api/tasks/{id}/subtasks` | GET | List subtasks |
| `/api/tasks/{id}/subtasks/progress` | GET | Get progress stats |

## Phase Planning Rules

### Domain Ordering

Phases should be ordered by domain dependencies:

```
1. database    - Schema changes, migrations
2. backend     - API endpoints, business logic
3. worker      - Background jobs, async processing
4. frontend    - UI components, state management
5. integration - E2E tests, integration tests
```

### Subtask Grouping by Complexity

| Complexity | Typical phaseNumbers | Description |
|------------|----------------------|-------------|
| simple | 1 | Single domain, few files |
| standard | 1-2 | Multiple domains, backend → frontend |
| complex | 1-4+ | All domains, external integrations |

### Subtask Guidelines

Each subtask should be:
- **Atomic** - Completable in a single session
- **Verifiable** - Has a clear verification method
- **Domain-specific** - Belongs to exactly one domain
- **Dependency-aware** - Lists other subtasks it depends on

## Analyze Mode

```bash
/phase-planning FT-001 --analyze
```

Output:
```
=== Plan Analysis: FT-001 ===

Task: Add user authentication
Complexity: standard
Status: in_progress (40% complete)

SUBTASKS BY PHASE:
┌─────────────────────────────────────────────────────────────┐
│ Phase 1 (Backend) [COMPLETED]                               │
│   → backend-auth-1: Create auth middleware ✓                │
│   → backend-auth-2: Add login/logout endpoints ✓            │
├─────────────────────────────────────────────────────────────┤
│ Phase 2 (Frontend) [IN_PROGRESS]                            │
│   → frontend-auth-1: Create login form component ✓          │
│   → frontend-auth-2: Add auth state management [PENDING]    │
│   → frontend-auth-3: Connect to backend [BLOCKED]           │
│     ↳ Blocked: Waiting for frontend-auth-2                  │
└─────────────────────────────────────────────────────────────┘

DEPENDENCY GRAPH:
backend-auth-1 ──► backend-auth-2
                        │
                        ▼
frontend-auth-1 ──► frontend-auth-2 ──► frontend-auth-3

NEXT SUBTASK: frontend-auth-2 (Add auth state management)
```

## Bash Implementation

### Create Plan

```bash
#!/bin/bash
source .env

TASK_ID="$1"
BUILD_DIR=".claude/builds/${TASK_ID}"

echo "=== Phase Planning: ${TASK_ID} ==="

# 1. Fetch task
TASK=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

if echo "$TASK" | grep -q '"error"'; then
  echo "Task not found: ${TASK_ID}"
  exit 1
fi

TITLE=$(echo "$TASK" | jq -r '.title')
COMPLEXITY=$(echo "$TASK" | jq -r '.complexity // "standard"')

echo "Task: ${TITLE}"
echo "Complexity: ${COMPLEXITY}"

# 2. Create build directory
mkdir -p "$BUILD_DIR"

# 3. Run phase-planner agent to create plan
# (Agent creates implementation_plan.json with flat subtasks array)

# 4. Read created plan
PLAN=$(cat "${BUILD_DIR}/implementation_plan.json")

# 5. Create subtasks in Convex (flat array, not nested in phases)
SUBTASKS=$(echo "$PLAN" | jq -c '.subtasks')

curl -s -X POST "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "{\"subtasks\": ${SUBTASKS}}"

echo ""
echo "=== Plan Created ==="
echo "Subtasks: $(echo "$PLAN" | jq '.summary.totalSubtasks')"
```

### Get Status

```bash
#!/bin/bash
source .env

TASK_ID="$1"

# Get progress from Convex
PROGRESS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks/progress" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

TOTAL=$(echo "$PROGRESS" | jq -r '.total')
COMPLETED=$(echo "$PROGRESS" | jq -r '.completed')
PERCENT=$(echo "$PROGRESS" | jq -r '.percentComplete')

echo "=== Plan Status: ${TASK_ID} ==="
echo "Progress: ${COMPLETED}/${TOTAL} (${PERCENT}%)"

# Get subtasks
SUBTASKS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

echo ""
echo "Subtasks:"
echo "$SUBTASKS" | jq -r '.[] | "  [\(.status)] \(.subtaskId): \(.description)"'
```

### Analyze Plan

```bash
#!/bin/bash
source .env

TASK_ID="$1"
BUILD_DIR=".claude/builds/${TASK_ID}"

# Read local plan
if [ ! -f "${BUILD_DIR}/implementation_plan.json" ]; then
  echo "No plan found. Run /phase-planning ${TASK_ID} first."
  exit 1
fi

PLAN=$(cat "${BUILD_DIR}/implementation_plan.json")

echo "=== Plan Analysis: ${TASK_ID} ==="
echo ""
echo "Task: $(echo "$PLAN" | jq -r '.title')"
echo "Complexity: $(echo "$PLAN" | jq -r '.complexity')"

# Get current progress from Convex
PROGRESS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks/progress" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

PERCENT=$(echo "$PROGRESS" | jq -r '.percentComplete')
echo "Status: in_progress (${PERCENT}% complete)"
echo ""

echo "SUBTASKS BY PHASE:"
echo "┌─────────────────────────────────────────────────────────────┐"

# List subtasks grouped by phaseNumber
echo "$PLAN" | jq -r '.subtasks | group_by(.phaseNumber)[] | "│ Phase \(.[0].phaseNumber):\n\(map("│   → \(.subtaskId): \(.description) [\(.status)]") | join("\n"))"'

echo "└─────────────────────────────────────────────────────────────┘"
echo ""

# Get next subtask
NEXT=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks/next" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

NEXT_ID=$(echo "$NEXT" | jq -r '.subtask.subtaskId // "none"')
NEXT_DESC=$(echo "$NEXT" | jq -r '.subtask.description // "All subtasks complete"')

echo "NEXT SUBTASK: ${NEXT_ID} (${NEXT_DESC})"
```

## Integration with Other Skills

```
                    ┌──────────────────┐
                    │ /task-dev --auto │  (orchestrator)
                    └────────┬─────────┘
                             │
        ┌────────────────────┼────────────────────┐
        ↓                    ↓                    ↓
┌────────────────┐  ┌────────────────┐  ┌───────────────┐
│/phase-planning │  │   /subtask     │  │  /qa-review   │
│ (create/mgmt)  │  │  (execute)     │  │ (validate)    │
└───────┬────────┘  └────────────────┘  └───────────────┘
        │
        ↓ (creates subtasks)
┌───────────────┐
│   /subtask    │
│  (execute)    │
└───────────────┘
```

**Key distinction:**
- `/phase-planning` - Creates phases and subtasks (input: task, output: implementation_plan.json). Complexity is emergent from the subtask count.
- `/subtask` - Executes a single subtask (input: subtask ID, output: code changes)
- `/task-dev --auto` - Orchestrates everything (calls /phase-planning → /subtask loop → /qa-review)

## Error Handling

| Scenario | Action |
|----------|--------|
| Task not found | Exit with error |
| Plan already exists | Prompt to overwrite or show status |
| Phase creation fails | Retry once, then report error |
| Invalid dependencies | Validate and fix circular deps |

## Local Files

```
.claude/builds/{taskId}/
├── implementation_plan.json  # Created by this skill
└── context.json              # From scout (if available)
```
