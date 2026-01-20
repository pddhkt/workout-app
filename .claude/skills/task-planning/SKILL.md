---
name: task-planning
description: Strategic task decomposition and planning patterns. Use when planning features or refactors that require multi-phase execution.
context: fork
agent: planner
---

# Task Planning Skill

You are creating an implementation plan from scout findings and task requirements.

## Step 1: Analyze Scout Findings

Parse the scout output to understand:
- Architecture overview and project type
- Relevant files with proposed actions (create/modify)
- Reusable components and patterns
- Integration points
- Domain recommendations

## Step 2: Apply Planning Strategy by Task Type

### For Features (FT-xxx)

**Order:** database → backend → frontend → integration

1. **Database first** (if schema changes needed)
   - Schema modifications
   - Index additions
   - Run migrations

2. **Backend next** (API layer)
   - Convex mutations/queries
   - HTTP endpoints if needed
   - Server-side validation

3. **Frontend after backend ready**
   - Components consuming APIs
   - Route pages
   - Client-side state

4. **Integration last**
   - Wire pieces together
   - E2E validation
   - Final testing

### For Bugfixes (BF-xxx)

**Note:** Only called for complex bugfixes (when scout sets `planning_required: true`).

**Order:** isolate → fix dependencies → fix → verify

1. **Isolate the problem**
   - Identify all affected code paths
   - Determine if changes span multiple domains

2. **Order fix tasks by dependency**
   - Database fixes before backend (if schema involved)
   - Backend fixes before frontend (if API involved)
   - Each fix should be independently testable

3. **Minimize blast radius**
   - Prefer surgical fixes over broad refactoring
   - Keep changes focused on the bug
   - Don't introduce unrelated improvements

4. **Include verification steps**
   - Test after each fix task
   - Regression test at the end

**Example complex bugfix breakdown:**
```
Phase 1: Backend Fix
- Fix the API validation logic

Phase 2: Frontend Fix
- Update error handling to match new API response
- Depends on Phase 1

Phase 3: Regression Test
- Add test covering this bug scenario
```

### For Refactors (RF-xxx)

**Order:** test → incremental → verify

1. **Ensure test coverage FIRST**
   - Add missing tests before touching code
   - Tests become the safety net
   - Document expected behavior

2. **Incremental steps with checkpoints**
   - Each step independently verifiable
   - Rollback points after major changes
   - No big-bang rewrites

3. **Verify behavior unchanged**
   - Run tests after each step
   - Compare before/after behavior
   - Check for regressions

## Step 3: Domain Classification

| Task Involves | Domain | Agent |
|--------------|--------|-------|
| UI components, pages, styling, React | frontend | impl |
| API endpoints, mutations, queries, auth | backend | impl |
| Schema changes, indexes, migrations | database | impl |
| Full user flow across layers | fullstack | impl |
| React/Playwright tests | frontend | test |
| API/Hurl tests | backend | test |

## Step 4: Identify Dependencies

**Dependency rules:**
- Frontend consuming API → depends on backend task
- Backend querying new fields → depends on database task
- Integration tasks → depend on all component tasks
- Tests → depend on implementation being complete

**Identify by asking:**
- Does task B need output from task A?
- Can task B start before task A completes?
- Does task B modify files that task A creates?

## Step 5: Parallelization Rules

**Can run in parallel:**
- Backend API + Frontend components (before wiring)
- Multiple independent frontend components
- Multiple independent backend functions
- Frontend tests + Backend tests (after implementation)
- Database migrations in different tables

**Must run sequentially:**
- Schema change → Backend using schema
- Backend → Frontend consuming API
- Implementation → Integration
- All phases → Final validation

## Step 6: ASCII Execution Diagram

Create a visual execution flow using this format:

```
┌─────────────────────────────────────────────────┐
│                Execution Flow                    │
├─────────────────────────────────────────────────┤
│ Phase 1 (Parallel)                              │
│ ┌──────────────┐  ┌──────────────┐              │
│ │ Task 1       │  │ Task 2       │              │
│ │ [backend]    │  │ [frontend]   │              │
│ └──────┬───────┘  └──────┬───────┘              │
│        │                 │                      │
│        └────────┬────────┘                      │
│                 ↓                               │
│ Phase 2 (Sequential)                            │
│ ┌──────────────────────────┐                    │
│ │ Task 3 [frontend]        │                    │
│ │ Wire UI to API           │                    │
│ │ Dependencies: 1, 2       │                    │
│ └────────────┬─────────────┘                    │
│              ↓                                  │
│ Phase 3 (Parallel)                              │
│ ┌──────────────┐  ┌──────────────┐              │
│ │ Task 4       │  │ Task 5       │              │
│ │ E2E tests    │  │ API tests    │              │
│ └──────────────┘  └──────────────┘              │
└─────────────────────────────────────────────────┘
```

**For refactors, include checkpoints:**
```
┌─────────────────────────────────────────────────┐
│ Phase 1: Safety Net                             │
│ ┌──────────────────────────┐                    │
│ │ Task 1: Add tests        │ ← Checkpoint      │
│ └────────────┬─────────────┘                    │
│              ↓                                  │
│ Phase 2: Refactor                               │
│ ┌──────────────────────────┐                    │
│ │ Task 2: Extract module   │ ← Checkpoint      │
│ └────────────┬─────────────┘                    │
│              ↓                                  │
│ Phase 3: Verify                                 │
│ ┌──────────────────────────┐                    │
│ │ Task 3: Run all tests    │                    │
│ └──────────────────────────┘                    │
└─────────────────────────────────────────────────┘
```

## Step 7: Structured Output Format

Return plan in this format:

```markdown
## Implementation Plan for [Task ID]: [Title]

### Summary
Brief approach description (1-2 sentences)

### Task Breakdown

#### Phase 1: [Name] (Parallel/Sequential)
| ID | Task | Domain | Agent | Dependencies |
|----|------|--------|-------|--------------|
| 1  | Create API endpoint | backend | impl | - |
| 2  | Create UI component | frontend | impl | - |

#### Phase 2: [Name]
| ID | Task | Domain | Agent | Dependencies |
|----|------|--------|-------|--------------|
| 3  | Wire UI to API | frontend | impl | 1, 2 |

### Execution Diagram
[ASCII diagram here]

### Critical Files
| File | Action | Task ID |
|------|--------|---------|
| convex/resource.ts | modify | 1 |
| src/components/Feature/ | create | 2 |

### Domain Context for Agents

#### Task 1 (Backend)
```json
{
  "domain": "backend",
  "task": "Create resource query and mutation",
  "files": ["convex/resource.ts"],
  "patterns": "Follow existing query structure",
  "dependencies": []
}
```

#### Task 2 (Frontend)
```json
{
  "domain": "frontend",
  "task": "Create ResourceList component",
  "files": ["src/components/resource/ResourceList.tsx"],
  "patterns": "Follow TaskList component pattern",
  "dependencies": []
}
```

### Testing Strategy
- Unit tests: [what to test]
- E2E tests: [user flows]
- API tests: [endpoints to verify]

### Risk Assessment
- [potential issues and mitigations]

### Acceptance Criteria
- [ ] Criterion from original task
- [ ] Additional verification items
```

## Constraints

- **Atomic tasks**: Each task completable independently
- **Clear domains**: Every task has exactly one domain
- **Explicit dependencies**: No hidden dependencies
- **Testable outputs**: Each task should be verifiable
- **Minimal scope**: Don't add tasks beyond requirements

## Notes

- Scout findings should provide sufficient context
- Can read `.claude/cache/inventory.md` if additional detail needed
- Prefer using scout findings to avoid duplicate exploration
