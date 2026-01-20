# Frontmatter Reference

Complete documentation of all frontmatter fields for Claude Code configurations.

---

## Skill Frontmatter

Skills are defined in `.claude/skills/{name}/SKILL.md`.

### Required Fields

| Field | Type | Max Length | Description |
|-------|------|------------|-------------|
| `name` | string | 64 chars | Skill identifier. Lowercase, numbers, hyphens only. Use gerund form (`processing-pdfs`). No reserved words ("anthropic", "claude"). |
| `description` | string | 1024 chars | What the skill does AND when to use it. Third person. Include trigger terms. |

### Optional Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `allowed-tools` | list | all tools | Tools Claude can use without permission prompts |
| `model` | string | inherit | Specific model to use (e.g., `claude-sonnet-4-20250514`) |
| `context` | string | none | Set to `fork` to run in isolated subagent context |
| `agent` | string | none | Which agent handles forked execution. Only works with `context: fork` |
| `user-invocable` | boolean | true | Show in `/slash` command menu. `false` hides but still allows programmatic use |
| `hooks` | object | none | Lifecycle hooks (see Hook Configuration below) |

### Example

```yaml
---
name: api-operations
description: Executes HTTP API calls to backend services. Use when making API requests, managing projects, or handling task operations.
allowed-tools:
  - Bash
  - Read
  - Grep
model: claude-sonnet-4-20250514
context: fork
agent: api-executor
user-invocable: false
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: ".claude/scripts/validate-env.sh"
---
```

---

## Agent Frontmatter

Agents are defined in `.claude/agents/{name}.md`.

### Required Fields

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Agent identifier. Lowercase, hyphens. |
| `description` | string | When to delegate to this agent. Used by orchestrator for selection. |

### Optional Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `tools` | list | inherit all | Tools the agent can use. Comma-separated or YAML list. |
| `disallowedTools` | list | none | Tools to explicitly deny. |
| `model` | string | inherit | `sonnet`, `opus`, `haiku`, or `inherit` (parent's model). |
| `skills` | list | none | Skills to load into agent context. Skill content injected at startup. |
| `permissionMode` | string | default | Permission handling mode (see below). |
| `hooks` | object | none | Agent-specific hooks. |

### Permission Modes

| Mode | Description |
|------|-------------|
| `default` | Normal permission prompts |
| `acceptEdits` | Auto-accept Edit tool, prompt for others |
| `dontAsk` | Skip permission prompts, but respect safety |
| `bypassPermissions` | Skip all prompts (use carefully) |
| `plan` | Plan mode - read-only exploration |

### Example

```yaml
---
name: api-executor
description: Executes HTTP API calls to Convex backend. Handles project creation, task operations, and API key management.
tools: Bash, Read, Grep
model: haiku
skills: api
permissionMode: default
---
```

---

## Command Frontmatter

Commands are defined in `.claude/commands/{name}.md`. They use the same fields as skills, plus support `$ARGUMENTS`.

### Additional Features

| Feature | Description |
|---------|-------------|
| `$ARGUMENTS` | Placeholder in body text. Replaced with user's command arguments. |

### Example

```yaml
---
name: project-init
description: Context-aware project initialization. Explores codebase, detects tech stack, registers project in Convex.
allowed-tools:
  - Read
  - Grep
  - Glob
  - Bash
  - Task
  - AskUserQuestion
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: ".claude/scripts/validate-project-init.sh"
---

# Project Initialization

Arguments: $ARGUMENTS
```

---

## Hook Configuration

Hooks run at lifecycle points. Available for skills, commands, and agents.

### Hook Events

| Event | When | Can Block | Available For |
|-------|------|-----------|---------------|
| `PreToolUse` | Before tool execution | Yes (exit 2) | Skills, Commands, Agents |
| `PostToolUse` | After tool completes | No | Skills, Commands, Agents |
| `Stop` | When execution finishes | No | Skills, Commands, Agents |
| `SubagentStart` | When subagent spawns | No | Agents only |
| `SubagentStop` | When subagent finishes | No | Agents only |

### Hook Structure

```yaml
hooks:
  PreToolUse:
    - matcher: "ToolName"        # Regex pattern for tool name
      hooks:
        - type: command          # Hook type
          command: "./script.sh" # Command to run
          once: true             # Optional: run only once per session
```

### Matcher Patterns

| Pattern | Matches |
|---------|---------|
| `"Bash"` | Bash tool only |
| `"Edit\|Write"` | Edit OR Write tools |
| `".*"` | All tools |

### Exit Codes

| Code | Effect |
|------|--------|
| `0` | Allow action to proceed |
| `2` | Block the action (PreToolUse only) |
| Other | Log warning, allow action |

### Hook Environment

Hooks receive environment variables:

| Variable | Description |
|----------|-------------|
| `TOOL_INPUT` | The input being passed to the tool |
| `TOOL_NAME` | Name of the tool being called |

### Example: Validation Hook

```yaml
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: ".claude/scripts/validate-env.sh"
```

Script:
```bash
#!/bin/bash
source .env.local 2>/dev/null || true

if [[ -z "$REQUIRED_VAR" ]]; then
  echo "ERROR: REQUIRED_VAR not set"
  exit 2  # Block the action
fi

exit 0  # Allow to proceed
```

---

## Context Forking

When a skill sets `context: fork`, it runs in an isolated subagent context.

### How It Works

1. Skill is invoked (programmatically or via slash command)
2. Claude spawns a subagent with the skill content as context
3. Subagent executes using the specified `agent` configuration
4. Results returned to main agent

### Benefits

- Keeps main agent context lean
- Isolates heavy domain knowledge
- Allows specialized model/tool selection

### Configuration

```yaml
---
name: heavy-skill
description: Does complex things...
context: fork
agent: specialized-executor
---
```

The `agent` field specifies which agent file (`.claude/agents/{name}.md`) handles execution.

---

## Tool Reference

Common tools and when to restrict them:

| Tool | Purpose | When to Allow |
|------|---------|---------------|
| `Read` | Read files | Almost always safe |
| `Grep` | Search content | Safe, read-only |
| `Glob` | Find files | Safe, read-only |
| `Write` | Create files | When creating is needed |
| `Edit` | Modify files | When editing is needed |
| `Bash` | Run commands | Be selective, add hooks |
| `Task` | Spawn subagents | When orchestrating |
| `AskUserQuestion` | Interactive questions | Main agent only |

### Read-Only Tool Set

For exploration/analysis agents:
```yaml
tools: Read, Grep, Glob
```

### Implementation Tool Set

For agents that modify code:
```yaml
tools: Read, Write, Edit, Bash, Glob, Grep
```

---

## Model Selection Guide

| Model | Use When | Cost |
|-------|----------|------|
| `haiku` | Simple, well-defined tasks. API calls, formatting. | Lowest |
| `sonnet` | Most implementation work. Good balance. | Medium |
| `opus` | Complex planning, architecture decisions. | Highest |
| `inherit` | Use parent agent's model. | Varies |

---

## Field Validation

### Name Requirements

- Max 64 characters
- Lowercase letters, numbers, hyphens only
- No spaces or special characters
- No reserved words: "anthropic", "claude"
- Prefer gerund form: `processing-pdfs`, `managing-tasks`

### Description Requirements

- Max 1024 characters
- Third person (not "I can help" or "You can use")
- Include what it does AND when to use it
- Include specific trigger terms
- Be specific, not vague

**Good:**
```yaml
description: Extracts text from PDF files, fills forms, merges documents. Use when working with PDF files or when the user mentions PDFs, forms, or document extraction.
```

**Bad:**
```yaml
description: Helps with documents  # Too vague
description: I can help you process PDFs  # Wrong person
```
