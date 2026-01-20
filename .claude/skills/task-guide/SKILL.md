---
name: task-guide
description: Domain-specific task criteria and best practices. Use when creating tasks via /task-init to generate appropriate acceptance criteria, testing requirements, and quality gates for each domain.
---

# Task Guide Skill

Provides domain-specific criteria templates and best practices for creating well-defined tasks.

## Contents

- [When to Use](#when-to-use)
- [Domain References](#domain-references)
- [Task Quality Checklist](#task-quality-checklist)
- [Criteria Templates](#criteria-templates)

---

## When to Use

Use this guide when:

- Creating tasks via `/task-init`
- Reviewing task definitions for completeness
- Generating acceptance criteria for specific domains
- Ensuring tasks follow domain best practices

---

## Domain References

| Domain        | Reference                                        | Focus Areas                                  |
| ------------- | ------------------------------------------------ | -------------------------------------------- |
| **Frontend**  | [reference/frontend.md](reference/frontend.md)   | UI, accessibility, responsiveness, state     |
| **Backend**   | [reference/backend.md](reference/backend.md)     | API design, validation, auth, error handling |
| **Database**  | [reference/database.md](reference/database.md)   | Schema, migrations, indexes, data integrity  |
| **Fullstack** | [reference/fullstack.md](reference/fullstack.md) | Integration, E2E flow, cross-layer concerns  |

---

## Task Quality Checklist

Every task should have:

| Requirement                 | Description                                |
| --------------------------- | ------------------------------------------ |
| **Clear title**             | Action verb + specific target (< 60 chars) |
| **Description**             | What, why, and any context needed          |
| **Acceptance criteria**     | 3-5 testable criteria from domain guide    |
| **Domain assigned**         | frontend, backend, database, or fullstack  |
| **Complexity estimated**    | simple, medium, or complex                 |
| **Dependencies identified** | List blocking tasks or "none"              |

---

## Criteria Templates

### Feature Tasks (FT-xxx)

```
- [ ] [Feature] is visible and accessible
- [ ] [Feature] functions as specified
- [ ] Error states are handled gracefully
- [ ] Loading states shown appropriately
- [ ] [Domain-specific criteria from reference]
```

### Bugfix Tasks (BF-xxx)

```
- [ ] Bug no longer occurs under [condition]
- [ ] Root cause identified and addressed
- [ ] No regression in related functionality
- [ ] [Domain-specific criteria from reference]
```

### Refactor Tasks (RF-xxx)

```
- [ ] Existing tests continue to pass
- [ ] Behavior remains unchanged
- [ ] [Specific improvement] achieved
- [ ] Code is more maintainable/readable
- [ ] [Domain-specific criteria from reference]
```

---

## Usage with /task-init

When `/task-init` identifies a domain, load the corresponding reference file to:

1. **Suggest acceptance criteria** based on domain patterns
2. **Add required quality gates** (e.g., "API validates input" for backend)
3. **Include testing requirements** (e.g., "E2E test covers happy path" for frontend)
4. **Apply best practices** (e.g., "Migration is reversible" for database)

Example flow:

```
/task-init Add user profile API endpoint

→ Domain detected: backend
→ Load reference/backend.md
→ Suggest criteria:
  - [ ] Endpoint follows REST conventions
  - [ ] Input validation with proper error messages
  - [ ] Authentication required
  - [ ] Response matches API contract
  - [ ] Error cases return appropriate status codes
```
