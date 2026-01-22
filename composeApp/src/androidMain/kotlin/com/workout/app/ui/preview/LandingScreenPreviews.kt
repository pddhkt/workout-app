package com.workout.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.workout.app.ui.screens.landing.LandingScreen
import com.workout.app.ui.theme.WorkoutAppTheme

@Preview(
    name = "Landing Screen - Light",
    showBackground = true,
    backgroundColor = 0xFFF8F8F7,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun LandingScreenLightPreview() {
    WorkoutAppTheme(darkTheme = false) {
        LandingScreen()
    }
}

@Preview(
    name = "Landing Screen - Dark",
    showBackground = true,
    backgroundColor = 0xFF2C2821,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun LandingScreenDarkPreview() {
    WorkoutAppTheme(darkTheme = true) {
        LandingScreen()
    }
}
