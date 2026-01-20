# Testing Workflows

UI testing and validation patterns using agent-browser.

## Contents

- [Visual Regression](#visual-regression)
- [Functional Testing](#functional-testing)
- [Accessibility Testing](#accessibility-testing)
- [Performance Checks](#performance-checks)

## Visual Regression

### Before/After Screenshots

```bash
agent-browser open http://localhost:3000
agent-browser screenshot baseline.png

# Make changes to the app...

agent-browser reload
agent-browser screenshot current.png

# Compare images externally
```

### Full Page Capture

```bash
agent-browser screenshot --full fullpage.png
```

### Specific Element

```bash
agent-browser eval "document.querySelector('.header').getBoundingClientRect()"
# Use coordinates to crop screenshot
```

## Functional Testing

### Form Validation

```bash
# Test empty submission
agent-browser open http://localhost:3000/form
agent-browser snapshot
agent-browser click @e5  # Submit button
agent-browser snapshot   # Check for error messages

# Test valid input
agent-browser fill @e2 "valid@email.com"
agent-browser click @e5
agent-browser wait 1000
agent-browser snapshot   # Verify success
```

### Navigation Testing

```bash
agent-browser open http://localhost:3000
agent-browser snapshot
agent-browser click @e3  # Menu link
agent-browser eval "window.location.pathname"  # Verify URL changed
agent-browser back
agent-browser eval "window.location.pathname"  # Verify back works
```

### Interactive Elements

```bash
# Test dropdown
agent-browser snapshot
agent-browser select @e4 "option2"
agent-browser eval "document.querySelector('select').value"

# Test checkbox
agent-browser check @e6
agent-browser eval "document.querySelector('input[type=checkbox]').checked"

# Test modal
agent-browser click @e7  # Open modal button
agent-browser wait ".modal"
agent-browser snapshot
agent-browser press Escape
agent-browser eval "document.querySelector('.modal')?.style.display"
```

## Accessibility Testing

### Get Accessibility Tree

```bash
agent-browser snapshot
# Review tree structure for proper hierarchy, labels, roles
```

### Check ARIA Labels

```bash
agent-browser eval "Array.from(document.querySelectorAll('[role]')).map(el => ({
  role: el.getAttribute('role'),
  label: el.getAttribute('aria-label'),
  tag: el.tagName
}))"
```

### Keyboard Navigation

```bash
agent-browser press Tab
agent-browser eval "document.activeElement.tagName"
agent-browser press Tab
agent-browser eval "document.activeElement.tagName"
agent-browser press Enter  # Activate focused element
```

### Color Contrast Check

```bash
agent-browser eval "(() => {
  const el = document.querySelector('.text-element');
  const style = getComputedStyle(el);
  return { color: style.color, background: style.backgroundColor };
})()"
```

## Performance Checks

### Page Load Timing

```bash
agent-browser open https://example.com
agent-browser eval "JSON.stringify(performance.timing)"
```

### Resource Count

```bash
agent-browser eval "performance.getEntriesByType('resource').length"
```

### DOM Size

```bash
agent-browser eval "document.querySelectorAll('*').length"
```

### Memory Usage

```bash
agent-browser eval "performance.memory ? {
  used: Math.round(performance.memory.usedJSHeapSize / 1024 / 1024) + 'MB',
  total: Math.round(performance.memory.totalJSHeapSize / 1024 / 1024) + 'MB'
} : 'Not available'"
```

## Test Assertions Pattern

```bash
# Pattern for validating conditions
agent-browser eval "(() => {
  const tests = [];

  // Test 1: Title exists
  tests.push({
    name: 'Page title',
    pass: document.title.length > 0,
    value: document.title
  });

  // Test 2: No console errors (check manually)
  tests.push({
    name: 'Main content exists',
    pass: !!document.querySelector('main'),
    value: !!document.querySelector('main')
  });

  return tests;
})()"
```
