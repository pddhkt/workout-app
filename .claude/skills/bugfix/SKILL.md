---
name: bugfix
description: Investigate and fix bugs using focused agent coordination. Use when you need to diagnose and fix issues.
---

# Bugfix Workflow

Investigate and fix a bug using focused agent coordination.

## Arguments

Provide a bug description, error message, or reproduction steps.

## Workflow

### Phase 1: Scout (Bug Investigation)

Spawn scout agent focused on bug investigation:

```
Use Task tool with subagent_type: "scout"
Prompt:
Bug Report: [bug description provided by user]

Find:
1. Files related to the bug
2. Code paths that could cause this
3. Error handling in affected areas
4. Related tests (if any)
5. Recent changes to affected files
```

**Output**: Affected files, potential causes, code paths

### Phase 2: Diagnose

Analyze the root cause:

```
Use Task tool with subagent_type: "planner"
Prompt:
Bug Report: [bug description provided by user]
Scout Findings: [paste scout output]

Determine:
1. Root cause of the bug
2. Why this is happening
3. What fix is needed
4. Domain affected (frontend/backend/database)
5. Risk of regression
```

**Output**: Root cause analysis, fix approach, domain

### Phase 3: Fix

Spawn impl agent to fix the bug:

```
Use Task tool with subagent_type: "impl"
Prompt:
Task Context:
{
  "domain": "[from diagnosis]",
  "task": "Fix: [bug description]",
  "files": ["[affected files]"],
  "root_cause": "[from diagnosis]"
}
```

**Output**: Fixed code, explanation of changes

### Phase 4: Test

Add regression test and verify fix:

```
Use Task tool with subagent_type: "test"
Prompt:
Task Context:
{
  "test_type": "[frontend|backend]",
  "task": "Add regression test for: [bug description]",
  "files": ["[fixed files]"],
  "coverage": [
    "Test that the bug is fixed",
    "Test related edge cases",
    "Ensure no regression"
  ]
}
```

**Output**: Regression test, test results

## Example

```
/bugfix Login shows "Network error" instead of "Invalid credentials" when password is wrong
```

**Scout Phase**:

- Finds login form component
- Finds auth API endpoint
- Finds error handling code

**Diagnose Phase**:

- Root cause: API returns 401 but frontend catches as network error
- Fix: Check response status before treating as network error

**Fix Phase**:

- Updates error handling in login component
- Properly parses 401 response

**Test Phase**:

- Adds test for wrong password error message
- Verifies network errors still show correct message

## Context from Bug Report

Include any of these if available:

- Error message
- Stack trace
- Reproduction steps
- Screenshot
- Browser/environment info

## Error Handling

If diagnosis is unclear:

1. Add logging to gather more info
2. Create hypothesis tests
3. Ask user for clarification

If fix introduces new issues:

1. Rollback changes
2. Identify side effects
3. Plan more careful approach
