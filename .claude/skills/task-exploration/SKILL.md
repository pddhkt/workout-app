---
name: task-exploration
description: Explores codebase for task implementation context. Use when scouting for features, bugfixes, or refactors before planning.
context: fork
agent: task-scout
---

# Task Exploration Skill

You are exploring the codebase to gather implementation context for a task.

## Step 1: Load Codebase Inventory

Read the pre-generated inventory file for current codebase structure:

```
.claude/cache/inventory.md
```

If the file doesn't exist or is stale, generate it:

```bash
./.claude/scripts/generate-inventory.sh > .claude/cache/inventory.md
```

The inventory shows:
- Custom components and their paths
- Available UI primitives (shadcn/ui)
- Custom hooks
- Routes and URL paths
- Backend functions (Convex exports)
- Database tables and indexes
- Shared types

## Step 2: Apply Exploration Strategy

Based on task type, follow the appropriate strategy:

### For Features (FT-xxx)

1. **Find Similar Features**
   - Search for components/pages with similar functionality
   - Look for patterns to follow

2. **Locate Integration Points**
   - Where will new code connect to existing code?
   - What existing utilities/hooks can be reused?

3. **Identify File Targets**
   - Which files need modification?
   - Which files need creation?

4. **Check Dependencies**
   - What backend functions exist?
   - What types are available?

### For Bugfixes (BF-xxx)

1. **Trace Error Path**
   - Where does the bug manifest?
   - What code path leads there?

2. **Find Root Cause**
   - Use Grep to find relevant code
   - Read files to understand logic

3. **Assess Impact**
   - What else might be affected?
   - What tests exist for this area?

4. **Document Fix Approach**
   - What needs to change?
   - What edge cases to consider?

5. **Assess Complexity (REQUIRED for bugfixes)**

   Determine if this bugfix needs a planning phase:

   **Simple (skip planning):**
   - Single file fix
   - One domain only (frontend OR backend OR database)
   - Clear, obvious fix approach
   - No schema changes required
   - No dependency ordering needed

   **Complex (needs planning):**
   - Multiple files across 2+ domains
   - Requires both frontend AND backend changes
   - Needs database schema change + code fix
   - Multiple potential fix approaches to evaluate
   - Requires tests to be added before fixing
   - Has dependency ordering (fix A before B)

   **Output `planning_required: true` or `planning_required: false`**

### For Refactors (RF-xxx)

1. **Map Current Implementation**
   - What's the current structure?
   - What code smells exist?

2. **Find All Usages**
   - Where is this code used?
   - What depends on it?

3. **Assess Test Coverage**
   - What tests exist?
   - Will refactoring break them?

4. **Define Refactoring Scope**
   - What's in scope?
   - What's the boundary?

## Step 3: Efficient Exploration

Use tools efficiently:

```
Glob  → Find files by pattern (fast)
Grep  → Search content (fast)
Read  → Examine specific files (after finding with Glob/Grep)
```

**Do NOT:**
- Read files without first narrowing with Glob/Grep
- Explore irrelevant directories
- Get lost in rabbit holes

## Step 4: Output Structured Findings

Return findings in this format:

```markdown
## Scout Findings for [Task ID]: [Title]

### Architecture Overview
- Project type: [SPA/fullstack/etc]
- Relevant domain: [frontend/backend/database/fullstack]

### Relevant Files
| File | Purpose | Action |
|------|---------|--------|
| path/to/file.tsx | Description | create/modify |

### Reusable Components
- Component X (path) - can reuse for Y
- Hook Z (path) - handles A

### Patterns to Follow
- Similar feature at path/to/example.tsx
- Naming convention: X

### Integration Points
- Connects to: [API function, component, etc]
- Uses: [shared types, utilities]

### Recommendations
- Domain: [frontend/backend/database/fullstack]
- Complexity: [simple/medium/complex]
- Key considerations: [list]

### For Bugfixes Only
- **planning_required**: [true/false]
- **Reason**: [why planning is/isn't needed]
- **Root cause**: [identified cause]
- **Fix approach**: [how to fix]
```

## Constraints

- **Read-only exploration** - Never modify files
- **Stay focused** - Only explore what's relevant to the task
- **Be efficient** - Use Glob/Grep before Read
- **Summarize** - Keep output scannable for the planner
