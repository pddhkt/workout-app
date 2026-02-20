package com.workout.app.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.exercise.ExerciseSelectionCard
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview: Default state - Exercise not added
 */
@Preview(
    name = "Default State",
    showBackground = true
)
@Composable
private fun ExerciseSelectionCardDefaultPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ExerciseSelectionCard(
                exerciseName = "Barbell Squat",
                exerciseCategory = "Legs",
                isAdded = false,
                onToggle = { },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preview: Added state - Exercise added
 */
@Preview(
    name = "Added State",
    showBackground = true
)
@Composable
private fun ExerciseSelectionCardAddedPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ExerciseSelectionCard(
                exerciseName = "Bench Press",
                exerciseCategory = "Push",
                isAdded = true,
                onToggle = { },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preview: Interactive state - Toggle between added/default
 */
@Preview(
    name = "Interactive",
    showBackground = true
)
@Composable
private fun ExerciseSelectionCardInteractivePreview() {
    WorkoutAppTheme {
        var isAdded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ExerciseSelectionCard(
                exerciseName = "Deadlift",
                exerciseCategory = "Back",
                isAdded = isAdded,
                onToggle = { isAdded = !isAdded },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preview: Multiple exercises in list
 */
@Preview(
    name = "Multiple Exercises",
    showBackground = true
)
@Composable
private fun ExerciseSelectionCardListPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Exercise 1: Default state
            ExerciseSelectionCard(
                exerciseName = "Barbell Squat",
                exerciseCategory = "Legs",
                isAdded = false,
                onToggle = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Exercise 2: Added
            ExerciseSelectionCard(
                exerciseName = "Bench Press",
                exerciseCategory = "Push",
                isAdded = true,
                onToggle = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Exercise 3: Added
            ExerciseSelectionCard(
                exerciseName = "New Exercise",
                exerciseCategory = "Core",
                isAdded = true,
                onToggle = { },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
