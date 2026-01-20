---
name: spec
description: Project specification and feature backlog generation patterns. Use when creating new project specs, generating feature lists, or planning project roadmaps.
---

# Project Specification Skill

## Overview

This skill guides the generation of comprehensive project specifications and feature backlogs through interactive discovery.

## Spec Generation Process

### Phase 1: Project Discovery

Gather foundational information:

1. **Project Identity**
   - Name (required)
   - Description (1-2 sentences)
   - Type (web app, mobile, API, etc.)

2. **Tech Stack**
   - Frontend framework
   - Backend framework
   - Database
   - Key libraries/tools

3. **Target Users**
   - Primary user types
   - User roles/permissions
   - Expected scale

4. **Design Theme**
   - Color scheme (custom CSS, default, or generated)
   - Dark mode support
   - Typography preferences
   - Border radius/spacing

### Phase 2: Feature Discovery

Understand what needs to be built:

1. **Core User Journeys**
   - Primary flows (3-5)
   - Critical paths
   - Edge cases

2. **Integrations**
   - External services (payment, auth, email)
   - Third-party APIs
   - Data sources

3. **Constraints**
   - Security requirements
   - Compliance needs
   - Performance targets
   - Platform requirements

### Phase 3: Feature Generation

Transform discoveries into actionable features:

1. **Feature Categories**
   - Foundation (auth, database, API structure)
   - Core (main business logic)
   - Supporting (admin, settings, profiles)
   - Enhancement (analytics, notifications)

2. **Feature Attributes**
   - ID (unique identifier)
   - Title (concise name)
   - Description (detailed explanation)
   - Domain (frontend/backend/database/fullstack)
   - Priority (1 = highest)
   - Complexity (simple/medium/complex)
   - Dependencies (prerequisite feature IDs)
   - Acceptance Criteria (testable conditions)
   - Tags (for filtering/grouping)

### Phase 4: Prioritization

Order features by implementation sequence:

**Priority Factors**:

1. **Dependencies** - Prerequisites must come first
2. **Foundation** - Core infrastructure before features
3. **Business Value** - Critical features before nice-to-haves
4. **Complexity** - Balance simple wins with complex work
5. **Risk** - Address unknowns early

**Priority Tiers**:

- P1: Foundation (must have for anything to work)
- P2: Core (main value proposition)
- P3: Essential (expected features)
- P4: Enhancement (improves experience)
- P5: Nice-to-have (future consideration)

## Feature Schema

```json
{
  "id": 1,
  "title": "Feature Title",
  "description": "Detailed description of what this feature does",
  "domain": "frontend | backend | database | fullstack",
  "priority": 1,
  "complexity": "simple | medium | complex",
  "dependencies": [
    /* IDs of prerequisite features */
  ],
  "status": "pending | in_progress | completed | blocked",
  "acceptance_criteria": [
    "User can do X",
    "System validates Y",
    "Data persists to Z"
  ],
  "tags": ["category", "area", "type"],
  "estimated_effort": "hours | days | weeks",
  "notes": "Additional context or considerations"
}
```

## Domain Classification

| Domain      | Includes                                          |
| ----------- | ------------------------------------------------- |
| `frontend`  | UI components, pages, client-side logic, styling  |
| `backend`   | API endpoints, services, business logic, auth     |
| `database`  | Schema design, migrations, queries, data modeling |
| `fullstack` | Features spanning multiple domains (e.g., auth)   |

## Complexity Guidelines

| Complexity | Characteristics                                             |
| ---------- | ----------------------------------------------------------- |
| `simple`   | Single file, well-defined, clear patterns exist             |
| `medium`   | Multiple files, some decisions required, moderate scope     |
| `complex`  | Architectural decisions, multiple integrations, large scope |

## Common Feature Patterns

### Authentication Feature

```json
{
  "id": 1,
  "title": "User Authentication",
  "description": "Email/password authentication with JWT tokens",
  "domain": "fullstack",
  "priority": 1,
  "complexity": "medium",
  "dependencies": [],
  "acceptance_criteria": [
    "User can register with email and password",
    "User can login and receive JWT token",
    "Protected routes require valid token",
    "Passwords are hashed securely",
    "Token expires and can be refreshed"
  ],
  "tags": ["auth", "security", "core"]
}
```

### CRUD Feature

```json
{
  "id": 5,
  "title": "Product Management",
  "description": "CRUD operations for products with categories",
  "domain": "fullstack",
  "priority": 2,
  "complexity": "medium",
  "dependencies": [1, 2],
  "acceptance_criteria": [
    "Admin can create new products",
    "Products have name, description, price, category",
    "Products can be updated and deleted",
    "Products are listed with pagination",
    "Products can be filtered by category"
  ],
  "tags": ["products", "admin", "crud"]
}
```

## Output Files

### features.json

Main feature backlog with full schema for each feature.

Project section includes:

```json
{
  "project": {
    "name": "project-name",
    "description": "Project description",
    "tech_stack": { "frontend": "...", "backend": "...", "database": "..." },
    "design": {
      "theme": "custom | default | generated | skipped",
      "dark_mode": true,
      "css_path": "src/styles/theme.css"
    },
    "created_at": "ISO timestamp",
    "updated_at": "ISO timestamp"
  }
}
```

### project-spec.md

Human-readable specification including:

- Project overview
- Tech stack summary
- Design system summary
- Feature list (by priority)
- Dependency graph
- Timeline considerations

### src/styles/theme.css (if configured)

CSS variables for design system:

- Semantic color tokens (oklch format)
- Typography variables
- Spacing and radius
- Shadow definitions
- Dark mode overrides
- Tailwind v4 `@theme inline` mappings

## Interactive Question Guidelines

When gathering information:

1. **Start broad, then narrow**
   - Begin with project type and goals
   - Drill into specifics based on answers

2. **Offer common options**
   - Provide sensible defaults
   - Allow custom input when needed

3. **Validate understanding**
   - Summarize back to user
   - Confirm before generating

4. **Be iterative**
   - Generate initial list
   - Allow additions/removals
   - Refine priorities together
