# Backend Task Criteria

Domain-specific acceptance criteria, best practices, and quality gates for backend tasks (Convex serverless).

## Contents

- [Acceptance Criteria Templates](#acceptance-criteria-templates)
- [Best Practices Checklist](#best-practices-checklist)
- [Testing Requirements](#testing-requirements)
- [Quality Gates](#quality-gates)
- [Common Patterns](#common-patterns)

---

## Acceptance Criteria Templates

### Feature Tasks

```markdown
- [ ] Uses correct function type (query for reads, mutation for writes, action for external)
- [ ] Input validated with `v` validators or Zod schema
- [ ] Returns appropriate error messages for invalid input
- [ ] Authentication checked where required (`getAuthUserId`)
- [ ] Authorization verified (user can access this resource)
- [ ] Response structure matches expected contract
- [ ] Error cases return appropriate HTTP status codes
- [ ] Database operations use indexes (no table scans)
```

### Bugfix Tasks

```markdown
- [ ] Root cause identified and documented
- [ ] Bug no longer reproduces under original conditions
- [ ] No security vulnerabilities introduced
- [ ] Error messages are descriptive and actionable
- [ ] Related functions still work correctly
- [ ] No data corruption or loss
- [ ] Edge cases handled (null, empty, invalid input)
```

### Refactor Tasks

```markdown
- [ ] API contract unchanged (same inputs/outputs)
- [ ] All API tests continue to pass
- [ ] Performance equal or improved (check query efficiency)
- [ ] Code follows Convex best practices
- [ ] Single responsibility per function
- [ ] Proper error handling maintained
- [ ] No breaking changes for frontend
```

---

## Best Practices Checklist

### Function Design

- [ ] Single responsibility (one function = one purpose)
- [ ] Uses `query` for reads (cached, reactive)
- [ ] Uses `mutation` for writes (transactional)
- [ ] Uses `action` only when external APIs needed
- [ ] Internal functions use `internalQuery`/`internalMutation`
- [ ] Function name describes action (`getById`, `create`, `updateStatus`)

### Input Validation

- [ ] All inputs validated before processing
- [ ] Uses `v` validators in function args
- [ ] Optional fields marked with `v.optional()`
- [ ] Enums use `v.union(v.literal(...), ...)`
- [ ] IDs validated with `v.id("tableName")`
- [ ] Invalid input returns 400 with clear message

### Authentication & Authorization

- [ ] Public endpoints explicitly documented
- [ ] Protected endpoints check `getAuthUserId(ctx)`
- [ ] Resource ownership verified before access
- [ ] Sensitive data not exposed in responses
- [ ] Rate limiting considered for public endpoints

### Error Handling

- [ ] Errors are descriptive (what went wrong, what to do)
- [ ] Sensitive details not exposed in errors
- [ ] Consistent error response format
- [ ] Uses appropriate HTTP status codes:
  - 400: Bad request (validation error)
  - 401: Unauthorized (not logged in)
  - 403: Forbidden (no permission)
  - 404: Not found
  - 500: Internal error

### Performance

- [ ] Queries use database indexes
- [ ] No N+1 query patterns
- [ ] Large data sets paginated
- [ ] Heavy operations use background jobs (actions)
- [ ] Caching considered for expensive operations

---

## Testing Requirements

### API Tests (Hurl or curl)

```markdown
- [ ] Happy path: Valid input returns expected output
- [ ] Validation: Invalid input returns 400 with message
- [ ] Auth: Unauthenticated request returns 401
- [ ] Auth: Unauthorized request returns 403
- [ ] Not found: Missing resource returns 404
- [ ] Edge cases: Null, empty, boundary values
```

### Example Test Cases

```bash
# Happy path
curl -X POST /api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title": "Test", "type": "feature"}'
# Expected: 201 Created

# Validation error
curl -X POST /api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title": ""}'
# Expected: 400 Bad Request

# Unauthorized
curl -X POST /api/tasks \
  -d '{"title": "Test"}'
# Expected: 401 Unauthorized
```

---

## Quality Gates

Must pass before task completion:

| Gate            | Requirement                                  |
| --------------- | -------------------------------------------- |
| **Types**       | `npx convex dev` shows no type errors        |
| **Deploy**      | Function deploys without errors              |
| **Validation**  | Invalid inputs rejected with clear messages  |
| **Auth**        | Protected endpoints require authentication   |
| **Performance** | Queries use indexes (check Convex dashboard) |
| **API Test**    | curl/Hurl tests pass for all scenarios       |

---

## Common Patterns

### Well-Defined Feature Task

```markdown
**Title**: Add Task Search API Endpoint

**Acceptance Criteria**:

- [ ] `query` function: `tasks.search`
- [ ] Input: `{ projectId: Id<"projects">, query: string }`
- [ ] Validates projectId exists
- [ ] Returns tasks where title contains query (case-insensitive)
- [ ] Requires authentication
- [ ] User must have access to project
- [ ] Returns max 50 results
- [ ] Response: `{ tasks: Task[], hasMore: boolean }`

**Testing**:

- [ ] Search with matching query returns results
- [ ] Search with no matches returns empty array
- [ ] Unauthenticated request returns 401
- [ ] Invalid projectId returns 404
```

### Well-Defined Bugfix Task

```markdown
**Title**: Fix Task Status Update Not Persisting

**Acceptance Criteria**:

- [ ] Root cause: Mutation not awaited in handler
- [ ] Status updates now persist to database
- [ ] Optimistic update works correctly on frontend
- [ ] No duplicate status entries created
- [ ] Related task queries return updated status

**Testing**:

- [ ] Update status and verify in database
- [ ] Refresh page and verify status persisted
- [ ] Multiple rapid updates handled correctly
```

### Well-Defined Refactor Task

```markdown
**Title**: Extract Task Validation into Shared Schema

**Acceptance Criteria**:

- [ ] Create `lib/schemas/task.ts` with Zod schema
- [ ] Schema used in create, update, and HTTP endpoints
- [ ] All existing validation behavior preserved
- [ ] Error messages unchanged
- [ ] API tests pass without modification

**Testing**:

- [ ] Existing API tests pass
- [ ] Validation errors return same messages
- [ ] Types correctly inferred from schema
```

---

## Convex-Specific Criteria

### Query Functions

```markdown
- [ ] Returns cached data (reactive updates)
- [ ] Uses `ctx.db.query()` with index
- [ ] Handles "loading" state (`undefined`) on frontend
- [ ] Handles "not found" state (`null`) on frontend
```

### Mutation Functions

```markdown
- [ ] Transactional (all-or-nothing)
- [ ] Returns created/updated document
- [ ] Triggers reactive updates on related queries
- [ ] Validates input before database operations
```

### Action Functions

```markdown
- [ ] Only used for external API calls
- [ ] Calls mutations for database writes
- [ ] Handles external API errors gracefully
- [ ] Considers retry logic for transient failures
```

### HTTP Endpoints

```markdown
- [ ] Defined in `convex/http.ts`
- [ ] Proper HTTP method (GET, POST, PATCH, DELETE)
- [ ] Returns JSON response
- [ ] Sets appropriate status codes
- [ ] Validates Authorization header
```
