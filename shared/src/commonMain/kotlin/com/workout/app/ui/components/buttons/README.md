# Button Components

Atomic button components for the Workout App. All buttons support enabled/disabled states and follow the Material3 design system with custom theming.

## Components

### PrimaryButton
Filled button with primary green accent color.

**Usage:**
```kotlin
PrimaryButton(
    text = "Start Workout",
    onClick = { /* handle click */ }
)

// Full-width variant
PrimaryButton(
    text = "Continue",
    onClick = { /* handle click */ },
    fullWidth = true
)

// Disabled state
PrimaryButton(
    text = "Complete",
    onClick = { },
    enabled = false
)
```

**Properties:**
- Height: 48dp
- Corner radius: 8dp
- Colors: Primary (#13EC5B) / OnPrimary (#0A0A0A)
- Based on mockup elements: EL-14, EL-15, EL-34, EL-35

---

### SecondaryButton
Outlined button with transparent background.

**Usage:**
```kotlin
SecondaryButton(
    text = "Cancel",
    onClick = { /* handle click */ }
)

// Full-width variant
SecondaryButton(
    text = "Skip",
    onClick = { /* handle click */ },
    fullWidth = true
)
```

**Properties:**
- Height: 48dp
- Corner radius: 8dp
- Border: 1dp Primary color
- Background: Transparent
- Based on mockup elements: EL-38, EL-83

---

### AppIconButton
Standard icon button with transparent background.

**Usage:**
```kotlin
AppIconButton(
    icon = Icons.Default.Edit,
    contentDescription = "Edit exercise",
    onClick = { /* handle click */ }
)

// Custom tint
AppIconButton(
    icon = Icons.Default.Delete,
    contentDescription = "Delete",
    onClick = { /* handle click */ },
    tint = MaterialTheme.colorScheme.error
)
```

**Properties:**
- Size: 40dp
- Icon size: 24dp
- Background: Transparent
- Based on mockup elements: EL-85, EL-93

---

### FilledIconButton
Icon button with filled background.

**Usage:**
```kotlin
FilledIconButton(
    icon = Icons.Default.Favorite,
    contentDescription = "Favorite",
    onClick = { /* handle click */ }
)

// Custom colors
FilledIconButton(
    icon = Icons.Default.Add,
    contentDescription = "Add",
    onClick = { /* handle click */ },
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary
)
```

**Properties:**
- Size: 40dp
- Icon size: 24dp
- Corner radius: 8dp
- Default background: SurfaceVariant
- Based on mockup elements: EL-94, EL-101

---

### AppFloatingActionButton (FAB)
Floating action button with elevation.

**Usage:**
```kotlin
AppFloatingActionButton(
    icon = Icons.Default.Add,
    contentDescription = "Add exercise",
    onClick = { /* handle click */ }
)

// Custom colors
AppFloatingActionButton(
    icon = Icons.Default.Add,
    contentDescription = "Add",
    onClick = { /* handle click */ },
    containerColor = MaterialTheme.colorScheme.secondary,
    contentColor = MaterialTheme.colorScheme.onSecondary
)
```

**Properties:**
- Corner radius: 16dp
- Elevation: 6dp (default), 8dp (pressed/hovered)
- Default colors: Primary / OnPrimary
- Based on mockup element: EL-102

---

### ToggleButton
Button with selected/unselected states and animated transitions.

**Usage:**
```kotlin
var selected by remember { mutableStateOf(false) }

ToggleButton(
    text = "Reps",
    selected = selected,
    onClick = { selected = !selected }
)

// Multiple toggles for filter/segmented control
Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    ToggleButton(
        text = "All",
        selected = filter == Filter.All,
        onClick = { filter = Filter.All }
    )
    ToggleButton(
        text = "Active",
        selected = filter == Filter.Active,
        onClick = { filter = Filter.Active }
    )
    ToggleButton(
        text = "Completed",
        selected = filter == Filter.Completed,
        onClick = { filter = Filter.Completed }
    )
}
```

**Properties:**
- Height: 40dp
- Corner radius: 8dp
- Selected: Primary fill with OnPrimary text
- Unselected: Outlined with OnSurface text
- Animated color transitions

---

## Preview

To view all button variants:

1. **Android Studio**: Open `composeApp/src/androidMain/kotlin/com/workout/app/ui/preview/ButtonPreviews.kt` and use the Android Studio preview panel
2. **Showcase Screen**: Add `ButtonShowcase()` composable from `ButtonPreviews.kt` to any screen

## Design Tokens Used

- **Colors**: Primary, OnPrimary, Surface, OnSurface, Error, Outline
- **Spacing**: xs (4dp), sm (8dp), md (12dp), lg (16dp), xl (24dp)
- **Typography**: labelLarge (14sp, SemiBold), labelMedium (12sp, SemiBold)
- **Shapes**: RoundedCornerShape with 8dp or 16dp radius

## Accessibility

All button components include:
- Content description support for icon buttons
- Proper contrast ratios in disabled states (38% opacity)
- Touch target sizes meeting Material3 guidelines (min 48dp for buttons, 40dp for icon buttons)
- State changes communicated through color and opacity

## Platform Support

All components are in `commonMain` and work across:
- Android
- iOS
- Desktop (JVM)
- Web (when supported)
