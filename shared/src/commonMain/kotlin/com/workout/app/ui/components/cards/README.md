# Card Components

Foundation card components providing consistent elevation, rounded corners, and padding across the app.

## Components

### BaseCard

The fundamental card component with full customization options.

**Parameters:**
- `modifier` - Modifier to apply to the card
- `onClick` - Optional click handler (null for non-clickable)
- `enabled` - Whether card is interactive (default: true)
- `shape` - Corner radius shape (default: 12dp rounded)
- `elevation` - Shadow depth (default: 1dp)
- `backgroundColor` - Card background color (default: Material3 surface)
- `contentColor` - Content color (default: Material3 onSurface)
- `border` - Optional border for selected states
- `contentPadding` - Inner padding (default: 16dp)
- `content` - Composable content slot

**Usage:**
```kotlin
BaseCard(
    modifier = Modifier.fillMaxWidth(),
    onClick = { /* handle click */ }
) {
    Text("Card content")
}
```

---

### ElevatedCard

Card with higher shadow (4dp) for emphasized content.

**Usage:**
```kotlin
ElevatedCard(
    modifier = Modifier.fillMaxWidth()
) {
    Text("Important content")
}
```

---

### FlatCard

Card with no elevation (0dp) for subtle or nested appearance.

**Usage:**
```kotlin
FlatCard(
    modifier = Modifier.fillMaxWidth()
) {
    Text("Grouped content")
}
```

---

### OutlinedCard

Card with border, typically for selection or grouping.

**Parameters:**
- `borderColor` - Border color (default: Primary)
- `borderWidth` - Border width in dp (default: 2dp)

**Usage:**
```kotlin
OutlinedCard(
    modifier = Modifier.fillMaxWidth(),
    borderColor = Primary
) {
    Text("Selected item")
}
```

---

### SelectedCard

Combined border and elevation for selected state emphasis.

**Usage:**
```kotlin
SelectedCard(
    modifier = Modifier.fillMaxWidth()
) {
    Text("Currently selected")
}
```

---

### ColoredCard

Card with colored background for special states.

**Parameters:**
- `surfaceColor` - Background color

**Usage:**
```kotlin
ColoredCard(
    surfaceColor = Completed,
    modifier = Modifier.fillMaxWidth()
) {
    Text("Completed workout")
}
```

---

## Design Tokens Used

- **Corner Radius:** 12dp (RoundedCornerShape)
- **Elevation:** 0dp (flat), 1dp (default), 2dp (selected), 4dp (elevated)
- **Padding:** 16dp (AppTheme.spacing.lg)
- **Border Width:** 2dp (outlined/selected variants)

## Theme Colors

Cards integrate with the following theme colors:
- `Surface` - Default background
- `OnSurface` - Default content color
- `Primary` - Border color for outlined/selected
- `Completed` - Success state background
- `Active` - Active workout background
- `Error` - Warning state background

## States

### Interactive States
- **Default** - Normal appearance
- **Pressed** - Elevation +2dp
- **Focused** - Elevation +2dp
- **Hovered** - Elevation +2dp
- **Dragged** - Elevation +4dp
- **Disabled** - 50% opacity, no interaction

### Visual States
- **Standard** - Default surface with 1dp elevation
- **Elevated** - Higher shadow (4dp)
- **Flat** - No shadow (0dp)
- **Outlined** - Border with no elevation
- **Selected** - Border + 2dp elevation
- **Colored** - Custom background color

## Accessibility

Cards support:
- Click handling with proper touch target size
- Enabled/disabled states
- Semantic content descriptions (add to content composables)

## Preview

See `CardPreviews.kt` for comprehensive examples:
- `CardPreviewsScreen()` - Full preview screen with all variants
- Individual preview composables for Android Studio previews

## Future Extensions

Potential card types to build on this foundation:
- TemplateCard - Workout template cards
- ExerciseCard - Individual exercise cards
- ProgressCard - Progress tracking cards
- StatCard - Statistics display cards
- AchievementCard - Achievement/badge cards
