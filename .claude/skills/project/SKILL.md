---
name: project
description: Project conventions and cross-cutting patterns. Use when implementing features that span multiple domains or when establishing new patterns.
---

# Project Conventions Skill

## Tech Stack Overview

| Layer      | Technology                                         |
| ---------- | -------------------------------------------------- |
| Frontend   | TanStack Start, React 19, TypeScript, Tailwind CSS |
| Routing    | TanStack React Router                              |
| State      | Convex React hooks (useQuery, useMutation)         |
| Backend    | Convex (serverless functions)                      |
| Database   | Convex (built-in)                                  |
| Auth       | Convex Auth                                        |
| Deployment | Cloudflare                                         |
| Testing    | Playwright (E2E), Vitest (unit)                    |

## Directory Structure

```
src/                        # TanStack Start frontend
├── components/             # React components by feature
│   └── {feature}/
│       └── {Component}.tsx
├── hooks/                  # Custom React hooks (non-Convex)
├── routes/                 # TanStack Router route files
│   ├── __root.tsx          # Root layout
│   ├── index.tsx           # Home page
│   ├── login.tsx           # Login page
│   └── projects.$projectId.tasks.$taskId.tsx  # Nested routes
├── lib/                    # Utilities
└── types/                  # TypeScript types
convex/                     # Convex backend
├── _generated/             # Auto-generated types
├── schema.ts               # Database schema
├── auth.ts                 # Auth configuration
└── {resource}.ts           # Mutations/queries per resource
tests/
├── e2e/                    # Playwright E2E tests
└── unit/                   # Vitest unit tests
```

## Naming Conventions

### Files and Directories

| Type             | Convention               | Example                               |
| ---------------- | ------------------------ | ------------------------------------- |
| React components | PascalCase               | `TaskList.tsx`                        |
| Route files      | dot notation with params | `projects.$projectId.tasks.index.tsx` |
| Hooks            | camelCase with `use`     | `useTaskFilter.ts`                    |
| Convex functions | lowercase, resource name | `tasks.ts`, `projects.ts`             |
| Test files       | `.spec.ts` or `.test.ts` | `tasks.spec.ts`                       |

### Code

| Type             | Convention              | Example                         |
| ---------------- | ----------------------- | ------------------------------- |
| React components | PascalCase              | `TaskList`                      |
| Hooks            | camelCase, `use` prefix | `useTaskFilter`                 |
| Convex functions | camelCase               | `getByCustomId`, `updateStatus` |
| Types/Interfaces | PascalCase              | `Task`, `TaskFormData`          |
| Constants        | UPPER_SNAKE_CASE        | `TYPE_PREFIXES`                 |
| Database tables  | lowercase, plural       | `tasks`, `projects`             |

## Import Conventions

### Frontend (TanStack Start/React)

```typescript
// Use @/ alias for src directory
import { TaskList } from '@/components/tasks/TaskList'
import { useTaskFilter } from '@/hooks/useTaskFilter'
import type { Task } from '@/types/task'

// TanStack Router imports
import { createFileRoute, useNavigate, useParams } from '@tanstack/react-router'

// Convex imports
import { useQuery, useMutation } from 'convex/react'
import { api } from '../../convex/_generated/api'
```

### Convex Backend

```typescript
// Server imports
import { query, mutation, action } from './_generated/server'
import { v } from 'convex/values'
import { api } from './_generated/api'

// Data model types
import { Id, Doc } from './_generated/dataModel'
```

## Routing Conventions (TanStack Router)

### Route File Naming

TanStack Router uses file-based routing with dot notation for nested routes:

| Route Path                           | File Name                               |
| ------------------------------------ | --------------------------------------- |
| `/`                                  | `index.tsx`                             |
| `/login`                             | `login.tsx`                             |
| `/projects`                          | `projects.index.tsx`                    |
| `/projects/:projectId`               | `projects.$projectId.tsx`               |
| `/projects/:projectId/tasks`         | `projects.$projectId.tasks.index.tsx`   |
| `/projects/:projectId/tasks/:taskId` | `projects.$projectId.tasks.$taskId.tsx` |
| `/projects/:projectId/dashboard`     | `projects.$projectId.dashboard.tsx`     |

### Route File Structure

```typescript
// src/routes/projects.$projectId.tasks.index.tsx
import { createFileRoute } from '@tanstack/react-router'
import { useQuery } from 'convex/react'
import { api } from '../../../convex/_generated/api'

export const Route = createFileRoute('/projects/$projectId/tasks/')({
  component: TasksPage,
})

function TasksPage() {
  const { projectId } = Route.useParams()
  const tasks = useQuery(api.tasks.list, { projectId })

  if (!tasks) return <div>Loading...</div>

  return <div>{/* Component content */}</div>
}
```

### Navigation

```typescript
import { useNavigate } from '@tanstack/react-router'

function MyComponent() {
  const navigate = useNavigate()

  // Navigate to a route
  const handleClick = () => {
    navigate({ to: '/projects/$projectId/tasks', params: { projectId: '123' } })
  }
}
```

### Layout Routes

Root layout (`__root.tsx`) wraps all routes:

```typescript
// src/routes/__root.tsx
import { createRootRoute, Outlet } from '@tanstack/react-router'

export const Route = createRootRoute({
  component: RootLayout,
})

function RootLayout() {
  return (
    <>
      <Outlet />
    </>
  )
}
```

## API Conventions (Convex Style)

### Function Naming

```typescript
// Queries (read operations)
api.tasks.list // List all
api.tasks.get // Get by ID
api.tasks.getByCustomId // Get by custom field

// Mutations (write operations)
api.tasks.create // Create new
api.tasks.update // Update existing
api.tasks.remove // Delete (soft or hard)
api.tasks.updateStatus // Specific update

// Actions (external calls)
api.tasks.uploadImage // External API call
```

### Response Patterns

```typescript
// Query returns data directly or undefined (loading) or null (not found)
const task = useQuery(api.tasks.get, { id: taskId })
// task: Task | undefined | null

// Mutation returns the created/updated ID or document
const taskId = await createTask({ title: 'New task' })

// List returns array (empty if no results)
const tasks = useQuery(api.tasks.list, { projectId })
// tasks: Task[] | undefined
```

## Error Handling

### Frontend

```tsx
function TaskDetail({ taskId }: { taskId: string }) {
  const task = useQuery(api.tasks.get, { id: taskId })

  // Loading state (Convex returns undefined)
  if (task === undefined) {
    return <TaskSkeleton />
  }

  // Not found (Convex returns null)
  if (task === null) {
    return <NotFound message="Task not found" />
  }

  // Success
  return <TaskContent task={task} />
}
```

### Backend

```typescript
export const update = mutation({
  args: { id: v.id('tasks'), status: v.string() },
  handler: async (ctx, args) => {
    const task = await ctx.db.get(args.id)

    if (!task) {
      throw new Error('Task not found')
    }

    // Business rule validation
    if (task.status === 'completed' && args.status === 'pending') {
      throw new Error('Cannot revert completed task')
    }

    await ctx.db.patch(args.id, { status: args.status })
  },
})
```

## Authentication Flow

1. User clicks "Sign in" button
2. Convex Auth redirects to provider (Google, GitHub)
3. User authenticates with provider
4. Convex Auth creates/updates user in `users` table
5. Frontend receives auth state via `useConvexAuth()`
6. Protected functions check auth with `getAuthUserId(ctx)`

```tsx
// Frontend - check auth
import { useConvexAuth } from 'convex/react'

function App() {
  const { isAuthenticated, isLoading } = useConvexAuth()

  if (isLoading) return <LoadingSpinner />
  if (!isAuthenticated) return <LoginPage />
  return <Dashboard />
}

// Backend - protected function
import { getAuthUserId } from '@convex-dev/auth/server'

export const myTasks = query({
  handler: async (ctx) => {
    const userId = await getAuthUserId(ctx)
    if (!userId) throw new Error('Not authenticated')
    // ... query user's tasks
  },
})
```

## Testing Strategy

### Coverage Targets

| Type       | Target         | Framework  |
| ---------- | -------------- | ---------- |
| Unit tests | 80%            | Vitest     |
| E2E tests  | Critical paths | Playwright |

### Test Priority

1. **Critical path** - Auth, core CRUD, main workflows
2. **Error handling** - Error states, validation
3. **Edge cases** - Empty states, boundaries

### E2E Test Structure

```typescript
// tests/e2e/tasks.spec.ts
import { test, expect } from '@playwright/test'

test.describe('Tasks', () => {
  test('can create a new task', async ({ page }) => {
    await page.goto('/tasks')
    await page.click('button:has-text("New Task")')
    await page.fill('input[name="title"]', 'Test Task')
    await page.click('button[type="submit"]')
    await expect(page.locator('text=Test Task')).toBeVisible()
  })
})
```

### Unit Test Structure

```typescript
// tests/unit/taskId.test.ts
import { describe, it, expect } from 'vitest'
import { formatTaskId } from '../convex/lib/taskId'

describe('formatTaskId', () => {
  it('formats feature IDs correctly', () => {
    expect(formatTaskId('feature', 1)).toBe('FT-001')
    expect(formatTaskId('feature', 100)).toBe('FT-100')
  })
})
```

## Git Workflow

### Branch Naming

```
feature/{description}
bugfix/{description}
refactor/{description}
```

### Commit Messages

```
feat: add task creation form
fix: correct status transition validation
refactor: extract task ID generation
test: add E2E tests for task list
docs: update API documentation
```

## Environment Variables

### Local Development (.env.local)

```bash
# Convex
CONVEX_DEPLOYMENT=dev:your-deployment
VITE_CONVEX_URL=https://your-deployment.convex.cloud

# Auth Providers (optional)
AUTH_GITHUB_ID=your-github-id
AUTH_GITHUB_SECRET=your-github-secret
AUTH_GOOGLE_ID=your-google-id
AUTH_GOOGLE_SECRET=your-google-secret
```

### Production

Set in Cloudflare Pages environment variables.

## Dependencies

### Adding New Dependencies

1. Check if similar functionality exists in Convex or existing deps
2. Evaluate bundle size (frontend)
3. Check maintenance status
4. Add to `package.json`
5. Document why added in commit message

### Key Dependencies

**Frontend:**

- `@tanstack/react-start` - Full-stack React framework
- `@tanstack/react-router` - File-based routing
- `convex` - Convex client
- `react` - React 19
- `react-dom` - React DOM
- `tailwindcss` - Styling
- `@dnd-kit/*` - Drag and drop for Kanban

**Backend:**

- `convex` - Backend framework
- `@convex-dev/auth` - Authentication

**Testing:**

- `@playwright/test` - E2E testing
- `vitest` - Unit testing
