package com.workout.app.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.workout.app.ui.screens.library.ExerciseLibraryScreen
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Standard exercise library screen preview
 */
@Preview(
    name = "Exercise Library - Standard",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    device = "spec:width=393dp,height=852dp"
)
@Composable
private fun ExerciseLibraryScreenPreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
        ) {
            ExerciseLibraryScreen(
                onExerciseClick = { exerciseId ->
                    println("Exercise clicked: $exerciseId")
                },
                onFavoriteToggle = { exerciseId ->
                    println("Favorite toggled: $exerciseId")
                },
                onMoreOptionsClick = { exerciseId ->
                    println("More options clicked: $exerciseId")
                }
            )
        }
    }
}

/**
 * Interactive preview showing various interactions
 */
@Preview(
    name = "Exercise Library - Interactive",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    device = "spec:width=393dp,height=852dp"
)
@Composable
private fun ExerciseLibraryScreenInteractivePreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
        ) {
            ExerciseLibraryScreen(
                onExerciseClick = { exerciseId ->
                    println("Opening exercise details for: $exerciseId")
                },
                onFavoriteToggle = { exerciseId ->
                    println("Toggling favorite for: $exerciseId")
                },
                onMoreOptionsClick = { exerciseId ->
                    println("Opening options menu for: $exerciseId")
                }
            )
        }
    }
}

/**
 * Comprehensive showcase of all states
 */
@Preview(
    name = "Exercise Library - All States",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    device = "spec:width=393dp,height=2000dp"
)
@Composable
private fun AllExerciseLibraryStatesPreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
        ) {
            ExerciseLibraryScreen(
                onExerciseClick = { },
                onFavoriteToggle = { },
                onMoreOptionsClick = { }
            )
        }
    }
}
