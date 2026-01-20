---
name: smart-commit
description: Groups unstaged git changes into logical commits with user confirmation. Use when user wants to commit changes, organize commits, or mentions smart commit, group commits, or batch commits.
---

# Smart Commit

Group unstaged changes into logical commits with batch confirmation.

## Workflow

### Step 1: Analyze Changes

```bash
git status
git diff --stat
git diff --name-only
```

### Step 2: Group Changes

Analyze the changed files and group them logically by:

1. **Feature/Component** - Files that belong to the same feature
2. **Directory** - Files in the same directory/module
3. **Type of Change** - Config changes, tests, docs, etc.

### Step 3: Batch Confirmation

**REQUIRED**: Use `AskUserQuestion` with `multiSelect: true` to present ALL groups at once.

Present a single question listing all proposed commits. User selects which to commit.

Example AskUserQuestion call:
```json
{
  "questions": [{
    "question": "Which commits should I create?",
    "header": "Commits",
    "multiSelect": true,
    "options": [
      {
        "label": "Auth flow",
        "description": "src/auth/login.ts, src/auth/logout.ts → 'Implement auth flow'"
      },
      {
        "label": "Users API",
        "description": "src/api/users.ts → 'Add users API endpoint'"
      },
      {
        "label": "Documentation",
        "description": "README.md → 'Update documentation'"
      }
    ]
  }]
}
```

Each option label is the commit name, description shows files and commit message.

### Step 4: Commit Selected Groups

For each selected group:

```bash
git add <file1> <file2> ...
git commit -m "commit message here"
```

**IMPORTANT**: Do NOT add `Co-Authored-By` line. User wants clean commits.

### Step 5: Report Summary

After all commits:
- Number of commits created
- Files committed
- Files remaining uncommitted (if any)

## Grouping Strategies

### By Directory
```
src/api/* → "Update API endpoints"
src/components/* → "Update UI components"
tests/* → "Add/update tests"
```

### By Feature
```
UserAuth related files → "Implement user authentication"
Dashboard related files → "Add dashboard feature"
```

### By Change Type
```
*.config.* files → "Update configuration"
*.test.* files → "Add tests"
*.md files → "Update documentation"
package.json, lock files → "Update dependencies"
```

## Example Session

1. User: "commit my changes"

2. Claude runs:
   ```bash
   git status
   git diff --stat
   ```

3. Claude identifies 3 groups and asks once:

   ```
   Which commits should I create?

   [ ] Auth flow - src/auth/login.ts, logout.ts → "Implement auth flow"
   [ ] Users API - src/api/users.ts → "Add users API endpoint"
   [ ] Documentation - README.md → "Update documentation"
   ```

4. User selects: "Auth flow, Users API"

5. Claude commits selected groups:
   ```bash
   git add src/auth/login.ts src/auth/logout.ts
   git commit -m "Implement auth flow"

   git add src/api/users.ts
   git commit -m "Add users API endpoint"
   ```

6. Claude reports: "Created 2 commits. README.md left uncommitted."

## Rules

1. **Single batch confirmation** - Ask once with all groups, not one by one
2. **No Claude attribution** - Omit Co-Authored-By line
3. **Atomic commits** - Each commit should be focused
4. **Clear messages** - Commit messages describe the "what" and "why"
5. **Handle unselected** - Leave unselected files unstaged
