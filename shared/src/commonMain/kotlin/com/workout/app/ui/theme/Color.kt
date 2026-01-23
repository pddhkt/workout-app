package com.workout.app.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// BRAND COLORS - Citrus/Lemon Theme
// =============================================================================

// Primary colors - Citrus Yellow
val Primary = Color(0xFFFFD400)
val PrimaryDark = Color(0xFFE6BE00)
val OnPrimary = Color(0xFF332B2B) // Charcoal text on yellow

// Secondary colors - Gradient colors
val Secondary = Color(0xFFFFE600) // Gradient start
val Tertiary = Color(0xFFF7C500) // Golden Honey - Gradient end

// Accent colors
val AccentGreen = Color(0xFF6ACCBC) // Teal/mint for beginner badges
val AccentOrange = Color(0xFFFB923C) // Orange for high intensity

// Charcoal - Primary dark text color
val Charcoal = Color(0xFF332B2B)

// =============================================================================
// LIGHT THEME COLORS
// =============================================================================

val BackgroundLight = Color(0xFFF8F8F7)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFFFFFFF) // Pure white
val OnBackgroundLight = Color(0xFF332B2B) // Charcoal
val OnSurfaceLight = Color(0xFF332B2B)
val OnSurfaceVariantLight = Color(0xFF888888)
val BorderLight = Color(0xFFE5E5E5)
val BorderLightVariant = Color(0xFFF0F0F0)

// =============================================================================
// DARK THEME COLORS
// =============================================================================

val BackgroundDark = Color(0xFF2C2821)
val SurfaceDark = Color(0xFF3D362B)
val SurfaceVariantDark = Color(0xFF3D362B)
val OnBackgroundDark = Color(0xFFFFFFFF)
val OnSurfaceDark = Color(0xFFFFFFFF)
val OnSurfaceVariantDark = Color(0xFF9CA3AF)
val BorderDark = Color(0xFF4A4A4A)
val BorderDarkVariant = Color(0xFF3D3D3D)

// =============================================================================
// STATUS COLORS - Light Theme
// =============================================================================

val SuccessLight = Color(0xFF22C55E)
val OnSuccessLight = Color(0xFFFFFFFF)
val WarningLight = Color(0xFFFACC15)
val OnWarningLight = Color(0xFF000000)
val ErrorLight = Color(0xFFEF4444)
val OnErrorLight = Color(0xFFFFFFFF)
val InfoLight = Color(0xFF3B82F6)
val OnInfoLight = Color(0xFFFFFFFF)

// =============================================================================
// STATUS COLORS - Dark Theme (Lighter for visibility)
// =============================================================================

val SuccessDark = Color(0xFF4ADE80)
val OnSuccessDark = Color(0xFF000000)
val WarningDark = Color(0xFFFDE047)
val OnWarningDark = Color(0xFF000000)
val ErrorDark = Color(0xFFF87171)
val OnErrorDark = Color(0xFF000000)
val InfoDark = Color(0xFF60A5FA)
val OnInfoDark = Color(0xFF000000)

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
val Active = Color(0xFFFFD400) // Primary yellow - active
val Pending = Color(0xFF666666) // Gray - pending

// =============================================================================
// COMPONENT-SPECIFIC COLORS
// =============================================================================

// Card backgrounds for specific workout types
val CardYoga = Color(0xFFE0F2F1) // Light teal
val CardYogaDark = Color(0xFF1A3D3A) // Dark teal
val CardCardio = Color(0xFFFFF3E0) // Light orange
val CardCardioDark = Color(0xFF3D2A1A) // Dark orange

// Badge backgrounds (light mode - with alpha applied in components)
val BadgeBeginner = AccentGreen
val BadgeIntermediate = Primary
val BadgeHighIntensity = AccentOrange

// =============================================================================
// LEGACY TAG COLORS (for component identification in previews)
// =============================================================================

val TagHeader = Color(0xFF818CF8)
val TagNavigation = Color(0xFFF472B6)
val TagCard = Color(0xFF22D3EE)
val TagExercise = Color(0xFFFB923C)
val TagInput = Color(0xFFA3E635)
val TagButton = Color(0xFFFFD400) // Updated to primary
val TagStatus = Color(0xFFFACC15)
val TagDataViz = Color(0xFFC084FC)
val TagOverlay = Color(0xFF9CA3AF)

