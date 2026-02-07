package com.workout.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val info: Color,
    val onInfo: Color,
    val error: Color,
    val onError: Color,
    val primaryText: Color
)

val LightExtendedColors = ExtendedColors(
    success = SuccessLight,
    onSuccess = OnSuccessLight,
    warning = WarningLight,
    onWarning = OnWarningLight,
    info = InfoLight,
    onInfo = OnInfoLight,
    error = ErrorLight,
    onError = OnErrorLight,
    primaryText = PrimaryText
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
