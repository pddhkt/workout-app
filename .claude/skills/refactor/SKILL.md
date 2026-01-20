---
name: refactor
description: Safely refactor code while maintaining functionality with test coverage.
---

# Refactor Workflow

Safely refactor code while maintaining functionality.

## Arguments

Provide a description of what to refactor and why.

## Workflow

### Phase 1: Scout (Understand Current State)

Spawn scout agent to map the code:

```
Use Task tool with subagent_type: "scout"
Prompt:
Refactor Request: [user's refactor description]

Find:
1. All code affected by this refactor
2. Dependencies and dependents
3. Current test coverage
4. Usage patterns throughout codebase
5. Similar patterns already in use
```

**Output**: Affected files, dependencies, test coverage assessment

### Phase 2: Plan

Create safe refactoring plan:

```
Use Task tool with subagent_type: "planner"
Prompt:
Refactor Request: [user's refactor description]
Scout Findings: [paste scout output]

Create:
1. Step-by-step refactoring plan
2. Test requirements before refactoring
3. Incremental changes (each testable)
4. Rollback strategy
```

**Output**: Refactoring plan with safety checkpoints

### Phase 3: Ensure Test Coverage

Before refactoring, ensure adequate tests exist:

```
Use Task tool with subagent_type: "test"
Prompt:
Task Context:
{
  "test_type": "[frontend|backend]",
  "task": "Ensure test coverage before refactor",
  "files": ["[files to refactor]"],
  "coverage": [
    "All public interfaces",
    "Edge cases",
    "Error handling"
  ]
}
```

**Output**: Tests that will catch regressions

### Phase 4: Refactor (Incremental)

Execute refactoring in small, testable steps:

```
For each step in the plan:

Use Task tool with subagent_type: "impl"
Prompt:
Task Context:
{
  "domain": "[frontend|backend|database]",
  "task": "[specific refactoring step]",
  "files": ["[files for this step]"],
  "constraint": "Maintain all existing behavior"
}

After each step:
- Run tests
- If tests fail: rollback and diagnose
- If tests pass: proceed to next step
```

**Output**: Refactored code, passing tests

### Phase 5: Verify

Final validation:

```
Use Task tool with subagent_type: "integration"
Prompt:
Refactor Complete: [user's refactor description]
Changes Made: [list all changes]

Verify:
1. All tests pass
2. No behavior changes
3. Code is cleaner/better
4. Update LEARNED.md with patterns
```

## Example

```
/refactor Extract user authentication logic into reusable hook
```

**Scout Phase**:

- Finds all components using auth logic
- Maps authentication flow
- Identifies existing patterns

**Plan Phase**:

- Step 1: Create useAuth hook with extracted logic
- Step 2: Update LoginForm to use hook
- Step 3: Update ProtectedRoute to use hook
- Step 4: Remove duplicated code

**Test Phase**:

- Adds tests for auth hook
- Verifies existing auth flows

**Refactor Phase**:

- Implements each step
- Runs tests after each step
- Proceeds only if tests pass

**Verify Phase**:

- All auth flows work
- Code is DRYer
- Hook is reusable

## Safety Rules

1. **Never refactor without tests** - If no tests exist, write them first
2. **Small increments** - Each step should be independently testable
3. **Maintain behavior** - Refactoring should not change functionality
4. **Rollback ready** - Be prepared to undo at any step
5. **No new features** - Refactoring is not feature development

## Red Flags

Stop and reconsider if:

- Tests are failing after a step
- Scope is growing unexpectedly
- Behavior changes are required
- Dependencies are more complex than expected
