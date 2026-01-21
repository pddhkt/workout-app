package com.workout.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.workout.app.ui.screens.timer.ExerciseContext
import com.workout.app.ui.screens.timer.RestTimerScreen
import com.workout.app.ui.screens.timer.RestTimerState
import com.workout.app.ui.screens.timer.UpNextExercise
import com.workout.app.ui.theme.WorkoutAppTheme
import kotlinx.coroutines.delay

/**
 * Android Studio preview for Rest Timer screen with active countdown
 */
@Preview(
    name = "Rest Timer - Active",
    showBackground = true,
    device = "spec:width=393dp,height=852dp"
)
@Composable
fun RestTimerActivePreview() {
    WorkoutAppTheme {
        RestTimerScreen(
            state = RestTimerState(
                remainingSeconds = 75,
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
            onAddTime = {},
            onTimerComplete = {}
        )
    }
}

/**
 * Preview showing Rest Timer near completion (warning state)
 */
@Preview(
    name = "Rest Timer - Near Completion",
    showBackground = true,
    device = "spec:width=393dp,height=852dp"
)
@Composable
fun RestTimerNearCompletionPreview() {
    WorkoutAppTheme {
        RestTimerScreen(
            state = RestTimerState(
                remainingSeconds = 8,
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
 * Preview showing Rest Timer at full time
 */
@Preview(
    name = "Rest Timer - Full Time",
    showBackground = true,
    device = "spec:width=393dp,height=852dp"
)
@Composable
fun RestTimerFullTimePreview() {
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
                    reps = "8-10",
                    weight = "100 kg"
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
 * Preview showing Rest Timer with no next exercise (last exercise)
 */
@Preview(
    name = "Rest Timer - Last Exercise",
    showBackground = true,
    device = "spec:width=393dp,height=852dp"
)
@Composable
fun RestTimerLastExercisePreview() {
    WorkoutAppTheme {
        RestTimerScreen(
            state = RestTimerState(
                remainingSeconds = 45,
                totalSeconds = 60,
                exerciseContext = ExerciseContext(
                    currentExerciseName = "Calf Raises",
                    currentSetNumber = 4,
                    totalSets = 4
                ),
                upNext = null
            ),
            onDismiss = {},
            onSkipRest = {},
            onAddTime = {},
            onTimerComplete = {}
        )
    }
}

/**
 * Preview showing Rest Timer with minimal information (no weight/reps specified)
 */
@Preview(
    name = "Rest Timer - Minimal Info",
    showBackground = true,
    device = "spec:width=393dp,height=852dp"
)
@Composable
fun RestTimerMinimalInfoPreview() {
    WorkoutAppTheme {
        RestTimerScreen(
            state = RestTimerState(
                remainingSeconds = 60,
                totalSeconds = 90,
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
            onDismiss = {},
            onSkipRest = {},
            onAddTime = {},
            onTimerComplete = {}
        )
    }
}

/**
 * Preview showing Rest Timer with long exercise name
 */
@Preview(
    name = "Rest Timer - Long Exercise Name",
    showBackground = true,
    device = "spec:width=393dp,height=852dp"
)
@Composable
fun RestTimerLongNamePreview() {
    WorkoutAppTheme {
        RestTimerScreen(
            state = RestTimerState(
                remainingSeconds = 90,
                totalSeconds = 120,
                exerciseContext = ExerciseContext(
                    currentExerciseName = "Weighted Bulgarian Split Squats",
                    currentSetNumber = 1,
                    totalSets = 3
                ),
                upNext = UpNextExercise(
                    name = "Single Leg Romanian Deadlift",
                    sets = 3,
                    reps = "10 each side",
                    weight = "15 kg"
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
 * Interactive preview with countdown simulation
 */
@Preview(
    name = "Rest Timer - Interactive",
    showBackground = true,
    device = "spec:width=393dp,height=852dp"
)
@Composable
fun RestTimerInteractivePreview() {
    WorkoutAppTheme {
        var remainingSeconds by remember { mutableIntStateOf(30) }
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
                    currentExerciseName = "Overhead Press",
                    currentSetNumber = 3,
                    totalSets = 4
                ),
                upNext = UpNextExercise(
                    name = "Lateral Raises",
                    sets = 3,
                    reps = "12-15",
                    weight = "10 kg"
                )
            ),
            onDismiss = {
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
            onTimerComplete = {}
        )
    }
}

/**
 * Comprehensive showcase of all Rest Timer states
 */
@Preview(
    name = "All Rest Timer States",
    showBackground = true,
    device = "spec:width=1920dp,height=1080dp",
    showSystemUi = false
)
@Composable
fun AllRestTimerStatesPreview() {
    // This would ideally show multiple states side-by-side
    // For Android Studio preview, just showing the active state
    RestTimerActivePreview()
}
