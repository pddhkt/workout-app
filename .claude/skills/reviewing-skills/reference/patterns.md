# Pattern Catalog

Improvement patterns organized by component type with trigger conditions and implementations.

---

## Command Patterns

### validation-hook

**ID:** `validation-hook`

**When to Suggest:**
- Component uses Bash tool
- Body contains API calls, curl commands
- Body mentions environment variables

**Description:** Add PreToolUse hook to validate before Bash execution.

**Implementation:**

```yaml
# Add to frontmatter
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: ".claude/scripts/validate-{name}.sh"
```

**Before:**
```yaml
---
name: deploy
description: Deploys application to production.
allowed-tools:
  - Bash
  - Read
---
```

**After:**
```yaml
---
name: deploy
description: Deploys application to production.
allowed-tools:
  - Bash
  - Read
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: ".claude/scripts/validate-deploy.sh"
---
```

**Validation Script Template:**
```bash
#!/bin/bash
source .env.local 2>/dev/null || true

# Check required environment variables
if [[ -z "$REQUIRED_VAR" ]]; then
  echo "ERROR: REQUIRED_VAR not set"
  exit 2  # Block action
fi

exit 0  # Allow to proceed
```

---

### env-check

**ID:** `env-check`

**When to Suggest:**
- Component calls external APIs
- Body references API keys, tokens, URLs
- Body contains curl/fetch operations

**Description:** Validate required environment variables exist before API calls.

**Implementation:**

```yaml
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: ".claude/scripts/check-env.sh"
```

**Before:**
```yaml
---
name: api-sync
description: Syncs data with external API.
---
```

**After:**
```yaml
---
name: api-sync
description: Syncs data with external API.
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: ".claude/scripts/check-env.sh"
---
```

**Environment Check Script:**
```bash
#!/bin/bash
required_vars=("API_URL" "API_KEY")

for var in "${required_vars[@]}"; do
  if [[ -z "${!var}" ]]; then
    echo "ERROR: $var is not set"
    exit 2
  fi
done

exit 0
```

---

### tool-restriction

**ID:** `tool-restriction`

**When to Suggest:**
- No `allowed-tools` field present
- Component has broad tool access but limited purpose
- Read-only operations don't need Write/Edit/Bash

**Description:** Restrict to minimum needed tools (principle of least privilege).

**Implementation:**

Analyze the body to determine actual tool usage, then add:

```yaml
allowed-tools:
  - Tool1
  - Tool2
```

**Before:**
```yaml
---
name: analyze-code
description: Analyzes code patterns.
---
```

**After:**
```yaml
---
name: analyze-code
description: Analyzes code patterns.
allowed-tools:
  - Read
  - Grep
  - Glob
---
```

**Common Tool Sets:**
- **Read-only:** `Read, Grep, Glob`
- **Implementation:** `Read, Write, Edit, Bash, Glob, Grep`
- **Orchestration:** `Read, Grep, Glob, Task, AskUserQuestion`

---

### arguments-usage

**ID:** `arguments-usage`

**When to Suggest:**
- Component is a command (in `.claude/commands/`)
- Body does not contain `$ARGUMENTS`
- Command would benefit from user-provided input

**Description:** Add argument handling with `$ARGUMENTS` placeholder.

**Implementation:**

Add `$ARGUMENTS` in the body where user input should appear:

**Before:**
```markdown
---
name: search
description: Searches codebase for patterns.
---

# Search Command

Search for patterns in the codebase.
```

**After:**
```markdown
---
name: search
description: Searches codebase for patterns.
---

# Search Command

Search for: $ARGUMENTS

Search for the specified pattern in the codebase.
```

---

## Skill Patterns

### context-fork

**ID:** `context-fork`

**When to Suggest:**
- Skill has >300 lines of content
- Skill has multiple reference files
- Skill contains heavy domain documentation
- Skill execution should be isolated from main context

**Description:** Fork to isolated subagent to keep main agent context lean.

**Implementation:**

```yaml
context: fork
agent: {skill-name}-executor
```

**Before:**
```yaml
---
name: api-reference
description: Complete API documentation with endpoints and schemas.
---
```

**After:**
```yaml
---
name: api-reference
description: Complete API documentation with endpoints and schemas.
context: fork
agent: api-executor
---
```

**Note:** Requires creating paired agent. See `paired-agent` pattern.

---

### paired-agent

**ID:** `paired-agent`

**When to Suggest:**
- Skill has `context: fork` but no `agent` field
- Skill with fork needs specialized execution

**Description:** Create dedicated executor agent for forked skill.

**Implementation:**

1. Add agent reference to skill:
```yaml
context: fork
agent: {name}-executor
```

2. Create agent file at `.claude/agents/{name}-executor.md`:
```yaml
---
name: {name}-executor
description: Executes {skill-name} operations in isolated context.
tools: {minimal-toolset}
model: haiku  # or sonnet for complex tasks
---

# {Name} Executor

You execute {domain} operations.

## Available Context
The skill content is loaded into your context.

## Output Format
Return structured results for the parent agent.
```

**Before:**
```yaml
# Skill
---
name: api
context: fork
---
```

**After:**
```yaml
# Skill
---
name: api
context: fork
agent: api-executor
---

# Agent (new file: .claude/agents/api-executor.md)
---
name: api-executor
description: Executes API operations in isolated context.
tools: Bash, Read, Grep
model: haiku
---
```

---

### hide-from-menu

**ID:** `hide-from-menu`

**When to Suggest:**
- Skill is internal utility
- Skill is only used programmatically by other commands/agents
- Skill name suggests internal use (prefixed with underscore, "internal", "util")
- Description says "used by" other components

**Description:** Hide skill from `/slash` command menu while keeping it programmatically accessible.

**Implementation:**

```yaml
user-invocable: false
```

**Before:**
```yaml
---
name: task-loader
description: Loads task data from API. Used by /task-dev command.
---
```

**After:**
```yaml
---
name: task-loader
description: Loads task data from API. Used by /task-dev command.
user-invocable: false
---
```

---

### add-triggers

**ID:** `add-triggers`

**When to Suggest:**
- Description is vague (under 50 characters)
- Description lacks "Use when" phrase
- Description doesn't include specific trigger terms

**Description:** Add specific trigger terms to description for better auto-discovery.

**Implementation:**

Expand description with:
1. What it does (specific actions)
2. When to use it (trigger phrases)

**Before:**
```yaml
description: Handles database operations
```

**After:**
```yaml
description: Handles database operations including schema migrations, queries, and data management. Use when working with Prisma, database schemas, or running migrations.
```

**Trigger Term Examples:**
- API skills: "API calls", "HTTP requests", "endpoints"
- Database skills: "schema", "migrations", "queries"
- Frontend skills: "React", "components", "styling"

---

## Agent Patterns

### least-privilege

**ID:** `least-privilege`

**When to Suggest:**
- Agent has Write/Edit tools but description suggests read-only purpose
- Agent has Bash but doesn't need to execute commands
- Agent has more tools than referenced in body

**Description:** Remove unnecessary tools to follow principle of least privilege.

**Implementation:**

Analyze agent's purpose and body, restrict to minimum:

**Before:**
```yaml
---
name: analyzer
description: Analyzes code patterns and suggests improvements.
tools: Read, Write, Edit, Bash, Glob, Grep
---
```

**After:**
```yaml
---
name: analyzer
description: Analyzes code patterns and suggests improvements.
tools: Read, Grep, Glob
---
```

**Tool Guidelines:**
- Analysis/exploration: `Read, Grep, Glob`
- Implementation: Add `Write, Edit`
- System operations: Add `Bash` (with hooks)
- Orchestration: Add `Task`

---

### skill-injection

**ID:** `skill-injection`

**When to Suggest:**
- Agent is domain-specific (API, database, frontend)
- Agent would benefit from project-specific patterns
- Relevant skills exist that match agent's domain

**Description:** Load domain-specific skills into agent context via `skills` field.

**Implementation:**

```yaml
skills: skill1, skill2
```

**Before:**
```yaml
---
name: api-impl
description: Implements API endpoints.
tools: Read, Write, Edit, Bash
model: sonnet
---
```

**After:**
```yaml
---
name: api-impl
description: Implements API endpoints.
tools: Read, Write, Edit, Bash
model: sonnet
skills: api, backend
---
```

**Skill Matching:**
- API agents → `api` skill
- Database agents → `database` skill
- Frontend agents → `frontend` skill
- Generic impl → relevant domain skill

---

### model-optimization

**ID:** `model-optimization`

**When to Suggest:**
- Agent uses opus for simple tasks (waste of resources)
- Agent uses haiku for complex reasoning (underpowered)
- Agent model doesn't match task complexity

**Description:** Select appropriate model based on task complexity.

**Implementation:**

| Task Type | Recommended Model |
|-----------|-------------------|
| Simple, well-defined (API calls, formatting) | `haiku` |
| Implementation, balanced tasks | `sonnet` |
| Complex planning, architecture | `opus` |
| Same as parent | `inherit` |

**Before:**
```yaml
---
name: formatter
description: Formats code according to project standards.
model: opus
---
```

**After:**
```yaml
---
name: formatter
description: Formats code according to project standards.
model: haiku
---
```

---

### permission-mode

**ID:** `permission-mode`

**When to Suggest:**
- Agent runs automated tasks needing fewer prompts
- Agent is for trusted, repetitive operations
- Agent should auto-accept certain tool uses

**Description:** Configure permission handling mode for smoother automation.

**Implementation:**

```yaml
permissionMode: {mode}
```

**Modes:**
| Mode | Description | Use When |
|------|-------------|----------|
| `default` | Normal prompts | Most cases |
| `acceptEdits` | Auto-accept Edit | Trusted file modifications |
| `dontAsk` | Skip prompts | Automated pipelines |
| `plan` | Read-only mode | Exploration only |

**Before:**
```yaml
---
name: auto-formatter
description: Automatically formats all files.
tools: Read, Edit, Glob
---
```

**After:**
```yaml
---
name: auto-formatter
description: Automatically formats all files.
tools: Read, Edit, Glob
permissionMode: acceptEdits
---
```

---

## Anti-Patterns to Avoid

### No Frontmatter

```yaml
# BAD: No frontmatter at all
# Project Init Command

This command initializes projects...
```

**Fix:** Always include at least `name` and `description`.

---

### Overly Permissive Tools

```yaml
# BAD: Agent with all tools when it only needs to read
---
name: analyzer
tools: Read, Write, Edit, Bash, Glob, Grep, Task, WebFetch
---
```

**Fix:** Apply `least-privilege` pattern - restrict to `Read, Grep, Glob`.

---

### Missing Triggers in Description

```yaml
# BAD: No trigger terms
description: Handles API operations
```

**Fix:** Apply `add-triggers` pattern:
```yaml
description: Handles API operations including project creation, task management, and key generation. Use when making API calls or managing backend data.
```

---

### Fork Without Agent

```yaml
# BAD: Fork specified but no agent
---
name: my-skill
context: fork
---
```

**Fix:** Apply `paired-agent` pattern - add `agent` field and create executor.

---

### Wrong Person in Description

```yaml
# BAD: First person
description: I can help you analyze code

# BAD: Second person
description: You can use this to analyze code
```

**Fix:** Use third person:
```yaml
description: Analyzes code for patterns and issues
```

---

### Overkill Model Selection

```yaml
# BAD: Using opus for simple formatting
---
name: code-formatter
model: opus
---
```

**Fix:** Apply `model-optimization` pattern - use `haiku` for simple tasks.
