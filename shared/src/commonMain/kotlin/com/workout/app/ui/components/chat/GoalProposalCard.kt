package com.workout.app.ui.components.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.cards.ElevatedCard
import com.workout.app.ui.theme.AppTheme

/**
 * Card displaying a proposed fitness goal from the AI assistant.
 * Shows goal name, target, frequency, exercises, and a "Save Goal" button.
 *
 * @param name Goal name
 * @param exerciseNames List of exercise names the goal tracks
 * @param metric What metric to track
 * @param targetValue Target value per period
 * @param targetUnit Unit for the target value
 * @param frequency How often the target resets
 * @param isOngoing Whether this is an ongoing repeating goal
 * @param onSave Callback when "Save Goal" is tapped
 * @param modifier Optional modifier
 */
@Composable
fun GoalProposalCard(
    name: String,
    exerciseNames: List<String>,
    metric: String,
    targetValue: Double,
    targetUnit: String,
    frequency: String,
    isOngoing: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        // Goal name
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

        // Target and frequency
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.TrackChanges,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val formattedTarget = if (targetValue == targetValue.toLong().toDouble()) {
                    "${targetValue.toLong()} $targetUnit"
                } else {
                    "$targetValue $targetUnit"
                }
                Text(
                    text = formattedTarget,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Repeat,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = frequency.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Exercise list (if any)
        if (exerciseNames.isNotEmpty()) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
            ) {
                Text(
                    text = "Tracked exercises",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                exerciseNames.forEach { exerciseName ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = exerciseName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Ongoing badge
        if (isOngoing) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            Text(
                text = "Ongoing (repeats every $frequency period)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Save button
        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
        PrimaryButton(
            text = "Save Goal",
            onClick = onSave,
            fullWidth = true
        )
    }
}
