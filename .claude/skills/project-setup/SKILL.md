---
name: project-setup
description: Set up .claude folder for a project. Compares existing configuration with template and helps merge/copy files intelligently.
allowed-tools:
  - Read
  - Write
  - Grep
  - Glob
  - Bash
  - AskUserQuestion
---

# Project Setup Workflow

Set up the `.claude` folder for a project that was created via the Task Manager UI. This command intelligently handles existing `.claude` configurations by comparing with the template.

## Arguments

Optionally provide a path to a template .claude folder. Defaults to task-manager's .claude folder.

---

## Overview

When a project is created via the Task Manager modal:
1. Project is registered in Convex
2. API key is generated
3. `.env.local` is written to the project

This command handles the next step: setting up the `.claude` folder with commands, agents, and skills.

**Key Principle**: Don't blindly overwrite. Compare and merge intelligently.

---

## Workflow

### Phase 0: Verify Environment

Check that this project has been registered:

```bash
source .env.local 2>/dev/null || source .env 2>/dev/null || true

if [ -z "$CLAUDE_API_KEY" ]; then
  echo "Warning: No CLAUDE_API_KEY found in .env.local"
  echo "Run the Add Project flow in Task Manager first, or manually add your API key."
fi
```

If no API key found, ask:

```
questions: [
  {
    question: "No CLAUDE_API_KEY found. How would you like to proceed?",
    header: "Setup",
    multiSelect: false,
    options: [
      {label: "Continue anyway", description: "Set up .claude folder without API key"},
      {label: "Cancel", description: "Exit and set up API key first"}
    ]
  }
]
```

### Phase 1: Analyze Current State

Check what exists in the current project's `.claude` folder:

```bash
# Check if .claude exists
if [ -d ".claude" ]; then
  echo "Found existing .claude folder"
  ls -la .claude/
else
  echo "No .claude folder found"
fi
```

Use Glob to get detailed inventory:
- `.claude/commands/*.md` - User commands
- `.claude/agents/*.md` - Agent definitions
- `.claude/skills/*/SKILL.md` - Skills
- `.claude/CLAUDE.md` - Main instructions
- `.claude/settings.local.json` - Local settings

### Phase 2: Determine Template Source

The template `.claude` folder location:
- Default: The task-manager project's `.claude` folder
- Can be overridden via argument

For this project, the template is at:
`/home/lmt/Projects/personal/task-manager/.claude`

Read the template inventory to know what's available.

### Phase 3: Compare and Present Differences

Create a comparison table:

```markdown
## .claude Folder Comparison

| Item | Current Project | Template | Action Needed |
|------|-----------------|----------|---------------|
| CLAUDE.md | ✗ Missing | ✓ Available | Copy |
| commands/task-dev.md | ✗ Missing | ✓ Available | Copy |
| commands/ad-hoc.md | ✓ Exists (v1) | ✓ Available (v2) | Review |
| skills/frontend/ | ✓ Exists | ✓ Available | Skip |
| agents/impl.md | ✗ Missing | ✓ Available | Copy |
```

Categories:
- **Missing**: Not in project, available in template → Recommend copy
- **Exists (same)**: Identical content → Skip
- **Exists (different)**: Different content → Review/merge
- **Project-only**: In project but not template → Keep

### Phase 4: Ask User What to Set Up

Based on comparison, ask user:

```
questions: [
  {
    question: "What would you like to set up?",
    header: "Setup",
    multiSelect: true,
    options: [
      {label: "Core files", description: "CLAUDE.md, settings.local.json"},
      {label: "Commands", description: "task-dev, feature, bugfix, etc."},
      {label: "Agents", description: "impl, scout, planner, etc."},
      {label: "Skills", description: "frontend, backend, database skills"},
      {label: "All missing", description: "Copy everything that's missing"}
    ]
  }
]
```

If files exist with different content:

```
questions: [
  {
    question: "Some files exist with different content. How should I handle them?",
    header: "Conflicts",
    multiSelect: false,
    options: [
      {label: "Keep existing", description: "Don't overwrite any existing files"},
      {label: "Show diff", description: "Review each conflict individually"},
      {label: "Overwrite", description: "Replace with template versions"}
    ]
  }
]
```

### Phase 5: Copy Selected Files

For each selected category, copy files from template:

```bash
# Example: Copy a command file
TEMPLATE_PATH="/home/lmt/Projects/personal/task-manager/.claude"
TARGET_PATH=".claude"

# Ensure directory exists
mkdir -p "$TARGET_PATH/commands"

# Copy file
cp "$TEMPLATE_PATH/commands/task-dev.md" "$TARGET_PATH/commands/task-dev.md"
```

**Important**: Use Read tool to get template content, then Write tool to create in target. This ensures proper handling and lets us modify paths if needed.

### Phase 6: Handle Conflicts (if user chose "Show diff")

For each conflicting file:

1. Read both versions
2. Show key differences
3. Ask user which to keep or how to merge

```
questions: [
  {
    question: "CLAUDE.md differs. Which version do you want?",
    header: "CLAUDE.md",
    multiSelect: false,
    options: [
      {label: "Keep current", description: "Don't change existing file"},
      {label: "Use template", description: "Replace with template version"},
      {label: "Merge", description: "I'll manually merge after seeing both"}
    ]
  }
]
```

### Phase 7: Summary

Display what was set up:

```
+--------------------------------------------------+
|         Project Setup Complete                    |
+--------------------------------------------------+

## Files Copied
  - .claude/CLAUDE.md
  - .claude/commands/task-dev.md
  - .claude/commands/ad-hoc.md
  - .claude/agents/impl.md
  - .claude/skills/frontend/SKILL.md
  ... (N files total)

## Files Skipped (already exist)
  - .claude/commands/custom-command.md
  - .claude/settings.local.json

## Next Steps

  /project-init       Explore codebase and generate task backlog
  /task-dev FT-001    Start working on a task
  /ad-hoc [desc]     Quick implementation without tracking

+--------------------------------------------------+
```

---

## Template Files to Copy

### Core (Always Recommended)
- `CLAUDE.md` - Main project instructions
- `settings.local.json` - Local settings (if doesn't exist)

### Commands (Recommended)
- `commands/task-dev.md` - Task development workflow
- `commands/ad-hoc.md` - Quick implementation without tracking
- `commands/bugfix.md` - Bug fix workflow
- `commands/refactor.md` - Refactoring workflow
- `commands/test.md` - Test generation
- `commands/project-init.md` - Project initialization
- `commands/project-setup.md` - This command
- `commands/task-init.md` - Task creation
- `commands/task-list.md` - Task listing

### Agents (Recommended)
- `agents/impl.md` - Implementation agent
- `agents/scout.md` - Codebase exploration
- `agents/planner.md` - Task planning
- `agents/test.md` - Test generation
- `agents/commit.md` - Git commits
- `agents/integration.md` - Integration validation

### Skills (Optional - based on project type)
- `skills/frontend/` - React/TypeScript patterns
- `skills/backend/` - Convex/serverless patterns
- `skills/database/` - Schema/migration patterns
- `skills/api/` - HTTP API documentation
- `skills/project/` - Project conventions
- `skills/git/` - Git conventions

### Scripts (Optional)
- `scripts/` - Helper scripts for hooks

---

## Customization

After setup, users should customize:

1. **CLAUDE.md** - Add project-specific instructions
2. **skills/project/SKILL.md** - Define project conventions
3. **skills/project/LEARNED.md** - Will accumulate learnings
4. **settings.local.json** - Adjust agent models if needed

---

## Example Session

```
User: /project-setup

Claude: Checking current project state...

Found existing .claude folder with:
- CLAUDE.md (exists)
- commands/ (3 files)
- No agents
- No skills

Template has:
- 10 commands
- 8 agents
- 12 skills

## Comparison

| Category | Current | Template | Missing |
|----------|---------|----------|---------|
| Commands | 3 | 10 | 7 |
| Agents | 0 | 8 | 8 |
| Skills | 0 | 12 | 12 |

Claude: [AskUserQuestion]
What would you like to set up?
→ Commands, Agents selected

Claude: [AskUserQuestion]
CLAUDE.md exists with different content. How to handle?
→ Keep existing

Claude: Copying files...
- commands/task-dev.md ✓
- commands/ad-hoc.md ✓
- commands/bugfix.md ✓
- agents/impl.md ✓
- agents/scout.md ✓
...

Setup complete! 15 files copied, 1 file skipped.

Next: Run /project-init to explore your codebase and generate tasks.
```

---

## Error Handling

### Template Not Found
```
Error: Template .claude folder not found at [path]

Please ensure the task-manager project is available, or specify
a custom template path: /project-setup /path/to/template/.claude
```

### Permission Denied
```
Error: Cannot write to .claude folder

Please check file permissions and try again.
```

### Partial Failure
If some files fail to copy, continue with others and report at the end:

```
Warning: Could not copy 2 files:
- .claude/scripts/hook.sh (permission denied)
- .claude/agents/test.md (file locked)

Other 13 files copied successfully.
```
