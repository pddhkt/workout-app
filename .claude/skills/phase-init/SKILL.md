---
name: phase-init
description: Create a new phase with tasks from a high-level description. Reads existing phases/tasks to determine ordering, detects conflicts, and syncs to Convex.
allowed-tools:
  - Task
  - Bash
  - Read
  - AskUserQuestion
  - TodoWrite
---

# Phase Init Workflow

Create a new phase (milestone) by analyzing requirements, reading existing project state, creating tasks with proper dependencies, and syncing to Convex. This is the **planning layer** that creates work items.

## Understanding the Hierarchy

```
Phase (Milestone) - Big picture milestone (e.g., "v2.0 Authentication System")
    │
    ├── Task FT-001: Add user model & schema
    │   └── Subtasks (created by /task-dev internally)
    │
    ├── Task FT-002: Add login/logout API (depends on FT-001)
    │   └── Subtasks (created by /task-dev internally)
    │
    └── Task FT-003: Add login UI (depends on FT-002)
        └── Subtasks (created by /task-dev internally)
```

**Key Distinction:**
- `/phase-init` creates **phases and tasks** (planning layer)
- `/task-dev` implements tasks and creates **subtasks** internally (execution layer)
- `/dev-loop` orchestrates multiple task executions (execution layer)

## Usage

```bash
/phase-init "Add user authentication with email/password login"
/phase-init "Add OAuth provider support" --after-phase 2
```

**Options:**
- `--after-phase N` - Insert after specific phase number
- `--dry-run` - Show plan without syncing to Convex
- `--yolo` - Skip approval, proceed after showing plan

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `CONVEX_SITE_URL` | Convex HTTP site URL | `https://xxx-yyy.convex.site` |
| `CLAUDE_API_KEY` | API key for authentication | `sk-...` |

## Workflow Overview

```
/phase-init "Description"
    ↓
1. Fetch existing phases and tasks from Convex
    ↓
2. Analyze where new work fits
   → Detect conflicts and ordering issues
    ↓
3. Ask user for decisions if conflicts found
    ↓
4. Run phase-planner agent
   → Creates tasks with proper ordering
    ↓
5. Display ASCII plan to user
    ↓
6. User approves (or --yolo)
    ↓
7. Sync tasks to Convex
    ↓
8. Create phase record
    ↓
9. Output next steps (ready for /task-dev or /dev-loop)
```

---

## Phase 1: Fetch Current Project State

Fetch existing phases AND tasks to understand current project state:

```bash
source .env.local 2>/dev/null || source .env 2>/dev/null || true

# Verify API configuration
if [ -z "$CONVEX_SITE_URL" ] || [ -z "$CLAUDE_API_KEY" ]; then
  echo "Error: CONVEX_SITE_URL and CLAUDE_API_KEY must be set"
  echo "Run /project-init first to register this project"
  exit 1
fi

# Fetch existing phases
PHASES=$(curl -s -X GET "${CONVEX_SITE_URL}/api/phases" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

# Fetch existing tasks
TASKS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")
```

Display current state to user:

```markdown
## Current Project State

### Existing Phases
| # | Phase Name | Status | Tasks |
|---|------------|--------|-------|
| 1 | Database Setup | completed | 3 |
| 2 | Backend APIs | in_progress | 5 (2 pending) |
| 3 | Frontend | pending | 4 |

### Task Overview by Phase
| Phase | Pending | In Progress | Completed |
|-------|---------|-------------|-----------|
| 1     | 0       | 0           | 3         |
| 2     | 2       | 1           | 2         |
| 3     | 4       | 0           | 0         |

### Domains in Use
- Database: 3 tasks
- Backend: 5 tasks
- Frontend: 4 tasks
```

---

## Phase 2: Analyze Where New Work Fits

Determine how the new phase relates to existing work:

### Evolution Type Detection

Use `AskUserQuestion` if not obvious from description:

```
questions: [
  {
    question: "What type of evolution is this?",
    header: "Evolution",
    multiSelect: false,
    options: [
      {label: "Add New Phase", description: "Creates a new milestone with tasks"},
      {label: "Extend Existing Phase", description: "Add tasks to an existing phase"},
      {label: "Insert Before Phase", description: "Insert work that existing phases depend on"},
      {label: "Parallel Work", description: "Independent work that can run alongside existing"}
    ]
  }
]
```

### Conflict Detection

Analyze potential conflicts:

1. **Dependency Conflicts**: Does new work depend on incomplete tasks?
2. **Ordering Conflicts**: Does this change existing task priorities?
3. **Domain Overlap**: Will new tasks affect same files as pending tasks?

If conflicts detected, ask user:

```
questions: [
  {
    question: "This phase depends on Phase 2 which has pending tasks. How should I proceed?",
    header: "Conflict",
    multiSelect: false,
    options: [
      {label: "Insert after Phase 2", description: "Wait for Phase 2 to complete first"},
      {label: "Create as Phase 2.5", description: "Insert between Phase 2 and 3"},
      {label: "Make it Phase 4", description: "Add at the end, independent of Phase 2"}
    ]
  }
]
```

---

## Phase 3: Gather Details (Interactive)

Based on the phase description, gather specific details:

### For Adding Frontend

```
questions: [
  {
    question: "What frontend stack are you adding?",
    header: "Stack",
    multiSelect: false,
    options: [
      {label: "React + TanStack Router", description: "SPA with file-based routing (Recommended)"},
      {label: "React + React Router", description: "SPA with React Router v6"},
      {label: "Next.js", description: "Full-stack React framework"},
      {label: "Other", description: "Specify your frontend stack"}
    ]
  }
]
```

### For Adding Integration

```
questions: [
  {
    question: "What integration are you adding?",
    header: "Integration",
    multiSelect: true,
    options: [
      {label: "Authentication (Clerk)", description: "User auth with Clerk"},
      {label: "Payments (Stripe)", description: "Payment processing"},
      {label: "Email (Resend)", description: "Transactional email"},
      {label: "File Storage", description: "File upload and storage"},
      {label: "Other", description: "Specify integration"}
    ]
  }
]
```

### For Adding Capability

```
questions: [
  {
    question: "What capability are you adding?",
    header: "Capability",
    multiSelect: true,
    options: [
      {label: "Search", description: "Full-text search across entities"},
      {label: "Notifications", description: "In-app and push notifications"},
      {label: "Real-time Updates", description: "Live updates and presence"},
      {label: "Export/Import", description: "Data export (CSV, JSON) and import"},
      {label: "Admin Dashboard", description: "Admin interface for management"},
      {label: "Other", description: "Describe the capability"}
    ]
  }
]
```

---

## Phase 4: Run Phase Planning

Use the phase-planner agent to create tasks:

```
Use Task tool with subagent_type: "phase-planner"
Prompt:
## Phase Description

{description}

## Current Project State

Existing Phases: {phases_json}
Existing Tasks: {tasks_json}
Insert Position: {after_phase or "end"}

## Instructions

1. Analyze the phase requirements
2. Consider existing phases and tasks
3. Break into logical tasks (one per focused area)
4. Set task dependencies (database → backend → frontend)
5. Assign phaseNumber to each task for ordering
6. Output ASCII task graph + JSON task list

## Expected Output

1. ASCII dependency graph showing task ordering
2. JSON array of tasks with:
   - title
   - description
   - type (feature/bugfix/refactor)
   - domain (frontend/backend/database/fullstack)
   - phaseNumber (ordering within phase)
   - priority (1-4)
   - acceptanceCriteria (array)
   - dependencies (array of task titles or IDs)
```

---

## Phase 5: Display Plan

Show the plan in ASCII format:

```
=== PHASE PLAN: Authentication System ===

OVERVIEW
--------
Description: Implement complete user authentication with OAuth support
Insert Position: After Phase 2 (Backend APIs)
Tasks: 4

TASK DEPENDENCY GRAPH
---------------------
                    ┌─────────────────────────┐
                    │   FT-012: User Model    │
                    │   [database] phase 3    │
                    └───────────┬─────────────┘
                                │
              ┌─────────────────┼─────────────────┐
              │                 │                 │
              ▼                 │                 ▼
┌─────────────────────┐         │   ┌─────────────────────┐
│ FT-013: Backend API │         │   │ FT-014: JWT         │
│ [backend] phase 3   │         │   │ [backend] phase 3   │
│ depends: FT-012     │         │   │ depends: FT-012     │
└─────────┬───────────┘         │   └─────────┬───────────┘
          │                     │             │
          └──────────┬──────────┘─────────────┘
                     │
                     ▼
          ┌─────────────────────┐
          │ FT-015: Login UI    │
          │ [frontend] phase 3  │
          │ depends: FT-013,014 │
          └─────────────────────┘

TASK DETAILS
------------
FT-012: Add user model & schema
  Domain: database
  Phase: 3
  Depends: (none)
  Priority: 1
  Acceptance Criteria:
    - User table exists with email, password_hash, created_at
    - Indexes on email (unique)

FT-013: Add login/logout API endpoints
  Domain: backend
  Phase: 3
  Depends: FT-012
  Priority: 2
  Acceptance Criteria:
    - POST /api/auth/login returns JWT on valid credentials
    - POST /api/auth/logout invalidates session

FT-014: Add JWT middleware
  Domain: backend
  Phase: 3
  Depends: FT-012
  Priority: 2
  Acceptance Criteria:
    - Protected routes return 401 without valid token
    - Token payload includes user ID and expiration

FT-015: Add login UI components
  Domain: frontend
  Phase: 3
  Depends: FT-013, FT-014
  Priority: 3
  Acceptance Criteria:
    - Login form with email/password inputs
    - Error messages on invalid credentials
    - Redirect to dashboard on success

EXECUTION ORDER
---------------
1. FT-012 (database) - Foundation
2. FT-013, FT-014 (parallel) - Both depend only on FT-012
3. FT-015 (frontend) - Depends on backend tasks

=== END PHASE PLAN ===
```

---

## Phase 6: User Approval

**If not --yolo:**
- Wait for user confirmation
- User can request changes or approve

```
questions: [
  {
    question: "How would you like to proceed with this phase plan?",
    header: "Approval",
    multiSelect: false,
    options: [
      {label: "Approve and create", description: "Create phase and tasks in Convex"},
      {label: "Modify tasks", description: "Adjust the task breakdown"},
      {label: "Change ordering", description: "Modify phase position"},
      {label: "Cancel", description: "Discard this plan"}
    ]
  }
]
```

**If --yolo:**
- Show plan with "YOLO mode: Proceeding without approval..."
- Immediately proceed to Phase 7

---

## Phase 7: Sync to Convex

Create tasks in Convex:

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
    "phaseNumber": 3,
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
curl -s -X PUT "${CONVEX_SITE_URL}/api/tasks/FT-013" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"dependencies": ["FT-012"]}'
```

---

## Phase 8: Create Phase Record

Create the phase record:

```bash
curl -s -X POST "${CONVEX_SITE_URL}/api/phases" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Authentication System",
    "phaseNumber": 3,
    "description": "Implement complete user authentication with email/password login",
    "status": "pending",
    "dependsOn": [2],
    "taskIds": ["FT-012", "FT-013", "FT-014", "FT-015"],
    "summary": {
      "tasksCreated": 4,
      "domains": ["database", "backend", "frontend"]
    }
  }'
```

---

## Phase 9: Output Next Steps

```
=== Phase Created Successfully ===

Phase: Authentication System (Phase 3)
Tasks Created: 4 (FT-012 through FT-015)
Insert Position: After Phase 2 (Backend APIs)

NEXT STEPS:

Option 1: Execute tasks individually
  /task-dev FT-012
  /task-dev FT-013  (after FT-012 completes)
  ...

Option 2: Run dev-loop for automated execution
  /dev-loop --until-phase 3

Option 3: Execute all pending tasks
  /dev-loop --all-pending

View tasks: /task-list pending
View phase: GET /api/phases/3
```

---

## API Endpoints Used

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/phases` | GET | List existing phases |
| `/api/phases` | POST | Create phase record |
| `/api/tasks` | GET | List existing tasks |
| `/api/tasks` | POST | Create individual task |
| `/api/tasks/{id}` | PUT | Update task (set dependencies) |

---

## Error Handling

| Scenario | Action |
|----------|--------|
| Invalid description | Ask for more details |
| Task creation fails | Rollback created tasks, report error |
| Dependency cycle detected | Report and ask user to resolve |
| Phase number conflict | Ask user to choose different position |
| API unavailable | Report error, suggest retry |

---

## Task Templates by Evolution Type

### Adding Frontend

**Foundation (Priority 1):**
1. Project Setup: React + Router + Tailwind (medium)
2. Authentication UI: Login/Signup/Logout (medium)
3. Layout: Header, Navigation, Footer (simple)

**Core Features (Priority 2):**
4. [Entity] List Page with Filtering (medium)
5. [Entity] Detail Page with Edit (medium)
6. Create [Entity] Form (simple)

### Adding Integration (Auth)

**Foundation (Priority 1):**
1. Provider Setup (simple)
2. Environment Configuration (simple)
3. Auth Middleware/Guards (medium)

**Core Features (Priority 2):**
4. Login Page (simple)
5. Signup Page (simple)
6. Protected Routes (medium)

### Adding Capability (Search)

**Backend (Priority 1):**
1. Search Index Setup (medium)
2. Search Query Endpoint (medium)

**Frontend (Priority 2):**
3. Search Input Component (simple)
4. Search Results Display (medium)
5. Filters and Facets (medium)

---

## Integration with Dev-Loop

After creating a phase, use `/dev-loop` to execute tasks automatically:

```bash
# Execute all tasks in this new phase
/dev-loop --until-phase 3

# Execute specific task then continue
/dev-loop --until-task FT-015

# Execute all pending across all phases
/dev-loop --all-pending
```

---

## Related Skills

| Skill | Relationship |
|-------|--------------|
| `/task-init` | Create single task without phase |
| `/task-dev` | Execute individual tasks (has internal planner) |
| `/dev-loop` | Automated execution loop for multiple tasks |
| `/ad-hoc` | Quick implementation without task tracking |
| `/task-list` | View all tasks |
