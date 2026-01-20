---
name: task-init
description: Interactively create tasks with guided discovery of requirements, type, domain, and acceptance criteria.
skills: task-guide
allowed-tools:
  - Read
  - Bash
  - AskUserQuestion
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: ".claude/scripts/validate-task-init.sh"
---

# Task Creation Workflow

Interactively create a single task in Convex with guided discovery of requirements, type, domain, and acceptance criteria.

## Arguments

Optionally provide a task description. If provided, the initial question will be skipped.

---

## Overview

This command creates individual tasks through an interactive workflow:

1. Gathers task information through guided questions
2. Infers type, domain, and complexity from description
3. **Uses loaded `task-guide` skill** for domain-specific criteria
4. Generates acceptance criteria using domain best practices
5. **Checks for duplicates** via `duplicate-checker` skill
6. Creates task in Convex via HTTP API
7. Returns task ID for immediate use with `/task-dev`

---

## Configuration

### Frontmatter

| Field | Value | Purpose |
|-------|-------|---------|
| `skills` | `task-guide` | Auto-loads domain-specific criteria |
| `hooks.PreToolUse` | `validate-task-init.sh` | Validates env before Bash commands |

### Skills Used

| Skill | Purpose | Invocation |
|-------|---------|------------|
| `task-guide` | Domain criteria templates | Auto-loaded via frontmatter |
| `duplicate-checker` | Detect existing similar tasks | Invoked in Phase 5.5 |

---

## Environment Variables

Required in `.env.local` or `.env`:

| Variable          | Description                                  |
| ----------------- | -------------------------------------------- |
| `CONVEX_SITE_URL` | https://[deployment].convex.site             |
| `CLAUDE_API_KEY`  | Project-specific API key for task operations |

**Note**: Environment validation is handled by the `PreToolUse` hook. If env vars are missing, the hook blocks Bash commands with a helpful error message.

---

## Workflow

### Phase 1: Initial Discovery

If a task description is provided, use it as initial description. Otherwise, ask:

Use `AskUserQuestion`:

```
questions: [
  {
    question: "What do you want to build, fix, or improve? Please describe the task in detail.",
    header: "Task",
    multiSelect: false,
    options: [
      {label: "New feature", description: "I want to add new functionality"},
      {label: "Bug fix", description: "Something is broken or not working correctly"},
      {label: "Refactor", description: "I want to improve existing code without changing behavior"},
      {label: "Other", description: "Enter a detailed description"}
    ]
  }
]
```

Based on selection, ask follow-up for detailed description if needed.

### Phase 2: Type and Domain Inference

Based on the description, infer task type and domain. Then confirm with user:

Use `AskUserQuestion` (batch 2 questions):

```
questions: [
  {
    question: "What type of task is this?",
    header: "Type",
    multiSelect: false,
    options: [
      {label: "Feature", description: "New functionality or capability (FT-xxx)"},
      {label: "Bugfix", description: "Fix broken or incorrect behavior (BF-xxx)"},
      {label: "Refactor", description: "Improve code without changing behavior (RF-xxx)"}
    ]
  },
  {
    question: "Which domain does this task primarily affect?",
    header: "Domain",
    multiSelect: false,
    options: [
      {label: "Frontend", description: "UI components, client-side logic, styling"},
      {label: "Backend", description: "API endpoints, server logic, integrations"},
      {label: "Database", description: "Schema changes, migrations, data layer"},
      {label: "Fullstack", description: "Spans multiple layers (Recommended if unsure)"}
    ]
  }
]
```

**Type Inference Heuristics:**

- Keywords like "add", "create", "implement", "new", "build" → Feature
- Keywords like "fix", "broken", "bug", "error", "wrong", "not working", "issue" → Bugfix
- Keywords like "refactor", "clean up", "improve", "extract", "reorganize", "simplify" → Refactor

**Domain Inference Heuristics:**

- Keywords like "component", "page", "UI", "button", "form", "style", "display", "view" → Frontend
- Keywords like "API", "endpoint", "auth", "service", "server", "route", "handler" → Backend
- Keywords like "schema", "table", "migration", "query", "index", "database", "data model" → Database
- Multiple domain keywords or unclear → Fullstack

### Phase 3: Complexity and Priority

Use `AskUserQuestion` (batch 2 questions):

```
questions: [
  {
    question: "How complex is this task?",
    header: "Complexity",
    multiSelect: false,
    options: [
      {label: "Simple", description: "< 1 day, straightforward implementation"},
      {label: "Medium", description: "1-3 days, some complexity (Recommended)"},
      {label: "Complex", description: "3+ days, significant work, multiple parts"}
    ]
  },
  {
    question: "What priority should this task have?",
    header: "Priority",
    multiSelect: false,
    options: [
      {label: "1 - Critical", description: "Core functionality, must have, blocks other work"},
      {label: "2 - High", description: "Important, should have (Recommended)"},
      {label: "3 - Medium", description: "Nice to have"},
      {label: "4 - Low", description: "Polish, future enhancement"}
    ]
  }
]
```

### Phase 4: Acceptance Criteria (Using task-guide Skill)

**Step 1: Load domain-specific criteria from task-guide skill**

Based on the domain selected in Phase 2, read the corresponding reference file:

| Domain    | Reference File                                     |
| --------- | -------------------------------------------------- |
| frontend  | `.claude/skills/task-guide/reference/frontend.md`  |
| backend   | `.claude/skills/task-guide/reference/backend.md`   |
| database  | `.claude/skills/task-guide/reference/database.md`  |
| fullstack | `.claude/skills/task-guide/reference/fullstack.md` |

**Step 2: Extract criteria templates for the task type**

From the reference file, find the "Acceptance Criteria Templates" section and select criteria matching the task type (feature/bugfix/refactor).

**Step 3: Generate task-specific criteria**

Combine:

1. **Domain criteria** from the reference file (required quality gates)
2. **Task-specific criteria** derived from the user's description
3. **Testing requirements** from the reference file

**Step 4: Present to user for confirmation**

Use `AskUserQuestion`:

```
questions: [
  {
    question: "What are the acceptance criteria for this task? (Select applicable or add custom)",
    header: "Criteria",
    multiSelect: true,
    options: [
      {label: "[Domain criterion 1]", description: "From task-guide: [domain]"},
      {label: "[Domain criterion 2]", description: "From task-guide: [domain]"},
      {label: "[Task-specific criterion]", description: "Based on description"},
      {label: "[Testing requirement]", description: "Required for [domain]"},
      {label: "Add custom", description: "Enter your own criteria"}
    ]
  }
]
```

---

### Domain-Specific Criteria Examples

**Frontend Features** (from task-guide/reference/frontend.md):

- "Component uses shadcn/ui (no custom buttons, inputs)"
- "Responsive on mobile (320px) and desktop (1280px+)"
- "Loading states shown during async operations"
- "Error states display user-friendly messages"
- "Keyboard accessible (Tab, Enter, Escape)"
- "Works in light/dark mode"

**Backend Features** (from task-guide/reference/backend.md):

- "Input validated with proper error messages"
- "Authentication required where needed"
- "Uses correct function type (query/mutation/action)"
- "Error cases return appropriate status codes"
- "Database queries use indexes"

**Database Features** (from task-guide/reference/database.md):

- "Schema uses appropriate validators"
- "Indexes defined for query patterns"
- "Timestamps included (createdAt, updatedAt)"
- "Foreign keys use v.id() references"

**Fullstack Features** (from task-guide/reference/fullstack.md):

- "Frontend-backend contract defined"
- "E2E flow works end-to-end"
- "Errors propagate correctly across layers"
- "Loading states shown throughout flow"

---

### Fallback Heuristics (if task-guide unavailable)

For Features:

- "User can [action from description]"
- "[Component/feature] is visible and accessible"
- "[Functionality] works as expected"
- "Error states are handled gracefully"
- "Loading states are shown appropriately"

For Bugfixes:

- "Bug no longer occurs under [condition]"
- "Correct behavior: [expected outcome]"
- "Edge cases handled: [specific cases]"
- "No regression in related functionality"

For Refactors:

- "Existing tests continue to pass"
- "Behavior remains unchanged"
- "[Specific improvement] is achieved"
- "Code is more maintainable/readable"

### Phase 5: Dependencies and Tags (Optional)

Use `AskUserQuestion` (batch 2 questions):

```
questions: [
  {
    question: "Does this task depend on any existing tasks?",
    header: "Dependencies",
    multiSelect: false,
    options: [
      {label: "No dependencies", description: "Task can be started immediately"},
      {label: "Has dependencies", description: "Specify task IDs (e.g., FT-001, FT-002)"}
    ]
  },
  {
    question: "Add tags to help categorize this task?",
    header: "Tags",
    multiSelect: true,
    options: [
      {label: "auth", description: "Authentication/authorization"},
      {label: "ui", description: "User interface"},
      {label: "api", description: "API endpoints"},
      {label: "data", description: "Data handling"},
      {label: "perf", description: "Performance"},
      {label: "security", description: "Security-related"},
      {label: "testing", description: "Test coverage"},
      {label: "docs", description: "Documentation"}
    ]
  }
]
```

If "Has dependencies" selected, ask follow-up for specific task IDs.

### Phase 5.5: Duplicate Detection

Before creating the task, invoke the `duplicate-checker` skill to check for existing tasks with similar titles.

**Invoke the skill:**

```
Invoke the duplicate-checker skill with:
- Title: [the task title from Phase 1/6]

The skill:
1. Forks to task-checker agent (haiku model)
2. Hook pre-fetches existing tasks from API
3. Agent analyzes for duplicates
4. Returns structured JSON result
```

**Result handling:**

```json
// If no duplicates
{ "hasDuplicate": false, "matches": [] }

// If duplicates found
{
  "hasDuplicate": true,
  "matches": [
    {
      "customId": "FT-001",
      "title": "Similar task title",
      "status": "pending",
      "matchType": "exact"  // or "prefix" or "keyword"
    }
  ]
}
```

**If `hasDuplicate: false`**: Continue to Phase 6.

**If `hasDuplicate: true`**: Show matches and ask user with `AskUserQuestion`:

```
questions: [
  {
    question: "A similar task already exists: '[EXISTING_TITLE]' ([EXISTING_ID], status: [STATUS]). What would you like to do?",
    header: "Duplicate",
    multiSelect: false,
    options: [
      {label: "Create anyway", description: "Proceed despite the similar task"},
      {label: "Edit title", description: "Change the title to differentiate"},
      {label: "Use existing", description: "Cancel and work on the existing task instead"}
    ]
  }
]
```

**Actions based on selection:**

- **"Create anyway"**: Continue to Phase 6 (Review)
- **"Edit title"**: Ask for new title, return to Phase 5.5 check
- **"Use existing"**: Display existing task info and exit with message: "Use `/task-dev [EXISTING_ID]` to work on this task"

If no duplicates found, continue to Phase 6.

### Phase 6: Review and Confirm

Display task summary before creation:

```
## Task Summary

**Title**: [Generated title from description]
**Type**: [feature/bugfix/refactor] → [FT/BF/RF]-xxx
**Domain**: [frontend/backend/database/fullstack]
**Complexity**: [simple/medium/complex]
**Priority**: [1-4]

**Description**:
[Full description from Phase 1]

**Acceptance Criteria**:
- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3

**Dependencies**: [None or FT-001, FT-002]
**Tags**: [tag1, tag2, tag3]
```

Use `AskUserQuestion`:

```
questions: [
  {
    question: "Create this task?",
    header: "Confirm",
    multiSelect: false,
    options: [
      {label: "Yes, create task", description: "Proceed with task creation"},
      {label: "Edit title", description: "Change the task title"},
      {label: "Edit description", description: "Modify the description"},
      {label: "Cancel", description: "Abort task creation"}
    ]
  }
]
```

If "Edit title" or "Edit description" selected, ask for new value and return to review.

### Phase 7: Create Task

Build JSON payload and call API:

```bash
source .env.local 2>/dev/null || source .env 2>/dev/null || true

# Build the JSON payload from collected answers
TASK_JSON=$(cat <<'EOF'
{
  "type": "[TYPE]",
  "title": "[TITLE]",
  "description": "[DESCRIPTION]",
  "priority": [PRIORITY],
  "domain": "[DOMAIN]",
  "complexity": "[COMPLEXITY]",
  "acceptanceCriteria": [
    "[CRITERION_1]",
    "[CRITERION_2]",
    "[CRITERION_3]"
  ],
  "dependencies": ["[DEP_1]", "[DEP_2]"],
  "tags": ["[TAG_1]", "[TAG_2]"]
}
EOF
)

# Create task via API
RESULT=$(curl -s -X POST "${CONVEX_SITE_URL}/api/tasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "$TASK_JSON")

# Check for errors
if echo "$RESULT" | grep -q '"error"'; then
  echo "Error creating task:"
  echo "$RESULT"
  exit 1
fi

# Extract task ID
TASK_ID=$(echo "$RESULT" | grep -o '"customId":"[^"]*"' | cut -d'"' -f4)
echo "Task created: $TASK_ID"
```

### Phase 8: Summary

Display success message with next steps:

```
+------------------------------------------+
|           Task Created Successfully       |
+------------------------------------------+

  Task ID:     [TASK_ID]
  Title:       [TITLE]
  Type:        [TYPE]
  Domain:      [DOMAIN]
  Priority:    [PRIORITY]
  Complexity:  [COMPLEXITY]
  Status:      pending

+------------------------------------------+

Next Steps:
  /task-dev [TASK_ID]     Start working on this task
  /task-init          Create another task
```

---

## Title Generation

Generate concise, actionable titles from descriptions:

**Rules**:

1. Start with action verb for features (Add, Create, Implement)
2. Start with "Fix" for bugfixes
3. Start with verb for refactors (Extract, Reorganize, Simplify)
4. Keep under 60 characters
5. Be specific but concise
6. Avoid redundant words ("the", "a", "should")

**Examples**:

- Input: "I need to add a user profile page where users can see their info"
- Output: "Add User Profile Page"

- Input: "The login form is showing the wrong error message"
- Output: "Fix Login Form Error Message"

- Input: "We should extract the authentication logic into a reusable hook"
- Output: "Extract Auth Logic into Custom Hook"

---

## Quick Mode

If a task description is provided with enough detail, minimize questions:

**Example**: `/task-init Add login button to header - frontend - simple`

Parse inline hints:

- Description: "Add login button to header"
- Domain: "frontend" (explicit)
- Complexity: "simple" (explicit)
- Type: "feature" (inferred from "Add")

Skip to Phase 6 (Review) with inferred values, allowing user to confirm or edit.

---

## Error Handling

### API Key Missing

```
Error: CONVEX_SITE_URL and CLAUDE_API_KEY must be set

To fix:
1. Run /project to create a project (creates API key)
2. Or manually add to .env.local:
   CONVEX_SITE_URL=https://your-deployment.convex.site
   CLAUDE_API_KEY=your-api-key
```

### API Request Fails

```
Error creating task: [API error message]

Common issues:
- Invalid API key: Verify CLAUDE_API_KEY is correct
- Invalid field value: Check type/domain/complexity values
- Network error: Check CONVEX_SITE_URL is accessible
```

### User Cancels

```
Task creation cancelled. No task was created.
```

---

## Field Reference

### Type

| Value      | Prefix | When to Use            |
| ---------- | ------ | ---------------------- |
| `feature`  | FT-    | New functionality      |
| `bugfix`   | BF-    | Fix broken behavior    |
| `refactor` | RF-    | Improve code structure |

### Domain

| Value       | When to Use                  |
| ----------- | ---------------------------- |
| `frontend`  | UI, components, client logic |
| `backend`   | API, services, server logic  |
| `database`  | Schema, migrations, queries  |
| `fullstack` | Spans multiple layers        |

### Priority

| Value | Level    | Description                  |
| ----- | -------- | ---------------------------- |
| 1     | Critical | Must have, blocks other work |
| 2     | High     | Should have, important       |
| 3     | Medium   | Nice to have                 |
| 4     | Low      | Future enhancement           |

### Complexity

| Value     | Estimate | Description                      |
| --------- | -------- | -------------------------------- |
| `simple`  | < 1 day  | Straightforward, minimal risk    |
| `medium`  | 1-3 days | Some complexity, standard work   |
| `complex` | 3+ days  | Significant work, multiple parts |

---

## Example Sessions

### Example 1: Full Interactive Flow

```
User: /task-init

Claude: [Phase 0 - Environment check passes]

Claude: [AskUserQuestion - Phase 1]
What do you want to build, fix, or improve?

User: "Add a search bar to the task list that filters tasks by title"

Claude: [AskUserQuestion - Phase 2]
Based on your description, this looks like a Feature affecting Frontend.

- Type: Feature (confirmed)
- Domain: Frontend (confirmed)

Claude: [AskUserQuestion - Phase 3]
- Complexity: Simple (selected)
- Priority: 2 - High (selected)

Claude: [AskUserQuestion - Phase 4]
Suggested acceptance criteria:
- [x] Search bar is visible above task list
- [x] Tasks filter as user types
- [x] Empty state shown when no matches

Claude: [AskUserQuestion - Phase 5]
- Dependencies: No dependencies
- Tags: ui (selected)

Claude: [Phase 6 - Review]
## Task Summary

**Title**: Add Search Bar to Task List
**Type**: feature → FT-xxx
**Domain**: frontend
**Complexity**: simple
**Priority**: 2

**Acceptance Criteria**:
- [ ] Search bar is visible above task list
- [ ] Tasks filter as user types
- [ ] Empty state shown when no matches

**Tags**: ui

Create this task? → Yes, create task

Claude: [Phase 7 - API call]
Claude: [Phase 8 - Success]

+------------------------------------------+
|           Task Created Successfully       |
+------------------------------------------+

  Task ID:     FT-005
  Title:       Add Search Bar to Task List
  Type:        feature
  Domain:      frontend
  Priority:    2
  Complexity:  simple
  Status:      pending

+------------------------------------------+

Next Steps:
  /task-dev FT-005    Start working on this task
```

### Example 2: Quick Mode

```
User: /task-init Fix profile image not loading on mobile - frontend

Claude: [Parses arguments]
Detected from description:
- Description: "Fix profile image not loading on mobile"
- Type: bugfix (inferred from "Fix")
- Domain: frontend (explicit)

[Skips to Phase 3 for remaining fields]

- Complexity: Simple (confirmed)
- Priority: 1 - Critical (selected)

[Phase 4 - Acceptance criteria]
- [x] Profile image loads correctly on mobile
- [x] Works on iOS and Android browsers
- [x] No regression on desktop

[Phase 6 - Review and confirm]

Task Created: BF-003 - Fix Profile Image Not Loading on Mobile
```

### Example 3: Bugfix with Dependencies

```
User: /task-init

Claude: What do you want to build, fix, or improve?

User: "The task status dropdown is broken after the recent schema change"

Claude: [Infers: bugfix, fullstack]
- Type: Bugfix (confirmed)
- Domain: Fullstack (confirmed)

[Phase 3]
- Complexity: Medium (selected)
- Priority: 1 - Critical (selected)

[Phase 4 - Acceptance criteria]
- [x] Status dropdown displays all options
- [x] Status changes persist to database
- [x] No console errors when changing status

[Phase 5]
- Dependencies: Has dependencies → RF-001
- Tags: ui, data (selected)

[Phase 6 - Confirm]

Task Created: BF-004 - Fix Task Status Dropdown After Schema Change
Dependencies: RF-001
```

---

## API Reference

### POST /api/tasks

**Request**:

```json
{
  "type": "feature",
  "title": "Add Search Bar to Task List",
  "description": "Add a search bar that filters tasks by title",
  "priority": 2,
  "domain": "frontend",
  "complexity": "simple",
  "acceptanceCriteria": [
    "Search bar is visible above task list",
    "Tasks filter as user types"
  ],
  "dependencies": [],
  "tags": ["ui"]
}
```

**Response** (201 Created):

```json
{
  "customId": "FT-005",
  "title": "Add Search Bar to Task List",
  "type": "feature",
  "status": "pending",
  "priority": 2,
  "domain": "frontend",
  "complexity": "simple",
  "createdAt": 1704672000000,
  "updatedAt": 1704672000000
}
```

**Required Fields**: `type`, `title`
**Optional Fields**: `description`, `priority`, `domain`, `complexity`, `acceptanceCriteria`, `dependencies`, `tags`

---

## Integration with Other Commands

| Command           | Purpose                              |
| ----------------- | ------------------------------------ |
| `/task-dev [ID]`  | Implement the created task           |
| `/task-init`      | Create another task                  |
| `/project`        | Create a new project (if no API key) |
| `/feature [desc]` | Ad-hoc feature without task tracking |

---

## Related Skills

| Skill        | Purpose                                                            |
| ------------ | ------------------------------------------------------------------ |
| `task-guide` | Domain-specific acceptance criteria, best practices, quality gates |
| `task`       | API integration for fetching/updating tasks                        |

The `task-guide` skill provides domain-specific templates that this command uses to generate better acceptance criteria based on whether the task is frontend, backend, database, or fullstack.
