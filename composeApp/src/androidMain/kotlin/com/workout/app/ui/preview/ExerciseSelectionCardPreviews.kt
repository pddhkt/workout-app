package com.workout.app.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
                setCount = 3,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preview: Added state - Exercise added with set stepper
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
                setCount = 4,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
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
        var setCount by remember { mutableIntStateOf(3) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ExerciseSelectionCard(
                exerciseName = "Deadlift",
                exerciseCategory = "Back",
                isAdded = isAdded,
                setCount = setCount,
                onAddClick = { isAdded = true },
                onRemoveClick = { isAdded = false },
                onSetCountChange = { setCount = it },
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
                setCount = 3,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Exercise 2: Added with 4 sets
            ExerciseSelectionCard(
                exerciseName = "Bench Press",
                exerciseCategory = "Push",
                isAdded = true,
                setCount = 4,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Exercise 3: Added with 3 sets
            ExerciseSelectionCard(
                exerciseName = "Deadlift",
                exerciseCategory = "Back",
                isAdded = true,
                setCount = 3,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Exercise 4: Default state
            ExerciseSelectionCard(
                exerciseName = "Overhead Press",
                exerciseCategory = "Shoulders",
                isAdded = false,
                setCount = 3,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Exercise 5: Added with 5 sets
            ExerciseSelectionCard(
                exerciseName = "Pull-ups",
                exerciseCategory = "Back",
                isAdded = true,
                setCount = 5,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preview: Disabled state
 */
@Preview(
    name = "Disabled",
    showBackground = true
)
@Composable
private fun ExerciseSelectionCardDisabledPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Disabled default state
            ExerciseSelectionCard(
                exerciseName = "Barbell Squat",
                exerciseCategory = "Legs",
                isAdded = false,
                setCount = 3,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            // Disabled added state
            ExerciseSelectionCard(
                exerciseName = "Bench Press",
                exerciseCategory = "Push",
                isAdded = true,
                setCount = 4,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preview: Edge cases - Long names and min/max sets
 */
@Preview(
    name = "Edge Cases",
    showBackground = true
)
@Composable
private fun ExerciseSelectionCardEdgeCasesPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Long exercise name
            ExerciseSelectionCard(
                exerciseName = "Barbell Back Squat with Safety Bar",
                exerciseCategory = "Legs - Quadriceps",
                isAdded = false,
                setCount = 3,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Minimum sets (1)
            ExerciseSelectionCard(
                exerciseName = "Max Effort Deadlift",
                exerciseCategory = "Back",
                isAdded = true,
                setCount = 1,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                minSets = 1,
                maxSets = 10,
                modifier = Modifier.fillMaxWidth()
            )

            // Maximum sets (10)
            ExerciseSelectionCard(
                exerciseName = "Volume Squats",
                exerciseCategory = "Legs",
                isAdded = true,
                setCount = 10,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                minSets = 1,
                maxSets = 10,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preview: All states combined in one view
 */
@Preview(
    name = "All States",
    showBackground = true,
    heightDp = 800
)
@Composable
private fun AllExerciseSelectionCardStatesPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Default state examples
            ExerciseSelectionCard(
                exerciseName = "Barbell Squat",
                exerciseCategory = "Legs",
                isAdded = false,
                setCount = 3,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Added state examples
            ExerciseSelectionCard(
                exerciseName = "Bench Press",
                exerciseCategory = "Push",
                isAdded = true,
                setCount = 4,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                modifier = Modifier.fillMaxWidth()
            )

            ExerciseSelectionCard(
                exerciseName = "Deadlift",
                exerciseCategory = "Back",
                isAdded = true,
                setCount = 3,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Disabled states
            ExerciseSelectionCard(
                exerciseName = "Disabled Exercise",
                exerciseCategory = "N/A",
                isAdded = false,
                setCount = 3,
                onAddClick = { },
                onRemoveClick = { },
                onSetCountChange = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
