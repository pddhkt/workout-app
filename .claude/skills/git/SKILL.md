---
name: git
description: Git commit conventions and operations. Use for creating task checkpoint commits following project conventions.
---

# Git Skill

Conventions for creating task checkpoint commits.

## Commit Message Format

Format: `{type}({task-id}): {title}`

### Type Mapping

| Task Prefix | Commit Type | Description   |
| ----------- | ----------- | ------------- |
| FT-         | feat        | New feature   |
| BF-         | fix         | Bug fix       |
| RF-         | refactor    | Code refactor |

### Examples

```
feat(FT-001): Add user authentication
fix(BF-003): Correct login error handling
refactor(RF-002): Extract auth logic into hook
```

## Commands

### Check for Changes

```bash
git status --porcelain
```

Returns empty if no changes.

### Stage All Changes

```bash
git add .
```

### Create Commit

Use HEREDOC for proper message formatting:

```bash
git commit -m "$(cat <<'EOF'
{type}({task-id}): {title}

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

### Verify Commit

```bash
git log -1 --oneline
```

## Skip Conditions

Skip commit (no error) when:

- `git status --porcelain` returns empty (no changes)
- Only untracked files that should be ignored (.env, node_modules, etc.)

## Constraints

| Action                | Allowed                   |
| --------------------- | ------------------------- |
| Create new commit     | Yes                       |
| Amend existing commit | No                        |
| Force push            | No                        |
| Push to remote        | No (orchestrator handles) |
| Interactive rebase    | No                        |

## Error Handling

- If commit fails, report the error message
- Do not retry automatically
- Do not attempt workarounds
- Report to orchestrator for resolution
