package com.workout.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.workout.app.ui.screens.onboarding.OnboardingData
import com.workout.app.ui.screens.onboarding.OnboardingScreen
import com.workout.app.ui.screens.onboarding.WeightUnit
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview for Onboarding Screen
 * Shows the full onboarding flow at starting state
 */
@Preview(
    name = "Onboarding Screen",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    device = "spec:width=393dp,height=852dp"
)
@Composable
fun OnboardingScreenPreview() {
    WorkoutAppTheme {
        OnboardingScreen(
            onComplete = { data ->
                println("Onboarding completed: $data")
            },
            onSkip = {
                println("Onboarding skipped")
            }
        )
    }
}

/**
 * Interactive preview demonstrating the full onboarding flow
 * Includes callback logging for all interactions
 */
@Preview(
    name = "Onboarding Screen - Interactive",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    device = "spec:width=393dp,height=852dp",
    heightDp = 1200
)
@Composable
fun OnboardingScreenInteractivePreview() {
    WorkoutAppTheme {
        OnboardingScreen(
            onComplete = { data ->
                println("=== Onboarding Completed ===")
                println("Name: ${data.name}")
                println("Goals: ${data.selectedGoals.joinToString()}")
                println("Weight Unit: ${data.weightUnit}")
            },
            onSkip = {
                println("User skipped onboarding")
            }
        )
    }
}

/**
 * Comprehensive showcase of all onboarding states
 * For visual testing of the complete flow
 */
@Preview(
    name = "All Onboarding States",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    device = "spec:width=393dp,height=852dp",
    heightDp = 1500
)
@Composable
fun AllOnboardingStatesPreview() {
    WorkoutAppTheme {
        OnboardingScreen(
            onComplete = { data ->
                println("Onboarding data:")
                println("  Name: ${data.name}")
                println("  Goals: ${data.selectedGoals}")
                println("  Unit: ${data.weightUnit}")
            },
            onSkip = {
                println("Onboarding skipped")
            }
        )
    }
}
