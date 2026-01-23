# Agent Learnings & Best Practices

## UI/UX

### Edge-to-Edge Bottom Bar
To ensure a bottom bar extends behind the system navigation bar (full-bleed) while keeping content safe:

1.  **Remove** `windowInsetsPadding(WindowInsets.navigationBars)` from the top-level container (e.g., `Surface`) so it can extend to the bottom edge.
2.  **Apply** the background color to the inner container *before* applying the insets padding.
3.  **Add** `windowInsetsPadding(WindowInsets.navigationBars)` to the inner container (e.g., `Column`) *after* the background and *before* content padding.

**Example Pattern:**
```kotlin
Surface(
    modifier = Modifier.fillMaxWidth(), // No windowInsetsPadding here
    color = MaterialTheme.colorScheme.surface
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // Background fills the area
            .windowInsetsPadding(WindowInsets.navigationBars) // Push content up
            .padding(16.dp)
    ) {
        // Safe content here
    }
}
```

