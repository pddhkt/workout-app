# Troubleshooting

Common issues and solutions for agent-browser.

## Contents

- [Installation Issues](#installation-issues)
- [Runtime Errors](#runtime-errors)
- [Element Selection](#element-selection)
- [Performance](#performance)

## Installation Issues

### "Chromium not found"

```bash
agent-browser install
```

### Linux: Missing Dependencies

```bash
# Option 1: Auto-install
agent-browser install --with-deps

# Option 2: Manual install
sudo npx playwright install-deps chromium
```

### Arch Linux: Specific Dependencies

```bash
sudo pacman -S nss at-spi2-core libcups libxkbcommon libxcomposite libxdamage libxrandr mesa libgbm pango cairo alsa-lib
```

### Permission Denied

```bash
# Check npm global directory permissions
npm config get prefix
ls -la $(npm config get prefix)/lib/node_modules/
```

## Runtime Errors

### "Browser not started"

The browser must be started with `open` before other commands:

```bash
agent-browser open https://example.com
# Now other commands work
agent-browser snapshot
```

### "No page open"

```bash
agent-browser open https://example.com
```

### "Target closed"

Page was closed or navigated away. Reopen:

```bash
agent-browser open https://example.com
```

### Timeout Errors

Increase wait time or check if element exists:

```bash
agent-browser wait 5000  # Wait 5 seconds
agent-browser snapshot   # Check page state
```

### "Element not found"

1. Run `snapshot` to see current refs
2. Verify the ref still exists (refs change on page updates)
3. Try waiting for the element: `agent-browser wait <selector>`

## Element Selection

### Refs Not Working

Refs are regenerated on each snapshot. Always get fresh refs:

```bash
agent-browser snapshot     # Get current refs
agent-browser click @e3    # Use ref from latest snapshot
```

### CSS Selector Not Found

Verify selector with eval:

```bash
agent-browser eval "document.querySelector('.my-class')"
agent-browser eval "document.querySelectorAll('.my-class').length"
```

### Element in iframe

Agent-browser works with the main frame. For iframes, use eval:

```bash
agent-browser eval "document.querySelector('iframe').contentDocument.querySelector('button').click()"
```

### Hidden Elements

```bash
# Check if element is visible
agent-browser eval "(() => {
  const el = document.querySelector('.element');
  const style = getComputedStyle(el);
  return {
    display: style.display,
    visibility: style.visibility,
    opacity: style.opacity
  };
})()"
```

## Performance

### Slow Commands

Use headless mode (default). Headed mode is slower:

```bash
# Headless (fast) - default
agent-browser open https://example.com

# Only use headed for debugging
```

### Memory Issues

Close browser when done:

```bash
agent-browser close
```

### Multiple Sessions

Each `open` reuses the same browser instance. For fresh sessions:

```bash
agent-browser close
agent-browser open https://example.com
```

## Debugging

### See What's Happening

Take screenshots at each step:

```bash
agent-browser open https://example.com
agent-browser screenshot step1.png
agent-browser click @e2
agent-browser screenshot step2.png
```

### Check Page State

```bash
agent-browser eval "document.readyState"
agent-browser eval "window.location.href"
agent-browser eval "document.title"
```

### View Console Errors

```bash
agent-browser eval "(() => {
  const errors = [];
  const originalError = console.error;
  console.error = (...args) => { errors.push(args); originalError(...args); };
  return errors;
})()"
```

### Network Requests

```bash
agent-browser eval "performance.getEntriesByType('resource').map(r => ({
  name: r.name.split('/').pop(),
  duration: Math.round(r.duration) + 'ms'
}))"
```

## Common Mistakes

1. **Using stale refs** - Always run `snapshot` before interacting
2. **Not waiting for page load** - Use `wait` after navigation
3. **Forgetting to close** - Always `agent-browser close` when done
4. **Wrong selector syntax** - Use `@ref` for refs, CSS for selectors
