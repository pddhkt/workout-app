---
name: project-evolve
description: Evolve an existing project by adding new capabilities and generating tasks for them.
---

# Project Evolution Workflow

Evolve an existing project by specifying what's changing and interactively generating tasks for the new capabilities.

## Arguments

Optionally provide a description of what's being added (e.g., "adding React frontend").

---

## Overview

This command helps evolve registered projects by:

1. **Fetches current state** - Loads existing tasks to understand what exists
2. **Gathers evolution intent** - You specify what's being added/changed
3. **Updates project metadata** - Optionally updates project in Convex (if admin key available)
4. **Interactive task discussion** - Suggests tasks based on evolution, you select/modify
5. **Creates tasks** - Bulk creates approved tasks for the new work

---

## Required Skill

**Load the `task-guide` skill** for domain-specific acceptance criteria:

```
.claude/skills/task-guide/
├── SKILL.md                    # Overview
└── reference/
    ├── frontend.md             # UI, a11y, responsive criteria
    ├── backend.md              # API, validation, auth criteria
    ├── database.md             # Schema, indexes, migrations criteria
    └── fullstack.md            # Integration, E2E criteria
```

After determining the primary domain of the evolution, read the corresponding reference file to generate appropriate acceptance criteria for suggested tasks.

---

## Environment Variables

Required in `.env.local` or `.env`:

| Variable          | Description                                  |
| ----------------- | -------------------------------------------- |
| `CONVEX_SITE_URL` | https://[deployment].convex.site             |
| `CLAUDE_API_KEY`  | Project-specific API key for task operations |

Optional:

| Variable        | Description                             |
| --------------- | --------------------------------------- |
| `ADMIN_API_KEY` | Admin key for updating project metadata |

---

## Workflow

### Phase 0: Environment Check

Verify API configuration before starting:

```bash
source .env.local 2>/dev/null || source .env 2>/dev/null || true

if [ -z "$CONVEX_SITE_URL" ] || [ -z "$CLAUDE_API_KEY" ]; then
  echo "Error: CONVEX_SITE_URL and CLAUDE_API_KEY must be set"
  echo "Run /project-init first to register this project"
  exit 1
fi

# Test the API key
TEST_RESULT=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

if echo "$TEST_RESULT" | grep -q '"error"'; then
  echo "Error: Invalid API key. Run /project-init to register this project."
  exit 1
fi

echo "API configured: $CONVEX_SITE_URL"
```

### Phase 1: Fetch Current State

Fetch project info AND existing tasks to understand current project state:

```bash
# Fetch project info (uses project API key)
PROJECT_INFO=$(curl -s -X GET "${CONVEX_SITE_URL}/api/projects/current" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

# Fetch existing tasks
TASKS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")
```

Display current state to user:

```markdown
## Current Project State

### Project Info

**Name**: [PROJECT_NAME from PROJECT_INFO]
**Description**: [PROJECT_DESCRIPTION or "Not set"]

**Tech Stack**:
| Layer | Technology |
|-------|------------|
| Frontend | [techStack.frontend or "Not set"] |
| Backend | [techStack.backend or "Not set"] |
| Database | [techStack.database or "Not set"] |

**App Type**: [appType or "Not set"]

### Task Overview

| Status      | Count |
| ----------- | ----- |
| Pending     | [N]   |
| In Progress | [N]   |
| Completed   | [N]   |
| Blocked     | [N]   |

### Domains in Use

Inferred from existing tasks:

- Backend: [N] tasks
- Database: [N] tasks
- Frontend: [Not detected / N tasks]

### Recent Activity

- Most recent task: [ID] "[Title]" ([Status])
- Last update: [Timestamp]
```

### Phase 1.5: Show Evolution History (if exists)

Fetch and display evolution history:

```bash
# Fetch evolution history
EVOLUTIONS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/evolutions" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")
```

If evolutions exist, display them:

```markdown
### Evolution History

| Version | Date       | Type               | Tasks Created |
| ------- | ---------- | ------------------ | ------------- |
| v3      | 2024-01-15 | Add Search         | 6             |
| v2      | 2024-01-10 | Add Clerk Auth     | 7             |
| v1      | 2024-01-05 | Add React Frontend | 12            |

_View details: GET /api/evolutions/{version}_
```

If no evolutions exist yet, skip this display (this will be the first evolution).

### Phase 2: Gather Evolution Intent

If a description was provided, parse it for evolution type. Otherwise, ask:

**Question 1: What type of evolution?**

Use `AskUserQuestion`:

```
questions: [
  {
    question: "What are you adding or changing to this project?",
    header: "Evolution",
    multiSelect: false,
    options: [
      {label: "Add Frontend", description: "Adding a web UI to an API-only project"},
      {label: "Add Backend", description: "Adding server/API to a frontend-only project"},
      {label: "Add Database", description: "Adding data persistence layer"},
      {label: "Add Integration", description: "Adding auth provider, payments, email, etc."},
      {label: "Add Capability", description: "Adding a major feature area (search, notifications, etc.)"},
      {label: "Other", description: "Describe your changes"}
    ]
  }
]
```

**Question 2: Specific details (based on Q1)**

If "Add Frontend":

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
      {label: "Vue + Vue Router", description: "Vue 3 with Composition API"},
      {label: "Other", description: "Specify your frontend stack"}
    ]
  }
]
```

If "Add Backend":

```
questions: [
  {
    question: "What backend stack are you adding?",
    header: "Stack",
    multiSelect: false,
    options: [
      {label: "Convex", description: "Serverless backend with real-time sync (Recommended)"},
      {label: "Express + PostgreSQL", description: "Node.js with traditional database"},
      {label: "FastAPI + PostgreSQL", description: "Python async API"},
      {label: "Hono + D1", description: "Edge runtime with SQLite"},
      {label: "Other", description: "Specify your backend stack"}
    ]
  }
]
```

If "Add Integration":

```
questions: [
  {
    question: "What integration are you adding?",
    header: "Integration",
    multiSelect: true,
    options: [
      {label: "Authentication (Clerk)", description: "User auth with Clerk"},
      {label: "Authentication (Auth0)", description: "User auth with Auth0"},
      {label: "Payments (Stripe)", description: "Payment processing"},
      {label: "Email (Resend)", description: "Transactional email"},
      {label: "File Storage", description: "File upload and storage"},
      {label: "AI/LLM (OpenAI)", description: "OpenAI integration"},
      {label: "Other", description: "Specify integration"}
    ]
  }
]
```

If "Add Capability":

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
      {label: "Analytics", description: "Usage tracking and dashboards"},
      {label: "Audit Logging", description: "Track user actions"},
      {label: "Other", description: "Describe the capability"}
    ]
  }
]
```

**Question 3: Constraints (Optional)**

```
questions: [
  {
    question: "Any constraints or preferences for this evolution?",
    header: "Constraints",
    multiSelect: true,
    options: [
      {label: "Mobile-first", description: "Prioritize mobile experience"},
      {label: "Performance-critical", description: "Optimize for speed"},
      {label: "Offline support", description: "Work without network"},
      {label: "Accessibility priority", description: "WCAG AA compliance"},
      {label: "Incremental rollout", description: "Feature flags for gradual release"},
      {label: "None", description: "No special constraints"}
    ]
  }
]
```

### Phase 3: Update Project Metadata (Optional)

If `ADMIN_API_KEY` is available, update project metadata:

```bash
if [ -n "$ADMIN_API_KEY" ]; then
  # Update project with new tech stack info
  # Note: Requires project ID - can be fetched via admin API

  PROJECT_UPDATE=$(cat <<'EOF'
{
  "techStack": {
    "frontend": "[NEW_FRONTEND_STACK]",
    "backend": "[EXISTING_OR_NEW]",
    "database": "[EXISTING_OR_NEW]"
  },
  "appType": "[UPDATED_APP_TYPE]"
}
EOF
)

  curl -s -X PUT "${CONVEX_SITE_URL}/api/projects/${PROJECT_ID}" \
    -H "Authorization: Bearer ${ADMIN_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "$PROJECT_UPDATE"
fi
```

If no admin key, skip this phase and focus on task generation.

### Phase 4: Task Suggestion and Discussion

Based on the evolution type, generate relevant task suggestions.

**Step 1: Load domain-specific criteria from task-guide skill**

| Evolution Type  | Primary Domain | Reference File                                     |
| --------------- | -------------- | -------------------------------------------------- |
| Add Frontend    | frontend       | `.claude/skills/task-guide/reference/frontend.md`  |
| Add Backend     | backend        | `.claude/skills/task-guide/reference/backend.md`   |
| Add Database    | database       | `.claude/skills/task-guide/reference/database.md`  |
| Add Integration | fullstack      | `.claude/skills/task-guide/reference/fullstack.md` |
| Add Capability  | varies         | Based on capability domain                         |

**Step 2: Generate task suggestions organized by priority**

Display suggested tasks to user:

```markdown
## Suggested Tasks for "[EVOLUTION_DESCRIPTION]"

Based on adding [SPECIFIC_STACK/INTEGRATION/CAPABILITY], here are recommended tasks:

### Foundation (Priority 1 - Critical)

| #   | Title               | Domain   | Complexity   |
| --- | ------------------- | -------- | ------------ |
| 1   | [Foundation task 1] | [domain] | [complexity] |
| 2   | [Foundation task 2] | [domain] | [complexity] |
| 3   | [Foundation task 3] | [domain] | [complexity] |

### Core Features (Priority 2 - High)

| #   | Title            | Domain   | Complexity   |
| --- | ---------------- | -------- | ------------ |
| 4   | [Core feature 1] | [domain] | [complexity] |
| 5   | [Core feature 2] | [domain] | [complexity] |

### Polish (Priority 3 - Medium)

| #   | Title           | Domain   | Complexity   |
| --- | --------------- | -------- | ------------ |
| 6   | [Polish task 1] | [domain] | [complexity] |
| 7   | [Polish task 2] | [domain] | [complexity] |

### Testing (Priority 4 - Should Have)

| #   | Title         | Domain   | Complexity   |
| --- | ------------- | -------- | ------------ |
| 8   | [Test task 1] | [domain] | [complexity] |
```

**Step 3: Interactive selection**

Use `AskUserQuestion`:

```
questions: [
  {
    question: "Which tasks should I create? (Select individual tasks or use quick options)",
    header: "Tasks",
    multiSelect: true,
    options: [
      {label: "All Foundation (Priority 1)", description: "[N] critical tasks to get started"},
      {label: "Foundation + Core (Priority 1-2)", description: "[N] tasks for working MVP"},
      {label: "All suggested tasks", description: "[N] complete implementation"},
      {label: "Select individually", description: "Choose specific tasks from the list"}
    ]
  }
]
```

If "Select individually" chosen, present individual task selection:

```
questions: [
  {
    question: "Select specific tasks to create:",
    header: "Select",
    multiSelect: true,
    options: [
      {label: "[1] [Foundation task 1]", description: "Priority 1, [complexity]"},
      {label: "[2] [Foundation task 2]", description: "Priority 1, [complexity]"},
      {label: "[3] [Foundation task 3]", description: "Priority 1, [complexity]"},
      {label: "[4] [Core feature 1]", description: "Priority 2, [complexity]"},
      // ... more tasks
    ]
  }
]
```

**Step 4: Allow modifications**

```
questions: [
  {
    question: "Would you like to modify any tasks before creating?",
    header: "Modify",
    multiSelect: false,
    options: [
      {label: "Create as shown", description: "Create [N] selected tasks now"},
      {label: "Adjust priorities", description: "Change task priorities"},
      {label: "Add custom tasks", description: "Include additional tasks you specify"},
      {label: "Remove tasks", description: "Deselect some tasks"}
    ]
  }
]
```

### Phase 5: Create Tasks

Build task objects with full details and call bulk create API:

```bash
source .env.local 2>/dev/null || source .env 2>/dev/null || true

TASKS_JSON=$(cat <<'EOF'
{
  "tasks": [
    {
      "type": "feature",
      "title": "[TASK_TITLE]",
      "description": "[DETAILED_DESCRIPTION]",
      "priority": [1-4],
      "domain": "[frontend/backend/database/fullstack]",
      "complexity": "[simple/medium/complex]",
      "acceptanceCriteria": [
        "[Criterion from task-guide]",
        "[Task-specific criterion]"
      ],
      "dependencies": ["[EXISTING_TASK_ID]"],
      "tags": ["[relevant]", "[tags]"]
    }
  ]
}
EOF
)

RESULT=$(curl -s -X POST "${CONVEX_SITE_URL}/api/tasks/bulk" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "$TASKS_JSON")

if echo "$RESULT" | grep -q '"error"'; then
  echo "Error creating tasks:"
  echo "$RESULT"
  exit 1
fi

echo "Tasks created successfully"

# Extract created task IDs from response
CREATED_TASK_IDS=$(echo "$RESULT" | jq -r '.created[].customId')
```

### Phase 5.5: Record Evolution

After tasks are created successfully, record the evolution in the changelog:

```bash
# Collect unique domains from created tasks
DOMAINS=$(echo "$TASKS_JSON" | jq -r '[.tasks[].domain] | unique | @json')

# Build evolution record
EVOLUTION_JSON=$(cat <<EOF
{
  "evolutionType": "[EVOLUTION_TYPE from Phase 2]",
  "description": "[DESCRIPTION from user input or generated]",
  "techStackBefore": {
    "frontend": "[FROM PROJECT_INFO or null]",
    "backend": "[FROM PROJECT_INFO or null]",
    "database": "[FROM PROJECT_INFO or null]"
  },
  "techStackAfter": {
    "frontend": "[NEW_VALUE or existing]",
    "backend": "[NEW_VALUE or existing]",
    "database": "[NEW_VALUE or existing]"
  },
  "constraints": ["[CONSTRAINTS from Phase 2]"],
  "taskIds": [$(echo "$CREATED_TASK_IDS" | tr '\n' ',' | sed 's/,$//' | sed 's/\([^,]*\)/"\1"/g')],
  "summary": {
    "tasksCreated": [N],
    "domains": $DOMAINS
  }
}
EOF
)

# Record evolution
EVOLUTION_RESULT=$(curl -s -X POST "${CONVEX_SITE_URL}/api/evolutions" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "$EVOLUTION_JSON")

if echo "$EVOLUTION_RESULT" | grep -q '"error"'; then
  echo "Warning: Failed to record evolution"
  echo "$EVOLUTION_RESULT"
else
  EVOLUTION_VERSION=$(echo "$EVOLUTION_RESULT" | jq -r '.version')
  echo "Evolution recorded: v${EVOLUTION_VERSION}"
fi
```

### Phase 6: Summary

Display comprehensive summary:

```
+----------------------------------------------------------+
|              Project Evolution Complete                   |
+----------------------------------------------------------+

## Evolution v[VERSION] Applied

**Type**: [Add Frontend / Add Integration / etc.]
**Details**: [React + TanStack Router / Clerk Auth / etc.]
**Recorded**: [Timestamp]

## Tech Stack Changes
| Layer | Before | After |
|-------|--------|-------|
| Frontend | [old or "Not set"] | [new or unchanged] |
| Backend | [old or "Not set"] | [new or unchanged] |
| Database | [old or "Not set"] | [new or unchanged] |

## Tasks Created: [N]

### Foundation (Priority 1)
| ID | Title | Domain | Complexity |
|----|-------|--------|------------|
| FT-XXX | [Title] | [domain] | [complexity] |

### Core Features (Priority 2)
| ID | Title | Domain | Complexity |
|----|-------|--------|------------|
| FT-XXX | [Title] | [domain] | [complexity] |

[Additional priority groups if applicable]

## Suggested Execution Order

1. Start with foundation: /task-dev [FIRST_TASK_ID]
2. [Next logical step]
3. [Continue sequence...]

+----------------------------------------------------------+

## View Evolution History

  GET /api/evolutions         View all evolutions
  GET /api/evolutions/[N]     View this evolution details

## Next Steps

  /task-dev [FIRST_ID]      Start with first foundation task
  /task-list pending        View all pending tasks
  /project-evolve           Add more capabilities later
```

---

## Task Suggestion Templates

### Adding Frontend (React + TanStack Router)

**Foundation (Priority 1):**

1. Project Setup: React + TanStack Router + Tailwind (medium)
2. Authentication UI: Login/Signup/Logout (medium)
3. Layout: Header, Navigation, Footer (simple)

**Core Features (Priority 2):** 4. [Entity] List Page with Filtering (medium) 5. [Entity] Detail Page with Edit (medium) 6. Create [Entity] Form (simple)

**Polish (Priority 3):** 7. Loading States and Skeletons (simple) 8. Error Handling and Toast Notifications (simple) 9. Mobile Responsive Layout (medium) 10. Dark Mode Support (simple)

**Testing (Priority 4):** 11. E2E Tests: Authentication Flow (medium) 12. E2E Tests: [Entity] CRUD (medium)

### Adding Backend (Convex)

**Foundation (Priority 1):**

1. Convex Setup and Configuration (simple)
2. Schema Definition for Core Entities (medium)
3. Authentication Middleware Setup (medium)

**Core Features (Priority 2):** 4. CRUD Queries and Mutations (medium) 5. Input Validation Layer (simple) 6. Error Handling Patterns (simple)

**Infrastructure (Priority 3):** 7. File Storage Integration (medium) 8. Background Job Setup (medium) 9. API Documentation (simple)

### Adding Integration (Clerk Auth)

**Foundation (Priority 1):**

1. Clerk Provider Setup (simple)
2. Environment Configuration (simple)
3. Auth Middleware/Guards (medium)

**Core Features (Priority 2):** 4. Login Page (simple) 5. Signup Page (simple) 6. Logout Flow (simple) 7. Protected Routes (medium)

**Enhancement (Priority 3):** 8. User Profile Management (medium) 9. Session Handling (simple) 10. Social Login Options (medium)

### Adding Capability (Search)

**Backend (Priority 1):**

1. Search Index Setup (medium)
2. Search Query Endpoint (medium)
3. Index Existing Data (simple)

**Frontend (Priority 2):** 4. Search Input Component (simple) 5. Search Results Display (medium) 6. Filters and Facets (medium)

**Integration (Priority 3):** 7. E2E Search Flow Tests (medium) 8. Search Analytics (simple)

---

## Edge Cases

### No Existing Tasks

If the project has no tasks yet:

```
questions: [
  {
    question: "This project has no existing tasks. Would you like to:",
    header: "Empty Project",
    multiSelect: false,
    options: [
      {label: "Continue with evolution", description: "Generate tasks for the new capability only"},
      {label: "Run /project-init first", description: "Generate a full initial backlog, then evolve"},
      {label: "Cancel", description: "Exit without changes"}
    ]
  }
]
```

### Large Number of Suggested Tasks

If suggesting 15+ tasks, group by phase:

```
## Suggested Tasks: 22 total

Given the scope, I've organized tasks into phases:

**Phase 1 (MVP)**: 8 tasks
Get a working version with core functionality.

**Phase 2 (Enhancement)**: 7 tasks
Add polish and secondary features.

**Phase 3 (Complete)**: 7 tasks
Full feature set with testing.

Which phases would you like to create now?
You can run /project-evolve later for remaining phases.
```

### Conflicting Evolution

If evolution conflicts with existing structure:

```
I noticed you already have [EXISTING] tasks/setup.
Adding [NEW] might conflict.

Options:
1. Proceed anyway (may require migration)
2. Update existing [EXISTING] instead
3. Cancel and review current state
```

### Custom Evolution (Other)

If user selects "Other", ask for free-form description:

```
questions: [
  {
    question: "Describe what you're adding or changing:",
    header: "Custom",
    multiSelect: false,
    options: [
      {label: "Enter description", description: "Provide details about the evolution"}
    ]
  }
]
```

Then:

1. Infer the primary domain from description
2. Load appropriate task-guide reference
3. Generate relevant task suggestions
4. Allow full customization

---

## Error Handling

### API Key Missing

```
Error: CONVEX_SITE_URL and CLAUDE_API_KEY must be set

This project doesn't appear to be registered yet.
Run /project-init first to register and create an API key.
```

### API Request Fails

```
Error creating tasks: [API error message]

Common issues:
- Invalid API key: Verify CLAUDE_API_KEY is correct
- Network error: Check CONVEX_SITE_URL is accessible
- Invalid field values: Check task data format
```

### User Cancels

```
Evolution cancelled. No tasks were created.
```

---

## Field Reference

### Type

All tasks created by `/project-evolve` are features (type: "feature", prefix: FT-).

For bugfixes or refactors, use `/task-init` instead.

### Domain

| Value       | When to Use                  |
| ----------- | ---------------------------- |
| `frontend`  | UI, components, client logic |
| `backend`   | API, services, server logic  |
| `database`  | Schema, migrations, queries  |
| `fullstack` | Spans multiple layers        |

### Priority

| Value | Level    | Description                       |
| ----- | -------- | --------------------------------- |
| 1     | Critical | Foundation, must complete first   |
| 2     | High     | Core features for working product |
| 3     | Medium   | Polish and enhancements           |
| 4     | Low      | Nice to have, testing             |

### Complexity

| Value     | Estimate | Description                      |
| --------- | -------- | -------------------------------- |
| `simple`  | < 1 day  | Straightforward, minimal risk    |
| `medium`  | 1-3 days | Some complexity, standard work   |
| `complex` | 3+ days  | Significant work, multiple parts |

---

## Example Sessions

### Example 1: Adding Frontend to API Project

```
User: /project-evolve adding React frontend

Claude: [Phase 0 - Environment check passes]

Claude: [Phase 1 - Fetch current state]

## Current Project State

### Task Overview
| Status | Count |
|--------|-------|
| Pending | 3 |
| In Progress | 1 |
| Completed | 8 |

### Domains in Use
- Backend: 7 tasks
- Database: 5 tasks
- Frontend: Not detected

Claude: Based on your request, I'll help add a React frontend.

[AskUserQuestion - Stack details]
What frontend stack are you adding?
→ React + TanStack Router (selected)

[AskUserQuestion - Constraints]
→ Mobile-first, Accessibility priority (selected)

Claude: [Phase 4 - Task suggestions]

## Suggested Tasks for "Add React Frontend"

### Foundation (Priority 1 - Critical)
| # | Title | Domain | Complexity |
|---|-------|--------|------------|
| 1 | Project Setup: React + TanStack Router + Tailwind | frontend | medium |
| 2 | Authentication UI: Login/Signup/Logout | frontend | medium |
| 3 | Layout: Header, Navigation, Footer | frontend | simple |

### Core Features (Priority 2 - High)
| # | Title | Domain | Complexity |
|---|-------|--------|------------|
| 4 | Task List Page with Filtering | frontend | medium |
| 5 | Task Detail Page with Edit | frontend | medium |
| 6 | Create Task Form | frontend | simple |

### Polish (Priority 3 - Medium)
| # | Title | Domain | Complexity |
|---|-------|--------|------------|
| 7 | Loading States and Skeletons | frontend | simple |
| 8 | Error Handling and Toasts | frontend | simple |
| 9 | Mobile Responsive Layout | frontend | medium |
| 10 | Dark Mode Support | frontend | simple |

### Testing (Priority 4)
| # | Title | Domain | Complexity |
|---|-------|--------|------------|
| 11 | E2E Tests: Auth Flow | frontend | medium |
| 12 | E2E Tests: Task CRUD | frontend | medium |

[AskUserQuestion - Selection]
Which tasks should I create?
→ Foundation + Core (Priority 1-2) (selected)

[AskUserQuestion - Modify]
→ Create as shown (selected)

Claude: [Phase 5 - Create tasks via API]
Claude: [Phase 6 - Summary]

+----------------------------------------------------------+
|              Project Evolution Complete                   |
+----------------------------------------------------------+

## Evolution Applied

**Type**: Add Frontend
**Details**: React + TanStack Router

## Tasks Created

Total: 6 tasks

| ID | Title | Priority | Complexity |
|----|-------|----------|------------|
| FT-012 | Project Setup: React + TanStack Router + Tailwind | 1 | medium |
| FT-013 | Authentication UI: Login/Signup/Logout | 1 | medium |
| FT-014 | Layout: Header, Navigation, Footer | 1 | simple |
| FT-015 | Task List Page with Filtering | 2 | medium |
| FT-016 | Task Detail Page with Edit | 2 | medium |
| FT-017 | Create Task Form | 2 | simple |

## Suggested Execution Order

1. Start with setup: /task-dev FT-012
2. Add authentication: /task-dev FT-013
3. Build layout: /task-dev FT-014
4. Then core features: FT-015, FT-016, FT-017

+----------------------------------------------------------+

Next: /task-dev FT-012
```

### Example 2: Adding Clerk Authentication

```
User: /project-evolve

Claude: [Phase 1 - Current state]

[AskUserQuestion - Evolution type]
→ Add Integration (selected)

[AskUserQuestion - Integration type]
→ Authentication (Clerk) (selected)

Claude: [Phase 4 - Suggestions]

## Suggested Tasks for "Add Clerk Authentication"

### Foundation (Priority 1)
| # | Title | Domain | Complexity |
|---|-------|--------|------------|
| 1 | Clerk Provider Setup | fullstack | simple |
| 2 | Environment Configuration | backend | simple |
| 3 | Auth Middleware/Guards | backend | medium |

### Core Features (Priority 2)
| # | Title | Domain | Complexity |
|---|-------|--------|------------|
| 4 | Login Page | frontend | simple |
| 5 | Signup Page | frontend | simple |
| 6 | Logout Flow | frontend | simple |
| 7 | Protected Routes | frontend | medium |

[Selection and creation continues...]

Tasks Created: FT-018 through FT-024
```

### Example 3: Adding Search Capability

```
User: /project-evolve add search functionality

Claude: [Parses: "Add Capability" - "Search"]

## Suggested Tasks for "Add Search"

### Backend (Priority 1)
| # | Title | Domain | Complexity |
|---|-------|--------|------------|
| 1 | Search Index Setup | backend | medium |
| 2 | Search Query Endpoint | backend | medium |
| 3 | Index Existing Data | backend | simple |

### Frontend (Priority 2)
| # | Title | Domain | Complexity |
|---|-------|--------|------------|
| 4 | Search Input Component | frontend | simple |
| 5 | Search Results Display | frontend | medium |
| 6 | Search Filters | frontend | medium |

### Integration (Priority 3)
| # | Title | Domain | Complexity |
|---|-------|--------|------------|
| 7 | E2E Search Flow Tests | fullstack | medium |

[Selection: All Priority 1-2]

Tasks Created: FT-025 through FT-030
```

---

## API Reference

### GET /api/tasks

Fetches all tasks for the project.

**Request**:

```bash
curl -s -X GET "${CONVEX_SITE_URL}/api/tasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Response**:

```json
{
  "tasks": [
    {
      "customId": "FT-001",
      "title": "...",
      "status": "completed",
      "domain": "backend",
      ...
    }
  ]
}
```

### POST /api/tasks/bulk

Creates multiple tasks at once.

**Request**:

```json
{
  "tasks": [
    {
      "type": "feature",
      "title": "Task Title",
      "description": "Detailed description",
      "priority": 1,
      "domain": "frontend",
      "complexity": "medium",
      "acceptanceCriteria": ["Criterion 1", "Criterion 2"],
      "tags": ["ui", "setup"]
    }
  ]
}
```

**Response** (201 Created):

```json
{
  "created": [
    { "customId": "FT-012", "title": "Task Title" },
    { "customId": "FT-013", "title": "Another Task" }
  ]
}
```

---

## Integration with Other Commands

| Command          | Relationship                                                      |
| ---------------- | ----------------------------------------------------------------- |
| `/project-init`  | Creates new project; `/project-evolve` extends existing           |
| `/task-init`     | Creates single task manually; `/project-evolve` generates batches |
| `/task-dev [ID]` | Implement tasks created by `/project-evolve`                      |
| `/task-list`     | View all tasks including those from evolution                     |
| `/feature`       | Ad-hoc feature without task tracking                              |

---

## Related Skills

| Skill        | Purpose                                             |
| ------------ | --------------------------------------------------- |
| `task-guide` | Domain-specific acceptance criteria, best practices |
| `task`       | API integration for fetching/updating tasks         |
| `frontend`   | Frontend development patterns                       |
| `backend`    | Backend development patterns                        |
| `database`   | Database patterns                                   |
