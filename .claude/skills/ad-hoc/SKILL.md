---
name: ad-hoc
description: Implement features without task tracking using multi-agent orchestration. For quick fixes and one-off implementations.
---

# Ad-Hoc Implementation Workflow

Implement features quickly using multi-agent orchestration **without creating tasks in Convex**. Use this for quick fixes, one-off changes, or when you don't need task tracking.

## When to Use

| Use `/ad-hoc` when... | Use `/task-dev` when... |
|----------------------|------------------------|
| Quick fix or small change | Work needs to be tracked |
| One-off implementation | Part of a larger project |
| Prototyping/experimenting | Multiple people involved |
| No need for persistence | Recovery/resume needed |
| "Just do this quickly" | Visibility in UI required |

## Arguments

Provide a free-form description of what to implement:

```bash
/ad-hoc Add user profile page with avatar
/ad-hoc Fix the login button color to match the design
/ad-hoc Add dark mode toggle to settings
```

---

## Orchestrator Role

**IMPORTANT**: You (the orchestrator) coordinate the workflow but do NOT implement directly.

### Why Use Subagents

1. **Context Window Preservation** - Delegating to impl/test agents keeps your context free for high-level orchestration
2. **Domain Skill Loading** - Impl agents load domain-specific skills with project patterns
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

Scout agent explores the codebase:

```
Use Task tool with subagent_type: "scout"
Prompt:
Feature Request: [user's description]

Tasks:
1. Explore codebase for:
   - Project structure and architecture
   - Existing code related to this feature
   - Patterns matching the domain
   - Reusable components/utilities
   - Files that need modification

Output:
- Architecture overview
- Relevant files and patterns
- Recommendations
- Domain recommendation (frontend/backend/database/fullstack)
```

**Output**: Codebase analysis and recommendations

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
## Implementation Plan for: [Description]

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
Feature: [description]
Implementation summaries: [paste all impl outputs]

Validate:
1. All pieces work together
2. No integration issues
3. Code builds successfully
4. Update LEARNED.md with discoveries
```

---

## Examples

```bash
# Quick UI fix
/ad-hoc Fix the login button color to #3B82F6

# Add simple feature
/ad-hoc Add user profile page with avatar and settings link

# Quick backend change
/ad-hoc Add rate limiting to the API endpoints

# Experimental feature
/ad-hoc Prototype a dark mode toggle
```

---

## Comparison with Task-Dev

| Aspect | `/ad-hoc` | `/task-dev` |
|--------|-----------|-------------|
| Task tracking | No | Yes (Convex) |
| Status updates | No | Yes |
| Recovery/resume | No | Yes |
| Subtask creation | No | Yes |
| QA validation | No | Yes (with --auto) |
| UI visibility | No | Yes |
| Best for | Quick fixes | Tracked work |

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

---

## Stack-Aware Agent Routing

If a `.claude/stack.json` exists, impl agents are routed based on domain:

```json
{
  "domains": {
    "frontend": { "agent": "frontend-impl" },
    "backend": { "agent": "backend-impl" }
  }
}
```

**Example:** If domain is "frontend" and stack.json has `domains.frontend.agent: "nextjs-impl"`, use `subagent_type: "nextjs-impl"`.

---

## When to Switch to Task-Dev

Consider using `/task-dev` instead if:

- Work will take multiple sessions
- You need to track progress over time
- Multiple people are collaborating
- You want recovery if interrupted
- Work is part of a larger phase/milestone

To create a tracked task for existing work:
```bash
/task-init "Description of work" --type feature --domain frontend
```

---

## Related Skills

| Skill | Relationship |
|-------|--------------|
| `/task-dev` | Execute tracked tasks from Convex |
| `/phase-init` | Create phases with multiple tasks |
| `/task-init` | Create single tracked task |
| `/bugfix` | Quick bugfix workflow |
| `/refactor` | Quick refactor workflow |
