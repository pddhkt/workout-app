package com.workout.app.ui.screens.timer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.workout.app.ui.theme.WorkoutAppTheme
import kotlinx.coroutines.delay

/**
 * Preview composable showing Rest Timer screen with active countdown
 */
@Composable
fun RestTimerPreview() {
    WorkoutAppTheme {
        var remainingSeconds by remember { mutableIntStateOf(90) }

        // Simulate countdown
        LaunchedEffect(Unit) {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
        }

        RestTimerScreen(
            state = RestTimerState(
                remainingSeconds = remainingSeconds,
                totalSeconds = 90,
                exerciseContext = ExerciseContext(
                    currentExerciseName = "Barbell Bench Press",
                    currentSetNumber = 2,
                    totalSets = 4
                ),
                upNext = UpNextExercise(
                    name = "Incline Dumbbell Press",
                    sets = 3,
                    reps = "8-10",
                    weight = "25 kg"
                )
            ),
            onDismiss = {},
            onSkipRest = {},
            onAddTime = { seconds -> remainingSeconds += seconds },
            onTimerComplete = {}
        )
    }
}

/**
 * Preview showing Rest Timer near completion
 */
@Composable
fun RestTimerNearCompletionPreview() {
    WorkoutAppTheme {
        RestTimerScreen(
            state = RestTimerState(
                remainingSeconds = 5,
                totalSeconds = 90,
                exerciseContext = ExerciseContext(
                    currentExerciseName = "Barbell Squat",
                    currentSetNumber = 3,
                    totalSets = 5
                ),
                upNext = UpNextExercise(
                    name = "Romanian Deadlift",
                    sets = 4,
                    reps = "10-12",
                    weight = "80 kg"
                )
            ),
            onDismiss = {},
            onSkipRest = {},
            onAddTime = {},
            onTimerComplete = {}
        )
    }
}

/**
 * Preview showing Rest Timer with no next exercise
 */
@Composable
fun RestTimerLastExercisePreview() {
    WorkoutAppTheme {
        RestTimerScreen(
            state = RestTimerState(
                remainingSeconds = 60,
                totalSeconds = 90,
                exerciseContext = ExerciseContext(
                    currentExerciseName = "Calf Raises",
                    currentSetNumber = 4,
                    totalSets = 4
                ),
                upNext = null // No next exercise
            ),
            onDismiss = {},
            onSkipRest = {},
            onAddTime = {},
            onTimerComplete = {}
        )
    }
}

/**
 * Preview showing Rest Timer at start
 */
@Composable
fun RestTimerStartPreview() {
    WorkoutAppTheme {
        RestTimerScreen(
            state = RestTimerState(
                remainingSeconds = 120,
                totalSeconds = 120,
                exerciseContext = ExerciseContext(
                    currentExerciseName = "Deadlift",
                    currentSetNumber = 1,
                    totalSets = 3
                ),
                upNext = UpNextExercise(
                    name = "Bent Over Row",
                    sets = 4,
                    reps = "8-10"
                )
            ),
            onDismiss = {},
            onSkipRest = {},
            onAddTime = {},
            onTimerComplete = {}
        )
    }
}

/**
 * Interactive preview with state management
 */
@Composable
fun InteractiveRestTimerPreview() {
    WorkoutAppTheme {
        var remainingSeconds by remember { mutableIntStateOf(90) }
        var totalSeconds by remember { mutableIntStateOf(90) }

        // Simulate countdown
        LaunchedEffect(remainingSeconds) {
            if (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
        }

        RestTimerScreen(
            state = RestTimerState(
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds,
                exerciseContext = ExerciseContext(
                    currentExerciseName = "Pull-ups",
                    currentSetNumber = 2,
                    totalSets = 4
                ),
                upNext = UpNextExercise(
                    name = "Chin-ups",
                    sets = 3,
                    reps = "To failure"
                )
            ),
            onDismiss = {
                // Reset for demo
                remainingSeconds = 90
                totalSeconds = 90
            },
            onSkipRest = {
                remainingSeconds = 0
            },
            onAddTime = { seconds ->
                remainingSeconds = (remainingSeconds + seconds).coerceAtLeast(0)
                if (remainingSeconds > totalSeconds) {
                    totalSeconds = remainingSeconds
                }
            },
            onTimerComplete = {
                // Timer completed - could show completion state
            }
        )
    }
}
