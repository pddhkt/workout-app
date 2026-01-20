---
name: project-init
description: Context-aware project initialization. Explores codebase, detects tech stack, registers project in Convex, generates task backlog.
allowed-tools:
  - Read
  - Grep
  - Glob
  - Bash
  - Task
  - AskUserQuestion
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: ".claude/scripts/validate-project-init.sh"
---

# Project Initialization Workflow

Context-aware project initialization that explores the codebase first, then asks relevant questions based on what exists. Works for both new and existing projects.

## Arguments

Optionally provide a project name. If not provided, the name will be detected from package.json or directory name.

---

## Overview

This command replaces the old `/project` command with a smarter, context-aware approach:

1. **Explores codebase first** - Detects tech stack, structure, existing features
2. **Presents findings** - Shows what was discovered before asking questions
3. **Asks contextual questions** - Adapts questions based on what already exists
4. **Creates/updates project** - Registers in Convex with detected + user-provided info
5. **Generates smart task backlog** - Only for missing features, not duplicates

---

## Environment Variables

Required in `.env.local` or `.env`:

| Variable          | Description                      |
| ----------------- | -------------------------------- |
| `CONVEX_SITE_URL` | https://[deployment].convex.site |
| `ADMIN_API_KEY`   | Admin key for project creation   |

After running, creates:

| Variable         | Description                              |
| ---------------- | ---------------------------------------- |
| `CLAUDE_API_KEY` | Project-specific key for task operations |

---

## Workflow

### Phase 0: Project Exploration

**IMPORTANT**: Before asking any questions, explore the codebase to understand what exists.

Invoke the `project-exploration` skill:

```
The skill will automatically:
1. Fork to project-scout agent (with project + spec skills loaded)
2. Detect tech stack from package.json
3. Map project structure
4. Identify existing/missing features
5. Analyze code patterns
6. Return a structured markdown report
```

The skill handles all exploration logic. Simply invoke it and use the returned report.

### Phase 1: Present Findings & Ask Intent

Display the exploration results to the user:

```markdown
## Codebase Analysis

**Project**: [NAME from package.json or directory]

### Tech Stack Detected

| Layer    | Technology              |
| -------- | ----------------------- |
| Frontend | [React + TypeScript]    |
| Backend  | [Convex]                |
| Database | [Convex]                |
| Styling  | [Tailwind CSS]          |
| Auth     | [Clerk / None detected] |

### Project Structure

- `src/components/` - [N] components
- `src/routes/` - [N] routes
- `convex/` - [N] functions
- Tests: [Found / Not found]

### Feature Status

| Feature        | Status | Notes               |
| -------------- | ------ | ------------------- |
| Authentication | ✓      | Clerk integration   |
| User profiles  | ○      | Basic, no avatar    |
| Task CRUD      | ✓      | Full implementation |
| Search         | ✗      | Not implemented     |
| Export         | ✗      | Not implemented     |

### Patterns Detected

- Functional components with hooks
- Convex queries/mutations for data
- TanStack Router for routing
```

Then ask about intent:

```
questions: [
  {
    question: "Based on what I found, what would you like to do?",
    header: "Intent",
    multiSelect: false,
    options: [
      {label: "Register project", description: "Save to Convex, generate API key for /task commands"},
      {label: "Generate tasks", description: "Create task backlog for missing/incomplete features"},
      {label: "Both", description: "Register project AND generate task backlog (Recommended)"},
      {label: "Just exploring", description: "No changes needed, I just wanted the analysis"}
    ]
  }
]
```

If "Just exploring" → End workflow with summary.

### Phase 2: Dynamic Questions

Questions adapt based on what was detected:

#### If Project Name Unclear

```
questions: [
  {
    question: "What should this project be called?",
    header: "Name",
    multiSelect: false,
    options: [
      {label: "[DETECTED_NAME]", description: "From package.json"},
      {label: "[DIRECTORY_NAME]", description: "From current directory"},
      {label: "Custom", description: "Enter a different name"}
    ]
  }
]
```

#### If Description Missing

```
questions: [
  {
    question: "Describe this project in 1-2 sentences. What problem does it solve?",
    header: "Purpose",
    multiSelect: false,
    options: [
      {label: "[INFERRED_FROM_FEATURES]", description: "Based on detected features"},
      {label: "Custom", description: "Enter your own description"}
    ]
  }
]
```

#### If Tech Stack Detected → SKIP tech stack questions

Do NOT ask about tech stack if already detected. Use detected values.

#### If Features Detected → Ask About Priorities

```
questions: [
  {
    question: "Which missing features should we prioritize?",
    header: "Priorities",
    multiSelect: true,
    options: [
      // Dynamically generated from features marked ✗ or ○
      {label: "[MISSING_FEATURE_1]", description: "Not implemented"},
      {label: "[MISSING_FEATURE_2]", description: "Not implemented"},
      {label: "[PARTIAL_FEATURE]", description: "Partially implemented"},
      {label: "Other", description: "Something not listed"}
    ]
  }
]
```

#### If New/Empty Project → Ask Full Questions

Only if codebase is empty or minimal, ask the traditional questions:

```
questions: [
  {
    question: "What type of application is this?",
    header: "App Type",
    multiSelect: false,
    options: [
      {label: "Full-stack web", description: "Web app with backend API (Recommended)"},
      {label: "Web SPA", description: "Single-page app, external API"},
      {label: "API only", description: "Backend service, no frontend"},
      {label: "Mobile", description: "React Native mobile app"}
    ]
  },
  {
    question: "What tech stack do you prefer?",
    header: "Stack",
    multiSelect: false,
    options: [
      {label: "React + Convex", description: "Modern serverless stack (Recommended)"},
      {label: "React + FastAPI + PostgreSQL", description: "Traditional full-stack"},
      {label: "Next.js + Prisma", description: "Next.js with database ORM"},
      {label: "Custom", description: "Specify your own stack"}
    ]
  }
]
```

#### User Types (if not detected)

```
questions: [
  {
    question: "Who are the main users of this application?",
    header: "Users",
    multiSelect: false,
    options: [
      {label: "End users only", description: "Single user type"},
      {label: "Users + Admins", description: "Two-tier access"},
      {label: "Users + Staff + Admins", description: "Three-tier hierarchy"},
      {label: "Custom", description: "Define your own user types"}
    ]
  }
]
```

### Phase 3: Create/Update Project in Convex

Build JSON with detected + user-provided values:

```bash
source .env.local 2>/dev/null || source .env 2>/dev/null || true

# Verify environment
if [ -z "$CONVEX_SITE_URL" ] || [ -z "$ADMIN_API_KEY" ]; then
  echo "Error: CONVEX_SITE_URL and ADMIN_API_KEY must be set in .env.local"
  exit 1
fi

# Build payload with DETECTED values pre-filled
PROJECT_JSON=$(cat <<'EOF'
{
  "name": "[DETECTED_OR_USER_PROVIDED]",
  "description": "[USER_PROVIDED]",
  "techStack": {
    "frontend": "[DETECTED]",
    "backend": "[DETECTED]",
    "database": "[DETECTED]",
    "styling": "[DETECTED]",
    "auth": "[DETECTED]"
  },
  "appType": "[DETECTED_OR_USER_PROVIDED]",
  "users": ["[USER_TYPES]"],
  "existingFeatures": ["[DETECTED_FEATURES]"],
  "missingFeatures": ["[DETECTED_MISSING]"],
  "structure": {
    "components": [N],
    "routes": [N],
    "apiEndpoints": [N]
  }
}
EOF
)

# Create project
PROJECT_RESULT=$(curl -s -X POST "${CONVEX_SITE_URL}/api/projects" \
  -H "Authorization: Bearer ${ADMIN_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "$PROJECT_JSON")

# Check for errors
if echo "$PROJECT_RESULT" | grep -q '"error"'; then
  echo "Error creating project:"
  echo "$PROJECT_RESULT"
  exit 1
fi

PROJECT_ID=$(echo "$PROJECT_RESULT" | grep -o '"projectId":"[^"]*"' | cut -d'"' -f4)

# Create API key for the project
KEY_RESULT=$(curl -s -X POST "${CONVEX_SITE_URL}/api/projects/${PROJECT_ID}/keys" \
  -H "Authorization: Bearer ${ADMIN_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"name": "Claude Code"}')

API_KEY=$(echo "$KEY_RESULT" | grep -o '"key":"[^"]*"' | cut -d'"' -f4)

# Save to .env.local
if grep -q "CLAUDE_API_KEY=" .env.local 2>/dev/null; then
  sed -i "s/CLAUDE_API_KEY=.*/CLAUDE_API_KEY=$API_KEY/" .env.local
else
  echo "CLAUDE_API_KEY=$API_KEY" >> .env.local
fi

echo "Project ID: $PROJECT_ID"
echo "API Key: $API_KEY (saved to .env.local)"
```

### Phase 4: Smart Task Generation (Optional)

If user selected task generation, ask about scope:

```
questions: [
  {
    question: "How many tasks should I generate?",
    header: "Scope",
    multiSelect: false,
    options: [
      {label: "Just priorities", description: "Only features you selected as priorities"},
      {label: "All missing", description: "Tasks for all missing/partial features"},
      {label: "Comprehensive", description: "Missing features + improvements + polish"}
    ]
  }
]
```

Generate tasks based on exploration findings:

```
Task Generation Rules:

1. **DO generate tasks for**:
   - Features marked ✗ (not implemented)
   - Features marked ○ (partial) - completion tasks
   - User-selected priority features
   - Infrastructure if missing (tests, CI/CD, docs)

2. **DO NOT generate tasks for**:
   - Features marked ✓ (already implemented)
   - Features user explicitly excluded
   - Tech stack setup (already exists)

3. **Task structure from exploration**:
   - Domain inferred from feature type (UI → frontend, API → backend)
   - Complexity inferred from scope
   - Dependencies based on feature relationships

4. **CRITICAL - Task Granularity**:
   One task = one complete, usable feature. Never split tightly-coupled work:

   ❌ BAD (too granular):
   - "Create task update mutation"
   - "Create task update HTTP endpoint"

   ✅ GOOD (complete feature):
   - "Task Update API" (includes mutation + HTTP endpoint + validation)

   ❌ BAD (too granular):
   - "Create TaskCard component"
   - "Add TaskCard styling"
   - "Connect TaskCard to API"

   ✅ GOOD (complete feature):
   - "Task Card UI" (includes component + styling + data binding)

   Rule of thumb: If task A is useless without task B, combine them.
   A task should deliver working functionality that can be tested end-to-end.
```

Use bulk create API:

```bash
TASKS_JSON=$(cat <<'EOF'
{
  "tasks": [
    {
      "type": "feature",
      "title": "[MISSING_FEATURE_TITLE]",
      "description": "[BASED_ON_EXPLORATION]",
      "priority": [FROM_USER_PRIORITIES],
      "domain": "[INFERRED_FROM_FEATURE]",
      "complexity": "[INFERRED_FROM_SCOPE]",
      "tags": ["[RELEVANT_TAGS]"],
      "acceptanceCriteria": [
        "[BASED_ON_SIMILAR_EXISTING_FEATURES]"
      ],
      "dependencies": ["[IF_ANY]"]
    }
  ]
}
EOF
)

curl -s -X POST "${CONVEX_SITE_URL}/api/tasks/bulk" \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d "$TASKS_JSON"
```

### Phase 5: Summary

Display comprehensive summary:

```
+--------------------------------------------------+
|         Project Initialized Successfully          |
+--------------------------------------------------+

## Project Details
  Name:        [PROJECT_NAME]
  ID:          [PROJECT_ID]
  API Key:     [API_KEY] (saved to .env.local)

## Detected Configuration
  Frontend:    React + TypeScript
  Backend:     Convex
  Database:    Convex
  Auth:        Clerk

## Feature Analysis
  Implemented: [N] features
  Partial:     [N] features
  Missing:     [N] features

## Tasks Generated
  Total:       [N] tasks
  Features:    [N] (FT-001 to FT-XXX)
  Bugfixes:    [N] (if any detected issues)

  Top Priority:
  - FT-001: [Title]
  - FT-002: [Title]
  - FT-003: [Title]

+--------------------------------------------------+

## Next Steps

  /task-dev FT-001        Start working on top priority task
  /task-init          Create additional tasks manually
  /feature [desc]     Quick feature without task tracking

## Files Updated
  - .env.local (API key added)
```

---

## Handling Edge Cases

### Empty/New Project

If exploration finds no code or minimal setup:

```
## Codebase Analysis

**Status**: New or empty project detected

No significant code found. This appears to be a new project.

I'll ask you some questions to set up the project from scratch.
```

Then proceed with full question flow (app type, tech stack, users, features, etc.)

### Existing Project Already Registered

If project already has CLAUDE_API_KEY:

```
questions: [
  {
    question: "This project appears to already be registered. What would you like to do?",
    header: "Existing",
    multiSelect: false,
    options: [
      {label: "Update project info", description: "Refresh metadata based on current codebase"},
      {label: "Generate new tasks", description: "Add tasks for newly detected missing features"},
      {label: "Re-register", description: "Create new project entry (new API key)"},
      {label: "Cancel", description: "Keep existing configuration"}
    ]
  }
]
```

### No Backend Detected

If frontend-only or no clear backend:

```
questions: [
  {
    question: "I didn't detect a backend. How should tasks be stored?",
    header: "Backend",
    multiSelect: false,
    options: [
      {label: "Use Convex", description: "Add Convex as backend (Recommended)"},
      {label: "External API", description: "Tasks will be stored in external service"},
      {label: "Local only", description: "Don't persist to database"}
    ]
  }
]
```

---

## Example Sessions

### Example 1: Existing Project

```
User: /project-init

Claude: [Invokes project-exploration skill → forks to project-scout agent]

Claude:
## Codebase Analysis

**Project**: task-manager

### Tech Stack
| Layer | Technology |
|-------|------------|
| Frontend | React 18 + TypeScript |
| Backend | Convex |
| Styling | Tailwind CSS |
| Auth | Clerk |
| Router | TanStack Router |

### Structure
- src/components/ - 15 components
- src/routes/ - 6 routes
- convex/ - 12 functions

### Features
| Feature | Status |
|---------|--------|
| Authentication | ✓ Implemented |
| Projects CRUD | ✓ Implemented |
| Tasks CRUD | ✓ Implemented |
| Task search | ✗ Missing |
| Task export | ✗ Missing |
| File attachments | ✗ Missing |

Claude: [AskUserQuestion - Intent]
What would you like to do?
→ Both (Register + Generate tasks)

Claude: [AskUserQuestion - Priorities]
Which missing features should we prioritize?
→ Task search, Task export

Claude: [Creates project in Convex]
Claude: [Generates 8 tasks for missing features]

+--------------------------------------------------+
|         Project Initialized Successfully          |
+--------------------------------------------------+

  Project: task-manager
  API Key: sk_xxx... (saved to .env.local)

  Tasks Generated: 8
  - FT-001: Add Task Search
  - FT-002: Add Task Export (CSV)
  - FT-003: Add Task Export (JSON)
  ...

Next: /task-dev FT-001
```

### Example 2: New Project

```
User: /project-init

Claude: [Invokes project-exploration skill → forks to project-scout agent]

Claude:
## Codebase Analysis

**Status**: New project detected

Found minimal setup:
- package.json exists (name: "my-app")
- No src/ directory
- No dependencies installed

This appears to be a fresh project. Let me ask some questions.

Claude: [AskUserQuestion - Full flow]
- App type: Full-stack web
- Tech stack: React + Convex
- Users: Users + Admins
- Core features: Task management with projects

Claude: [Creates project]
Claude: [Generates 25 foundation tasks]

Project "my-app" created with 25 initial tasks.
```

---

## API Reference

Same as before - uses:

- `POST /api/projects` - Create project
- `POST /api/projects/{id}/keys` - Create API key
- `POST /api/tasks/bulk` - Bulk create tasks

---

## Integration with Other Commands

| Command           | Purpose                             |
| ----------------- | ----------------------------------- |
| `/task-dev [ID]`  | Implement a generated task          |
| `/task-init`      | Manually create additional tasks    |
| `/feature [desc]` | Quick feature without task tracking |
