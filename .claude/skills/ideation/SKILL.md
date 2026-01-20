---
name: ideation
description: Generate and explore feature ideas for your project. Suggests improvements based on codebase analysis, then creates phase plans from selected ideas.
allowed-tools:
  - Task
  - Bash
  - Read
  - Grep
  - Glob
  - AskUserQuestion
  - TodoWrite
---

# Ideation Skill

Generate and explore feature ideas for your project based on codebase analysis. This skill helps brainstorm improvements, then seamlessly transitions selected ideas into phase development.

## Usage

```bash
/ideation                           # General ideation session
/ideation --category performance    # Focus on specific category
/ideation --from-feedback           # Generate ideas from user feedback
```

**Categories:**
- `features` - New functionality ideas
- `performance` - Optimization opportunities
- `security` - Security improvements
- `ui-ux` - User experience enhancements
- `code-quality` - Refactoring opportunities
- `documentation` - Documentation needs
- `testing` - Test coverage improvements
- `all` - All categories (default)

## Workflow

```
/ideation
    ↓
1. Analyze codebase for context
    ↓
2. Generate ideas by category
    ↓
3. Present ideas with impact/effort ratings
    ↓
4. User selects idea(s)
    ↓
5. Run /phase-dev for selected idea
    ↓
6. Phase plan created with tasks
```

## Phase 1: Codebase Analysis

Scan the codebase to understand:
- Tech stack and frameworks
- Existing features and modules
- Test coverage gaps
- Common patterns and conventions
- Recent changes (git log)

```bash
# Get project structure
cat .claude/cache/inventory.md 2>/dev/null || ls -la

# Check package.json for dependencies
cat package.json 2>/dev/null | jq '{dependencies, devDependencies}'

# Recent git activity
git log --oneline -20 2>/dev/null
```

## Phase 2: Idea Generation

Generate ideas based on category. Each idea includes:
- **Title** - Short, descriptive name
- **Description** - What the idea involves
- **Impact** - High/Medium/Low business value
- **Effort** - S/M/L/XL estimation
- **Category** - Which category it falls under
- **Prerequisites** - What must exist first

### Idea Categories

#### Features
- Missing CRUD operations
- Integration opportunities
- User-requested features
- Competitive features

#### Performance
- N+1 query patterns
- Missing indexes
- Caching opportunities
- Bundle size optimizations

#### Security
- Auth improvements
- Input validation gaps
- Dependency vulnerabilities
- OWASP recommendations

#### UI/UX
- Accessibility improvements
- Mobile responsiveness
- Loading states
- Error handling UX

#### Code Quality
- Repeated code patterns
- Missing abstractions
- Inconsistent naming
- Dead code removal

#### Documentation
- Missing README sections
- API documentation
- Code comments
- User guides

#### Testing
- Untested critical paths
- Missing edge cases
- Integration test gaps
- E2E test needs

## Phase 3: Present Ideas

Format ideas in a clear, actionable format:

```
=== IDEATION SESSION ===

Category: [Selected or All]
Date: [Current date]

┌────────────────────────────────────────────────────────────────────┐
│ FEATURE IDEAS                                                       │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ 1. [HIGH Impact, M Effort] Add Real-Time Notifications              │
│    → WebSocket-based notifications for user actions                 │
│    → Enables: live updates, collaboration features                  │
│                                                                     │
│ 2. [MEDIUM Impact, S Effort] Add Export to CSV                      │
│    → Allow users to export data tables to CSV                       │
│    → Quick win: uses existing data fetching                         │
│                                                                     │
│ 3. [HIGH Impact, L Effort] Implement Role-Based Access Control      │
│    → Admin/User/Guest roles with permission system                  │
│    → Prerequisites: Auth system must be complete                    │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│ PERFORMANCE IDEAS                                                   │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ 4. [HIGH Impact, M Effort] Add Query Caching Layer                  │
│    → Redis/in-memory cache for frequent queries                     │
│    → Detected: 5 endpoints called 100+ times/min                    │
│                                                                     │
│ 5. [MEDIUM Impact, S Effort] Add Database Indexes                   │
│    → Missing indexes on frequently filtered columns                 │
│    → Detected: users.email, orders.created_at                       │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│ QUICK WINS (Small effort, visible impact)                          │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ • Add loading spinners to async actions                             │
│ • Add 404 page with navigation                                      │
│ • Add success toast notifications                                   │
│ • Improve form validation messages                                  │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘

=== END IDEATION ===

Select an idea number to develop, or type 'more [category]' for more ideas.
```

## Phase 4: User Selection

Use AskUserQuestion to get user's choice:

```
Which idea would you like to develop?

Options:
1. Add Real-Time Notifications [HIGH/M]
2. Add Export to CSV [MEDIUM/S]
3. Implement Role-Based Access Control [HIGH/L]
4. Add Query Caching Layer [HIGH/M]
5. Add Database Indexes [MEDIUM/S]
Other: Type your own idea or 'more [category]'
```

## Phase 5: Transition to Phase Development

Once user selects an idea:

```
User selected: "1. Add Real-Time Notifications"

Transitioning to /phase-dev...

Phase Description:
"Add real-time notifications using WebSockets. Users should receive
instant notifications for relevant events (new messages, status
updates, mentions). Include notification preferences and a
notification center UI."

Running /phase-dev...
```

Call `/phase-dev` with the elaborated idea description:

```
Use internal phase-planning to generate tasks:

## Selected Idea
Add Real-Time Notifications

## Elaborated Description
Implement WebSocket-based real-time notification system:
- Backend: WebSocket server, notification events, user preferences
- Frontend: Notification center, toast notifications, sound alerts
- Database: Notifications table, read/unread status

## Acceptance Criteria (derived from idea)
- Users receive notifications within 1 second of event
- Notification preferences can be configured
- Notification center shows history
- Unread count badge on notification icon
```

## Idea Template Structure

```json
{
  "id": 1,
  "title": "Add Real-Time Notifications",
  "description": "WebSocket-based notifications for user actions",
  "category": "features",
  "impact": "high",
  "effort": "M",
  "domains": ["backend", "frontend", "database"],
  "prerequisites": ["Auth system"],
  "acceptanceCriteria": [
    "Users receive notifications within 1 second",
    "Notifications can be marked as read",
    "User can configure notification preferences"
  ],
  "relatedFiles": [
    "src/api/notifications.ts",
    "src/components/NotificationCenter.tsx"
  ],
  "signals": [
    "No existing WebSocket implementation",
    "User feedback mentions need for real-time updates"
  ]
}
```

## Bash Helper Functions

```bash
# Count potential performance issues
check_performance_opportunities() {
  # Look for N+1 patterns
  grep -r "\.map.*fetch\|\.forEach.*await" src/ --include="*.ts" 2>/dev/null | wc -l

  # Look for missing indexes (check schema)
  cat convex/schema.ts 2>/dev/null | grep -c "\.index(" || echo 0
}

# Find untested files
find_untested_files() {
  # List source files without corresponding test files
  for f in $(find src -name "*.ts" -not -name "*.test.ts"); do
    test_file="${f%.ts}.test.ts"
    if [ ! -f "$test_file" ]; then
      echo "$f"
    fi
  done
}

# Check for security opportunities
check_security_gaps() {
  # Missing input validation
  grep -r "req\.body\." src/ --include="*.ts" | grep -v "validate" | wc -l

  # Hardcoded secrets
  grep -rE "(password|secret|key)\s*=\s*['\"]" src/ --include="*.ts" 2>/dev/null | wc -l
}
```

## Integration with Auto-Claude Patterns

This skill leverages Auto-Claude's ideation prompts:

| Auto-Claude Prompt | Maps To |
|-------------------|---------|
| `ideation_code_improvements.md` | Code Quality category |
| `ideation_code_quality.md` | Code Quality category |
| `ideation_performance.md` | Performance category |
| `ideation_security.md` | Security category |
| `ideation_ui_ux.md` | UI/UX category |
| `ideation_documentation.md` | Documentation category |

## Example Session

```bash
# User starts ideation
/ideation --category features

# Claude analyzes codebase
# → Scans src/, package.json, recent commits

# Claude generates ideas
# === IDEATION SESSION ===
# [Ideas presented]
# === END IDEATION ===

# User: "I like idea 2"

# Claude elaborates idea
# → Adds acceptance criteria
# → Identifies domains

# Claude transitions to /phase-dev
# → Creates tasks for the feature
# → Syncs to Convex

# User has actionable tasks
# Ready for /task-dev FT-001
```

## Error Handling

| Scenario | Action |
|----------|--------|
| No codebase context | Ask user for project description |
| Category has no ideas | Suggest related categories |
| User wants custom idea | Accept freeform input |
| Prerequisite missing | Note it and suggest prerequisite first |

## Related Skills

| Skill | Relationship |
|-------|--------------|
| `/phase-dev` | Creates tasks from selected idea |
| `/analyze` | Market research to inform ideation |
| `/project` | Project context for analysis |
| `/task-dev` | Execute created tasks |
