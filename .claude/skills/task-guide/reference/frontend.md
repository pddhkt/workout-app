# Frontend Task Criteria

Domain-specific acceptance criteria, best practices, and quality gates for frontend tasks.

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
- [ ] Component renders correctly in all states (loading, error, success, empty)
- [ ] Uses shadcn/ui components (no custom buttons, inputs, dialogs)
- [ ] Responsive design works on mobile (320px) and desktop (1280px+)
- [ ] Keyboard navigation works (Tab, Enter, Escape)
- [ ] Loading states shown during async operations
- [ ] Error states display user-friendly messages
- [ ] Form validation provides immediate feedback
- [ ] Theme-aware styling (works in light/dark mode)
```

### Bugfix Tasks

```markdown
- [ ] Bug no longer reproduces under original conditions
- [ ] Root cause identified and documented
- [ ] No visual regression in related components
- [ ] Works across supported browsers (Chrome, Firefox, Safari)
- [ ] No console errors or warnings introduced
- [ ] Accessibility not degraded (screen reader, keyboard)
- [ ] Related user flows still work correctly
```

### Refactor Tasks

```markdown
- [ ] Existing visual appearance unchanged
- [ ] All E2E tests continue to pass
- [ ] No new console errors or warnings
- [ ] Performance metrics maintained or improved
- [ ] Code follows project conventions (TypeScript, Tailwind)
- [ ] Hardcoded colors replaced with theme variables
- [ ] Component uses composition pattern
```

---

## Best Practices Checklist

### Component Development

- [ ] Uses `Item` component for lists and cards (prefer over custom)
- [ ] Uses `cn()` helper for conditional Tailwind classes
- [ ] Props interface defined with TypeScript
- [ ] No `any` types in component code
- [ ] Uses absolute imports (`@/components/...`)
- [ ] Component is in correct directory structure

### Accessibility (a11y)

- [ ] Interactive elements are keyboard accessible
- [ ] Focus states are visible
- [ ] Color contrast meets WCAG AA (4.5:1 for text)
- [ ] Images have alt text
- [ ] Form inputs have labels
- [ ] Error messages are announced to screen readers
- [ ] Skip links for navigation (if applicable)

### Responsive Design

- [ ] Mobile-first approach (base styles for mobile)
- [ ] Breakpoints: `sm:` (640px), `md:` (768px), `lg:` (1024px), `xl:` (1280px)
- [ ] Touch targets minimum 44x44px on mobile
- [ ] Text remains readable at all sizes
- [ ] No horizontal scroll on mobile
- [ ] Images scale appropriately

### State Management

- [ ] Loading state: Show skeleton or spinner
- [ ] Error state: Show user-friendly message with retry option
- [ ] Empty state: Show helpful message with action
- [ ] Success state: Show confirmation (toast or inline)
- [ ] Uses Convex `useQuery`/`useMutation` for server state
- [ ] Local UI state uses React `useState`

---

## Testing Requirements

### E2E Tests (Playwright)

```markdown
- [ ] Happy path covered (user completes main action)
- [ ] Error scenario tested (API failure, validation error)
- [ ] Mobile viewport tested (375px width)
- [ ] Desktop viewport tested (1280px width)
```

### Visual Testing

```markdown
- [ ] Component renders in Storybook (if applicable)
- [ ] Light mode appearance verified
- [ ] Dark mode appearance verified
- [ ] Loading state appearance verified
- [ ] Error state appearance verified
```

### Interaction Testing

```markdown
- [ ] Click handlers fire correctly
- [ ] Form submission works
- [ ] Navigation works (links, buttons)
- [ ] Keyboard shortcuts work (if applicable)
```

---

## Quality Gates

Must pass before task completion:

| Gate       | Requirement                             |
| ---------- | --------------------------------------- |
| **Build**  | `npm run build` succeeds without errors |
| **Types**  | No TypeScript errors                    |
| **Lint**   | No ESLint errors (warnings acceptable)  |
| **E2E**    | Playwright tests pass                   |
| **Visual** | No unintended visual changes            |
| **A11y**   | Keyboard navigation works               |

---

## Common Patterns

### Well-Defined Feature Task

```markdown
**Title**: Add Task Status Filter Dropdown

**Acceptance Criteria**:

- [ ] Dropdown uses shadcn/ui Select component
- [ ] Options: All, Pending, In Progress, Completed
- [ ] Filter persists in URL search params
- [ ] Loading skeleton shown while tasks load
- [ ] Empty state shown when no tasks match filter
- [ ] Works on mobile (full-width dropdown)
- [ ] Keyboard accessible (arrow keys, Enter, Escape)

**Testing**:

- [ ] E2E: Filter changes update task list
- [ ] E2E: Filter persists on page refresh
- [ ] Visual: Dropdown matches design system
```

### Well-Defined Bugfix Task

```markdown
**Title**: Fix Task Card Overflow on Mobile

**Acceptance Criteria**:

- [ ] Task title no longer overflows card boundary
- [ ] Long titles truncate with ellipsis
- [ ] Full title shown in tooltip on hover
- [ ] Works on iOS Safari and Android Chrome
- [ ] No regression on desktop layout

**Testing**:

- [ ] E2E: Long title renders correctly on mobile viewport
- [ ] Visual: Card maintains consistent height
```

### Well-Defined Refactor Task

```markdown
**Title**: Migrate TaskCard to Item Component

**Acceptance Criteria**:

- [ ] TaskCard replaced with Item + ItemContent pattern
- [ ] Visual appearance unchanged
- [ ] All existing functionality preserved
- [ ] Theme variables used (no hardcoded colors)
- [ ] E2E tests pass without modification

**Testing**:

- [ ] E2E: All task list tests pass
- [ ] Visual: Screenshot comparison shows no changes
```
