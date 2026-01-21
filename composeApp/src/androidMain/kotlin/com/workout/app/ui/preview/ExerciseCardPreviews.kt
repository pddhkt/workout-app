package com.workout.app.ui.preview

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.chips.SetState
import com.workout.app.ui.components.exercise.ExerciseCard
import com.workout.app.ui.components.exercise.ExerciseCardShowcase
import com.workout.app.ui.components.exercise.ExerciseCardState
import com.workout.app.ui.components.exercise.SetInfo
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Android Studio preview for completed exercise card state
 */
@Preview(name = "Exercise Card - Completed", showBackground = true)
@Composable
private fun ExerciseCardCompletedPreview() {
    WorkoutAppTheme {
        ExerciseCard(
            exerciseName = "Bench Press",
            muscleGroup = "Chest",
            sets = listOf(
                SetInfo(1, 12, 80f, SetState.COMPLETED),
                SetInfo(2, 10, 80f, SetState.COMPLETED),
                SetInfo(3, 8, 80f, SetState.COMPLETED)
            ),
            state = ExerciseCardState.COMPLETED,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Android Studio preview for active exercise card state (collapsed)
 */
@Preview(name = "Exercise Card - Active Collapsed", showBackground = true)
@Composable
private fun ExerciseCardActiveCollapsedPreview() {
    WorkoutAppTheme {
        var isExpanded by remember { mutableStateOf(false) }
        var reps by remember { mutableStateOf(10) }
        var weight by remember { mutableStateOf(60f) }

        ExerciseCard(
            exerciseName = "Squat",
            muscleGroup = "Legs",
            sets = listOf(
                SetInfo(1, 12, 100f, SetState.COMPLETED),
                SetInfo(2, 10, 100f, SetState.ACTIVE),
                SetInfo(3, 8, 100f, SetState.PENDING),
                SetInfo(4, 8, 100f, SetState.PENDING)
            ),
            state = ExerciseCardState.ACTIVE,
            isExpanded = isExpanded,
            onExpandToggle = { isExpanded = !isExpanded },
            currentReps = reps,
            currentWeight = weight,
            onRepsChange = { reps = it },
            onWeightChange = { weight = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Android Studio preview for active exercise card state (expanded)
 */
@Preview(name = "Exercise Card - Active Expanded", showBackground = true)
@Composable
private fun ExerciseCardActiveExpandedPreview() {
    WorkoutAppTheme {
        var isExpanded by remember { mutableStateOf(true) }
        var reps by remember { mutableStateOf(12) }
        var weight by remember { mutableStateOf(60f) }

        ExerciseCard(
            exerciseName = "Deadlift",
            muscleGroup = "Back",
            sets = listOf(
                SetInfo(1, 8, 120f, SetState.COMPLETED),
                SetInfo(2, 6, 120f, SetState.ACTIVE),
                SetInfo(3, 6, 120f, SetState.PENDING)
            ),
            state = ExerciseCardState.ACTIVE,
            isExpanded = isExpanded,
            onExpandToggle = { isExpanded = !isExpanded },
            currentReps = reps,
            currentWeight = weight,
            onRepsChange = { reps = it },
            onWeightChange = { weight = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Android Studio preview for pending exercise card state
 */
@Preview(name = "Exercise Card - Pending", showBackground = true)
@Composable
private fun ExerciseCardPendingPreview() {
    WorkoutAppTheme {
        ExerciseCard(
            exerciseName = "Shoulder Press",
            muscleGroup = "Shoulders",
            sets = listOf(
                SetInfo(1, 0, 0f, SetState.PENDING),
                SetInfo(2, 0, 0f, SetState.PENDING),
                SetInfo(3, 0, 0f, SetState.PENDING)
            ),
            state = ExerciseCardState.PENDING,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Android Studio preview showing all exercise card states together
 */
@Preview(name = "All Exercise Cards", showBackground = true, heightDp = 1200)
@Composable
private fun AllExerciseCardsPreview() {
    ExerciseCardShowcase()
}
