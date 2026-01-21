package com.workout.app.ui.screens.detail

import androidx.compose.runtime.Composable
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview composable for Exercise Detail screen.
 * Demonstrates the full screen composition with all elements.
 */
@Composable
fun ExerciseDetailPreview() {
    WorkoutAppTheme {
        ExerciseDetailScreen(
            exerciseId = "bench-press",
            onBackClick = { println("Back clicked") },
            onAddToWorkout = { println("Add to workout clicked") },
            onPlayVideo = { url -> println("Play video: $url") }
        )
    }
}

/**
 * Preview composable for Exercise Detail screen with interactive callbacks.
 */
@Composable
fun ExerciseDetailInteractivePreview() {
    WorkoutAppTheme {
        ExerciseDetailScreen(
            exerciseId = "bench-press",
            onBackClick = { println("Back clicked") },
            onAddToWorkout = { println("Add to workout clicked") },
            onPlayVideo = { url -> println("Play video: $url") }
        )
    }
}
