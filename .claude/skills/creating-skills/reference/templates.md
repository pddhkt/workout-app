# Skill Templates

## Basic Skill Template

```markdown
---
name: my-skill-name
description: Brief description of what this skill does. Use when [specific triggers or contexts].
---

# Skill Title

## Contents

- [Quick Start](#quick-start)
- [Core Functionality](#core-functionality)
- [Examples](#examples)

---

## Quick Start

Minimal example to get started:

\`\`\`language
// Essential code example
\`\`\`

---

## Core Functionality

### Feature A

Description and usage.

### Feature B

Description and usage.

---

## Examples

### Example 1: Basic Usage

\`\`\`language
// Code example
\`\`\`

### Example 2: Advanced Usage

\`\`\`language
// Code example
\`\`\`
```

---

## Skill with Progressive Disclosure Template

```markdown
---
name: complex-skill
description: Handles complex operations with multiple domains. Use when [triggers].
---

# Complex Skill

## Contents

- [Quick Start](#quick-start)
- [Reference Files](#reference-files)
- [Core Concepts](#core-concepts)

---

## Quick Start

Essential info only - get users productive immediately.

---

## Reference Files

Detailed documentation is split into focused files:

| Topic         | File                                             | Description              |
| ------------- | ------------------------------------------------ | ------------------------ |
| **Feature A** | [reference/feature-a.md](reference/feature-a.md) | Detailed A documentation |
| **Feature B** | [reference/feature-b.md](reference/feature-b.md) | Detailed B documentation |
| **Examples**  | [reference/examples.md](reference/examples.md)   | Code examples            |

---

## Core Concepts

Brief overview of key concepts. For details, see reference files.
```

---

## Reference File Template

```markdown
# Topic Title

## Contents

- Section 1
- Section 2
- Section 3

---

## Section 1

### Subsection

Content with code examples.

\`\`\`language
// Example
\`\`\`

---

## Section 2

More content...
```

---

## Workflow Skill Template

For skills that guide multi-step processes:

```markdown
---
name: workflow-skill
description: Guides through [process]. Use when [triggers].
---

# Workflow Skill

## Workflow Steps

Copy this checklist to track progress:

\`\`\`
Progress:

- [ ] Step 1: [Action]
- [ ] Step 2: [Action]
- [ ] Step 3: [Action]
- [ ] Step 4: [Action]
      \`\`\`

---

## Step 1: [Action]

**What to do:**

- Instruction 1
- Instruction 2

**Expected output:**
Description of what should result.

---

## Step 2: [Action]

**What to do:**

- Instruction 1
- Instruction 2

**If errors occur:**

- Troubleshooting guidance

---

## Step 3: [Action]

Continue pattern...

---

## Verification

How to verify the workflow completed successfully.
```

---

## Domain-Specific Organization Template

For skills covering multiple domains:

```markdown
---
name: multi-domain-skill
description: Covers multiple domains (A, B, C). Use when working with [domain triggers].
---

# Multi-Domain Skill

## Available Domains

| Domain       | File                                           | Description         |
| ------------ | ---------------------------------------------- | ------------------- |
| **Domain A** | [reference/domain-a.md](reference/domain-a.md) | A-specific patterns |
| **Domain B** | [reference/domain-b.md](reference/domain-b.md) | B-specific patterns |
| **Domain C** | [reference/domain-c.md](reference/domain-c.md) | C-specific patterns |

## Quick Search

Find specific topics:

\`\`\`bash
grep -i "keyword" reference/domain-a.md
grep -i "keyword" reference/domain-b.md
\`\`\`

## Common Patterns

Patterns that apply across all domains.
```
