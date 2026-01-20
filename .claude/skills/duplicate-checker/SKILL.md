---
name: duplicate-checker
description: Checks for duplicate tasks before creation. Compares title against existing pending, in_progress, and blocked tasks. Use before creating tasks to avoid duplicates.
context: fork
agent: task-checker
user-invocable: false
hooks:
  PreToolUse:
    - matcher: ".*"
      hooks:
        - type: command
          command: ".claude/scripts/fetch-tasks.sh"
          once: true
---

# Duplicate Checker

Checks if a task with similar title already exists before creation.

---

## Input

You receive:
1. **New task title** - From the invoking agent's prompt
2. **Existing tasks** - Pre-fetched by hook, available at `/tmp/tasks-for-duplicate-check.json`

---

## Status Filter

Only check against tasks with status:
- `pending` - Not started
- `in_progress` - Being worked on
- `blocked` - Stuck/waiting

**Exclude** `completed` tasks (OK to create similar work after completion).

---

## Comparison Rules

### 1. Exact Match (case-insensitive)
```
existing.title.toLowerCase() === new.title.toLowerCase()
```

### 2. Prefix Match
First 10 characters match (catches variations like "Add search" vs "Add search bar")

### 3. Keyword Overlap
More than 50% of significant words match (catches rephrasing)

---

## Process

1. Read task list from `/tmp/tasks-for-duplicate-check.json`
2. Parse JSON and extract tasks array
3. Filter to only: pending, in_progress, blocked status
4. For each existing task, compare against new title
5. Collect all matches with match type
6. Return structured result

---

## Output Format

### If duplicates found:

```json
{
  "hasDuplicate": true,
  "matches": [
    {
      "customId": "FT-001",
      "title": "Add search to task list",
      "status": "pending",
      "matchType": "exact"
    },
    {
      "customId": "FT-003",
      "title": "Add search functionality",
      "status": "in_progress",
      "matchType": "prefix"
    }
  ]
}
```

### If no duplicates:

```json
{
  "hasDuplicate": false,
  "matches": []
}
```

---

## Match Types

| Type | Description |
|------|-------------|
| `exact` | Titles are identical (case-insensitive) |
| `prefix` | First 10 characters match |
| `keyword` | >50% word overlap |
