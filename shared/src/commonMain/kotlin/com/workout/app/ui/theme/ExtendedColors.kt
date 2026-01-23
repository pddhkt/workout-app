package com.workout.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended color palette providing theme-aware semantic colors.
 *
 * These colors automatically adapt to light/dark theme, ensuring proper
 * visibility and contrast in both modes. Use `AppTheme.colors` to access
 * these colors from within composables.
 *
 * @property success Color for positive states (completed, valid, etc.)
 * @property onSuccess Color for content on success backgrounds
 * @property warning Color for warning/caution states
 * @property onWarning Color for content on warning backgrounds
 * @property info Color for informational states
 * @property onInfo Color for content on info backgrounds
 * @property error Color for error/negative states (mirrors MaterialTheme for consistency)
 * @property onError Color for content on error backgrounds
 */
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val info: Color,
    val onInfo: Color,
    val error: Color,
    val onError: Color
)

/**
 * Light theme extended colors.
 * Uses standard semantic colors optimized for light backgrounds.
 */
val LightExtendedColors = ExtendedColors(
    success = SuccessLight,
    onSuccess = OnSuccessLight,
    warning = WarningLight,
    onWarning = OnWarningLight,
    info = InfoLight,
    onInfo = OnInfoLight,
    error = ErrorLight,
    onError = OnErrorLight
)

/**
 * Dark theme extended colors.
 * Uses lighter variants for better visibility on dark backgrounds.
 */
val DarkExtendedColors = ExtendedColors(
    success = SuccessDark,
    onSuccess = OnSuccessDark,
    warning = WarningDark,
    onWarning = OnWarningDark,
    info = InfoDark,
    onInfo = OnInfoDark,
    error = ErrorDark,
    onError = OnErrorDark
)

/**
 * CompositionLocal for providing ExtendedColors through the composition tree.
 * Access via `AppTheme.colors` within composables.
 */
val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
