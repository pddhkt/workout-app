package com.workout.app.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.cards.ElevatedCard
import com.workout.app.ui.theme.AppTheme

/**
 * Card displaying a proposed exercise from the AI assistant.
 * Shows exercise name, muscle group chip, category, equipment,
 * difficulty, instructions, and a "Save Exercise" button.
 *
 * @param name Exercise name
 * @param muscleGroup Target muscle group
 * @param category Exercise category (e.g., compound, isolation)
 * @param equipment Equipment needed
 * @param difficulty Difficulty level
 * @param instructions Exercise instructions / form cues
 * @param onSave Callback when "Save Exercise" is tapped
 * @param modifier Optional modifier
 */
@Composable
fun ExerciseProposalCard(
    name: String,
    muscleGroup: String,
    category: String?,
    equipment: String?,
    difficulty: String?,
    instructions: String?,
    recordingFields: List<com.workout.app.domain.model.RecordingField>? = null,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        // Exercise name
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

        // Muscle group chip
        MuscleGroupChip(muscleGroup = muscleGroup)

        // Detail rows
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) {
            if (!category.isNullOrBlank()) {
                DetailRow(label = "Category", value = category)
            }
            if (!equipment.isNullOrBlank()) {
                DetailRow(label = "Equipment", value = equipment)
            }
            if (!difficulty.isNullOrBlank()) {
                DetailRow(label = "Difficulty", value = difficulty)
            }
            if (recordingFields != null && recordingFields.isNotEmpty()) {
                val desc = recordingFields.joinToString(" + ") { field ->
                    if (field.unit.isNotEmpty()) "${field.label} (${field.unit})" else field.label
                }
                DetailRow(label = "Records", value = desc)
            }
        }

        // Instructions
        if (!instructions.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
            Text(
                text = instructions,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Save button
        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
        PrimaryButton(
            text = "Save Exercise",
            onClick = onSave,
            fullWidth = true
        )
    }
}

/**
 * Muscle group chip with primary color background at 15% alpha.
 */
@Composable
private fun MuscleGroupChip(
    muscleGroup: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = muscleGroup,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(
                horizontal = AppTheme.spacing.sm,
                vertical = AppTheme.spacing.xs
            )
    )
}

/**
 * Label-value detail row for exercise properties.
 */
@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
