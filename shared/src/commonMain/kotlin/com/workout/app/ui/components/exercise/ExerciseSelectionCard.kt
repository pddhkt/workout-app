package com.workout.app.ui.components.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.FilledIconButton
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.components.inputs.NumberStepper
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.OnSurfaceVariant
import com.workout.app.ui.theme.Primary
import com.workout.app.ui.theme.Success

/**
 * Exercise selection card for session planning with added/default states.
 * Based on mockup elements EL-39, EL-40.
 *
 * This card displays exercise information and allows users to:
 * - Add exercise to workout (default state)
 * - Remove exercise from workout (added state)
 * - Adjust number of sets (when added)
 *
 * @param exerciseName Name of the exercise (e.g., "Barbell Squat")
 * @param exerciseCategory Category or muscle group (e.g., "Legs", "Push")
 * @param isAdded Whether the exercise is currently added to the workout
 * @param setCount Number of sets when added. Only displayed when isAdded = true
 * @param onAddClick Callback when add button is clicked
 * @param onRemoveClick Callback when remove button is clicked (only when isAdded = true)
 * @param onSetCountChange Callback when set count changes (only when isAdded = true)
 * @param modifier Modifier to be applied to the card
 * @param enabled Whether the card interactions are enabled
 * @param minSets Minimum number of sets allowed. Defaults to 1
 * @param maxSets Maximum number of sets allowed. Defaults to 10
 */
@Composable
fun ExerciseSelectionCard(
    exerciseName: String,
    exerciseCategory: String,
    isAdded: Boolean,
    setCount: Int,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onSetCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minSets: Int = 1,
    maxSets: Int = 10
) {
    BaseCard(
        modifier = modifier,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Exercise info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                Text(
                    text = exerciseCategory,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            // Right: Add/Remove button or Added indicator
            if (isAdded) {
                // Added state: Show checkmark and remove button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Exercise added",
                        tint = Success,
                        modifier = Modifier.size(24.dp)
                    )
                    FilledIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Remove exercise",
                        onClick = onRemoveClick,
                        enabled = enabled,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                // Default state: Show add button
                FilledIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Add exercise",
                    onClick = onAddClick,
                    enabled = enabled,
                    containerColor = Primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Set count stepper - only visible when added
        if (isAdded) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            NumberStepper(
                value = setCount,
                onValueChange = onSetCountChange,
                label = "Sets",
                minValue = minSets,
                maxValue = maxSets,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
