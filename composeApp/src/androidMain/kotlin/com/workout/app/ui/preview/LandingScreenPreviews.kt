package com.workout.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.workout.app.ui.screens.landing.LandingScreen
import com.workout.app.ui.theme.WorkoutAppTheme

@Preview(
    name = "Landing Screen",
    showBackground = true,
    backgroundColor = 0xFFF4F4F4,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun LandingScreenPreview() {
    WorkoutAppTheme {
        LandingScreen()
    }
}
