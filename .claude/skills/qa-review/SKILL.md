---
name: qa-review
description: Validate that all acceptance criteria are met after implementation. Part of the QA loop in the autonomous build pipeline.
---

# QA Review Skill

Validates that a completed task meets all acceptance criteria. This skill triggers the QA loop:
- **APPROVED** → Task complete, move to done
- **REJECTED** → Create QA_FIX_REQUEST.md, trigger qa-fix loop

## Usage

```bash
/qa-review FT-001
```

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `CONVEX_SITE_URL` | Convex HTTP site URL | `https://xxx-yyy.convex.site` |
| `CLAUDE_API_KEY` | API key for authentication | `sk-...` |

## API Endpoints

### GET /api/tasks/qa?customId={customId}

Get current QA status.

```bash
curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/qa?customId=FT-001" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}"
```

**Response:**
```json
{
  "customId": "FT-001",
  "qaStatus": "pending",
  "qaReport": null,
  "qaIterations": 0
}
```

### PATCH /api/tasks/{customId}/qa

Update QA status with report.

```bash
curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/FT-001/qa" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "qaStatus": "approved",
    "qaReport": "## QA Report\n\nAll acceptance criteria met."
  }'
```

**Request body:**
- `qaStatus` (required) - `pending`, `approved`, `rejected`
- `qaReport` (optional) - Markdown report of findings

## QA Review Process

### 1. Pre-flight Checks

Before reviewing, verify:
- All subtasks are `completed`
- No subtasks are `blocked` or `failed`
- Task status is `in_progress`

```bash
# Check subtask progress
PROGRESS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/FT-001/subtasks/progress" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

TOTAL=$(echo "$PROGRESS" | jq -r '.total')
COMPLETED=$(echo "$PROGRESS" | jq -r '.completed')
FAILED=$(echo "$PROGRESS" | jq -r '.failed')

if [ "$TOTAL" -ne "$COMPLETED" ] || [ "$FAILED" -gt 0 ]; then
  echo "Cannot review: subtasks incomplete or failed"
  exit 1
fi
```

### 2. Load Acceptance Criteria

```bash
TASK=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/FT-001" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

CRITERIA=$(echo "$TASK" | jq -r '.acceptanceCriteria // []')
```

### 3. Validate Each Criterion

For each acceptance criterion:
1. Determine validation method (test, visual, functional)
2. Execute validation
3. Record result (pass/fail with details)

### 4. Run Tests

```bash
# Run project tests
npm test 2>&1 | tee test-output.txt

if [ $? -ne 0 ]; then
  echo "Tests failed"
  # Will be included in QA report
fi
```

### 5. Check for Errors

```bash
# Build check
npm run build 2>&1 | tee build-output.txt

# Type check (if TypeScript)
npm run typecheck 2>&1 | tee typecheck-output.txt

# Lint check
npm run lint 2>&1 | tee lint-output.txt
```

### 6. Generate Report

## QA Report Format

### Approved Report

```markdown
## QA Report: FT-001

**Status:** APPROVED
**Reviewer:** QA Agent
**Date:** 2024-01-15

### Acceptance Criteria

- [x] User can log in with email and password
- [x] Invalid credentials show error message
- [x] Session persists across page refresh

### Test Results

- Unit tests: 24/24 passing
- Integration tests: 8/8 passing
- Build: Success
- Type check: No errors

### Summary

All acceptance criteria met. Implementation is complete and ready for merge.
```

### Rejected Report

```markdown
## QA Report: FT-001

**Status:** REJECTED
**Reviewer:** QA Agent
**Date:** 2024-01-15
**Iteration:** 2

### Acceptance Criteria

- [x] User can log in with email and password
- [ ] Invalid credentials show error message
- [x] Session persists across page refresh

### Issues Found

1. **Missing error message for invalid credentials**
   - File: `src/components/LoginForm.tsx`
   - Line: 45
   - Expected: Error toast appears when credentials are wrong
   - Actual: No feedback shown to user
   - Fix: Add error state handling in catch block

2. **TypeScript error in auth middleware**
   - File: `src/middleware/auth.ts`
   - Line: 23
   - Error: `Property 'user' does not exist on type 'Request'`
   - Fix: Extend Express Request type

### Test Results

- Unit tests: 22/24 passing (2 failures)
- Build: Failed (TypeScript error)

### QA Fix Request

See QA_FIX_REQUEST.md for detailed fix instructions.
```

## QA_FIX_REQUEST.md Format

When rejecting, create a detailed fix request:

```markdown
# QA Fix Request: FT-001

**Iteration:** 2
**Created:** 2024-01-15

## Issues to Fix

### Issue 1: Missing error message
**Priority:** High
**File:** `src/components/LoginForm.tsx:45`

**Problem:**
No error message displayed when login fails with invalid credentials.

**Expected Behavior:**
Toast notification should appear with "Invalid email or password"

**Suggested Fix:**
```tsx
try {
  await login(email, password);
} catch (error) {
  toast.error('Invalid email or password');  // Add this line
}
```

**Verification:**
1. Enter invalid credentials
2. Click login button
3. Verify error toast appears

---

### Issue 2: TypeScript error
**Priority:** Critical (blocks build)
**File:** `src/middleware/auth.ts:23`

**Problem:**
TypeScript compilation error due to untyped request extension.

**Suggested Fix:**
```typescript
// Add type declaration
declare global {
  namespace Express {
    interface Request {
      user?: UserPayload;
    }
  }
}
```

**Verification:**
Run `npm run typecheck` - should pass with no errors

---

## After Fixing

1. Run all tests: `npm test`
2. Run build: `npm run build`
3. Re-run QA review: `/qa-review FT-001`
```

## Bash Helper Functions

```bash
# Get QA status
get_qa_status() {
  local task_id="$1"
  curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/qa?customId=${task_id}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}"
}

# Update QA status
update_qa_status() {
  local task_id="$1"
  local status="$2"
  local report="$3"

  local body="{\"qaStatus\": \"${status}\""
  if [ -n "$report" ]; then
    # Escape newlines and quotes for JSON
    local escaped_report=$(echo "$report" | jq -Rs .)
    body="${body}, \"qaReport\": ${escaped_report}"
  fi
  body="${body}}"

  curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/${task_id}/qa" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "$body"
}

# Approve task
approve_qa() {
  local task_id="$1"
  local report="$2"
  update_qa_status "$task_id" "approved" "$report"
}

# Reject task
reject_qa() {
  local task_id="$1"
  local report="$2"
  update_qa_status "$task_id" "rejected" "$report"
}
```

## Integration with Build Pipeline

```
All subtasks complete
       ↓
   QA Review
       ↓
   ┌───┴───┐
   ↓       ↓
APPROVED  REJECTED
   ↓       ↓
Complete  QA Fix
   ↓       ↓
 Done   Review again
           ↓
        (max 3 iterations)
```

## QA Iteration Limits

To prevent infinite loops:
- Maximum 3 QA iterations
- After 3 rejections, mark task as `blocked` with reason
- Requires human intervention

```bash
QA_STATUS=$(get_qa_status "FT-001")
ITERATIONS=$(echo "$QA_STATUS" | jq -r '.qaIterations')

if [ "$ITERATIONS" -ge 3 ]; then
  echo "Max QA iterations reached. Escalating to human review."
  # Mark task as blocked
  curl -s -X PATCH "${CONVEX_SITE_URL}/api/tasks/FT-001/status" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
    -H "Content-Type: application/json" \
    -d '{"status": "blocked", "blockedReason": "QA failed after 3 iterations"}'
  exit 1
fi
```

## Error Handling

| HTTP Status | Meaning |
|-------------|---------|
| 200 | Success |
| 400 | Invalid qaStatus |
| 401 | Unauthorized |
| 404 | Task not found |
