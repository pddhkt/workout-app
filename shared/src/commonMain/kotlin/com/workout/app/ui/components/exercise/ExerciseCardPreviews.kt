package com.workout.app.ui.components.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.workout.app.ui.components.chips.SetState
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview composable demonstrating all ExerciseCard states.
 * This can be used in Android Studio preview or as a showcase screen.
 */
@Composable
fun ExerciseCardShowcase() {
    WorkoutAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
            ) {
                // Section title
                Text(
                    text = "Exercise Cards",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Completed state
                Text(
                    text = "Completed State",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExerciseCard(
                    exerciseName = "Bench Press",
                    muscleGroup = "Chest",
                    sets = listOf(
                        SetInfo(1, 12, 80f, SetState.COMPLETED),
                        SetInfo(2, 10, 80f, SetState.COMPLETED),
                        SetInfo(3, 8, 80f, SetState.COMPLETED)
                    ),
                    state = ExerciseCardState.COMPLETED
                )

                // Active state (collapsed)
                Text(
                    text = "Active State (Collapsed)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                var isExpandedCollapsed by remember { mutableStateOf(false) }
                var repsCollapsed by remember { mutableStateOf(10) }
                var weightCollapsed by remember { mutableStateOf(60f) }

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
                    isExpanded = isExpandedCollapsed,
                    onExpandToggle = { isExpandedCollapsed = !isExpandedCollapsed },
                    currentReps = repsCollapsed,
                    currentWeight = weightCollapsed,
                    onRepsChange = { repsCollapsed = it },
                    onWeightChange = { weightCollapsed = it }
                )

                // Active state (expanded)
                Text(
                    text = "Active State (Expanded)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                var isExpandedExpanded by remember { mutableStateOf(true) }
                var repsExpanded by remember { mutableStateOf(12) }
                var weightExpanded by remember { mutableStateOf(60f) }

                ExerciseCard(
                    exerciseName = "Deadlift",
                    muscleGroup = "Back",
                    sets = listOf(
                        SetInfo(1, 8, 120f, SetState.COMPLETED),
                        SetInfo(2, 6, 120f, SetState.ACTIVE),
                        SetInfo(3, 6, 120f, SetState.PENDING)
                    ),
                    state = ExerciseCardState.ACTIVE,
                    isExpanded = isExpandedExpanded,
                    onExpandToggle = { isExpandedExpanded = !isExpandedExpanded },
                    currentReps = repsExpanded,
                    currentWeight = weightExpanded,
                    onRepsChange = { repsExpanded = it },
                    onWeightChange = { weightExpanded = it }
                )

                // Pending state
                Text(
                    text = "Pending State",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExerciseCard(
                    exerciseName = "Shoulder Press",
                    muscleGroup = "Shoulders",
                    sets = listOf(
                        SetInfo(1, 0, 0f, SetState.PENDING),
                        SetInfo(2, 0, 0f, SetState.PENDING),
                        SetInfo(3, 0, 0f, SetState.PENDING)
                    ),
                    state = ExerciseCardState.PENDING
                )
            }
        }
    }
}
