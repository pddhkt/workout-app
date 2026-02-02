package com.workout.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.workout.app.ui.screens.complete.WorkoutCompleteScreen
import com.workout.app.ui.screens.complete.WorkoutSummary
import com.workout.app.ui.screens.timer.ExerciseContext
import com.workout.app.ui.screens.timer.RestTimerScreen
import com.workout.app.ui.screens.timer.RestTimerState
import com.workout.app.ui.screens.timer.UpNextExercise
import com.workout.app.ui.screens.workout.ExerciseData
import com.workout.app.ui.screens.workout.WorkoutScreen
import com.workout.app.ui.screens.workout.WorkoutSession

/**
 * Navigation wrappers for screens that require state management.
 * These composables handle state hoisting for the navigation layer.
 */

/**
 * Wrapper for WorkoutScreen with state management.
 * Creates a mock session and handles all callbacks.
 *
 * @param onNavigateBack Callback when back is pressed
 * @param onRestTimerClick Callback when rest timer should be shown
 * @param onCompleteWorkout Callback when workout is completed
 * @param onSaveAndExit Callback when user saves and exits
 * @param onCancelWorkout Callback when workout is cancelled
 */
@Composable
fun WorkoutScreen(
    onNavigateBack: () -> Unit,
    onRestTimerClick: () -> Unit,
    onCompleteWorkout: (sessionId: String) -> Unit,
    onSaveAndExit: () -> Unit,
    onCancelWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Mock session data
    val session = remember {
        WorkoutSession(
            workoutName = "Push Day",
            exercises = listOf(
                ExerciseData(
                    id = "1",
                    name = "Barbell Bench Press",
                    muscleGroup = "Chest",
                    targetSets = 4,
                    completedSets = 0
                ),
                ExerciseData(
                    id = "2",
                    name = "Overhead Press",
                    muscleGroup = "Shoulders",
                    targetSets = 3,
                    completedSets = 0
                ),
                ExerciseData(
                    id = "3",
                    name = "Incline Dumbbell Press",
                    muscleGroup = "Chest",
                    targetSets = 3,
                    completedSets = 0
                )
            )
        )
    }

    WorkoutScreen(
        session = session,
        onCompleteSet = { _, _, _, _, _ ->
            // TODO: Handle set completion
        },
        onSkipSet = { _ ->
            // TODO: Handle set skip
        },
        onExerciseExpand = { _ ->
            // TODO: Handle exercise expand
        },
        onEndWorkout = {
            // Show bottom sheet or navigate
            onCompleteWorkout("session_123")
        },
        modifier = modifier
    )
}

/**
 * Wrapper for RestTimerScreen with state management.
 *
 * @param onDismiss Callback when timer is dismissed
 * @param onSkipRest Callback when rest is skipped
 * @param onAddTime Callback when time is added
 * @param onTimerComplete Callback when timer completes
 */
@Composable
fun RestTimerScreen(
    onDismiss: () -> Unit,
    onSkipRest: () -> Unit,
    onAddTime: (Int) -> Unit,
    onTimerComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingSeconds by remember { mutableIntStateOf(90) }

    val state = RestTimerState(
        remainingSeconds = remainingSeconds,
        totalSeconds = 90,
        exerciseContext = ExerciseContext(
            currentExerciseName = "Barbell Bench Press",
            currentSetNumber = 2,
            totalSets = 4
        ),
        upNext = UpNextExercise(
            name = "Overhead Press",
            sets = 3,
            reps = "8",
            weight = "60 kg"
        )
    )

    RestTimerScreen(
        state = state,
        onDismiss = onDismiss,
        onSkipRest = onSkipRest,
        onAddTime = { seconds ->
            remainingSeconds += seconds
            onAddTime(seconds)
        },
        onTimerComplete = onTimerComplete,
        modifier = modifier
    )
}

/**
 * Wrapper for WorkoutCompleteScreen with state management.
 *
 * @param onDoneClick Callback when done button is clicked
 * @param onSaveDraft Callback when save draft is clicked
 */
@Composable
fun WorkoutCompleteScreen(
    onDoneClick: () -> Unit,
    onSaveDraft: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMuscleGroups by remember { mutableStateOf(setOf("Chest", "Shoulders", "Triceps")) }
    var partnerModeEnabled by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    val summary = WorkoutSummary(
        duration = 3720, // 62 minutes
        totalVolume = 5250f,
        exerciseCount = 6,
        setCount = 18,
        muscleGroups = listOf("Chest", "Shoulders", "Triceps")
    )

    WorkoutCompleteScreen(
        summary = summary,
        selectedMuscleGroups = selectedMuscleGroups,
        onMuscleGroupToggle = { group ->
            selectedMuscleGroups = if (selectedMuscleGroups.contains(group)) {
                selectedMuscleGroups - group
            } else {
                selectedMuscleGroups + group
            }
        },
        partnerModeEnabled = partnerModeEnabled,
        onPartnerModeToggle = {
            partnerModeEnabled = !partnerModeEnabled
        },
        notes = notes,
        onNotesChange = { notes = it },
        onSave = onSaveDraft,
        onDone = onDoneClick,
        modifier = modifier
    )
}
