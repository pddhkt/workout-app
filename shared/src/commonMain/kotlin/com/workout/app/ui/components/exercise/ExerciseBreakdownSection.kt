package com.workout.app.ui.components.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.ExerciseWithSets
import com.workout.app.domain.model.SetData
import com.workout.app.ui.components.chips.Badge
import com.workout.app.ui.components.chips.BadgeVariant
import com.workout.app.ui.theme.AppTheme

/**
 * Section showing exercise breakdown with sets (always expanded).
 *
 * @param exercises List of exercises with their sets
 * @param modifier Optional modifier
 */
@Composable
fun ExerciseBreakdownSection(
    exercises: List<ExerciseWithSets>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header (non-clickable)
        Text(
            text = "Exercises (${exercises.size})",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = AppTheme.spacing.sm)
        )

        // Exercise cards
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            exercises.forEach { exercise ->
                ExerciseBreakdownCard(
                    exercise = exercise
                )
            }
        }
    }
}

@Composable
private fun ExerciseBreakdownCard(
    exercise: ExerciseWithSets,
    modifier: Modifier = Modifier
) {
    var showSets by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { showSets = !showSets }
            .padding(AppTheme.spacing.md)
    ) {
        // Exercise header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${exercise.sets.size} sets" + (exercise.bestSet?.let {
                        " | Best: ${it.weight}kg x ${it.reps}"
                    } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (exercise.hasPR) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "PR",
                        modifier = Modifier.size(16.dp),
                        tint = AppTheme.colors.primaryText
                    )
                    Badge(
                        text = "PR",
                        variant = BadgeVariant.WARNING
                    )
                }
            }
        }

        // Expanded sets view
        AnimatedVisibility(
            visible = showSets,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = AppTheme.spacing.md)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                // Sets table header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Set",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Weight",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Reps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "RPE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

                // Sets rows
                exercise.sets.forEach { set ->
                    SetRow(set = set)
                }
            }
        }
    }
}

@Composable
private fun SetRow(
    set: SetData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) {
            Text(
                text = if (set.isWarmup) "W" else set.setNumber.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (set.isWarmup) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            if (set.isPR) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "PR",
                    modifier = Modifier.size(12.dp),
                    tint = AppTheme.colors.primaryText
                )
            }
        }
        Text(
            text = "${set.weight} kg",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${set.reps}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = set.rpe?.toString() ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
