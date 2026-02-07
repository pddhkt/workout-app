package com.workout.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Secondary,
    onPrimaryContainer = Charcoal,
    secondary = PrimaryDark,
    onSecondary = OnPrimary,
    secondaryContainer = Tertiary,
    onSecondaryContainer = Charcoal,
    tertiary = AccentGreen,
    onTertiary = Charcoal,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = Error,
    onError = SurfaceLight,
    outline = BorderLight,
    outlineVariant = BorderLightVariant
)

@Composable
fun WorkoutAppTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalExtendedColors provides LightExtendedColors
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

/**
 * Convenience accessor for custom theme values.
 *
 * Usage:
 * ```
 * val padding = AppTheme.spacing.lg // 16.dp
 * ```
 */
object AppTheme {
    /**
     * Access spacing tokens within a composable.
     */
    val spacing: Spacing
        @Composable
        get() = LocalSpacing.current

    /**
     * Access extended semantic colors within a composable.
     */
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
