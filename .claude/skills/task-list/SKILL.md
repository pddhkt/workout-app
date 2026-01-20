---
name: task-list
description: Fetch and display all tasks from the backend with optional filtering by status, type, or domain.
---

# Task List Command

Fetch and display all tasks from the Convex backend with optional filtering.

## Arguments

Optionally provide a filter (status, type, or domain).

---

## Overview

This command lists all tasks for the current project with formatted output:

1. Fetches tasks from Convex via HTTP API
2. Applies optional filters (status, type, domain)
3. Displays formatted table with task details
4. Shows summary counts by status

---

## Environment Variables

Required in `.env.local` or `.env`:

| Variable          | Description                                  |
| ----------------- | -------------------------------------------- |
| `CONVEX_SITE_URL` | https://[deployment].convex.site             |
| `CLAUDE_API_KEY`  | Project-specific API key for task operations |

---

## Workflow

### Phase 0: Environment Check

Verify API configuration before starting:

```bash
source .env.local 2>/dev/null || source .env 2>/dev/null || true

if [ -z "$CONVEX_SITE_URL" ] || [ -z "$CLAUDE_API_KEY" ]; then
  echo "Error: CONVEX_SITE_URL and CLAUDE_API_KEY must be set"
  echo "Run /project-init first to create a project and API key"
  exit 1
fi

echo "API configured: $CONVEX_SITE_URL"
```

### Phase 1: Parse Arguments

Parse provided arguments to determine filters:

**Shorthand filters (positional):**

- `pending`, `in_progress`, `completed`, `blocked` → Status filter
- `feature`, `bugfix`, `refactor` → Type filter
- `frontend`, `backend`, `database`, `fullstack` → Domain filter

**Named filters:**

- `--status=pending` → Status filter
- `--type=feature` → Type filter
- `--domain=frontend` → Domain filter

**Examples:**

```
/task-list                    # All tasks
/task-list pending            # Filter by status=pending
/task-list feature            # Filter by type=feature
/task-list --status=pending   # Named filter
/task-list --type=bugfix --domain=frontend  # Multiple filters
```

### Phase 2: Fetch Tasks

Build API URL with query parameters and fetch:

```bash
source .env.local 2>/dev/null || source .env 2>/dev/null || true

# Build query string from parsed filters
QUERY_PARAMS=""
# Add ?status=X, &type=Y, &domain=Z as needed

# Fetch tasks
RESULT=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks${QUERY_PARAMS}" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json")

# Check for errors
if echo "$RESULT" | grep -q '"error"'; then
  echo "Error fetching tasks:"
  echo "$RESULT"
  exit 1
fi
```

### Phase 3: Display Results

Parse JSON response and display formatted table:

```
+----------------------------------------------------------+
|                     Task List                             |
+----------------------------------------------------------+

Showing: [X] tasks [filter info if applied]

| ID      | Title                        | Type    | Status      | Pri | Domain   |
|---------|------------------------------|---------|-------------|-----|----------|
| FT-001  | Add user authentication      | feature | pending     | 1   | fullstack|
| FT-002  | Add search bar to task list  | feature | in_progress | 2   | frontend |
| BF-001  | Fix login error message      | bugfix  | completed   | 1   | frontend |

+----------------------------------------------------------+
Summary: X pending | Y in_progress | Z completed | W blocked
+----------------------------------------------------------+

Commands:
  /task-list                  Show all tasks
  /task-list pending          Filter by status
  /task-list --type=bugfix    Filter by type
  /task-dev FT-001                Start working on a task
```

**Empty state:**

```
+----------------------------------------------------------+
|                     Task List                             |
+----------------------------------------------------------+

No tasks found.

Commands:
  /task-init                  Create a new task
  /project-init               Initialize a project with tasks
```

---

## Filter Reference

### Status

| Value         | Description               |
| ------------- | ------------------------- |
| `pending`     | Not yet started           |
| `in_progress` | Currently being worked on |
| `completed`   | Finished                  |
| `blocked`     | Cannot proceed            |

### Type

| Value      | Prefix | Description         |
| ---------- | ------ | ------------------- |
| `feature`  | FT-    | New functionality   |
| `bugfix`   | BF-    | Fix broken behavior |
| `refactor` | RF-    | Code improvement    |

### Domain

| Value       | Description                  |
| ----------- | ---------------------------- |
| `frontend`  | UI, components, client logic |
| `backend`   | API, services, server logic  |
| `database`  | Schema, migrations, queries  |
| `fullstack` | Spans multiple layers        |

---

## Output Formatting

### Priority Display

| Value | Display      |
| ----- | ------------ |
| 1     | 1 (Critical) |
| 2     | 2 (High)     |
| 3     | 3 (Medium)   |
| 4     | 4 (Low)      |

### Title Truncation

Truncate titles longer than 30 characters with "..." to fit table format.

### Sorting

Tasks are displayed sorted by:

1. Priority (ascending: 1 first)
2. Type (feature, bugfix, refactor)
3. Custom ID (ascending: FT-001 before FT-002)

---

## Error Handling

### API Key Missing

```
Error: CONVEX_SITE_URL and CLAUDE_API_KEY must be set

To fix:
1. Run /project-init to create a project (creates API key)
2. Or manually add to .env.local:
   CONVEX_SITE_URL=https://your-deployment.convex.site
   CLAUDE_API_KEY=your-api-key
```

### API Request Fails

```
Error fetching tasks: [API error message]

Common issues:
- Invalid API key: Verify CLAUDE_API_KEY is correct
- Network error: Check CONVEX_SITE_URL is accessible
```

### Invalid Filter

```
Invalid filter: "[value]"

Valid filters:
  Status: pending, in_progress, completed, blocked
  Type: feature, bugfix, refactor
  Domain: frontend, backend, database, fullstack
```

---

## API Reference

### GET /api/tasks

**Request:**

```
GET /api/tasks?status=pending&type=feature&domain=frontend
Authorization: Bearer <CLAUDE_API_KEY>
```

**Response (200 OK):**

```json
{
  "tasks": [
    {
      "customId": "FT-001",
      "title": "Add user authentication",
      "description": "Implement secure user authentication",
      "type": "feature",
      "status": "pending",
      "priority": 1,
      "domain": "fullstack",
      "complexity": "medium",
      "tags": ["auth", "security"],
      "acceptanceCriteria": ["Users can register", "Users can log in"],
      "dependencies": [],
      "createdAt": 1704672000000,
      "updatedAt": 1704672000000
    }
  ]
}
```

---

## Integration with Other Commands

| Command          | Purpose                              |
| ---------------- | ------------------------------------ |
| `/task-dev [ID]` | Start working on a task              |
| `/task-init`     | Create a new task                    |
| `/project-init`  | Initialize project with task backlog |

---

## Examples

### List All Tasks

```
User: /task-list

Claude: [Fetches all tasks]

+----------------------------------------------------------+
|                     Task List                             |
+----------------------------------------------------------+

Showing: 5 tasks

| ID      | Title                        | Type    | Status      | Pri | Domain   |
|---------|------------------------------|---------|-------------|-----|----------|
| FT-001  | Add user authentication      | feature | pending     | 1   | fullstack|
| FT-002  | Add search bar               | feature | pending     | 2   | frontend |
| FT-003  | Add task filtering           | feature | in_progress | 2   | frontend |
| BF-001  | Fix login error              | bugfix  | completed   | 1   | frontend |
| RF-001  | Extract auth hook            | refactor| pending     | 3   | frontend |

+----------------------------------------------------------+
Summary: 3 pending | 1 in_progress | 1 completed | 0 blocked
+----------------------------------------------------------+
```

### Filter by Status

```
User: /task-list pending

Claude: [Fetches tasks with status=pending]

+----------------------------------------------------------+
|                     Task List                             |
+----------------------------------------------------------+

Showing: 3 tasks (filtered by status: pending)

| ID      | Title                        | Type    | Status  | Pri | Domain   |
|---------|------------------------------|---------|---------|-----|----------|
| FT-001  | Add user authentication      | feature | pending | 1   | fullstack|
| FT-002  | Add search bar               | feature | pending | 2   | frontend |
| RF-001  | Extract auth hook            | refactor| pending | 3   | frontend |

+----------------------------------------------------------+
```

### Filter by Type

```
User: /task-list --type=bugfix

Claude: [Fetches tasks with type=bugfix]

+----------------------------------------------------------+
|                     Task List                             |
+----------------------------------------------------------+

Showing: 1 task (filtered by type: bugfix)

| ID      | Title                        | Type   | Status    | Pri | Domain   |
|---------|------------------------------|--------|-----------|-----|----------|
| BF-001  | Fix login error              | bugfix | completed | 1   | frontend |

+----------------------------------------------------------+
```
