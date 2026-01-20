---
name: test
description: Generate comprehensive tests for existing code including unit, integration, and E2E tests.
---

# Test Generation Workflow

Generate comprehensive tests for existing code.

## Arguments

Provide what to test (feature, component, endpoint, or file path).

## Workflow

### Phase 1: Scout (Analyze Code)

Spawn scout agent to understand the code:

```
Use Task tool with subagent_type: "scout"
Prompt:
Test Target: [user's test description]

Find:
1. Code to be tested (files, functions, components)
2. Existing tests (if any)
3. Dependencies and mocks needed
4. Edge cases and error conditions
5. Test patterns used in project
```

**Output**: Code analysis, test requirements, existing coverage

### Phase 2: Plan

Create comprehensive test plan:

```
Use Task tool with subagent_type: "planner"
Prompt:
Test Target: [user's test description]
Scout Findings: [paste scout output]

Create:
1. Test categories needed (unit, integration, E2E)
2. Test cases for each category
3. Mocking strategy
4. Priority order (critical paths first)
```

**Output**: Test plan with categorized test cases

### Phase 3: Generate Tests

Spawn test agents based on the plan:

#### Frontend Tests (if applicable)

```
Use Task tool with subagent_type: "test"
Prompt:
Task Context:
{
  "test_type": "frontend",
  "task": "Generate tests for: [user's test description]",
  "files": ["[files to test]"],
  "coverage": [
    "Happy path",
    "Error states",
    "Loading states",
    "User interactions",
    "Edge cases"
  ]
}
```

#### Backend Tests (if applicable)

```
Use Task tool with subagent_type: "test"
Prompt:
Task Context:
{
  "test_type": "backend",
  "task": "Generate tests for: [user's test description]",
  "files": ["[files to test]"],
  "coverage": [
    "Success responses",
    "Validation errors",
    "Authentication",
    "Authorization",
    "Edge cases"
  ]
}
```

### Phase 4: Run and Verify

Execute tests and report results:

```
Run all generated tests
Report:
- Tests passed
- Tests failed (with reasons)
- Coverage metrics
- Gaps identified
```

## Example

```
/test Add E2E tests for checkout flow
```

**Scout Phase**:

- Maps checkout component flow
- Finds Cart, Payment, Confirmation components
- Identifies API endpoints involved

**Plan Phase**:

- E2E: Complete checkout flow
- E2E: Apply coupon code
- E2E: Handle payment failure
- E2E: Empty cart handling

**Generate Phase**:

- Creates Playwright tests for each case
- Mocks payment API for failure cases

**Verify Phase**:

- Runs all tests
- Reports 4/4 passing

## Test Categories

### Unit Tests

- Individual functions
- Pure logic
- Utilities

### Integration Tests

- Component with dependencies
- Service with database
- API with middleware

### E2E Tests

- Complete user flows
- Cross-component interactions
- Real browser behavior

## Coverage Goals

| Category       | Target |
| -------------- | ------ |
| Critical paths | 100%   |
| Error handling | 90%    |
| Edge cases     | 80%    |
| UI variations  | 70%    |

## Quality Criteria

Tests should be:

- [ ] **Isolated** - No test depends on another
- [ ] **Deterministic** - Same result every run
- [ ] **Fast** - Quick feedback loop
- [ ] **Readable** - Clear what's being tested
- [ ] **Maintainable** - Easy to update with code

## Output

Final report includes:

- Tests created (file paths)
- Test cases covered
- Test results (pass/fail)
- Coverage metrics
- Recommendations for additional tests
