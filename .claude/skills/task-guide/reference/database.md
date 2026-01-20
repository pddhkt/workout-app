# Database Task Criteria

Domain-specific acceptance criteria, best practices, and quality gates for database tasks (Convex schema).

## Contents

- [Acceptance Criteria Templates](#acceptance-criteria-templates)
- [Best Practices Checklist](#best-practices-checklist)
- [Testing Requirements](#testing-requirements)
- [Quality Gates](#quality-gates)
- [Common Patterns](#common-patterns)

---

## Acceptance Criteria Templates

### Feature Tasks (New Table/Field)

```markdown
- [ ] Schema defined in `convex/schema.ts`
- [ ] Fields use appropriate `v` validators
- [ ] Required timestamps: `createdAt`, `updatedAt`
- [ ] Foreign keys use `v.id("tableName")`
- [ ] Indexes defined for all query patterns
- [ ] Optional fields marked with `v.optional()`
- [ ] Enums use `v.union(v.literal(...), ...)`
- [ ] Schema change is additive (non-breaking)
```

### Bugfix Tasks

```markdown
- [ ] Data integrity issue identified and fixed
- [ ] No data loss during fix
- [ ] Existing records remain valid
- [ ] Queries return correct results
- [ ] Indexes updated if query pattern changed
- [ ] Migration script tested on sample data
```

### Refactor Tasks

```markdown
- [ ] All existing data preserved
- [ ] Queries still performant (use indexes)
- [ ] Schema change is reversible
- [ ] No breaking changes for existing code
- [ ] Types correctly generated (`npx convex dev`)
- [ ] Related functions updated if needed
```

---

## Best Practices Checklist

### Schema Design

- [ ] Table names: lowercase, plural (`tasks`, `projects`)
- [ ] Field names: camelCase (`createdAt`, `projectId`)
- [ ] Every table has `createdAt: v.number()`
- [ ] Every table has `updatedAt: v.number()`
- [ ] Relationships use `v.id("tableName")`
- [ ] No `v.any()` - use specific types
- [ ] Optional fields have sensible defaults in code

### Index Design

- [ ] Index exists for every query pattern
- [ ] Compound indexes: equality fields first, then range
- [ ] Index name describes the query: `by_project`, `by_status_and_date`
- [ ] No redundant indexes (subset of another)
- [ ] Search indexes for text search (if needed)

### Data Integrity

- [ ] Required fields are not optional
- [ ] Enums constrain valid values
- [ ] IDs reference existing tables
- [ ] Cascading deletes handled (or soft delete used)
- [ ] No orphaned records possible

### Schema Evolution

- [ ] Adding fields: Make optional or provide default
- [ ] Removing fields: Remove from schema after code cleanup
- [ ] Renaming fields: Add new, migrate, remove old
- [ ] Changing types: Migration script required

---

## Testing Requirements

### Schema Validation

```markdown
- [ ] Schema deploys without errors (`npx convex dev`)
- [ ] Types generate correctly in `_generated/`
- [ ] Existing queries still compile
- [ ] New queries use defined indexes
```

### Data Testing

```markdown
- [ ] Insert sample data successfully
- [ ] Query returns expected results
- [ ] Update preserves other fields
- [ ] Delete handles relationships
- [ ] Edge cases: null, empty string, max values
```

### Index Testing

```markdown
- [ ] Query uses index (check Convex dashboard)
- [ ] No table scans for common queries
- [ ] Index covers all fields in query filter
```

---

## Quality Gates

Must pass before task completion:

| Gate        | Requirement                             |
| ----------- | --------------------------------------- |
| **Schema**  | `npx convex dev` deploys without errors |
| **Types**   | Generated types compile correctly       |
| **Indexes** | All queries use indexes                 |
| **Data**    | Existing data remains valid             |
| **Queries** | Related queries return correct results  |

---

## Common Patterns

### Well-Defined Feature Task (New Table)

```markdown
**Title**: Add Comments Table for Tasks

**Acceptance Criteria**:

- [ ] Table `comments` defined in schema
- [ ] Fields:
  - `taskId: v.id("tasks")` (required)
  - `authorId: v.id("users")` (required)
  - `content: v.string()` (required)
  - `createdAt: v.number()` (required)
  - `updatedAt: v.number()` (required)
- [ ] Indexes:
  - `by_task`: for loading comments by task
  - `by_author`: for loading comments by user
- [ ] Types generated correctly

**Testing**:

- [ ] Insert comment linked to task
- [ ] Query comments by taskId returns correct results
- [ ] Query uses `by_task` index
```

### Well-Defined Feature Task (New Field)

```markdown
**Title**: Add Priority Field to Tasks

**Acceptance Criteria**:

- [ ] Field `priority: v.optional(v.number())` added
- [ ] Values: 1 (critical), 2 (high), 3 (medium), 4 (low)
- [ ] Default: 3 (medium) in application code
- [ ] Index `by_project_and_priority` for sorted queries
- [ ] Existing tasks unaffected (field is optional)

**Testing**:

- [ ] Existing tasks load without errors
- [ ] New tasks can specify priority
- [ ] Query by project sorted by priority works
```

### Well-Defined Bugfix Task

```markdown
**Title**: Fix Missing Index for Task Status Query

**Acceptance Criteria**:

- [ ] Add index `by_project_and_status` on tasks table
- [ ] Index: `.index("by_project_and_status", ["projectId", "status"])`
- [ ] Query `tasks.listByStatus` updated to use index
- [ ] Query performance improved (verify in dashboard)

**Testing**:

- [ ] Query uses new index (check Convex dashboard)
- [ ] Results unchanged from before
- [ ] Query time reduced
```

### Well-Defined Refactor Task

```markdown
**Title**: Migrate Task Type from String to Enum

**Acceptance Criteria**:

- [ ] Type changed from `v.string()` to `v.union(v.literal("feature"), v.literal("bugfix"), v.literal("refactor"))`
- [ ] Migration: Verify all existing values are valid
- [ ] Invalid values: None expected (or list exceptions)
- [ ] Related functions updated to use enum type
- [ ] All queries return correct results

**Testing**:

- [ ] Existing tasks load correctly
- [ ] Creating task with invalid type rejected
- [ ] Types correctly inferred in frontend
```

---

## Convex-Specific Criteria

### Validators

```markdown
- [ ] Use `v.string()` for text
- [ ] Use `v.number()` for integers and timestamps
- [ ] Use `v.float64()` for decimals
- [ ] Use `v.boolean()` for flags
- [ ] Use `v.id("table")` for references
- [ ] Use `v.array(v.xxx())` for lists
- [ ] Use `v.object({...})` for nested objects
- [ ] Use `v.union(...)` for enums
- [ ] Use `v.optional(...)` for nullable fields
```

### Index Patterns

```markdown
# Single field index

.index("by_project", ["projectId"])

# Compound index (equality + range)

.index("by_project_and_date", ["projectId", "createdAt"])

# Compound index (multiple equality)

.index("by_project_and_status", ["projectId", "status"])

# Search index (text search)

.searchIndex("search_title", { searchField: "title" })
```

### Migration Safety

```markdown
**Safe changes (no migration needed):**

- Add optional field
- Add new index
- Add new table

**Requires migration:**

- Remove field (remove from code first)
- Rename field
- Change field type
- Remove index (update queries first)
```
