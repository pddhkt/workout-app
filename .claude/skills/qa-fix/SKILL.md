---
name: qa-fix
description: Fix issues identified in QA review. Reads QA_FIX_REQUEST.md and systematically addresses each issue.
---

# QA Fix Skill

Systematically fixes issues identified during QA review. Reads the QA_FIX_REQUEST.md file and addresses each issue in order.

## Usage

```bash
/qa-fix FT-001
```

## Prerequisites

- Task has `qaStatus: rejected`
- QA_FIX_REQUEST.md exists in task build directory
- `qaIterations < 3` (not exceeded max iterations)

## Workflow

```
1. Read QA_FIX_REQUEST.md
       ↓
2. Parse issues into fix list
       ↓
3. For each issue:
   a. Read the problematic file
   b. Apply the fix
   c. Run verification
   d. Commit fix
       ↓
4. Run full test suite
       ↓
5. Trigger QA re-review
```

## QA_FIX_REQUEST.md Format

Input file structure:

```markdown
# QA Fix Request: FT-001

**Iteration:** 2
**Created:** 2024-01-15

## Issues to Fix

### Issue 1: Missing error message
**Priority:** High
**File:** `src/components/LoginForm.tsx:45`

**Problem:**
No error message displayed when login fails.

**Suggested Fix:**
```tsx
toast.error('Invalid email or password');
```

**Verification:**
Enter invalid credentials, verify error appears.

---

### Issue 2: TypeScript error
**Priority:** Critical
**File:** `src/middleware/auth.ts:23`

**Problem:**
TypeScript compilation error.

**Suggested Fix:**
```typescript
// type declaration fix
```

**Verification:**
Run `npm run typecheck`
```

## Fix Process

### 1. Load Fix Request

```bash
# Check if fix request exists
FIX_REQUEST=".claude/builds/FT-001/QA_FIX_REQUEST.md"

if [ ! -f "$FIX_REQUEST" ]; then
  echo "No QA_FIX_REQUEST.md found"
  exit 1
fi

# Read the file
FIX_CONTENT=$(cat "$FIX_REQUEST")
```

### 2. Parse Issues

Extract structured issues from markdown:

```bash
# Extract issue sections
ISSUES=$(echo "$FIX_CONTENT" | grep -E "^### Issue" | wc -l)
echo "Found $ISSUES issues to fix"
```

### 3. Fix Each Issue

For each issue:

1. **Read context** - Load the file mentioned in the issue
2. **Understand the problem** - Parse the Problem section
3. **Apply suggested fix** - Use the Suggested Fix code
4. **Verify** - Run the verification command

### 4. Commit Fixes

After each issue:

```bash
# Commit with clear message
git add -A
git commit -m "fix(FT-001): [Issue N] Brief description

QA iteration: 2
Issue: Description of what was wrong
Fix: What was done to fix it"
```

### 5. Run Full Verification

After all fixes:

```bash
# Full test suite
npm test

# Build check
npm run build

# Type check
npm run typecheck

# Lint
npm run lint
```

### 6. Trigger Re-Review

```bash
# Update QA status back to pending
curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/FT-001/qa" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"qaStatus": "pending"}'

# Trigger QA review
echo "Fixes complete. Running QA review..."
# /qa-review FT-001 (will be called by orchestrator)
```

## Bash Helper Functions

```bash
# Parse issue count from fix request
count_issues() {
  local fix_file="$1"
  grep -c "^### Issue" "$fix_file" || echo 0
}

# Extract issue by number
get_issue() {
  local fix_file="$1"
  local issue_num="$2"

  # Extract the issue section (from ### Issue N to next ### or end)
  awk "/^### Issue ${issue_num}:/,/^### Issue [0-9]:|^---$|^## /" "$fix_file" | head -n -1
}

# Extract file path from issue
get_issue_file() {
  local issue_content="$1"
  echo "$issue_content" | grep -oP '\*\*File:\*\* `\K[^`]+' | head -1
}

# Extract suggested fix code
get_suggested_fix() {
  local issue_content="$1"
  # Extract code block after "Suggested Fix:"
  echo "$issue_content" | awk '/\*\*Suggested Fix:\*\*/,/```/' | tail -n +2 | head -n -1
}

# Run issue verification
run_verification() {
  local issue_content="$1"
  local verification=$(echo "$issue_content" | grep -A5 "\*\*Verification:\*\*")
  # Extract and run verification command if present
  local cmd=$(echo "$verification" | grep -oP '`\K[^`]+' | head -1)
  if [ -n "$cmd" ]; then
    eval "$cmd"
    return $?
  fi
  return 0
}

# Reset QA status for re-review
reset_for_review() {
  local task_id="$1"
  curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/${task_id}/qa" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d '{"qaStatus": "pending"}'
}
```

## Fix Priority Order

Issues should be fixed in priority order:

1. **Critical** - Build/compile blockers (fix first)
2. **High** - Functionality issues
3. **Medium** - UX/UI issues
4. **Low** - Style/documentation issues

## Recovery Patterns

### If Fix Fails

```bash
# Mark issue as failed in notes
git stash
echo "Issue N fix failed, reverting changes"

# Continue to next issue or escalate
```

### If Verification Fails

```bash
# Re-read the issue
# Try alternative approach
# If still failing, add to notes for next QA iteration
```

### If All Fixes Complete But Tests Fail

```bash
# Analyze test failures
# May have introduced regression
# Document in commit message
# Let QA review catch and report new issues
```

## Integration with Build Pipeline

```
QA Rejected
    ↓
/qa-fix FT-001
    ↓
┌─────────────┐
│ For each    │
│ issue:      │
│  - Fix      │
│  - Verify   │←──┐
│  - Commit   │   │ (if fail, retry once)
└─────────────┘───┘
    ↓
Full test suite
    ↓
/qa-review FT-001
    ↓
┌───┴───┐
↓       ↓
PASS    FAIL
↓       ↓
Done    Loop (max 3x)
```

## Example Complete Session

```bash
#!/bin/bash
source .env

TASK_ID="FT-001"
BUILD_DIR=".claude/builds/${TASK_ID}"
FIX_REQUEST="${BUILD_DIR}/QA_FIX_REQUEST.md"

# Check prerequisites
if [ ! -f "$FIX_REQUEST" ]; then
  echo "No fix request found at $FIX_REQUEST"
  exit 1
fi

# Count issues
ISSUE_COUNT=$(count_issues "$FIX_REQUEST")
echo "=== QA Fix: $TASK_ID ==="
echo "Issues to fix: $ISSUE_COUNT"

# Process each issue
for i in $(seq 1 $ISSUE_COUNT); do
  echo ""
  echo "--- Fixing Issue $i of $ISSUE_COUNT ---"

  ISSUE=$(get_issue "$FIX_REQUEST" $i)
  FILE=$(get_issue_file "$ISSUE")

  echo "File: $FILE"

  # Agent applies fix here...
  # (Read file, apply suggested fix, write back)

  # Verify
  if run_verification "$ISSUE"; then
    echo "✓ Issue $i verified"
    git add -A
    git commit -m "fix(${TASK_ID}): Issue $i fixed"
  else
    echo "✗ Issue $i verification failed"
  fi
done

# Run full test suite
echo ""
echo "=== Running Full Test Suite ==="
npm test

# Reset for re-review
reset_for_review "$TASK_ID"
echo ""
echo "Fixes complete. Ready for QA re-review."
```

## Error Handling

| Scenario | Action |
|----------|--------|
| Fix request not found | Exit with error |
| File not found | Log and continue to next issue |
| Fix doesn't apply | Try alternative approach |
| Verification fails | Retry once, then continue |
| Tests fail after fixes | Document and let QA catch |
| Max iterations reached | Escalate to human |
