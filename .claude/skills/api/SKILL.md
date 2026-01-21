---
name: api
description: Task Manager HTTP API reference. Complete documentation for all endpoints including authentication, request/response schemas, and error handling.
context: fork
agent: api-executor
---

# Task Manager HTTP API

Complete reference for the Task Manager Convex HTTP API endpoints.

## Authentication

Two types of API keys:

| Key Type        | Variable         | Purpose                                            |
| --------------- | ---------------- | -------------------------------------------------- |
| **Admin Key**   | `ADMIN_API_KEY`  | Project management (create, list, delete projects) |
| **Project Key** | `CLAUDE_API_KEY` | Task operations (create, list, update tasks)       |

**Usage:**

```bash
curl -X GET "${CONVEX_SITE_URL}/api/endpoint" \
  -H "Authorization: Bearer ${API_KEY}"
```

**Important:** HTTP endpoints use `.convex.site` domain, not `.convex.cloud`.

---

## Admin Endpoints

These require `ADMIN_API_KEY` for authentication.

### POST /api/projects

Create a new project.

```bash
curl -X POST "${CONVEX_SITE_URL}/api/projects" \
  -H "Authorization: Bearer ${ADMIN_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Project",
    "description": "Project description",
    "techStack": {
      "frontend": "React",
      "backend": "Convex",
      "database": "Convex"
    },
    "appType": "web"
  }'
```

**Request Body:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Project name |
| `description` | string | No | Project description |
| `techStack` | object | No | `{frontend, backend, database}` |
| `appType` | string | No | Application type |

**Response (201):**

```json
{
  "_id": "abc123...",
  "name": "My Project",
  "description": "Project description",
  "createdAt": 1704672000000
}
```

---

### GET /api/projects

List all projects. Supports optional `?name=` filter.

```bash
# List all projects
curl -X GET "${CONVEX_SITE_URL}/api/projects" \
  -H "Authorization: Bearer ${ADMIN_API_KEY}"

# Filter by name (for duplicate detection)
curl -X GET "${CONVEX_SITE_URL}/api/projects?name=MyProject" \
  -H "Authorization: Bearer ${ADMIN_API_KEY}"
```

**Response (200) - List:**

```json
{
  "projects": [
    {
      "_id": "abc123...",
      "name": "My Project",
      "description": "...",
      "createdAt": 1704672000000
    }
  ]
}
```

**Response (200) - Filter by name:**

```json
{
  "project": {
    "_id": "abc123...",
    "name": "My Project",
    ...
  }
}
```

Returns `{ "project": null }` if not found.

---

### GET /api/projects/{projectId}

Get a single project by ID.

```bash
curl -X GET "${CONVEX_SITE_URL}/api/projects/${PROJECT_ID}" \
  -H "Authorization: Bearer ${ADMIN_API_KEY}"
```

**Response (200):**

```json
{
  "_id": "abc123...",
  "name": "My Project",
  "description": "Project description",
  "techStack": {...},
  "appType": "web",
  "createdAt": 1704672000000
}
```

---

### PUT /api/projects/{projectId}

Update project fields.

```bash
curl -X PUT "${CONVEX_SITE_URL}/api/projects/${PROJECT_ID}" \
  -H "Authorization: Bearer ${ADMIN_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated description",
    "techStack": {
      "frontend": "React",
      "backend": "Convex",
      "database": "Convex"
    }
  }'
```

**Request Body:** Any project fields to update.

**Response (200):** Updated project object.

---

### DELETE /api/projects/{projectId}

Delete a project and all associated data (tasks, API keys, counters).

```bash
curl -X DELETE "${CONVEX_SITE_URL}/api/projects/${PROJECT_ID}" \
  -H "Authorization: Bearer ${ADMIN_API_KEY}"
```

**Response (200):**

```json
{
  "success": true,
  "deleted": {
    "tasks": 5,
    "apiKeys": 2,
    "counters": 3
  }
}
```

---

### POST /api/projects/{projectId}/keys

Create an API key for a project.

```bash
curl -X POST "${CONVEX_SITE_URL}/api/projects/${PROJECT_ID}/keys" \
  -H "Authorization: Bearer ${ADMIN_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"name": "Claude Code Key"}'
```

**Request Body:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Key name/description |

**Response (201):**

```json
{
  "key": "sk_abc123..."
}
```

**Important:** The key is only shown once. Save it immediately.

---

### DELETE /api/projects/{projectId}/keys/{keyId}

Revoke an API key.

```bash
curl -X DELETE "${CONVEX_SITE_URL}/api/projects/${PROJECT_ID}/keys/${KEY_ID}" \
  -H "Authorization: Bearer ${ADMIN_API_KEY}"
```

**Response (200):**

```json
{
  "success": true
}
```

---

## Project Endpoints

These require a project-specific `CLAUDE_API_KEY` for authentication.

### GET /api/projects/current

Get the current project (from API key). Useful for CLI tools to fetch project metadata without knowing the project ID.

```bash
curl -X GET "${CONVEX_SITE_URL}/api/projects/current" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Note:** Must use project API key, NOT admin key.

**Response (200):**

```json
{
  "_id": "abc123...",
  "name": "My Project",
  "description": "Project description",
  "techStack": {
    "frontend": "React",
    "backend": "Convex",
    "database": "Convex"
  },
  "appType": "web",
  "createdAt": 1704672000000
}
```

**Errors:**

- `400` - Admin key used (must use project key)
- `401` - Invalid API key

---

### POST /api/tasks

Create a single task.

```bash
curl -X POST "${CONVEX_SITE_URL}/api/tasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "feature",
    "title": "Add search bar",
    "description": "Add search functionality to task list",
    "priority": 2,
    "domain": "frontend",
    "complexity": "medium",
    "acceptanceCriteria": ["Search works", "Results update in real-time"],
    "tags": ["ui", "search"]
  }'
```

**Request Body:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `type` | string | Yes | `feature`, `bugfix`, or `refactor` |
| `title` | string | Yes | Task title |
| `description` | string | No | Detailed description |
| `priority` | number | No | 1-4 (1=critical, 4=low) |
| `domain` | string | No | `frontend`, `backend`, `database`, `fullstack` |
| `complexity` | string | No | `simple`, `medium`, `complex` |
| `acceptanceCriteria` | string[] | No | List of acceptance criteria |
| `dependencies` | string[] | No | Task IDs this depends on |
| `tags` | string[] | No | Categorization tags |

**Response (201):**

```json
{
  "customId": "FT-001",
  "title": "Add search bar",
  "type": "feature",
  "status": "pending",
  "priority": 2,
  "domain": "frontend",
  "createdAt": 1704672000000
}
```

---

### POST /api/tasks/bulk

Create multiple tasks at once.

```bash
curl -X POST "${CONVEX_SITE_URL}/api/tasks/bulk" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "tasks": [
      {"type": "feature", "title": "Task 1", "priority": 1},
      {"type": "feature", "title": "Task 2", "priority": 2}
    ]
  }'
```

**Request Body:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `tasks` | array | Yes | Array of task objects (same fields as POST /api/tasks) |

**Response (201):**

```json
{
  "created": [
    { "customId": "FT-001", "title": "Task 1" },
    { "customId": "FT-002", "title": "Task 2" }
  ]
}
```

---

### POST /api/tasks/bulk-fetch

Fetch multiple tasks by custom ID. Used by the `/task-dev` pre-fetch hook.

```bash
curl -X POST "${CONVEX_SITE_URL}/api/tasks/bulk-fetch" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "customIds": ["FT-001", "FT-002", "BF-003"]
  }'
```

**Request Body:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `customIds` | string[] | Yes | Array of task custom IDs (max 50) |

**Response (200):**

```json
{
  "tasks": [
    {
      "_id": "...",
      "customId": "FT-001",
      "title": "Add search bar",
      "type": "feature",
      "status": "pending",
      "priority": 2,
      "domain": "frontend",
      "description": "...",
      "acceptanceCriteria": ["..."],
      "dependencies": [],
      "tags": ["ui"],
      "createdAt": 1704672000000,
      "updatedAt": 1704672000000
    }
  ],
  "notFound": ["BF-999"]
}
```

**Notes:**
- Returns all found tasks in `tasks` array
- Any IDs not found are listed in `notFound` array
- Maximum 50 IDs per request

---

### POST /api/tasks/bulk-status

Update status for multiple tasks at once. Used by the `/task-dev` pre-fetch hook.

```bash
curl -X POST "${CONVEX_SITE_URL}/api/tasks/bulk-status" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "customIds": ["FT-001", "FT-002"],
    "status": "in_progress"
  }'
```

**Request Body:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `customIds` | string[] | Yes | Array of task custom IDs (max 50) |
| `status` | string | Yes | `pending`, `in_progress`, `completed`, `blocked` |

**Response (200):**

```json
{
  "updated": ["FT-001", "FT-002"],
  "notFound": ["BF-999"]
}
```

**Notes:**
- Successfully updated IDs in `updated` array
- Any IDs not found are listed in `notFound` array
- Maximum 50 IDs per request
- Does not support `blockedReason` (use single PATCH for that)

---

### GET /api/tasks

List all tasks with optional filters.

```bash
# All tasks
curl -X GET "${CONVEX_SITE_URL}/api/tasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"

# Filter by status
curl -X GET "${CONVEX_SITE_URL}/api/tasks?status=pending" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"

# Multiple filters
curl -X GET "${CONVEX_SITE_URL}/api/tasks?status=pending&type=feature&domain=frontend" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Query Parameters:**
| Param | Values |
|-------|--------|
| `status` | `pending`, `in_progress`, `completed`, `blocked` |
| `type` | `feature`, `bugfix`, `refactor` |
| `domain` | `frontend`, `backend`, `database`, `fullstack` |
| `priority` | `1`, `2`, `3`, `4` |
| `tags` | Comma-separated tags (e.g., `auth,ui`) |

**Response (200):**

```json
{
  "tasks": [
    {
      "_id": "...",
      "customId": "FT-001",
      "title": "Add search bar",
      "type": "feature",
      "status": "pending",
      "priority": 2,
      "domain": "frontend",
      "createdAt": 1704672000000,
      "updatedAt": 1704672000000
    }
  ]
}
```

---

### GET /api/tasks/{customId}

Get a single task by custom ID.

```bash
curl -X GET "${CONVEX_SITE_URL}/api/tasks/FT-001" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Response (200):**

```json
{
  "_id": "...",
  "customId": "FT-001",
  "projectId": "...",
  "type": "feature",
  "title": "Add search bar",
  "description": "...",
  "status": "pending",
  "priority": 2,
  "domain": "frontend",
  "complexity": "medium",
  "acceptanceCriteria": ["..."],
  "dependencies": [],
  "tags": ["ui"],
  "images": [],
  "createdAt": 1704672000000,
  "updatedAt": 1704672000000
}
```

---

### PATCH /api/tasks/{customId}/status

Update task status. Supports `blockedReason` when setting status to "blocked".

```bash
# Update to in_progress
curl -X PATCH "${CONVEX_SITE_URL}/api/tasks/FT-001/status" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"status": "in_progress"}'

# Set as blocked with reason
curl -X PATCH "${CONVEX_SITE_URL}/api/tasks/FT-001/status" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"status": "blocked", "blockedReason": "Waiting for API design approval"}'
```

**Request Body:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `status` | string | Yes | `pending`, `in_progress`, `completed`, `blocked` |
| `blockedReason` | string | No | Reason for blocking (when status="blocked") |

**Response (200):**

```json
{
  "success": true
}
```

**Note:** When status changes from "blocked" to another status, `blockedReason` is automatically cleared.

---

### PUT /api/tasks/{customId}

Update task fields. Supports `images` for reference screenshots.

```bash
curl -X PUT "${CONVEX_SITE_URL}/api/tasks/FT-001" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Add search bar with autocomplete",
    "priority": 1,
    "tags": ["ui", "search", "urgent"],
    "images": ["storage_id_1", "storage_id_2"]
  }'
```

**Request Body:** Any task fields to update:
| Field | Type | Description |
|-------|------|-------------|
| `title` | string | Task title |
| `description` | string | Detailed description |
| `priority` | number | 1-4 |
| `domain` | string | `frontend`, `backend`, `database`, `fullstack` |
| `complexity` | string | `simple`, `medium`, `complex` |
| `acceptanceCriteria` | string[] | Acceptance criteria |
| `dependencies` | string[] | Task IDs |
| `tags` | string[] | Tags |
| `images` | string[] | Storage IDs for reference images |

**Response (200):** Updated task object.

---

### DELETE /api/tasks/{customId}

Delete a single task.

```bash
curl -X DELETE "${CONVEX_SITE_URL}/api/tasks/FT-001" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Response (200):**

```json
{
  "success": true,
  "deletedId": "FT-001"
}
```

---

### DELETE /api/tasks

Delete all tasks for the project.

```bash
curl -X DELETE "${CONVEX_SITE_URL}/api/tasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Response (200):**

```json
{
  "success": true,
  "deletedCount": 10
}
```

---

## Evolution Endpoints

These require a project-specific `CLAUDE_API_KEY` for authentication.

### POST /api/evolutions

Record a project evolution (changelog entry).

```bash
curl -X POST "${CONVEX_SITE_URL}/api/evolutions" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "evolutionType": "Add Frontend",
    "description": "Adding React + TanStack Router frontend",
    "techStackBefore": {
      "frontend": null,
      "backend": "Convex",
      "database": "Convex"
    },
    "techStackAfter": {
      "frontend": "React",
      "backend": "Convex",
      "database": "Convex"
    },
    "constraints": ["mobile-first", "accessibility"],
    "taskIds": ["FT-001", "FT-002", "FT-003"],
    "summary": {
      "tasksCreated": 3,
      "domains": ["frontend"]
    }
  }'
```

**Request Body:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `evolutionType` | string | Yes | Type of evolution (e.g., "Add Frontend", "Add Integration") |
| `description` | string | Yes | Description of the evolution |
| `techStackBefore` | object | No | Tech stack before evolution `{frontend, backend, database}` |
| `techStackAfter` | object | No | Tech stack after evolution `{frontend, backend, database}` |
| `constraints` | string[] | No | Constraints applied (e.g., "mobile-first") |
| `taskIds` | string[] | Yes | Task IDs created in this evolution |
| `summary` | object | No | Summary stats `{tasksCreated, domains}` |

**Response (201):**

```json
{
  "evolutionId": "abc123...",
  "version": 1
}
```

**Note:** Version auto-increments per project (1, 2, 3...).

---

### GET /api/evolutions

List evolution history for the project (ordered by version desc).

```bash
curl -X GET "${CONVEX_SITE_URL}/api/evolutions" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Response (200):**

```json
{
  "evolutions": [
    {
      "_id": "...",
      "projectId": "...",
      "version": 2,
      "timestamp": 1704672000000,
      "evolutionType": "Add Search",
      "description": "Adding full-text search capability",
      "techStackBefore": {...},
      "techStackAfter": {...},
      "constraints": ["performance-critical"],
      "taskIds": ["FT-004", "FT-005", "FT-006"],
      "summary": {"tasksCreated": 3, "domains": ["backend", "frontend"]}
    },
    {
      "_id": "...",
      "version": 1,
      "evolutionType": "Add Frontend",
      ...
    }
  ]
}
```

---

### GET /api/evolutions/{version}

Get a single evolution by version number.

```bash
curl -X GET "${CONVEX_SITE_URL}/api/evolutions/1" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Response (200):**

```json
{
  "_id": "...",
  "projectId": "...",
  "version": 1,
  "timestamp": 1704585600000,
  "evolutionType": "Add Frontend",
  "description": "Adding React + TanStack Router frontend",
  "techStackBefore": {
    "frontend": null,
    "backend": "Convex",
    "database": "Convex"
  },
  "techStackAfter": {
    "frontend": "React",
    "backend": "Convex",
    "database": "Convex"
  },
  "constraints": ["mobile-first"],
  "taskIds": ["FT-001", "FT-002", "FT-003"],
  "summary": {
    "tasksCreated": 3,
    "domains": ["frontend"]
  }
}
```

**Errors:**

- `404` - Evolution version not found

---

## Task ID Prefixes

| Prefix | Type     |
| ------ | -------- |
| `FT-`  | feature  |
| `BF-`  | bugfix   |
| `RF-`  | refactor |

IDs auto-increment per type (FT-001, FT-002, BF-001, etc.)

---

## Task Status Values

| Status        | Description                               |
| ------------- | ----------------------------------------- |
| `pending`     | Not started                               |
| `in_progress` | Currently being worked on                 |
| `completed`   | Successfully finished                     |
| `blocked`     | Cannot proceed (use with `blockedReason`) |

---

## Error Responses

All errors return JSON:

```json
{
  "error": "Error message description"
}
```

**HTTP Status Codes:**
| Code | Meaning |
|------|---------|
| `200` | Success |
| `201` | Created |
| `400` | Bad request (invalid input) |
| `401` | Unauthorized (invalid or missing API key) |
| `404` | Not found |
| `500` | Server error |

---

## Bash Helper Functions

```bash
# Source environment
source .env 2>/dev/null || source .env.local 2>/dev/null || true

# Fetch task by ID
fetch_task() {
  local task_id="$1"
  curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${task_id}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}"
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

# Get current project
get_current_project() {
  curl -s -X GET "${CONVEX_SITE_URL}/api/projects/current" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}"
}

# List tasks with filter
list_tasks() {
  local filter="${1:-}"
  curl -s -X GET "${CONVEX_SITE_URL}/api/tasks${filter}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}"
}
```

---

## Related Commands

| Command           | Uses                                                                   |
| ----------------- | ---------------------------------------------------------------------- |
| `/project-init`   | POST /api/projects, POST /api/projects/{id}/keys, POST /api/tasks/bulk |
| `/phase-init`     | GET /api/phases, GET /api/tasks, POST /api/phases, POST /api/tasks     |
| `/task-init`      | GET /api/tasks (duplicate check), POST /api/tasks                      |
| `/task`           | GET /api/tasks/{id}, PATCH /api/tasks/{id}/status                      |
| `/task-list`      | GET /api/tasks with filters                                            |
| `/dev-loop`       | POST/GET/PATCH/DELETE /api/loop-sessions                               |

---

## Loop Sessions API

The dev-loop system uses a `loopSessions` table to track automated execution sessions.

### POST /api/loop-sessions

Create a new loop session.

**Request:**
```json
{
  "active": true,
  "cycle": 0,
  "maxCycles": 50,
  "completionType": "until-phase",
  "completionValue": "2",
  "batchSize": 1,
  "tasksCompleted": [],
  "tasksBlocked": [],
  "currentBatch": []
}
```

**Response (201 Created):**
```json
{
  "_id": "session_abc123",
  "projectId": "proj_xyz",
  "active": true,
  "cycle": 0,
  "maxCycles": 50,
  "completionType": "until-phase",
  "completionValue": "2",
  "batchSize": 1,
  "tasksCompleted": [],
  "tasksBlocked": [],
  "currentBatch": [],
  "startedAt": 1705320000000,
  "lastCycleAt": 1705320000000
}
```

### GET /api/loop-sessions/active

Get the active loop session for the current project.

**Response:**
```json
{
  "_id": "session_abc123",
  "active": true,
  "cycle": 3,
  "maxCycles": 50,
  "completionType": "until-phase",
  "completionValue": "2",
  "tasksCompleted": ["FT-001", "FT-002", "FT-003"],
  "currentBatch": ["FT-004"]
}
```

### GET /api/loop-sessions/{id}

Get a specific loop session by ID.

### PATCH /api/loop-sessions/{id}

Update a loop session.

**Request:**
```json
{
  "cycle": 4,
  "tasksCompleted": ["FT-001", "FT-002", "FT-003", "FT-004"],
  "currentBatch": ["FT-005"],
  "lastCycleAt": 1705321000000
}
```

### DELETE /api/loop-sessions/{id}

End and delete a loop session.

### Convex Schema (loopSessions table)

```typescript
loopSessions: defineTable({
  projectId: v.id("projects"),
  active: v.boolean(),
  cycle: v.number(),
  maxCycles: v.number(),
  completionType: v.string(),  // "until-phase" | "until-task" | "all-pending" | "until-milestone"
  completionValue: v.string(), // phase number, task ID, or milestone name
  batchSize: v.number(),
  tasksCompleted: v.array(v.string()),
  tasksBlocked: v.array(v.string()),
  currentBatch: v.array(v.string()),
  startedAt: v.number(),
  lastCycleAt: v.number(),
}).index("by_project_active", ["projectId", "active"])
