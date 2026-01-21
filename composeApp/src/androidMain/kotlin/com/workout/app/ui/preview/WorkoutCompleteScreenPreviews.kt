package com.workout.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.workout.app.ui.screens.complete.WorkoutCompleteScreen
import com.workout.app.ui.screens.complete.WorkoutSummary
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview for WorkoutCompleteScreen with typical workout data
 */
@Preview(
    name = "Workout Complete - Standard",
    showBackground = true,
    heightDp = 1200
)
@Composable
private fun WorkoutCompleteScreenPreview() {
    WorkoutAppTheme {
        var notes by remember { mutableStateOf("") }
        var selectedMuscleGroups by remember { mutableStateOf(setOf("Chest", "Triceps")) }
        var partnerMode by remember { mutableStateOf(false) }

        WorkoutCompleteScreen(
            summary = WorkoutSummary(
                duration = 3725, // 62:05
                totalVolume = 2450.5f,
                exerciseCount = 6,
                setCount = 18,
                muscleGroups = listOf(
                    "Chest",
                    "Triceps",
                    "Shoulders",
                    "Core"
                )
            ),
            selectedMuscleGroups = selectedMuscleGroups,
            onMuscleGroupToggle = { group ->
                selectedMuscleGroups = if (selectedMuscleGroups.contains(group)) {
                    selectedMuscleGroups - group
                } else {
                    selectedMuscleGroups + group
                }
            },
            partnerModeEnabled = partnerMode,
            onPartnerModeToggle = { partnerMode = !partnerMode },
            notes = notes,
            onNotesChange = { notes = it },
            onSave = {},
            onDone = {}
        )
    }
}

/**
 * Preview for WorkoutCompleteScreen with partner mode enabled
 */
@Preview(
    name = "Workout Complete - Partner Mode",
    showBackground = true,
    heightDp = 1200
)
@Composable
private fun WorkoutCompleteScreenPartnerModePreview() {
    WorkoutAppTheme {
        var notes by remember { mutableStateOf("Both of us crushed it today!") }
        var selectedMuscleGroups by remember { mutableStateOf(setOf("Legs", "Glutes", "Core")) }

        WorkoutCompleteScreen(
            summary = WorkoutSummary(
                duration = 4520, // 75:20
                totalVolume = 3850.0f,
                exerciseCount = 8,
                setCount = 24,
                muscleGroups = listOf(
                    "Legs",
                    "Glutes",
                    "Hamstrings",
                    "Quads",
                    "Core"
                )
            ),
            selectedMuscleGroups = selectedMuscleGroups,
            onMuscleGroupToggle = { group ->
                selectedMuscleGroups = if (selectedMuscleGroups.contains(group)) {
                    selectedMuscleGroups - group
                } else {
                    selectedMuscleGroups + group
                }
            },
            partnerModeEnabled = true,
            onPartnerModeToggle = {},
            notes = notes,
            onNotesChange = { notes = it },
            onSave = {},
            onDone = {}
        )
    }
}

/**
 * Preview for WorkoutCompleteScreen with short workout
 */
@Preview(
    name = "Workout Complete - Short Session",
    showBackground = true,
    heightDp = 1200
)
@Composable
private fun WorkoutCompleteScreenShortPreview() {
    WorkoutAppTheme {
        WorkoutCompleteScreen(
            summary = WorkoutSummary(
                duration = 1200, // 20:00
                totalVolume = 850.0f,
                exerciseCount = 3,
                setCount = 9,
                muscleGroups = listOf(
                    "Back",
                    "Biceps"
                )
            ),
            selectedMuscleGroups = emptySet(),
            onMuscleGroupToggle = {},
            partnerModeEnabled = false,
            onPartnerModeToggle = {},
            notes = "",
            onNotesChange = {},
            onSave = {},
            onDone = {}
        )
    }
}

/**
 * Preview for WorkoutCompleteScreen with long workout and many muscle groups
 */
@Preview(
    name = "Workout Complete - Long Session",
    showBackground = true,
    heightDp = 1400
)
@Composable
private fun WorkoutCompleteScreenLongPreview() {
    WorkoutAppTheme {
        var notes by remember {
            mutableStateOf(
                "Full body session today. Started with compound movements, " +
                        "finished with isolation work. Energy levels were great throughout!"
            )
        }

        WorkoutCompleteScreen(
            summary = WorkoutSummary(
                duration = 5940, // 99:00
                totalVolume = 5200.5f,
                exerciseCount = 12,
                setCount = 36,
                muscleGroups = listOf(
                    "Chest",
                    "Back",
                    "Shoulders",
                    "Biceps",
                    "Triceps",
                    "Legs",
                    "Core",
                    "Glutes"
                )
            ),
            selectedMuscleGroups = setOf(
                "Chest",
                "Back",
                "Shoulders",
                "Legs",
                "Core"
            ),
            onMuscleGroupToggle = {},
            partnerModeEnabled = false,
            onPartnerModeToggle = {},
            notes = notes,
            onNotesChange = { notes = it },
            onSave = {},
            onDone = {}
        )
    }
}

/**
 * Preview showing all states in a scrollable view
 */
@Preview(
    name = "Workout Complete - All States",
    showBackground = true,
    heightDp = 1200
)
@Composable
private fun WorkoutCompleteScreenAllStatesPreview() {
    WorkoutAppTheme {
        var notes by remember { mutableStateOf("") }
        var selectedMuscleGroups by remember { mutableStateOf(emptySet<String>()) }
        var partnerMode by remember { mutableStateOf(false) }

        WorkoutCompleteScreen(
            summary = WorkoutSummary(
                duration = 2700, // 45:00
                totalVolume = 1850.0f,
                exerciseCount = 5,
                setCount = 15,
                muscleGroups = listOf(
                    "Shoulders",
                    "Triceps",
                    "Core"
                )
            ),
            selectedMuscleGroups = selectedMuscleGroups,
            onMuscleGroupToggle = { group ->
                selectedMuscleGroups = if (selectedMuscleGroups.contains(group)) {
                    selectedMuscleGroups - group
                } else {
                    selectedMuscleGroups + group
                }
            },
            partnerModeEnabled = partnerMode,
            onPartnerModeToggle = { partnerMode = !partnerMode },
            notes = notes,
            onNotesChange = { notes = it },
            onSave = {},
            onDone = {}
        )
    }
}
