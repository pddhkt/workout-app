# Lemon Workouts Design System

A comprehensive design system for the Lemon Workouts mobile app, built with Jetpack Compose and Kotlin Multiplatform.

## Table of Contents

- [Color Palette](#color-palette)
- [Typography](#typography)
- [Spacing](#spacing)
- [Shadows](#shadows)
- [Border Radius](#border-radius)
- [Components](#components)
- [Usage Guidelines](#usage-guidelines)

---

## Color Palette

### Brand Colors (Citrus Theme)

| Token | Hex | RGB | Usage |
|-------|-----|-----|-------|
| `Primary` | `#FFD400` | `255, 212, 0` | Main brand color, CTAs, active states |
| `PrimaryDark` | `#E6BE00` | `230, 190, 0` | Hover states, pressed states |
| `OnPrimary` | `#332B2B` | `51, 43, 43` | Text/icons on primary backgrounds |
| `Secondary` | `#FFE600` | `255, 230, 0` | Gradient start, highlights |
| `Tertiary` | `#F7C500` | `247, 197, 0` | Gradient end (Golden Honey) |

### Accent Colors

| Token | Hex | RGB | Usage |
|-------|-----|-----|-------|
| `AccentGreen` | `#6ACCBC` | `106, 204, 188` | Beginner badges, yoga theme |
| `AccentOrange` | `#FB923C` | `251, 146, 60` | High intensity, cardio theme |

### Background Colors

| Token | Light Mode | Dark Mode | Usage |
|-------|------------|-----------|-------|
| `Background` | `#F8F8F7` | `#2C2821` | Page background |
| `Surface` | `#FFFFFF` | `#3D362B` | Card surfaces |
| `SurfaceVariant` | `#FFFBF0` | `#3D362B` | Card backgrounds (Pale Honey Cream) |

### Text Colors

| Token | Light Mode | Dark Mode | Usage |
|-------|------------|-----------|-------|
| `OnBackground` | `#332B2B` | `#FFFFFF` | Primary text |
| `OnSurface` | `#332B2B` | `#FFFFFF` | Text on cards |
| `OnSurfaceVariant` | `#888888` | `#9CA3AF` | Secondary text, labels |

### Border Colors

| Token | Light Mode | Dark Mode | Usage |
|-------|------------|-----------|-------|
| `Border` | `#E5E5E5` | `#4A4A4A` | Default borders |
| `BorderLight` | `#F0F0F0` | `#3D3D3D` | Subtle borders |

### Status Colors

| Token | Hex | Usage |
|-------|-----|-------|
| `Success` | `#22C55E` | Completed states, success messages |
| `Warning` | `#FACC15` | Warning states |
| `Error` | `#EF4444` | Error states |
| `Info` | `#3B82F6` | Informational states |

### Exercise State Colors

| Token | Hex | Usage |
|-------|-----|-------|
| `Completed` | `#22C55E` | Completed exercises/sets |
| `Active` | `#FFD400` | Currently active exercise |
| `Pending` | `#666666` | Upcoming exercises/sets |

---

## Typography

### Font Family

**Primary:** Spline Sans (or system default sans-serif)

### Type Scale

| Style | Size | Weight | Line Height | Letter Spacing | Usage |
|-------|------|--------|-------------|----------------|-------|
| `headlineLarge` | 28sp | Bold (700) | 36sp | 0sp | Page titles |
| `headlineMedium` | 24sp | Bold (700) | 32sp | 0sp | Section titles |
| `headlineSmall` | 20sp | SemiBold (600) | 28sp | 0sp | Card titles |
| `bodyLarge` | 16sp | Normal (400) | 24sp | 0.5sp | Primary body text |
| `bodyMedium` | 14sp | Normal (400) | 20sp | 0.25sp | Secondary body text |
| `bodySmall` | 12sp | Normal (400) | 16sp | 0.4sp | Captions, metadata |
| `labelLarge` | 14sp | SemiBold (600) | 20sp | 0.1sp | Button text |
| `labelMedium` | 12sp | SemiBold (600) | 16sp | 0.5sp | Chip text |
| `labelSmall` | 10sp | SemiBold (600) | 14sp | 0.5sp | Small labels, badges |

---

## Spacing

A consistent spacing scale using 4dp base unit.

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4dp | Tight spacing, icon gaps |
| `sm` | 8dp | Small component padding |
| `md` | 12dp | Medium component spacing |
| `lg` | 16dp | Standard content padding |
| `xl` | 24dp | Section spacing |
| `xxl` | 32dp | Large section gaps |

### Usage in Code

```kotlin
// Access via AppTheme
modifier = Modifier.padding(AppTheme.spacing.lg) // 16dp
modifier = Modifier.padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)
```

---

## Shadows

### Warm Shadow System

Shadows use the brand yellow (`#FFD400`) in the alpha channel for a warm, cohesive glow.

| Token | Definition | Usage |
|-------|------------|-------|
| `ShadowWarm` | `0 10px 30px -10px rgba(255, 212, 0, 0.4)` | Elevated cards, prominent elements |
| `ShadowWarmSm` | `0 4px 12px -4px rgba(255, 212, 0, 0.3)` | Subtle elevation, hover states |
| `ShadowDefault` | `0 4px 16px rgba(0, 0, 0, 0.08)` | Standard card elevation |

### Usage in Code

```kotlin
// Compose shadow
Modifier.shadow(
    elevation = 8.dp,
    shape = RoundedCornerShape(24.dp),
    ambientColor = Primary.copy(alpha = 0.3f),
    spotColor = Primary.copy(alpha = 0.2f)
)
```

---

## Border Radius

| Token | Value | Usage |
|-------|-------|-------|
| `default` | 8dp / 0.5rem | Buttons, inputs |
| `lg` | 16dp / 1rem | Small cards |
| `xl` | 24dp / 1.5rem | Medium cards |
| `2xl` | 32dp / 2rem | Large cards |
| `3xl` | 40dp / 2.5rem | Featured cards |
| `full` | 9999dp | Circular buttons, avatars |

---

## Components

### Buttons

#### Primary Button
- Background: `Primary` (#FFD400)
- Text: `Charcoal` (#332B2B)
- Border Radius: `xl` (24dp)
- Height: 48dp
- Shadow: `ShadowWarmSm` on hover

#### Secondary Button
- Background: `Charcoal` (#332B2B)
- Text: `White` (#FFFFFF)
- Border Radius: `xl` (24dp)
- Height: 48dp

#### Icon Button (Circular)
- Background: `Primary/20` (20% opacity)
- Icon Color: `PrimaryDark`
- Hover: `Primary` with `Charcoal` icon
- Size: 40dp x 40dp
- Border Radius: `full`

### Cards

#### Featured Card (Gradient)
```
Background: gradient(from-secondary, to-tertiary)
Border Radius: 3xl (40dp)
Shadow: ShadowWarm
Min Height: 220dp
Press effect: scale(0.98)
```

#### Compact Card
```
Background: SurfaceVariant
Border: 1dp Border
Border Radius: 3xl (40dp)
Shadow: ShadowDefault → ShadowWarmSm on hover
Height: 144dp (approx)
Press effect: scale(0.98)
```

### Badges

| Variant | Background | Text Color |
|---------|------------|------------|
| Beginner | `AccentGreen/20` | `AccentGreen` |
| Intermediate | `Primary/30` | `Charcoal` |
| High Intensity | `AccentOrange/20` | `AccentOrange` |
| Time Badge | `White/30` + blur | `Charcoal` |

### Bottom Navigation

```
Background: White/90 (light) or #1A1814/90 (dark)
Backdrop: blur-xl
Border Radius: 2xl (32dp)
Shadow: shadow-2xl
Height: 64dp
Active: Primary color with filled icon
Inactive: Gray400
```

### Floating Action Button (FAB)

```
Background: Charcoal (light) or Primary (dark)
Text: White (light) or Charcoal (dark)
Border Radius: 2xl (32dp)
Shadow: shadow-xl
Hover: scale(1.05)
Press: scale(0.95)
Icon: Animated bounce
```

---

## Usage Guidelines

### Theme Application

All screens are wrapped with `WorkoutAppTheme`:

```kotlin
// MainActivity.kt
WorkoutAppTheme {
    AppNavigation(...)
}
```

### Accessing Theme Values

```kotlin
@Composable
fun MyComponent() {
    // Colors
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    // Typography
    val titleStyle = MaterialTheme.typography.headlineMedium
    val bodyStyle = MaterialTheme.typography.bodyMedium

    // Spacing
    val padding = AppTheme.spacing.lg // 16dp
    val gap = AppTheme.spacing.md // 12dp
}
```

### Dark Mode Support

The theme automatically provides dark mode variants:

```kotlin
// Theme.kt
WorkoutAppTheme(
    darkTheme = isSystemInDarkTheme(), // Follows system
    content = { ... }
)
```

### Glassmorphism Pattern

For floating elements and overlays:

```kotlin
Modifier
    .background(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(24.dp)
    )
    .blur(radius = 16.dp) // If using experimental blur
```

### Micro-interactions

Standard interaction patterns:

| Interaction | Effect |
|-------------|--------|
| Card press | `scale(0.98)` |
| Button hover | `scale(1.05)` |
| Button press | `scale(0.95)` |
| Icon bounce | `animateDpAsState` with spring |

---

## File Structure

```
shared/src/commonMain/kotlin/com/workout/app/ui/
├── theme/
│   ├── Color.kt         # Color definitions
│   ├── Theme.kt         # WorkoutAppTheme + AppTheme accessor
│   ├── Spacing.kt       # Spacing tokens
│   └── Type.kt          # Typography definitions
├── components/
│   ├── buttons/         # PrimaryButton, SecondaryButton, IconButton, etc.
│   ├── cards/           # BaseCard, CardVariants
│   ├── chips/           # SetChip, FilterChip, Badge
│   ├── inputs/          # TextField, NumberStepper, SearchBar
│   ├── navigation/      # BottomNavBar, BottomActionBar
│   ├── headers/         # SectionHeader
│   └── overlays/        # BottomSheet
└── screens/             # Feature screens
```

---

## Color Migration Notes

This design system represents the updated citrus/yellow brand identity. The previous green theme (`#13EC5B`) has been replaced with:

- Primary: `#13EC5B` → `#FFD400`
- Background: `#0A0A0A` → `#F8F8F7` (light) / `#2C2821` (dark)
- Surface: `#111111` → `#FFFBF0` (light) / `#3D362B` (dark)

Components should be updated to use the new color tokens through `MaterialTheme.colorScheme`.
