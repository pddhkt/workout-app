package com.workout.app.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.workout.app.ui.screens.detail.ExerciseDetailScreen
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Standard Exercise Detail screen preview.
 */
@Preview(
    name = "Exercise Detail Screen",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    heightDp = 2000,
    widthDp = 393
)
@Composable
private fun ExerciseDetailScreenPreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
        ) {
            ExerciseDetailScreen(
                exerciseId = "bench-press",
                onBackClick = {},
                onAddToWorkout = {},
                onPlayVideo = {}
            )
        }
    }
}

/**
 * Interactive Exercise Detail screen preview with console logging.
 */
@Preview(
    name = "Exercise Detail Screen - Interactive",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    heightDp = 2000,
    widthDp = 393
)
@Composable
private fun ExerciseDetailScreenInteractivePreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
        ) {
            ExerciseDetailScreen(
                exerciseId = "bench-press",
                onBackClick = { println("Back clicked") },
                onAddToWorkout = { println("Add to workout clicked") },
                onPlayVideo = { url -> println("Play video: $url") }
            )
        }
    }
}

/**
 * Comprehensive preview showing all states.
 */
@Preview(
    name = "Exercise Detail - All States",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    heightDp = 2400,
    widthDp = 393
)
@Composable
private fun AllExerciseDetailStatesPreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
        ) {
            ExerciseDetailScreen(
                exerciseId = "bench-press",
                onBackClick = { println("Back clicked") },
                onAddToWorkout = { println("Add to workout clicked") },
                onPlayVideo = { url -> println("Play video: $url") }
            )
        }
    }
}
