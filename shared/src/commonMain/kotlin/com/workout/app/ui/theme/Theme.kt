package com.workout.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Light color scheme for the Lemon Workouts app.
 * Uses citrus/yellow primary with warm cream backgrounds.
 */
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

/**
 * Dark color scheme for the Lemon Workouts app.
 * Uses citrus/yellow primary with warm brown backgrounds.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Charcoal,
    secondary = Secondary,
    onSecondary = OnPrimary,
    secondaryContainer = Tertiary,
    onSecondaryContainer = Charcoal,
    tertiary = AccentGreen,
    onTertiary = Charcoal,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = Error,
    onError = SurfaceDark,
    outline = BorderDark,
    outlineVariant = BorderDarkVariant
)

/**
 * Main theme composable for the Lemon Workouts app.
 *
 * Provides:
 * - MaterialTheme with citrus/yellow color palette
 * - Typography via AppTypography
 * - Spacing tokens via CompositionLocal
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param content The content to be themed.
 */
@Composable
fun WorkoutAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalSpacing provides Spacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
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
}
