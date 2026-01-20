---
name: feature
description: Implement features using multi-agent orchestration with scout, planner, and impl agents.
---

# Feature Implementation Workflow

Implement features using multi-agent orchestration. Supports both feature IDs from `features.json` and free-form descriptions.

## Arguments

Provide either:

- **Feature ID(s)**: `1` or `1,2,3` or `1-5`
- **Free-form description**: "Add user profile page with avatar"

---

## Orchestrator Role

**IMPORTANT**: You (the orchestrator) coordinate the workflow but do NOT implement directly.

### Why Use Subagents

1. **Context Window Preservation** - Delegating to impl/test agents keeps your context free for high-level orchestration, decision-making, and user communication
2. **Domain Skill Loading** - Impl agents load domain-specific skills (frontend/backend/database SKILL.md) with project patterns
3. **Parallel Execution** - Multiple subagents can work concurrently on independent tasks
4. **Separation of Concerns** - Each agent focuses on its specific domain

### Orchestrator Responsibilities

- Spawn scout/planner/impl/integration agents
- **Display the plan with ASCII diagram to user** after Phase 2 (before implementation)
- Track progress and report status
- Handle errors and coordinate retries
- Summarize results to user

### What NOT to Do

- Do NOT write implementation code directly (use impl agents)
- Do NOT skip showing the plan to the user
- Do NOT run all phases without user visibility

---

## Workflow

### Phase 1: Scout

Scout agent handles both feature loading (if ID provided) and codebase exploration:

```
Use Task tool with subagent_type: "scout"
Prompt:
Feature Request: [user's feature description or ID]

Tasks:
1. If input matches feature ID pattern (e.g., "1", "1,2,3", "1-5"):
   - Read .claude/features.json
   - Load feature(s) by ID
   - Validate: IDs exist, dependencies satisfied, not already completed
   - Set status to "in_progress" in features.json
   - Extract: id, title, description, domain, acceptance_criteria, dependencies, tags

2. Explore codebase for:
   - Project structure and architecture
   - Existing code related to this feature
   - Patterns matching the domain
   - Reusable components/utilities
   - Files that need modification
   - Dependencies already implemented

Output:
- Feature context (if ID-based)
- Architecture overview
- Relevant files and patterns
- Recommendations
```

**Output**: Feature context + codebase analysis

### Phase 2: Plan

Planner creates implementation strategy:

```
Use Task tool with subagent_type: "planner"
Prompt:
Scout Findings: [paste scout output]

Create:
1. Task breakdown with domain assignments (frontend/backend/database)
2. Dependency graph between tasks
3. Parallel execution opportunities
4. Files to create/modify per task
5. ASCII execution flow diagram
```

**Output**: Implementation plan with domain-tagged tasks

#### After Planner Returns: Display Plan to User

**REQUIRED**: Before proceeding to Phase 3, display the plan summary to the user:

```
## Implementation Plan for Feature X

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

This gives the user visibility into the plan before implementation begins.

### Phase 3: Implement (Parallel where possible)

**IMPORTANT**: Always use impl subagents. Do NOT implement directly.

For each implementation task from the plan, spawn an impl agent:

```
Use Task tool with subagent_type: "impl"
Prompt:
Task Context:
{
  "domain": "[from planner]",
  "task": "[task description]",
  "files": ["[files to create/modify]"],
  "patterns": "[relevant patterns from scout]"
}
```

**Why subagents, not direct implementation:**

- Impl agents load domain-specific skills (`.claude/skills/{domain}/SKILL.md`)
- Preserves orchestrator context for coordination
- Enables true parallel execution
- Maintains separation of concerns

**Parallel execution**: Tasks with no dependencies can run concurrently using multiple Task tool calls in a single message.

### Phase 4: Integrate

Integration agent validates and finalizes:

```
Use Task tool with subagent_type: "integration"
Prompt:
Feature: [title from scout]
Implementation summaries: [paste all impl outputs]

Validate:
1. All pieces work together
2. No integration issues
3. Code builds successfully
4. Update features.json status to "completed" (if ID-based)
5. Update LEARNED.md with discoveries
```

---

## Examples

### Using Feature IDs

```bash
# Single feature
/feature 1

# Multiple features (executed in order)
/feature 1,2,3,4

# Range of features
/feature 1-5

# Mixed
/feature 1,3-5,8
```

### Using Free-form Description

```bash
/feature Add user profile page with avatar and settings link
```

---

## Feature Status Updates

When using feature IDs, status is tracked in `features.json`:

| Status        | Meaning                                       |
| ------------- | --------------------------------------------- |
| `pending`     | Not started                                   |
| `in_progress` | Currently being implemented (set by scout)    |
| `completed`   | Successfully implemented (set by integration) |
| `blocked`     | Cannot proceed (dependency issues)            |

---

## Dependency Handling

Scout agent checks dependencies when loading feature IDs:

1. **Check dependencies**: Warn if prerequisite features are not completed
2. **Suggest order**: If dependencies exist, suggest implementing them first
3. **Allow override**: User can proceed despite warnings

Example warning:

```
Feature 5 (User Profile) depends on Feature 1 (Authentication).
Feature 1 status: pending

Recommendation: Implement feature 1 first.
Proceed anyway? [y/N]
```

---

## Context Passing

Each phase passes context to the next:

```
Scout Output → Planner Input
Planner Output → Impl Inputs (per task)
All Impl Outputs → Integration Input
```

## Error Handling

If any phase fails:

1. Capture error context
2. Report to user
3. Suggest remediation
4. Do not proceed to next phase
5. Keep feature status as "in_progress" (not completed)

---

## Testing

Use the `/test` command separately to generate tests for implemented features:

```bash
/test [feature description or file paths]
```
