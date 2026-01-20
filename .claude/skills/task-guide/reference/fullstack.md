# Fullstack Task Criteria

Domain-specific acceptance criteria, best practices, and quality gates for fullstack tasks spanning frontend, backend, and database.

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
**Database Layer:**

- [ ] Schema defined with appropriate validators
- [ ] Indexes created for query patterns
- [ ] Relationships properly defined

**Backend Layer:**

- [ ] API functions created (query/mutation)
- [ ] Input validation implemented
- [ ] Authentication/authorization enforced
- [ ] Error responses follow conventions

**Frontend Layer:**

- [ ] UI components created using shadcn/ui
- [ ] Data fetching with useQuery/useMutation
- [ ] Loading, error, and empty states handled
- [ ] Responsive design implemented

**Integration:**

- [ ] Frontend-backend contract matches
- [ ] Error propagation works correctly
- [ ] Optimistic updates work (if applicable)
- [ ] E2E flow tested
```

### Bugfix Tasks

```markdown
- [ ] Root cause identified across all layers
- [ ] Fix applied in correct layer(s)
- [ ] No regression in other layers
- [ ] E2E test added to prevent recurrence
- [ ] Error handling improved where gap existed
- [ ] Related features still work correctly
```

### Refactor Tasks

```markdown
- [ ] API contracts unchanged
- [ ] Frontend behavior unchanged
- [ ] Database schema unchanged (or migrated safely)
- [ ] All E2E tests pass
- [ ] Performance maintained or improved
- [ ] Code organization improved
```

---

## Best Practices Checklist

### Contract Definition

- [ ] Types shared between frontend and backend
- [ ] API response structure documented
- [ ] Error codes consistent across endpoints
- [ ] Validation rules match frontend and backend

### Data Flow

- [ ] Frontend uses `useQuery` for reactive data
- [ ] Mutations return updated data
- [ ] Optimistic updates for better UX
- [ ] Loading states at each async boundary
- [ ] Error states propagate user-friendly messages

### Error Handling

- [ ] Backend returns structured errors
- [ ] Frontend displays appropriate error UI
- [ ] Validation errors show per-field feedback
- [ ] Network errors handled gracefully
- [ ] Retry logic for transient failures

### Performance

- [ ] Database queries use indexes
- [ ] Frontend doesn't over-fetch data
- [ ] Pagination for large lists
- [ ] Code splitting for routes
- [ ] Preloading for navigation

---

## Testing Requirements

### E2E Tests (Playwright)

```markdown
**Happy Path:**

- [ ] User can complete full flow
- [ ] Data persists correctly
- [ ] UI updates after actions

**Error Scenarios:**

- [ ] Validation errors display correctly
- [ ] API errors show user-friendly message
- [ ] Network failure handled gracefully

**Edge Cases:**

- [ ] Empty state (no data)
- [ ] Boundary values
- [ ] Concurrent operations
```

### Integration Tests

```markdown
- [ ] API returns expected data format
- [ ] Frontend parses API response correctly
- [ ] Mutations trigger query updates
- [ ] Auth flows work end-to-end
```

### Cross-Browser

```markdown
- [ ] Chrome (desktop + mobile)
- [ ] Firefox (desktop)
- [ ] Safari (desktop + iOS)
```

---

## Quality Gates

Must pass before task completion:

| Gate       | Requirement                                 |
| ---------- | ------------------------------------------- |
| **Schema** | Database deploys without errors             |
| **API**    | Backend functions work correctly            |
| **Types**  | No TypeScript errors in frontend or backend |
| **Build**  | `npm run build` succeeds                    |
| **E2E**    | Playwright tests pass                       |
| **Visual** | UI matches expectations                     |

---

## Common Patterns

### Well-Defined Feature Task

```markdown
**Title**: Add Task Comments Feature

**Description**: Allow users to add and view comments on tasks.

**Acceptance Criteria**:

Database:

- [ ] `comments` table with taskId, authorId, content, timestamps
- [ ] Index `by_task` for efficient loading

Backend:

- [ ] `comments.create` mutation with validation
- [ ] `comments.listByTask` query with pagination
- [ ] Auth required for both operations
- [ ] Returns structured error for invalid input

Frontend:

- [ ] Comment list component in task detail page
- [ ] Comment input form with validation
- [ ] Loading skeleton while comments load
- [ ] Empty state "No comments yet"
- [ ] Optimistic update on submit

Integration:

- [ ] E2E: Add comment and verify it appears
- [ ] E2E: Comments persist on page refresh
- [ ] E2E: Validation error displays correctly
```

### Well-Defined Bugfix Task

```markdown
**Title**: Fix Task Status Not Updating in UI

**Description**: After changing task status, the UI doesn't reflect the change until page refresh.

**Acceptance Criteria**:

Investigation:

- [ ] Root cause: Mutation not returning updated task

Backend Fix:

- [ ] `tasks.updateStatus` returns updated task document

Frontend Fix:

- [ ] Mutation invalidates task query (if not reactive)
- [ ] Optimistic update shows change immediately

Integration:

- [ ] E2E: Change status and verify immediate UI update
- [ ] E2E: Status persists on page refresh
- [ ] E2E: Multiple status changes work correctly
```

### Well-Defined Refactor Task

```markdown
**Title**: Extract Task Form into Reusable Component

**Description**: Task creation and editing use separate forms with duplicated logic.

**Acceptance Criteria**:

Frontend:

- [ ] Create `TaskForm` component with mode prop
- [ ] Supports "create" and "edit" modes
- [ ] Shares validation logic
- [ ] Uses same shadcn/ui components

Backend:

- [ ] No changes required (API unchanged)

Integration:

- [ ] E2E: Create task still works
- [ ] E2E: Edit task still works
- [ ] Visual: Forms look identical to before
```

---

## Cross-Layer Coordination

### Adding a New Feature

```
1. Database: Define schema + indexes
2. Backend: Create query/mutation functions
3. Frontend: Build UI components
4. Integration: Wire up with useQuery/useMutation
5. Testing: Add E2E tests
```

### Debugging Cross-Layer Issues

```
1. Check browser console for frontend errors
2. Check Convex dashboard for backend errors
3. Verify data in Convex dashboard
4. Check network tab for request/response
5. Add logging at each layer boundary
```

### Performance Optimization

```
1. Check Convex dashboard for slow queries
2. Add/optimize indexes for query patterns
3. Implement pagination for large lists
4. Add loading states to improve perceived performance
5. Consider optimistic updates for faster feedback
```

---

## Decomposition Guidelines

When a fullstack task is too large, decompose into:

| Subtask          | Domain   | Dependencies |
| ---------------- | -------- | ------------ |
| Schema + indexes | database | None         |
| API functions    | backend  | Database     |
| UI components    | frontend | Backend      |
| E2E tests        | frontend | All above    |

**Example Decomposition:**

```
FT-010: Add Task Comments Feature
├── FT-010a: [database] Create comments schema
├── FT-010b: [backend] Create comments API (depends: FT-010a)
├── FT-010c: [frontend] Build comments UI (depends: FT-010b)
└── FT-010d: [frontend] Add E2E tests (depends: FT-010c)
```
