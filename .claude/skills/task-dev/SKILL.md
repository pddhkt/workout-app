---
name: task-dev
description: Executes tasks from the task manager by ID (FT-xxx, BF-xxx, RF-xxx). Auto-detects type and runs the appropriate workflow with full agent orchestration. Use --auto for fully autonomous execution with QA validation.
allowed-tools:
  - Task
  - Bash
  - Read
  - AskUserQuestion
  - TodoWrite
---

# Task Dev Workflow

Execute a task from the task manager by ID, auto-detecting type from prefix and running the appropriate workflow with full agent orchestration.

## Arguments

Provide Task ID(s) (e.g., FT-001, BF-002, RF-003) and optional flags.

**Formats:**

- Single task: `FT-001`
- Multiple tasks: `FT-001,FT-002` or `FT-001 FT-002`

**Flags:**

- `--auto` - Fully autonomous mode. Runs complete pipeline including subtask execution loop and QA validation without user prompts.

**Examples:**

- `/task-dev FT-001` - Interactive mode with approval prompts
- `/task-dev FT-001 --auto` - Full autonomous execution with QA loop
- `/task-dev FT-001,FT-002` - Multiple tasks (interactive)

---

## Pre-fetch Hook

This command uses a `UserPromptSubmit` hook to pre-fetch task data before processing starts.

**How it works:**

1. User runs `/task-dev FT-001`
2. Hook script parses task ID(s) from the prompt
3. Script calls `POST /api/tasks/bulk-fetch` endpoint
4. Task data is injected into context as `<task-context>` block
5. Claude skips the manual fetch and uses pre-loaded data

**If pre-fetch fails:** Claude falls back to manual fetching.

**Configuration:** See `.claude/settings.local.json` for hook setup.

---

## Stack-Aware Agent Routing

This workflow dynamically routes to stack-specific agents based on `stack.json`.

**Read stack configuration:**
```bash
cat .claude/stack.json
```

The `stack.json` file defines domain-to-agent mappings:
```json
{
  "domains": {
    "frontend": { "agent": "frontend-impl" },
    "backend": { "agent": "backend-impl" },
    ...
  }
}
```

**Agent resolution:**
1. Read `stack.json` from `.claude/` directory
2. Look up the domain in `domains[domain].agent`
3. Use that agent name as `subagent_type` in Task tool

**Example:** If domain is "frontend" and stack.json has `domains.frontend.agent: "nextjs-impl"`, use `subagent_type: "nextjs-impl"`.

---

## Orchestrator Role

**IMPORTANT**: You (the orchestrator) coordinate the workflow but do NOT implement directly.

### Why Use Subagents

1. **Context Window Preservation** - Delegating to impl/test agents keeps your context free for high-level orchestration, decision-making, and user communication
2. **Domain Skill Loading** - Impl agents load domain-specific skills (frontend/backend/database SKILL.md) with project patterns
3. **Parallel Execution** - Multiple subagents can work concurrently on independent tasks
4. **Separation of Concerns** - Each agent focuses on its specific domain

### Orchestrator Responsibilities

- Fetch task data from API and update status
- Spawn scout/planner/impl/integration agents
- **Display the plan with ASCII diagram to user** after planning phase (before implementation)
- Track progress and report status
- Handle errors and coordinate retries
- Update task status to completed on success
- Summarize results to user

### What NOT to Do

- Do NOT write implementation code directly (use impl agents)
- Do NOT skip showing the plan to the user
- Do NOT run all phases without user visibility
- Do NOT proceed if task fetch fails

---

## Workflow

### Phase 0: Initialize (Handled by Hook)

The `UserPromptSubmit` hook handles initialization automatically:
- Fetches task data via `POST /api/tasks/bulk-fetch`
- Updates status to `in_progress` for each task
- Injects `<task-context>` block with all task details

**Check for `<task-context>` block above** - if present, skip to Phase 1.

**Parse AUTO mode from arguments:**
- `--auto` flag present → Autonomous mode (no user prompts, full pipeline with QA)
- No flag → Interactive mode (uses AskUserQuestion for approval at key points)

**Task context fields** (from `<task-context>`):

- `customId` - Task ID (FT-001, BF-002, etc.)
- `title` - Task title
- `description` - Task description
- `type` - "feature" | "bugfix" | "refactor"
- `priority` - 1-4 (1=highest)
- `images` - Array of storage IDs for reference images

### Type Detection

Determine workflow from task ID prefix OR type field:

| Prefix | Type     | Workflow                                                    |
| ------ | -------- | ----------------------------------------------------------- |
| FT-xxx | feature  | Feature workflow (Scout → Plan → Implement → Integrate)     |
| BF-xxx | bugfix   | Bugfix workflow (Scout/Diagnose → Fix → Test)               |
| RF-xxx | refactor | Refactor workflow (Scout → Plan → Test → Refactor → Verify) |

**Auto-detection logic**:

1. If customId starts with "FT-", use feature workflow
2. If customId starts with "BF-", use bugfix workflow
3. If customId starts with "RF-", use refactor workflow
4. If prefix missing, fall back to `type` field from API

---

## Route to Workflow by Type

### If Feature (FT-xxx or type="feature")

#### Phase 1: Scout

Use the task-exploration skill which forks to task-scout agent:

```
Use Task tool with subagent_type: "task-scout"
Prompt:
## Task Context

- **ID**: [customId]
- **Type**: feature
- **Title**: [title]
- **Description**: [description]
- **Priority**: [priority]
- **Images**: [if any, note storage IDs for reference]

## Instructions

1. Read `.claude/cache/inventory.md` for codebase structure
2. Follow the Feature exploration strategy from task-exploration skill
3. Return structured findings for the planner

## Expected Output

Return findings in this format:
- Architecture overview
- Relevant files with actions (create/modify)
- Reusable components
- Patterns to follow
- Integration points
- Domain recommendation (frontend/backend/database/fullstack)
```

**Note**: The prefetch hook generates `.claude/cache/inventory.md` before this runs, so scout has fresh codebase data without bloating orchestrator context.

**Output**: Structured scout findings for planner

#### Phase 2: Plan

Use the task-planning skill which forks to planner agent:

```
Use Task tool with subagent_type: "planner"
Prompt:
## Task Context

- **ID**: [customId]
- **Type**: feature
- **Title**: [title]
- **Description**: [description]
- **Priority**: [priority]
- **Acceptance Criteria**: [list from task]

## Scout Findings

[paste scout output]

## Instructions

1. Follow the Feature planning strategy from task-planning skill
2. Create task breakdown with domain assignments
3. Include ASCII execution diagram
4. Return structured plan for orchestrator

## Expected Output

Return plan in this format:
- Summary
- Task breakdown by phase (with parallelization)
- ASCII execution diagram
- Domain context JSON for each impl task
- Testing strategy
- Acceptance criteria
```

**Output**: Implementation plan with domain-tagged tasks and execution diagram

#### After Planner Returns: Create Subtasks in Convex

**IMPORTANT**: Save the plan to Convex for tracking and recovery:

```bash
# Create subtasks from plan
curl -s -X POST "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "subtasks": [
      {
        "subtaskId": "frontend-auth-1",
        "phaseNumber": 1,
        "description": "Create login form component",
        "domain": "frontend",
        "dependsOn": [],
        "filesToCreate": ["src/components/LoginForm.tsx"],
        "verification": {
          "type": "command",
          "command": "npm run test:auth"
        }
      },
      {
        "subtaskId": "backend-auth-1",
        "phaseNumber": 1,
        "description": "Create auth API endpoint",
        "domain": "backend",
        "dependsOn": [],
        "filesToCreate": ["src/api/auth.ts"]
      }
    ]
  }'
```

**Subtask Fields:**
- `subtaskId` - Human-readable ID (e.g., "frontend-auth-1")
- `phaseNumber` - Execution order (1, 2, 3...)
- `description` - What the subtask does
- `domain` - "frontend" | "backend" | "database" | "fullstack"
- `dependsOn` - Array of subtaskIds this depends on
- `filesToCreate` - New files to create
- `filesToModify` - Existing files to modify
- `patternsFrom` - Example files to follow
- `verification` - How to verify completion

**Benefits of Convex Subtask Tracking:**
- Recovery if session interrupted
- Progress visible in UI
- Clear status per subtask
- Dependency tracking

#### After Planner Returns: Display Plan to User

**REQUIRED**: Before proceeding to Phase 3, display the plan summary to the user:

```
## Implementation Plan for [Task ID]: [Title]

### Tasks
| ID | Task | Domain | Dependencies |
|----|------|--------|--------------|
| 1  | ... | frontend | - |
| 2  | ... | backend | 1 |

### Execution Flow
┌─────────────────────────────────────┐
│ Phase 1: [Task 1] + [Task 2]        │  ← Parallel
│              ↓                      │
│ Phase 2: [Task 3]                   │  ← Sequential
└─────────────────────────────────────┘

### Files to Create/Modify
- path/to/file.ts (create)
- path/to/other.ts (modify)
```

**Approval Handling (based on AUTO_MODE):**

- **If AUTO_MODE is false (default - interactive):**
  - Display the plan with ASCII diagram
  - Use AskUserQuestion tool:
    ```
    Question: "How would you like to proceed with this implementation plan?"
    Options:
    1. "Approve and proceed" - Continue to implementation
    2. "Request changes" - Modify the plan
    3. "Cancel" - Stop the workflow
    ```
  - Wait for user response before proceeding

- **If AUTO_MODE is true (--auto flag):**
  - Display the plan (still required for visibility)
  - Log: "Auto mode: Proceeding with implementation..."
  - Immediately proceed without waiting

#### Phase 3: Implement (Parallel where possible)

**IMPORTANT**: Always use domain-specific impl agents. Do NOT implement directly.

For each implementation task from the plan, spawn the appropriate impl agent based on domain:

**Domain to Agent Mapping (from stack.json):**

Read `.claude/stack.json` to get the agent for each domain:
```json
domains.[domain].agent → subagent_type
```

Default mappings (if stack.json unavailable):
| Domain | Agent |
|--------|-------|
| frontend | frontend-impl |
| backend | backend-impl |
| database | database-impl |
| fullstack | fullstack-impl |

```
Use Task tool with subagent_type: "[agent from stack.json]"

Example: If domain is "frontend" and stack.json has domains.frontend.agent: "nextjs-impl",
use subagent_type: "nextjs-impl"

Prompt:
Task Context:
{
  "task": "[task description]",
  "files": ["[files to create/modify]"],
  "patterns": "[relevant patterns from scout]",
  "dependencies": ["[completed task IDs]"]
}
```

**Why domain-specific agents:**

- Skills auto-loaded via frontmatter (no manual skill reading)
- Agent has full domain knowledge from start
- Faster execution (no turns wasted loading skills)
- Clear responsibility per domain

**Parallel execution**: Tasks with no dependencies can run concurrently using multiple Task tool calls in a single message.

#### Phase 4: Integrate

Integration agent validates and finalizes:

```
Use Task tool with subagent_type: "integration"
Prompt:
Task: [title from API]
Task ID: [customId]
Implementation summaries: [paste all impl outputs]

Validate:
1. All pieces work together
2. No integration issues
3. Code builds successfully
4. Update LEARNED.md with discoveries
5. Summarize what was implemented
```

#### After Integration: User Check or QA (based on AUTO_MODE)

**If AUTO_MODE is false (interactive):**

Use AskUserQuestion tool:
```
Question: "Task implementation complete. How would you like to proceed?"
Options:
1. "Looks good, task complete" - Mark task completed
2. "I see issues to address" - Describe issues for fixing
3. "Proceed to next task" - Mark complete and show next task
```

Based on response:
- **Option 1**: Mark task completed, show summary, proceed to Phase Commit
- **Option 2**: Address user's feedback, loop back to implementation
- **Option 3**: Mark completed, show next pending task from queue

**If AUTO_MODE is true:**

Skip user check and proceed to Auto Mode QA Validation (see below).

---

### If Bugfix (BF-xxx or type="bugfix")

#### Phase 1: Scout & Diagnose

Use the task-exploration skill which forks to task-scout agent:

```
Use Task tool with subagent_type: "task-scout"
Prompt:
## Task Context

- **ID**: [customId]
- **Type**: bugfix
- **Title**: [title]
- **Description**: [description]
- **Priority**: [priority]
- **Images**: [if any, note storage IDs showing bug]

## Instructions

1. Read `.claude/cache/inventory.md` for codebase structure
2. Follow the Bugfix exploration strategy from task-exploration skill
3. Trace the error path and identify root cause
4. **Assess complexity** - determine if planning is required
5. Return structured diagnosis with planning recommendation

## Expected Output

Return findings in this format:
- Root cause analysis
- Affected files
- Recommended fix approach
- Test scenarios to prevent recurrence
- **planning_required**: true/false
- **Reason**: why planning is/isn't needed
```

**Output**: Diagnosis with root cause, fix approach, and planning recommendation

#### Phase 2: Conditional Planning

**Check scout's `planning_required` field:**

**If `planning_required: false` (simple bugfix):**
- Skip to Phase 3 (Fix)
- Scout's diagnosis is sufficient

**If `planning_required: true` (complex bugfix):**

Use the task-planning skill which forks to planner agent:

```
Use Task tool with subagent_type: "planner"
Prompt:
## Task Context

- **ID**: [customId]
- **Type**: bugfix
- **Title**: [title]
- **Description**: [description]
- **Priority**: [priority]

## Scout Findings

[paste scout output including root cause and complexity assessment]

## Instructions

1. Follow the Bugfix planning strategy from task-planning skill
2. Create task breakdown if multiple domains involved
3. Order tasks by dependency (e.g., backend fix before frontend)
4. Include ASCII execution diagram
5. Return structured plan for orchestrator

## Expected Output

Return plan in this format:
- Summary
- Task breakdown by phase
- ASCII execution diagram
- Domain context JSON for each fix task
- Test requirements
```

**Output**: Fix plan with domain-assigned tasks

#### Phase 3: Fix

Apply the fix using domain-specific impl agent(s):

**For simple bugfix (no planning):**

Use the appropriate agent based on scout's domain recommendation.

**Resolve agent from stack.json:**
```json
domains.[domain].agent → subagent_type
```

Default mappings (if stack.json unavailable):
| Domain | Agent |
|--------|-------|
| frontend | frontend-impl |
| backend | backend-impl |
| database | database-impl |
| fullstack | fullstack-impl |

```
Use Task tool with subagent_type: "[domain]-impl"
Prompt:
Bug Fix Context:
{
  "task": "Fix: [title]",
  "root_cause": "[from scout diagnosis]",
  "fix_approach": "[from scout]",
  "files": ["[files to modify from scout]"]
}
```

**For complex bugfix (with planning):**
- Spawn domain-specific impl agents per plan's task breakdown
- Use `[domain]-impl` for each task based on domain assignment
- Execute in dependency order (parallel where possible)

**Output**: Fixed code

#### Phase 4: Test

Add regression test:

```
Use Task tool with subagent_type: "test"
Prompt:
Bug: [title]
Fix Applied: [summary from impl agent(s)]
Test Scenarios: [from scout]

Add regression test to prevent this bug from recurring.
Verify fix works as expected.
```

**Output**: Test coverage for the bug

---

### If Refactor (RF-xxx or type="refactor")

#### Phase 1: Scout

Use the task-exploration skill which forks to task-scout agent:

```
Use Task tool with subagent_type: "task-scout"
Prompt:
## Task Context

- **ID**: [customId]
- **Type**: refactor
- **Title**: [title]
- **Description**: [description]
- **Priority**: [priority]

## Instructions

1. Read `.claude/cache/inventory.md` for codebase structure
2. Follow the Refactor exploration strategy from task-exploration skill
3. Map current implementation and assess scope
4. Return structured analysis for planning

## Expected Output

Return findings in this format:
- Current state analysis
- Refactoring scope and boundaries
- Affected files and dependencies
- Test coverage assessment
- Impact analysis
```

**Output**: Current state and refactoring scope

#### Phase 2: Plan

Use the task-planning skill which forks to planner agent:

```
Use Task tool with subagent_type: "planner"
Prompt:
## Task Context

- **ID**: [customId]
- **Type**: refactor
- **Title**: [title]
- **Description**: [description]
- **Priority**: [priority]
- **Acceptance Criteria**: [list from task]

## Scout Findings

[paste scout output]

## Instructions

1. Follow the Refactor planning strategy from task-planning skill
2. Create safe incremental steps with test checkpoints
3. Include ASCII execution diagram with rollback points
4. Return structured plan for orchestrator

## Expected Output

Return plan in this format:
- Summary (test-first approach)
- Task breakdown (ensuring tests before changes)
- ASCII execution diagram with checkpoints
- Domain context JSON for each task
- Rollback points
- Acceptance criteria
```

**Output**: Incremental refactoring plan with test checkpoints

#### After Planner Returns: Display Plan to User

**REQUIRED**: Show the refactoring plan to user before proceeding.

**Approval Handling (based on AUTO_MODE):**

- **If AUTO_MODE is false (default - interactive):**
  - Display the plan with ASCII diagram
  - Use AskUserQuestion tool:
    ```
    Question: "How would you like to proceed with this implementation plan?"
    Options:
    1. "Approve and proceed" - Continue to implementation
    2. "Request changes" - Modify the plan
    3. "Cancel" - Stop the workflow
    ```
  - Wait for user response before proceeding

- **If AUTO_MODE is true (--auto flag):**
  - Display the plan (still required for visibility)
  - Log: "Auto mode: Proceeding with implementation..."
  - Immediately proceed without waiting

#### Phase 3: Ensure Tests

Verify test coverage before refactoring:

```
Use Task tool with subagent_type: "test"
Prompt:
Refactor: [title]
Code to Refactor: [from scout]

Verify test coverage exists for code being refactored.
Add missing tests if needed to ensure safe refactoring.
```

**Output**: Test coverage verified or added

#### Phase 4: Refactor

Apply refactoring incrementally using domain-specific impl agents.

**Resolve agent from stack.json:**
```json
domains.[domain].agent → subagent_type
```

Default mappings (if stack.json unavailable):
| Domain | Agent |
|--------|-------|
| frontend | frontend-impl |
| backend | backend-impl |
| database | database-impl |
| fullstack | fullstack-impl |

```
Use Task tool with subagent_type: "[domain]-impl"
Prompt:
Refactor Context:
{
  "task": "[refactor step from planner]",
  "files": ["[files to refactor]"],
  "test_coverage": "[confirmed from test phase]",
  "checkpoint": "[rollback point if needed]"
}

Apply refactoring incrementally following the plan.
Run tests after each step to verify behavior unchanged.
```

**Output**: Refactored code

#### Phase 5: Verify

Final validation:

```
Use Task tool with subagent_type: "integration"
Prompt:
Refactor: [title]
Changes Applied: [from impl agent]

Validate:
1. All tests pass
2. Behavior unchanged
3. Code quality improved
4. No regressions introduced
5. Update LEARNED.md
```

**Output**: Verification report

---

## Phase Commit: Git Checkpoint

After successful implementation/integration, create a git checkpoint:

```
Use Task tool with subagent_type: "commit"
Prompt:
Task Context:
- Task ID: [customId]
- Title: [title]
- Type: [type from prefix: FT→feature, BF→bugfix, RF→refactor]

Create a checkpoint commit for this task following the git skill conventions.
```

**Output**: Commit hash or "skipped" (if no changes)

**Note**: This phase runs for all workflow types (feature, bugfix, refactor) after their respective final implementation/integration phase.

---

## Phase Final: Update Status to Completed

After successful completion of all phases:

```bash
curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/status" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"status": "completed"}'
```

Display success message to user:

```
✓ Task [customId] completed successfully
  Status updated: in_progress → completed
```

---

## Error Handling

### If any phase fails:

1. Report error to user with context
2. **Do NOT update status to completed**
3. Task remains `in_progress` for manual resolution
4. Suggest remediation steps
5. Provide error details and affected files

### If task fetch fails:

1. Report "Task not found" error
2. Verify task ID format (FT-xxx, BF-xxx, RF-xxx)
3. Check environment variables are set (.env file)
4. Verify CONVEX_SITE_URL and CLAUDE_API_KEY are correct

### If status update fails:

1. Log the error but continue workflow
2. Warn user that status may need manual update
3. Show the API response for debugging

---

## Examples

```bash
# Work on a feature task (interactive mode with approval prompts)
/task-dev FT-001

# Work on a feature task (full autonomous execution with QA)
/task-dev FT-001 --auto

# Fix a bug (interactive)
/task-dev BF-003

# Perform a refactor (autonomous)
/task-dev RF-002 --auto
```

---

## Environment Setup

Required environment variables in `.env`:

| Variable          | Description                                                     | Example                               |
| ----------------- | --------------------------------------------------------------- | ------------------------------------- |
| `CONVEX_SITE_URL` | Convex HTTP site URL (note: `.convex.site` not `.convex.cloud`) | `https://your-deployment.convex.site` |
| `CLAUDE_API_KEY`  | API key for authentication                                      | `your-api-key-here`                   |

Create `.env` file in project root:

```bash
CONVEX_SITE_URL=https://your-deployment.convex.site
CLAUDE_API_KEY=your-api-key-here
```

---

## Context Passing

Task data flows through phases:

```
API Response → Scout Input
Scout Output → Planner Input
Planner Output → Impl Inputs (per task)
All Impl Outputs → Integration Input
Integration Output → Commit Input
Commit Output → Status Update
```

Each phase receives:

- Original task context (customId, title, description, type, priority, images)
- Output from previous phase
- Relevant patterns from scout
- Domain assignments from planner

---

## Task Status Lifecycle

| Status        | When Set              | By Whom             |
| ------------- | --------------------- | ------------------- |
| `pending`     | Task created          | User/System         |
| `in_progress` | Phase 0 (fetch)       | Orchestrator        |
| `completed`   | Phase Final (success) | Orchestrator        |
| `in_progress` | Error occurred        | Remains (no change) |

---

## Integration with Task Manager UI

This command integrates with the task manager UI:

1. User creates task in UI with type and description
2. Task gets ID (FT-001, BF-002, etc.) and status=pending
3. User runs `/task [ID]` in Claude Code
4. Status updates to in_progress automatically
5. Orchestrator runs appropriate workflow
6. Status updates to completed on success
7. UI reflects updated status in real-time

---

## Reference Images

If task has `images` field with storage IDs:

- Scout agent should note these for context
- Implementation agents can reference them for UI/design requirements
- Storage IDs can be retrieved via Convex API if needed

---

## Testing

After task completion, use `/test` command separately to add comprehensive tests:

```bash
/test [description based on completed task]
```

Or let the task workflow include testing phases (bugfix and refactor already do).

---

## Auto Mode: Autonomous Execution

When `--auto` flag is used, the skill runs a complete autonomous pipeline with subtask execution and QA validation.

### Subtask Execution Loop

After planning completes in auto mode, execute subtasks systematically:

```bash
# Get next pending subtask (respecting dependencies)
NEXT=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks/next" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

SUBTASK=$(echo "$NEXT" | jq -r '.subtask')
```

**For each subtask:**

1. **Mark in_progress:**
   ```bash
   curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks/${SUBTASK_ID}" \
     -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
     -H "Content-Type: application/json" \
     -d '{"status": "in_progress"}'
   ```

2. **Execute using domain-specific impl agent** (resolved from stack.json)

3. **Mark completed:**
   ```bash
   curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks/${SUBTASK_ID}" \
     -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
     -H "Content-Type: application/json" \
     -d '{"status": "completed", "notes": "Implementation summary..."}'
   ```

4. **Check for more subtasks → Loop back to step 1**

5. **When all subtasks complete → Proceed to QA Review**

### QA Validation Loop

After all subtasks complete:

1. **Run QA Review:**
   - Use `qa-reviewer` agent to validate acceptance criteria
   - Check tests pass
   - Output: APPROVED or QA_FIX_REQUEST.md

   ```bash
   # Check QA status
   QA_STATUS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/qa" \
     -H "Authorization: Bearer ${CLAUDE_API_KEY}")
   ```

2. **If Rejected (max 3 iterations):**
   - Use `qa-fixer` agent to address issues
   - Re-run QA review
   - Loop until approved or max iterations

3. **If Approved:**
   - Mark task completed
   - Create phase record
   - Output summary

4. **If Failed after 3 iterations:**
   - Mark task as `blocked` with reason "QA failed after max iterations"
   - Report failure to user

### Subtask Progress Tracking

During implementation, update subtask status:

```bash
# Mark subtask in_progress
curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks/${SUBTASK_ID}" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"status": "in_progress"}'

# After completion
curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks/${SUBTASK_ID}" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"status": "completed", "notes": "Implemented successfully"}'
```

### Progress Monitoring

Check overall progress:

```bash
# Get progress stats
curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks/progress" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"

# Response: {"total": 8, "completed": 5, "percentComplete": 62}
```

### QA Loop Integration

After all subtasks complete, the QA loop triggers:

1. `/qa-review` validates acceptance criteria
2. If rejected, creates QA_FIX_REQUEST.md
3. `/qa-fix` addresses issues
4. Loop until approved (max 3 iterations)

### Full Autonomous Mode

For fully autonomous execution:

```bash
/task-dev FT-001 --auto
```

This runs the complete pipeline:
```
Task → Scout → Plan → Subtask Loop → QA Review → (QA Fix Loop) → Complete
```

### Recovery (Auto Mode)

If a session is interrupted during `--auto` execution:

```bash
# Check progress
curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks/progress" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"

# Get next pending subtask
curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${TASK_ID}/subtasks/next" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"

# Resume from where you left off
/task-dev FT-001 --auto
```

The skill will:
1. Detect existing subtasks in Convex
2. Skip planning if subtasks exist (use existing plan)
3. Resume from next pending subtask
4. Continue to QA when all complete

### Related Skills

| Skill | Purpose |
|-------|---------|
| `/subtask` | Execute single subtask |
| `/qa-review` | Validate acceptance criteria |
| `/qa-fix` | Fix QA issues |
