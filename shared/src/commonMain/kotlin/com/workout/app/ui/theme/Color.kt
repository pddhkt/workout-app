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
val SurfaceVariantLight = Color(0xFFFFFBF0) // Pale Honey Cream
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
// STATUS COLORS (Shared between themes)
// =============================================================================

val Success = Color(0xFF22C55E)
val Warning = Color(0xFFFACC15)
val Error = Color(0xFFEF4444)
val Info = Color(0xFF3B82F6)

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

// =============================================================================
// BACKWARD COMPATIBILITY ALIASES
// These allow existing components to continue using direct color imports.
// New components should use MaterialTheme.colorScheme instead.
// =============================================================================

val Background = BackgroundDark
val Surface = SurfaceDark
val SurfaceVariant = SurfaceVariantDark
val OnBackground = OnBackgroundDark
val OnSurface = OnSurfaceDark
val OnSurfaceVariant = OnSurfaceVariantDark
val Border = BorderDark
val BorderVariant = BorderDarkVariant
