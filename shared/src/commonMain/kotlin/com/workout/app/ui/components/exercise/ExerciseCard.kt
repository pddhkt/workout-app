package com.workout.app.ui.components.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.components.chips.SetChip
import com.workout.app.ui.components.chips.SetState
import com.workout.app.ui.components.inputs.NumberStepper
import com.workout.app.ui.theme.Active
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.Completed
import com.workout.app.ui.theme.Pending

/**
 * State of an exercise card
 */
enum class ExerciseCardState {
    /** Exercise is completed (all sets done) */
    COMPLETED,
    /** Exercise is currently active/in progress */
    ACTIVE,
    /** Exercise is pending/not started */
    PENDING
}

/**
 * Data class for set information
 */
data class SetInfo(
    val setNumber: Int,
    val reps: Int,
    val weight: Float,
    val state: SetState
)

/**
 * Exercise card component with three states: completed, active, and pending.
 * Based on elements EL-09, EL-10, EL-79 from mockups.
 *
 * Features:
 * - Completed state: Green accent with checkmark
 * - Active state: Highlighted, expanded with set input form
 * - Pending state: Muted styling
 * - SetChip row showing set progress
 * - Click to expand/collapse functionality
 *
 * @param exerciseName Name of the exercise (e.g., "Bench Press")
 * @param muscleGroup Muscle group targeted (e.g., "Chest")
 * @param sets List of set information
 * @param state Current state of the exercise card
 * @param isExpanded Whether the card is expanded (only relevant for ACTIVE state)
 * @param onExpandToggle Callback when card is clicked to expand/collapse
 * @param onSetClick Optional callback when a set chip is clicked
 * @param onRepsChange Callback when reps value changes in the active form
 * @param onWeightChange Callback when weight value changes in the active form
 * @param currentReps Current reps value for the active set (only used in ACTIVE state)
 * @param currentWeight Current weight value for the active set (only used in ACTIVE state)
 * @param modifier Optional modifier for customization
 */
@Composable
fun ExerciseCard(
    exerciseName: String,
    muscleGroup: String,
    sets: List<SetInfo>,
    state: ExerciseCardState,
    isExpanded: Boolean = false,
    onExpandToggle: () -> Unit = {},
    onSetClick: ((Int) -> Unit)? = null,
    onRepsChange: (Int) -> Unit = {},
    onWeightChange: (Float) -> Unit = {},
    currentReps: Int = 0,
    currentWeight: Float = 0f,
    modifier: Modifier = Modifier
) {
    val stateColor = when (state) {
        ExerciseCardState.COMPLETED -> Completed
        ExerciseCardState.ACTIVE -> Active
        ExerciseCardState.PENDING -> Pending
    }

    val backgroundColor = when (state) {
        ExerciseCardState.COMPLETED -> MaterialTheme.colorScheme.surface
        ExerciseCardState.ACTIVE -> MaterialTheme.colorScheme.surface
        ExerciseCardState.PENDING -> MaterialTheme.colorScheme.surface
    }

    val colors = AppTheme.colors
    val border = when (state) {
        ExerciseCardState.COMPLETED -> BorderStroke(1.dp, colors.success.copy(alpha = 0.5f))
        ExerciseCardState.ACTIVE -> BorderStroke(2.dp, Active)
        ExerciseCardState.PENDING -> null
    }

    val contentColor = when (state) {
        ExerciseCardState.COMPLETED -> MaterialTheme.colorScheme.onSurface
        ExerciseCardState.ACTIVE -> MaterialTheme.colorScheme.onSurface
        ExerciseCardState.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    BaseCard(
        modifier = modifier.fillMaxWidth(),
        onClick = if (state == ExerciseCardState.ACTIVE) onExpandToggle else null,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        border = border,
        contentPadding = AppTheme.spacing.lg
    ) {
        // Header section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                Text(
                    text = muscleGroup,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Completed checkmark
            if (state == ExerciseCardState.COMPLETED) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Exercise completed",
                    tint = colors.success,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Set chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            sets.forEach { setInfo ->
                SetChip(
                    setNumber = setInfo.setNumber,
                    state = setInfo.state,
                    onClick = if (onSetClick != null) {
                        { onSetClick(setInfo.setNumber) }
                    } else null
                )
            }
        }

        // Expanded form for active state
        if (state == ExerciseCardState.ACTIVE) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

                    // Set input form
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
                    ) {
                        // Reps stepper
                        NumberStepper(
                            value = currentReps,
                            onValueChange = onRepsChange,
                            label = "Reps",
                            minValue = 0,
                            maxValue = 999,
                            modifier = Modifier.weight(1f)
                        )

                        // Weight stepper
                        com.workout.app.ui.components.inputs.DecimalNumberStepper(
                            value = currentWeight,
                            onValueChange = onWeightChange,
                            label = "Weight",
                            minValue = 0f,
                            maxValue = 999.9f,
                            step = 2.5f,
                            unit = "kg",
                            decimalPlaces = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
