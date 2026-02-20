package com.workout.app.ui.components.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.TemplateExerciseInfo
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.cards.ElevatedCard
import com.workout.app.ui.theme.AppTheme

/**
 * Card displaying a proposed workout template from the AI assistant.
 * Shows template name, description, exercise list with sets/reps,
 * estimated duration, and a "Save Template" button.
 *
 * @param name Template name
 * @param description Optional template description
 * @param exercises List of exercises in the template
 * @param estimatedDuration Estimated workout duration in minutes
 * @param onSave Callback when "Save Template" is tapped
 * @param modifier Optional modifier
 */
@Composable
fun TemplateProposalCard(
    name: String,
    description: String?,
    exercises: List<TemplateExerciseInfo>,
    estimatedDuration: Int?,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        // Template name
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Description
        if (!description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Duration and exercise count
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise count
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${exercises.size} exercises",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Estimated duration
            if (estimatedDuration != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$estimatedDuration min",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Divider
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

        // Exercise list
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) {
            exercises.forEachIndexed { index, exercise ->
                ExerciseRow(
                    number = index + 1,
                    exercise = exercise
                )
            }
        }

        // Save button
        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
        PrimaryButton(
            text = "Save Template",
            onClick = onSave,
            fullWidth = true
        )
    }
}

/**
 * Row displaying a single exercise within the template proposal.
 * Shows: "1. Exercise Name - 3 x 8-12"
 */
@Composable
private fun ExerciseRow(
    number: Int,
    exercise: TemplateExerciseInfo,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$number. ${exercise.name}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${exercise.sets} x ${exercise.reps}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
