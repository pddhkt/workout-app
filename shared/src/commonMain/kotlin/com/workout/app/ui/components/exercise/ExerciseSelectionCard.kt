package com.workout.app.ui.components.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.presentation.planning.ExercisePreset
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.theme.AppTheme

/**
 * Exercise selection card with exercise name, category, selection checkmark,
 * and expandable preset options for controlling how input fields are pre-filled.
 *
 * @param exerciseName Name of the exercise
 * @param exerciseCategory Category of the exercise
 * @param isAdded Whether the exercise is selected/added
 * @param onCardClick Callback when card is clicked
 * @param modifier Modifier
 * @param isExpanded Whether the preset options are visible
 * @param lastWorkoutSummary Summary text for last workout preset
 * @param isLoadingLastWorkout Whether last workout data is being loaded
 * @param onPresetSelected Callback when a preset option is chosen
 * @param equipmentType Equipment type badge text
 * @param enabled Whether interactions are enabled
 */
@Composable
fun ExerciseSelectionCard(
    exerciseName: String,
    exerciseCategory: String,
    isAdded: Boolean,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    lastWorkoutSummary: String? = null,
    isLoadingLastWorkout: Boolean = false,
    onPresetSelected: ((ExercisePreset) -> Unit)? = null,
    equipmentType: String? = null,
    selectedSummary: String? = null,
    enabled: Boolean = true
) {
    val cardBorder = if (isAdded) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    val checkScale by animateFloatAsState(
        targetValue = if (isAdded) 1f else 0.8f,
        animationSpec = tween(durationMillis = 200),
        label = "check_scale"
    )

    BaseCard(
        modifier = modifier,
        enabled = enabled,
        onClick = onCardClick,
        border = cardBorder,
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentPadding = AppTheme.spacing.md
    ) {
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
                if (equipmentType != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = equipmentType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isAdded && !isExpanded && selectedSummary != null) {
                    Text(
                        text = selectedSummary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right: Indicators
            Icon(
                imageVector = if (isAdded) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isAdded) "Selected" else "Select",
                tint = if (isAdded) AppTheme.colors.success else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(24.dp)
                    .scale(checkScale)
            )
        }

        // Expandable preset options
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(tween(150))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
            ) {
                PresetOptionRow(
                    icon = Icons.Outlined.AutoAwesome,
                    label = "Recommended",
                    subtitle = "10 reps, add weight during workout",
                    enabled = true,
                    onClick = { onPresetSelected?.invoke(ExercisePreset.RECOMMENDED) }
                )
                PresetOptionRow(
                    icon = Icons.Outlined.History,
                    label = "Last Workout",
                    subtitle = when {
                        isLoadingLastWorkout -> "Loading..."
                        lastWorkoutSummary != null -> lastWorkoutSummary
                        else -> "No history"
                    },
                    enabled = !isLoadingLastWorkout && lastWorkoutSummary != null,
                    onClick = { onPresetSelected?.invoke(ExercisePreset.LAST_WORKOUT) }
                )
                PresetOptionRow(
                    icon = Icons.Outlined.EditNote,
                    label = "Empty",
                    subtitle = "Fill in during workout",
                    enabled = true,
                    onClick = { onPresetSelected?.invoke(ExercisePreset.EMPTY) }
                )
            }
        }
    }
}

@Composable
private fun PresetOptionRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = if (enabled) 1f else 0.4f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
        )
        Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
            )
        }
    }
}
