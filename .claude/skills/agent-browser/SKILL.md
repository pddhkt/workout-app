---
name: agent-browser
description: Controls headless Chromium browser for web automation, scraping, and testing using agent-browser CLI. Use when automating browser tasks, scraping websites, testing web UIs, or when user mentions browser automation, web scraping, headless browser, or agent-browser.
---

# Agent Browser Automation

Fast browser automation CLI optimized for AI agents. Uses Playwright's isolated Chromium.

## Contents

- [Quick Start](#quick-start)
- [Core Commands](#core-commands)
- [AI Agent Workflow](#ai-agent-workflow)
- [Common Patterns](#common-patterns)
- [Reference Files](#reference-files)

## Quick Start

```bash
# Open a page
agent-browser open https://example.com

# Get accessibility tree with refs (primary way to understand page)
agent-browser snapshot

# Interact using refs from snapshot
agent-browser click @e2
agent-browser type @e3 "search query"
agent-browser press Enter

# Take screenshot
agent-browser screenshot page.png

# Close browser
agent-browser close
```

## Core Commands

### Navigation

| Command | Description |
|---------|-------------|
| `open <url>` | Navigate to URL |
| `back` / `forward` | Browser history |
| `reload` | Reload page |

### Interaction

| Command | Description |
|---------|-------------|
| `click <sel>` | Click element (`@ref` or CSS selector) |
| `dblclick <sel>` | Double-click |
| `type <sel> <text>` | Type into element (appends) |
| `fill <sel> <text>` | Clear and fill |
| `press <key>` | Press key (`Enter`, `Tab`, `Control+a`) |
| `check <sel>` / `uncheck <sel>` | Checkbox toggle |
| `select <sel> <val>` | Select dropdown option |
| `hover <sel>` | Hover element |
| `drag <src> <dst>` | Drag and drop |
| `upload <sel> <files>` | Upload files |

### Inspection

| Command | Description |
|---------|-------------|
| `snapshot` | **Accessibility tree with refs** (use this for AI) |
| `screenshot [path]` | Take screenshot |
| `pdf <path>` | Save as PDF |
| `eval <js>` | Run JavaScript |
| `source` | Get page source |
| `cookies` | Get cookies |

### Scrolling & Waiting

| Command | Description |
|---------|-------------|
| `scroll <dir> [px]` | Scroll (`up`/`down`/`left`/`right`) |
| `scrollintoview <sel>` | Scroll element into view |
| `wait <sel\|ms>` | Wait for element or milliseconds |

### Session

| Command | Description |
|---------|-------------|
| `close` | Close browser |
| `connect <port>` | Connect to existing browser via CDP |

## AI Agent Workflow

The `snapshot` command returns an accessibility tree with `@ref` identifiers. This is the primary way AI agents should understand and interact with pages.

### Step 1: Get Page State

```bash
agent-browser open https://example.com
agent-browser snapshot
```

Output:
```
- document:
  - heading "Welcome" [ref=e1] [level=1]
  - textbox "Email" [ref=e2]
  - textbox "Password" [ref=e3]
  - button "Sign In" [ref=e4]
```

### Step 2: Interact Using Refs

```bash
agent-browser fill @e2 "user@example.com"
agent-browser fill @e3 "password123"
agent-browser click @e4
```

### Step 3: Verify Result

```bash
agent-browser snapshot  # Check new page state
agent-browser screenshot result.png
```

## Common Patterns

### Web Scraping

```bash
agent-browser open https://news.ycombinator.com
agent-browser eval "Array.from(document.querySelectorAll('.titleline a')).map(a => ({title: a.textContent, url: a.href}))"
```

### Form Submission

```bash
agent-browser open https://example.com/contact
agent-browser snapshot
agent-browser fill @e2 "John Doe"
agent-browser fill @e3 "john@example.com"
agent-browser fill @e4 "Hello, this is my message"
agent-browser click @e5  # Submit button
```

### Testing UI Elements

```bash
agent-browser open http://localhost:3000
agent-browser snapshot
agent-browser eval "document.querySelectorAll('button').length"
agent-browser screenshot before.png
agent-browser click @e3
agent-browser wait 1000
agent-browser screenshot after.png
```

### Extract Table Data

```bash
agent-browser eval "Array.from(document.querySelectorAll('table tr')).map(row => Array.from(row.cells).map(cell => cell.textContent))"
```

### Wait for Dynamic Content

```bash
agent-browser open https://spa-app.com
agent-browser wait "[data-loaded='true']"
agent-browser snapshot
```

## Reference Files

- **[Scraping Patterns](reference/scraping.md)**: Advanced data extraction techniques
- **[Testing Workflows](reference/testing.md)**: UI testing and validation patterns
- **[Troubleshooting](reference/troubleshooting.md)**: Common issues and solutions

## Notes

- Uses Playwright's **isolated Chromium** - does NOT affect your Chrome browser
- Runs **headless by default** - no window appears
- Browser persists across commands until `close`
- Refs (`@e1`, `@e2`) are regenerated on each `snapshot` call
- For Linux: may need `sudo npx playwright install-deps chromium`
