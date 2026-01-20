# Analysis Prompts

Questions grouped by pattern for gathering user preferences during interactive refinement.

---

## Pattern Selection (Step 4)

Present applicable patterns based on component analysis. Use multi-select:

```
AskUserQuestion:
  question: "Which improvement patterns would you like to apply?"
  header: "Patterns"
  multiSelect: true
  options:
    - label: "{pattern-name}"
      description: "{brief description of what it does}"
```

### Command Pattern Selection

When component is a command, offer relevant patterns:

```
AskUserQuestion:
  question: "Which patterns would you like to apply to this command?"
  header: "Patterns"
  multiSelect: true
  options:
    - label: "validation-hook"
      description: "Add PreToolUse hook to validate before Bash execution"
    - label: "env-check"
      description: "Validate required environment variables exist"
    - label: "tool-restriction"
      description: "Restrict to minimum needed tools"
    - label: "arguments-usage"
      description: "Add $ARGUMENTS placeholder for user input"
```

### Skill Pattern Selection

When component is a skill:

```
AskUserQuestion:
  question: "Which patterns would you like to apply to this skill?"
  header: "Patterns"
  multiSelect: true
  options:
    - label: "context-fork"
      description: "Fork to isolated subagent for heavy content"
    - label: "paired-agent"
      description: "Create dedicated executor agent"
    - label: "hide-from-menu"
      description: "Hide from /slash menu (programmatic only)"
    - label: "add-triggers"
      description: "Add specific trigger terms to description"
```

### Agent Pattern Selection

When component is an agent:

```
AskUserQuestion:
  question: "Which patterns would you like to apply to this agent?"
  header: "Patterns"
  multiSelect: true
  options:
    - label: "least-privilege"
      description: "Remove unnecessary tools"
    - label: "skill-injection"
      description: "Load domain-specific skills"
    - label: "model-optimization"
      description: "Select appropriate model for task complexity"
    - label: "permission-mode"
      description: "Configure permission handling mode"
```

---

## Preference Questions by Pattern (Step 5)

### validation-hook

**Question 1: What to validate**
```
AskUserQuestion:
  question: "What should the validation hook check?"
  header: "Validation"
  multiSelect: true
  options:
    - label: "Environment variables"
      description: "Check required env vars exist before execution"
    - label: "External services"
      description: "Verify API endpoints are reachable"
    - label: "Prerequisites"
      description: "Check files, directories, or state exist"
    - label: "Input validation"
      description: "Validate command arguments or input format"
```

**Question 2: Failure behavior**
```
AskUserQuestion:
  question: "How should validation failures be handled?"
  header: "On Failure"
  multiSelect: false
  options:
    - label: "Block execution (Recommended)"
      description: "Exit code 2 - prevent tool from running"
    - label: "Warn and continue"
      description: "Exit code 0 - log warning but proceed"
    - label: "Prompt user"
      description: "Ask user whether to proceed or abort"
```

**Question 3: Environment variables (if selected)**
```
AskUserQuestion:
  question: "Which environment variables should be required?"
  header: "Env Vars"
  multiSelect: true
  options:
    - label: "API_URL"
      description: "Base URL for API calls"
    - label: "API_KEY"
      description: "Authentication key"
    - label: "Custom"
      description: "I'll specify custom variable names"
```

---

### env-check

**Question 1: Required variables**
```
AskUserQuestion:
  question: "Which environment variables are required?"
  header: "Required"
  multiSelect: true
  options:
    - label: "API credentials"
      description: "API_URL, API_KEY, API_SECRET"
    - label: "Database connection"
      description: "DATABASE_URL, DB_HOST, DB_PASSWORD"
    - label: "Service URLs"
      description: "CONVEX_URL, BACKEND_URL"
    - label: "Custom"
      description: "I'll specify custom variables"
```

---

### tool-restriction

**Question 1: Tool set selection**
```
AskUserQuestion:
  question: "Which tools does this component actually need?"
  header: "Tools"
  multiSelect: false
  options:
    - label: "Read-only (Recommended for analysis)"
      description: "Read, Grep, Glob - safe, no modifications"
    - label: "Implementation"
      description: "Read, Write, Edit, Bash, Glob, Grep"
    - label: "Orchestration"
      description: "Read, Grep, Glob, Task, AskUserQuestion"
    - label: "Custom selection"
      description: "I'll specify exactly which tools"
```

**Question 2: Custom tools (if selected)**
```
AskUserQuestion:
  question: "Select the specific tools needed:"
  header: "Tools"
  multiSelect: true
  options:
    - label: "Read"
      description: "Read file contents"
    - label: "Grep"
      description: "Search file contents"
    - label: "Glob"
      description: "Find files by pattern"
    - label: "Write"
      description: "Create new files"
    - label: "Edit"
      description: "Modify existing files"
    - label: "Bash"
      description: "Run shell commands"
    - label: "Task"
      description: "Spawn subagents"
```

---

### arguments-usage

**Question 1: Argument placement**
```
AskUserQuestion:
  question: "Where should user arguments appear in the command?"
  header: "Arguments"
  multiSelect: false
  options:
    - label: "At the beginning"
      description: "Target: $ARGUMENTS - place args at start"
    - label: "In context section"
      description: "Add dedicated 'Arguments' section"
    - label: "Multiple locations"
      description: "Reference $ARGUMENTS in several places"
```

---

### context-fork

**Question 1: Agent selection**
```
AskUserQuestion:
  question: "Which agent should handle this forked skill?"
  header: "Agent"
  multiSelect: false
  options:
    - label: "Create new executor"
      description: "Generate {skill-name}-executor agent"
    - label: "Use existing agent"
      description: "Select from available agents"
    - label: "Generic executor"
      description: "Use a general-purpose executor"
```

**Question 2: Agent model (if creating new)**
```
AskUserQuestion:
  question: "What model should the executor agent use?"
  header: "Model"
  multiSelect: false
  options:
    - label: "haiku (Recommended for simple tasks)"
      description: "Fast, cost-effective - good for API calls, formatting"
    - label: "sonnet"
      description: "Balanced - good for implementation tasks"
    - label: "opus"
      description: "Most capable - for complex reasoning"
    - label: "inherit"
      description: "Use same model as parent agent"
```

---

### paired-agent

**Question 1: Agent tools**
```
AskUserQuestion:
  question: "What tools should the paired agent have?"
  header: "Agent Tools"
  multiSelect: false
  options:
    - label: "Minimal (Read, Grep, Glob)"
      description: "Read-only operations"
    - label: "With Bash"
      description: "Read, Grep, Glob, Bash - for API/script execution"
    - label: "Implementation"
      description: "Read, Write, Edit, Bash, Glob, Grep"
    - label: "Custom"
      description: "I'll specify the tools"
```

**Question 2: Model selection**
```
AskUserQuestion:
  question: "What's the complexity of tasks this agent will handle?"
  header: "Complexity"
  multiSelect: false
  options:
    - label: "Simple (use haiku)"
      description: "Well-defined tasks, API calls, formatting"
    - label: "Moderate (use sonnet)"
      description: "Implementation, code generation"
    - label: "Complex (use opus)"
      description: "Architecture decisions, complex reasoning"
```

---

### hide-from-menu

**Question 1: Confirm hiding**
```
AskUserQuestion:
  question: "Confirm this skill should be hidden from the /slash menu?"
  header: "Visibility"
  multiSelect: false
  options:
    - label: "Yes, hide it (Recommended)"
      description: "Only accessible programmatically"
    - label: "No, keep visible"
      description: "Users can invoke directly"
    - label: "Let me reconsider"
      description: "Skip this pattern for now"
```

---

### add-triggers

**Question 1: Domain identification**
```
AskUserQuestion:
  question: "What domain does this component serve?"
  header: "Domain"
  multiSelect: true
  options:
    - label: "API/Backend"
      description: "Add triggers: API calls, HTTP, endpoints"
    - label: "Database"
      description: "Add triggers: schema, migrations, queries"
    - label: "Frontend"
      description: "Add triggers: React, components, UI"
    - label: "DevOps"
      description: "Add triggers: deploy, build, CI/CD"
    - label: "Custom"
      description: "I'll provide custom trigger terms"
```

---

### least-privilege

**Question 1: Actual tool needs**
```
AskUserQuestion:
  question: "What operations does this agent actually perform?"
  header: "Operations"
  multiSelect: true
  options:
    - label: "Read/analyze files"
      description: "Needs: Read, Grep, Glob"
    - label: "Create new files"
      description: "Also needs: Write"
    - label: "Modify existing files"
      description: "Also needs: Edit"
    - label: "Run commands"
      description: "Also needs: Bash"
    - label: "Spawn subagents"
      description: "Also needs: Task"
```

---

### skill-injection

**Question 1: Relevant skills**
```
AskUserQuestion:
  question: "Which skills should this agent have access to?"
  header: "Skills"
  multiSelect: true
  options:
    - label: "api"
      description: "API documentation and patterns"
    - label: "database"
      description: "Database schemas and migrations"
    - label: "frontend"
      description: "Frontend components and patterns"
    - label: "backend"
      description: "Backend service patterns"
    - label: "Custom"
      description: "I'll specify skill names"
```

---

### model-optimization

**Question 1: Task complexity assessment**
```
AskUserQuestion:
  question: "How complex are the tasks this agent handles?"
  header: "Complexity"
  multiSelect: false
  options:
    - label: "Simple, well-defined (use haiku)"
      description: "API calls, formatting, straightforward transforms"
    - label: "Moderate implementation (use sonnet)"
      description: "Code generation, debugging, refactoring"
    - label: "Complex reasoning (use opus)"
      description: "Architecture, planning, multi-step analysis"
    - label: "Same as caller (use inherit)"
      description: "Match whatever model invokes this agent"
```

---

### permission-mode

**Question 1: Permission requirements**
```
AskUserQuestion:
  question: "How should this agent handle permission prompts?"
  header: "Permissions"
  multiSelect: false
  options:
    - label: "Normal prompts (default)"
      description: "Ask for permission as usual"
    - label: "Auto-accept edits (acceptEdits)"
      description: "Auto-approve Edit tool, prompt for others"
    - label: "Skip all prompts (dontAsk)"
      description: "For trusted automation pipelines"
    - label: "Read-only mode (plan)"
      description: "Exploration only, no modifications"
```

---

## Apply Changes (Step 7)

```
AskUserQuestion:
  question: "Apply these changes to {file_path}?"
  header: "Apply"
  multiSelect: false
  options:
    - label: "Yes, apply all"
      description: "Apply all recommended changes"
    - label: "Critical only"
      description: "Apply only critical severity changes"
    - label: "Let me customize"
      description: "Select specific changes to apply"
    - label: "No, don't apply"
      description: "Cancel and keep current configuration"
```

---

## Continue or Done (Step 8)

```
AskUserQuestion:
  question: "Continue refining this component or done?"
  header: "Next"
  multiSelect: false
  options:
    - label: "Continue refining"
      description: "Look for more improvements"
    - label: "Done with this component"
      description: "Finish reviewing this file"
    - label: "Review another component"
      description: "Move to a different file"
```

---

## Initial Checklist Questions

Before pattern proposal, verify basics are correct:

### Name Validation
- Is name lowercase with hyphens only?
- Is name under 64 characters?
- Does name avoid reserved words?

### Description Validation
- Is description in third person?
- Does description include trigger terms?
- Does description explain when to use?

### Component-Specific Checks

**For Commands:**
- Does body reference $ARGUMENTS if useful?
- Are tools restricted via allowed-tools?
- Are hooks configured for Bash usage?

**For Skills:**
- Is user-invocable appropriate?
- Is context:fork needed for heavy content?
- Is agent specified if forking?

**For Agents:**
- Are tools minimally scoped?
- Is model appropriate for complexity?
- Are relevant skills loaded?
