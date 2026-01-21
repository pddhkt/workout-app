package com.workout.app.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Preview composable for Onboarding Screen
 * Can be used in both common and platform-specific preview contexts
 */
@Composable
fun OnboardingPreview(modifier: Modifier = Modifier) {
    OnboardingScreen(
        onComplete = { data ->
            println("Onboarding completed with data: $data")
        },
        onSkip = {
            println("Onboarding skipped")
        },
        modifier = modifier
    )
}

/**
 * Interactive preview with console logging for all callbacks
 */
@Composable
fun OnboardingInteractivePreview(modifier: Modifier = Modifier) {
    OnboardingScreen(
        onComplete = { data ->
            println("=== Onboarding Complete ===")
            println("Name: ${data.name}")
            println("Selected Goals: ${data.selectedGoals.joinToString(", ")}")
            println("Weight Unit: ${data.weightUnit}")
        },
        onSkip = {
            println("Onboarding skipped by user")
        },
        modifier = modifier
    )
}
