package com.workout.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.workout.app.domain.model.SessionMode
import com.workout.app.domain.model.SessionParticipant
import com.workout.app.presentation.planning.AddedExerciseData
import com.workout.app.presentation.planning.SessionPlanningState
import com.workout.app.database.Exercise
import com.workout.app.ui.screens.planning.SessionPlanningScreen
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview for Session Planning Screen
 * Shows the complete screen layout with all components
 */
@Preview(
    name = "Session Planning Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun SessionPlanningScreenPreview() {
    val mockExercises = listOf(
        Exercise(
            id = "ex_bench_press",
            name = "Barbell Bench Press",
            muscleGroup = "Chest",
            category = "Compound",
            equipment = "Barbell",
            difficulty = "Intermediate",
            instructions = null,
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = 0,
            updatedAt = 0
        ),
        Exercise(
            id = "ex_squat",
            name = "Barbell Squat",
            muscleGroup = "Legs",
            category = "Compound",
            equipment = "Barbell",
            difficulty = "Intermediate",
            instructions = null,
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = 0,
            updatedAt = 0
        )
    )

    WorkoutAppTheme {
        SessionPlanningScreen(
            state = SessionPlanningState(allExercises = mockExercises),
            onBackClick = {},
            onStartSession = {},
            onToggleExercise = {},
            onAddExercise = { _, _ -> },
            onToggleTimeRange = {}
        )
    }
}

/**
 * Preview showing screen with exercises added
 */
@Preview(
    name = "Session Planning - With Exercises",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun SessionPlanningScreenWithExercisesPreview() {
    val mockExercises = listOf(
        Exercise(
            id = "ex_bench_press",
            name = "Barbell Bench Press",
            muscleGroup = "Chest",
            category = "Compound",
            equipment = "Barbell",
            difficulty = "Intermediate",
            instructions = null,
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = 0,
            updatedAt = 0
        ),
        Exercise(
            id = "ex_squat",
            name = "Barbell Squat",
            muscleGroup = "Legs",
            category = "Compound",
            equipment = "Barbell",
            difficulty = "Intermediate",
            instructions = null,
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = 0,
            updatedAt = 0
        )
    )

    val addedExercises = mapOf(
        "ex_bench_press" to AddedExerciseData(exerciseId = "ex_bench_press", setCount = 3)
    )

    WorkoutAppTheme {
        SessionPlanningScreen(
            state = SessionPlanningState(
                allExercises = mockExercises,
                addedExercises = addedExercises
            ),
            onBackClick = {},
            onStartSession = {},
            onToggleExercise = {},
            onAddExercise = { _, _ -> },
            onToggleTimeRange = {}
        )
    }
}

/**
 * Preview showing screen with chest filter selected
 */
@Preview(
    name = "Session Planning - Filtered",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun SessionPlanningScreenFilteredPreview() {
    val mockExercises = listOf(
        Exercise(
            id = "ex_bench_press",
            name = "Barbell Bench Press",
            muscleGroup = "Chest",
            category = "Compound",
            equipment = "Barbell",
            difficulty = "Intermediate",
            instructions = null,
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = 0,
            updatedAt = 0
        ),
        Exercise(
            id = "ex_squat",
            name = "Barbell Squat",
            muscleGroup = "Legs",
            category = "Compound",
            equipment = "Barbell",
            difficulty = "Intermediate",
            instructions = null,
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = 0,
            updatedAt = 0
        )
    )

    WorkoutAppTheme {
        SessionPlanningScreen(
            state = SessionPlanningState(allExercises = mockExercises),
            onBackClick = {},
            onStartSession = {},
            onToggleExercise = {},
            onAddExercise = { _, _ -> },
            onToggleTimeRange = {}
        )
    }
}

/**
 * Preview showing Group mode with participants
 */
@Preview(
    name = "Session Planning - Group Mode",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun SessionPlanningGroupModePreview() {
    val mockExercises = listOf(
        Exercise(
            id = "ex_bench_press",
            name = "Barbell Bench Press",
            muscleGroup = "Chest",
            category = "Compound",
            equipment = "Barbell",
            difficulty = "Intermediate",
            instructions = null,
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = 0,
            updatedAt = 0
        )
    )

    val addedExercises = mapOf(
        "ex_bench_press" to AddedExerciseData(exerciseId = "ex_bench_press", setCount = 4)
    )

    WorkoutAppTheme {
        SessionPlanningScreen(
            state = SessionPlanningState(
                allExercises = mockExercises,
                addedExercises = addedExercises,
                sessionMode = SessionMode.GROUP,
                participants = listOf(
                    SessionParticipant(id = "owner", name = "You", isOwner = true),
                    SessionParticipant(id = "p1", name = "Alex")
                ),
                recentPartners = listOf("Alex", "Mike", "Sarah")
            ),
            onBackClick = {},
            onStartSession = {},
            onToggleExercise = {},
            onAddExercise = { _, _ -> },
            onToggleTimeRange = {}
        )
    }
}
