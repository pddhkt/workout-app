# Skill Quality Checklist

Use this checklist before sharing a skill.

---

## Core Quality

- [ ] **Name** follows conventions (lowercase, hyphens, gerund form)
- [ ] **Description** is specific and includes trigger terms
- [ ] **Description** written in third person
- [ ] **Description** includes both what it does AND when to use it
- [ ] **SKILL.md body** is under 500 lines
- [ ] **Table of contents** included for quick navigation
- [ ] **No time-sensitive information** (or in "old patterns" section)
- [ ] **Consistent terminology** throughout
- [ ] **Examples are concrete**, not abstract

---

## Structure

- [ ] **File references are one level deep** (SKILL.md → reference files)
- [ ] **No deeply nested references** (A → B → C chains)
- [ ] **Progressive disclosure** used appropriately
- [ ] **Reference files have table of contents** (if over 100 lines)
- [ ] **Descriptive file names** (not doc1.md, doc2.md)
- [ ] **Forward slashes** in all paths (not backslashes)

---

## Content

- [ ] **Quick start section** gets users productive immediately
- [ ] **Code examples** are complete and runnable
- [ ] **Workflows have clear steps** with checkpoints
- [ ] **Error handling** documented where relevant
- [ ] **No over-explanation** of things Claude already knows

---

## Scripts (if applicable)

- [ ] **Scripts handle errors explicitly** (don't punt to Claude)
- [ ] **No magic numbers** (all values justified/documented)
- [ ] **Required packages listed** in instructions
- [ ] **Scripts have clear documentation**
- [ ] **Validation/verification steps** for critical operations

---

## Testing

- [ ] **Tested with real usage scenarios**
- [ ] **Works with target model** (Haiku/Sonnet/Opus)
- [ ] **Skill activates when expected** (description triggers correctly)
- [ ] **Instructions are clear** and don't cause confusion

---

## Common Issues to Check

### Description Problems

- Too vague ("helps with documents")
- Wrong person ("I can help you...")
- Missing triggers ("Use when...")

### Structure Problems

- Over 500 lines in SKILL.md
- Deep reference nesting
- Missing table of contents in long files

### Content Problems

- Over-explaining basics
- Abstract examples instead of concrete ones
- Inconsistent terminology
- Time-sensitive information without versioning

---

## Quick Validation Commands

```bash
# Check line count
wc -l SKILL.md

# Verify all reference links exist
grep -oE '\[.*\]\(.*\.md\)' SKILL.md | while read link; do
  file=$(echo "$link" | grep -oE '\(.*\)' | tr -d '()')
  [ -f "$file" ] || echo "Missing: $file"
done

# Check for Windows paths
grep -E '\\\\' SKILL.md && echo "Found Windows-style paths"
```
