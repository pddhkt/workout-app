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

## Theme Files
All in `shared/src/commonMain/kotlin/com/workout/app/ui/theme/`:
- `Color.kt` - color definitions
- `Theme.kt` - Material theme setup
- `ExtendedColors.kt` - custom extended color properties
- `Type.kt` - typography
- `Spacing.kt` - spacing values
