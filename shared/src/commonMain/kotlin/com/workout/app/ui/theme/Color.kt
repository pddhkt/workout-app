package com.workout.app.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// BRAND COLORS - 3-Color Palette: #F4F4F4, #FFE302, #000000
// =============================================================================

// Primary colors - Yellow
val Primary = Color(0xFFFFE302)
val PrimaryDark = Color(0xFFE6CC02) // slightly darker yellow for secondary
val PrimaryText = Color(0xFFC9A800) // darker yellow for text/icons on light backgrounds
val OnPrimary = Color(0xFF000000)

// Secondary/Tertiary - same yellow
val Secondary = Color(0xFFFFE302)
val Tertiary = Color(0xFFFFE302)

// Accent colors
val AccentGreen = Color(0xFF22C55E) // keep green for heatmap/success
val AccentOrange = Color(0xFFFFE302) // replaced with yellow

// Charcoal - Primary dark text color
val Charcoal = Color(0xFF000000)

// =============================================================================
// LIGHT THEME COLORS
// =============================================================================

val BackgroundLight = Color(0xFFF4F4F4)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF4F4F4)
val OnBackgroundLight = Color(0xFF000000)
val OnSurfaceLight = Color(0xFF000000)
val OnSurfaceVariantLight = Color(0xFF666666) // slightly lighter for secondary text
val BorderLight = Color(0xFFE0E0E0)
val BorderLightVariant = Color(0xFFF4F4F4)

// =============================================================================
// STATUS COLORS
// =============================================================================

val SuccessLight = Color(0xFF22C55E) // keep green
val OnSuccessLight = Color(0xFFFFFFFF)
val WarningLight = Color(0xFFFFE302) // yellow
val OnWarningLight = Color(0xFF000000)
val ErrorLight = Color(0xFF000000) // black for error
val OnErrorLight = Color(0xFFFFFFFF)
val InfoLight = Color(0xFF000000)
val OnInfoLight = Color(0xFFFFFFFF)

// =============================================================================
// LEGACY STATUS COLORS (for backward compatibility)
// New code should use AppTheme.colors.* instead
// =============================================================================

val Success = SuccessLight
val Warning = WarningLight
val Error = ErrorLight
val Info = InfoLight

// =============================================================================
// EXERCISE STATE COLORS
// =============================================================================

val Completed = Color(0xFF22C55E) // Green - completed
val Active = Color(0xFFFFE302) // Primary yellow - active
val Pending = Color(0xFF999999) // Gray - pending

// =============================================================================
// COMPONENT-SPECIFIC COLORS
// =============================================================================

// Card backgrounds for specific workout types
val CardYoga = Color(0xFFF4F4F4)
val CardCardio = Color(0xFFF4F4F4)

// Badge backgrounds
val BadgeBeginner = Color(0xFF22C55E) // green
val BadgeIntermediate = Color(0xFFFFE302) // yellow
val BadgeHighIntensity = Color(0xFF000000) // black

// =============================================================================
// LEGACY TAG COLORS (for component identification in previews)
// =============================================================================

val TagHeader = Color(0xFF000000)
val TagNavigation = Color(0xFF000000)
val TagCard = Color(0xFF000000)
val TagExercise = Color(0xFFFFE302)
val TagInput = Color(0xFFFFE302)
val TagButton = Color(0xFFFFE302)
val TagStatus = Color(0xFFFFE302)
val TagDataViz = Color(0xFF000000)
val TagOverlay = Color(0xFF999999)
