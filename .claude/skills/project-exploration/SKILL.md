---
name: project-exploration
description: Explores codebase for project initialization. Detects tech stack, existing features, code patterns, and project structure. Use when initializing projects, analyzing codebases, or detecting what exists before planning.
context: fork
agent: project-scout
---

# Project Exploration

Comprehensive codebase analysis for project initialization.

---

## Exploration Checklist

### 1. Project Metadata

- [ ] Read `package.json`: name, description, version, scripts, dependencies
- [ ] Read `README.md` if exists: purpose, setup instructions
- [ ] Check `.env.example`: required environment variables

### 2. Tech Stack Detection

| Layer | Check For | Look In |
|-------|-----------|---------|
| Frontend | react, vue, svelte, angular, solid | dependencies |
| Meta-framework | next, nuxt, sveltekit, remix | dependencies |
| Backend | convex, express, fastify, hono, fastapi | dependencies |
| Database | convex, prisma, drizzle, mongoose, pg | dependencies |
| Styling | tailwindcss, styled-components, emotion, sass | devDependencies |
| Auth | @clerk/clerk-react, next-auth, @auth0/auth0-react | dependencies |
| Router | @tanstack/react-router, react-router-dom | dependencies |

### 3. Project Structure

Map key directories:
- `src/components/` → count and list key components
- `src/routes/` or `src/pages/` → list routes
- `src/hooks/` → list custom hooks
- `src/lib/` or `src/utils/` → list utilities
- `convex/` → list functions and schema
- `tests/` or `__tests__/` → identify test setup

### 4. Feature Detection

Mark each as: **✓** implemented, **○** partial, **✗** not found

| Category | Features to Check |
|----------|-------------------|
| **Auth** | login, signup, logout, password reset, sessions |
| **Users** | profiles (view, edit), avatar, settings |
| **Authorization** | roles, permissions, guards |
| **Data** | CRUD for main entities (identify what entities exist) |
| **Search** | search, filtering, pagination |
| **Files** | file upload, storage, download |
| **Notifications** | email, push, in-app |
| **Admin** | admin dashboard, user management |
| **Infrastructure** | error handling, loading states, tests, CI/CD |

### 5. Code Patterns

Analyze coding conventions:
- **Component style**: functional vs class, hooks usage
- **State management**: Context, Redux, Zustand, Convex reactivity
- **Data fetching**: useQuery, useSWR, Convex useQuery
- **Error handling**: try/catch, error boundaries, toast notifications
- **File naming**: camelCase, kebab-case, PascalCase
- **Folder structure**: feature-based, layer-based, hybrid

---

## Output Format

Return a structured markdown report:

```markdown
## Codebase Analysis

**Project**: [NAME from package.json or directory]
**Status**: [Existing project | New/empty project]

### Tech Stack Detected

| Layer | Technology |
|-------|------------|
| Frontend | [React + TypeScript] |
| Backend | [Convex] |
| Database | [Convex] |
| Styling | [Tailwind CSS] |
| Auth | [Clerk / None detected] |
| Router | [TanStack Router] |

### Project Structure

| Directory | Count | Purpose |
|-----------|-------|---------|
| src/components/ | [N] | UI components |
| src/routes/ | [N] | Page routes |
| convex/ | [N] | Backend functions |

### Feature Status

| Feature | Status | Notes |
|---------|--------|-------|
| Authentication | ✓ / ○ / ✗ | [Details] |
| User profiles | ✓ / ○ / ✗ | [Details] |
| [Entity] CRUD | ✓ / ○ / ✗ | [Details] |
| Search | ✓ / ○ / ✗ | [Details] |

### Code Patterns

- **Components**: [Functional with hooks]
- **State**: [Convex reactivity]
- **Styling**: [Tailwind utility classes]
- **Naming**: [camelCase files, PascalCase components]

### Existing Entities

List main data entities found:
- Entity 1: [location, purpose]
- Entity 2: [location, purpose]

### Recommendations

Based on findings:
- Missing infrastructure: [tests, CI, docs]
- Incomplete features: [list]
- Potential improvements: [list]

### Questions

- [Anything needing clarification from user]
```

---

## Process

1. **Start with package.json** - Get name, dependencies, scripts
2. **Map structure with Glob** - Find directories and count files
3. **Detect features with Grep** - Search for patterns
4. **Read key files** - Examine entry points, schemas, configs
5. **Synthesize findings** - Build structured report
