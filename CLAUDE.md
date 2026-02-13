# Workout App

## Build

Requires **JDK 17**. The system default may be a newer version (e.g. JDK 25), which will fail.

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :shared:compileDebugKotlinAndroid
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :composeApp:assembleDebug
```

Or set it permanently in `gradle.properties` (do not commit):
```
org.gradle.java.home=/usr/lib/jvm/java-17-openjdk
```

## Project Structure
- Kotlin Multiplatform + Compose Multiplatform
- `shared/` - shared code (UI, theme, logic)
- `composeApp/` - platform-specific app entry points

## Color Scheme
3-color palette: `#F4F4F4` (light gray), `#FFE302` (yellow), `#000000` (black)

Defined in `shared/src/commonMain/kotlin/com/workout/app/ui/theme/Color.kt`:
- `Primary` = `#FFE302` (yellow)
- `PrimaryDark` = `#E6CC02` (darker yellow, used for secondary)
- `PrimaryText` = `#000000` (black, for text/icons on light backgrounds)
- `OnPrimary` = `#000000` (black)
- `AccentGreen` = `#22C55E` (green, for heatmap/success)
- Backgrounds: `#F4F4F4` (background), `#FFFFFF` (surface)

## Color Usage Principles

### On light/white backgrounds
- **Text:** black (`onSurface`) for primary, gray (`onSurfaceVariant`) for secondary — never yellow or green for body text
- **Buttons:** yellow bg + black text (primary action) OR black bg + white text (secondary action)
- **Links/tappable text:** use `primary` (yellow) sparingly; prefer black text with underline or icon affordance

### On dark/black backgrounds
- **Buttons:** yellow bg + black text only

### Yellow (`#FFE302`) usage
- Primary action buttons
- Active/selected state indicators (chips, tabs) — use at 15% alpha for backgrounds
- Warning status badges — use at 15% alpha for backgrounds
- Never use as text color on light backgrounds (poor contrast)

### Green (`#22C55E`) usage
- Reserved for success and completion states only (completed sets, ready recovery, heatmap activity)
- Use alpha variations (30%/60%/100%) for intensity levels in data visualizations
- Never use for buttons or navigation

### Black (`#000000`) usage
- Primary text color on all light backgrounds
- Error and info status color
- Secondary button background (with white text)

### General rules
- Always use theme tokens (`MaterialTheme.colorScheme.*`, `AppTheme.colors.*`) — never raw hex `Color()` values in components
- Use `.copy(alpha = ...)` for emphasis variations, not separate color definitions
- Status colors: green = success/ready, yellow = warning/recovering, black = error/rest/info

## Theme Files
All in `shared/src/commonMain/kotlin/com/workout/app/ui/theme/`:
- `Color.kt` - color definitions
- `Theme.kt` - Material theme setup
- `ExtendedColors.kt` - custom extended color properties
- `Type.kt` - typography
- `Spacing.kt` - spacing values
