package com.workout.app.ui.components.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.theme.AppTheme

/**
 * Data model for a previous workout record.
 * Used for displaying history summary in selection cards.
 */
data class PreviousRecord(
    val date: String,
    val reps: String,
    val weight: String
)

/**
 * Exercise selection card with accordion behavior showing history.
 *
 * @param exerciseName Name of the exercise
 * @param exerciseCategory Category of the exercise
 * @param isAdded Whether the exercise is selected/added
 * @param history List of previous history items
 * @param onToggle Callback when card is clicked (toggles selection and expansion)
 * @param modifier Modifier
 * @param enabled Whether interactions are enabled
 */
@Composable
fun ExerciseSelectionCard(
    exerciseName: String,
    exerciseCategory: String,
    isAdded: Boolean,
    history: List<PreviousRecord> = emptyList(),
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    BaseCard(
        modifier = modifier,
        enabled = enabled,
        onClick = onToggle,
        contentPadding = AppTheme.spacing.md
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Exercise info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
                ) {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = exerciseCategory,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Right: Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = if (isAdded) "Selected" else "Select",
                        tint = if (isAdded) AppTheme.colors.success else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Expanded History
            AnimatedVisibility(visible = isAdded) {
                Column(
                    modifier = Modifier.padding(top = AppTheme.spacing.md)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    if (history.isEmpty()) {
                        Text(
                            text = "No previous history",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = AppTheme.spacing.md)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(top = AppTheme.spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                        ) {
                            history.forEach { item ->
                                HistoryRow(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(item: PreviousRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.date,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${item.reps} reps • ${item.weight}",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
