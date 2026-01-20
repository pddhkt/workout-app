---
name: reviewing-skills
description: Reviews and refines Claude Code commands, skills, and agents interactively. Analyzes configurations, proposes improvement patterns, gathers preferences, and applies changes. Use when reviewing .claude/ files, optimizing workflows, or improving component configurations.
allowed-tools:
  - Read
  - Grep
  - Glob
  - Edit
  - Write
  - AskUserQuestion
---

# Reviewing Skills

Interactive skill for reviewing and refining Claude Code components one at a time.

## How to Use

This skill works with **one component per session** through an iterative refinement cycle.

**Start a review:**
```
"Review .claude/commands/project-init.md"
"Review the api skill"
"Help me improve the scout agent"
```

**Or list available components:**
```
"What skills can I review?"
"Show me my agents"
```

---

## Interactive Refinement Flow

```
┌─────────────────────────────────────────────────────────────┐
│ Step 1: Target Selection                                    │
│   User provides skill/command/agent path                    │
│   OR list available components for selection                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 2: Read & Analyze                                      │
│   - Read target file                                        │
│   - Identify component type                                 │
│   - Parse current frontmatter                               │
│   - Analyze body structure                                  │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 3: Present Current State                               │
│   - Show current frontmatter                                │
│   - Highlight what's configured vs missing                  │
│   - Show component-specific checklist results               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 4: Propose Applicable Patterns                         │
│   Based on component type and analysis, suggest patterns:   │
│   - validation-hook, env-check, tool-restriction           │
│   - context-fork, paired-agent, hide-from-menu             │
│   - least-privilege, skill-injection, model-optimization   │
│   User selects which patterns to apply                      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 5: Gather Preferences                                  │
│   For selected patterns, ask targeted questions:            │
│   - "Which tools does this actually need?"                  │
│   - "Should errors block execution or just warn?"           │
│   - "Which model is appropriate for complexity?"            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 6: Generate Recommendations                            │
│   - Show before/after frontmatter diff                      │
│   - Explain each change                                     │
│   - Provide severity: critical / suggested / optional       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 7: Apply Changes                                       │
│   Ask: "Apply these changes?"                               │
│   - Apply selected improvements                             │
│   - Show final result                                       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 8: Continue or Done                                    │
│   "Continue refining or done with this component?"          │
│   → Loop back to Step 3 if continuing                       │
└─────────────────────────────────────────────────────────────┘
```

---

## Step-by-Step Instructions

### Step 1: Target Selection

If the user provides a path, use it directly. Otherwise, help them select:

```bash
# List commands
ls .claude/commands/*.md 2>/dev/null

# List skills
ls .claude/skills/*/SKILL.md 2>/dev/null

# List agents
ls .claude/agents/*.md 2>/dev/null
```

### Step 2: Read & Analyze

Read the target file and identify:

1. **Component type** based on path:
   - `.claude/commands/` → Command
   - `.claude/skills/` → Skill
   - `.claude/agents/` → Agent

2. **Parse frontmatter** between `---` delimiters

3. **Analyze body** for tool usage, workflow patterns

### Step 3: Present Current State

Show a summary:

```markdown
## Current State: {component_name}

**Type:** {Command | Skill | Agent}
**Location:** {file_path}

### Current Frontmatter
```yaml
{current frontmatter}
```

### Configuration Checklist
- [x] name: valid format
- [x] description: present
- [ ] allowed-tools: not configured
- [ ] hooks: no validation hooks
```

### Step 4: Propose Applicable Patterns

Based on analysis, propose relevant patterns from the catalog. Use `AskUserQuestion` with multi-select:

```
Which patterns would you like to apply?

[ ] validation-hook - Add PreToolUse hook to validate before Bash execution
[ ] tool-restriction - Restrict to minimum needed tools
[ ] context-fork - Fork to isolated subagent for heavy content
```

See [Pattern Catalog](reference/patterns.md) for full list with triggers.

### Step 5: Gather Preferences

For each selected pattern, ask targeted questions. See [Analysis Prompts](reference/analysis-prompts.md) for questions by pattern.

Example for `validation-hook`:
```
What should the validation check?
- [ ] Required environment variables
- [ ] External service availability
- [ ] Valid state/prerequisites
```

Example for `tool-restriction`:
```
Which tools does this component actually need?
- [ ] Read, Grep, Glob (read-only)
- [ ] Read, Write, Edit, Bash (implementation)
- [ ] Custom selection
```

### Step 6: Generate Recommendations

Show before/after diff with severity:

```markdown
## Recommendations

### Critical
1. **Add missing description triggers**
   - Current: "Handles API operations"
   - Suggested: "Handles API operations including project creation and task management. Use when making API calls or managing backend data."

### Suggested
2. **Add validation hook**
   - Pattern: validation-hook
   - Checks: CONVEX_SITE_URL, ADMIN_API_KEY

### Optional
3. **Restrict tools**
   - Current: all tools
   - Suggested: Read, Grep, Glob, Bash

### Before/After Frontmatter

**Before:**
```yaml
---
name: my-command
description: Handles API operations
---
```

**After:**
```yaml
---
name: my-command
description: Handles API operations including project creation and task management. Use when making API calls or managing backend data.
allowed-tools:
  - Read
  - Grep
  - Glob
  - Bash
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: ".claude/scripts/validate-env.sh"
---
```
```

### Step 7: Apply Changes

Ask for confirmation:
```
Apply these changes to {file_path}?
- Yes, apply all
- Apply critical only
- Let me customize
- No, don't apply
```

If approved, use `Edit` or `Write` to apply changes. Show the final result.

### Step 8: Continue or Done

```
Continue refining this component or done?
- Continue (loop to Step 3)
- Done with this component
- Review another component
```

---

## Reference Files

- **[Pattern Catalog](reference/patterns.md)** - Full pattern list with triggers and implementations
- **[Analysis Prompts](reference/analysis-prompts.md)** - Questions grouped by pattern
- **[Frontmatter Reference](reference/frontmatter-reference.md)** - Complete field documentation

---

## Quick Pattern Reference

### Command Patterns
| Pattern | When to Suggest |
|---------|-----------------|
| `validation-hook` | Uses Bash |
| `env-check` | Calls APIs |
| `tool-restriction` | Has many tools or no restrictions |
| `arguments-usage` | No $ARGUMENTS in body |

### Skill Patterns
| Pattern | When to Suggest |
|---------|-----------------|
| `context-fork` | Heavy content (>300 lines) |
| `paired-agent` | Has context:fork but no agent |
| `hide-from-menu` | Internal utility |
| `add-triggers` | Vague description |

### Agent Patterns
| Pattern | When to Suggest |
|---------|-----------------|
| `least-privilege` | Has Write/Edit but read-only purpose |
| `skill-injection` | Domain-specific without skills |
| `model-optimization` | Wrong model for task complexity |
| `permission-mode` | Needs auto-approval |
