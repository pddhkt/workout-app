# Web Scraping Patterns

Advanced data extraction techniques using agent-browser.

## Contents

- [Structured Data Extraction](#structured-data-extraction)
- [Pagination Handling](#pagination-handling)
- [Dynamic Content](#dynamic-content)
- [Authentication](#authentication)

## Structured Data Extraction

### Extract List Items

```bash
agent-browser eval "Array.from(document.querySelectorAll('.product')).map(el => ({
  name: el.querySelector('.title')?.textContent?.trim(),
  price: el.querySelector('.price')?.textContent?.trim(),
  url: el.querySelector('a')?.href
}))"
```

### Extract Table to JSON

```bash
agent-browser eval "(() => {
  const headers = Array.from(document.querySelectorAll('th')).map(th => th.textContent.trim());
  return Array.from(document.querySelectorAll('tbody tr')).map(row => {
    const cells = Array.from(row.cells).map(cell => cell.textContent.trim());
    return Object.fromEntries(headers.map((h, i) => [h, cells[i]]));
  });
})()"
```

### Extract Links with Context

```bash
agent-browser eval "Array.from(document.querySelectorAll('a[href]')).map(a => ({
  text: a.textContent.trim(),
  href: a.href,
  parent: a.parentElement?.className
})).filter(l => l.text)"
```

## Pagination Handling

### Click Through Pages

```bash
# Get initial data
agent-browser eval "extractData()"

# Check for next page
agent-browser snapshot
# Look for pagination ref, e.g., @e15

agent-browser click @e15  # Next page button
agent-browser wait 1000
agent-browser eval "extractData()"
```

### Infinite Scroll

```bash
agent-browser eval "window.scrollTo(0, document.body.scrollHeight)"
agent-browser wait 2000
agent-browser eval "document.querySelectorAll('.item').length"  # Check if more loaded
```

## Dynamic Content

### Wait for AJAX

```bash
agent-browser open https://spa-app.com
agent-browser wait "[data-loaded]"
agent-browser snapshot
```

### Wait for Specific Element

```bash
agent-browser wait ".results-container"
agent-browser eval "document.querySelector('.results-container').innerHTML"
```

### Trigger Lazy Load

```bash
agent-browser eval "document.querySelectorAll('img[data-src]').forEach(img => {
  img.src = img.dataset.src;
})"
agent-browser wait 2000
```

## Authentication

### Login Flow

```bash
agent-browser open https://example.com/login
agent-browser snapshot
agent-browser fill @e2 "username"
agent-browser fill @e3 "password"
agent-browser click @e4
agent-browser wait 2000
agent-browser snapshot  # Verify logged in
```

### Cookie-Based Session

```bash
# After login, cookies are preserved
agent-browser cookies  # View current cookies

# Navigate to protected pages
agent-browser open https://example.com/dashboard
```

## Output Patterns

### Save to File

```bash
agent-browser eval "JSON.stringify(extractedData, null, 2)" > data.json
```

### CSV Format

```bash
agent-browser eval "data.map(row => Object.values(row).join(',')).join('\n')"
```
