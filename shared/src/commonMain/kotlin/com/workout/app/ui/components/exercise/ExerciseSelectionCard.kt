package com.workout.app.ui.components.exercise

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.theme.AppTheme

/**
 * Exercise selection card with exercise name, category, and selection checkmark.
 *
 * @param exerciseName Name of the exercise
 * @param exerciseCategory Category of the exercise
 * @param isAdded Whether the exercise is selected/added
 * @param onToggle Callback when card is clicked (toggles selection)
 * @param modifier Modifier
 * @param enabled Whether interactions are enabled
 */
@Composable
fun ExerciseSelectionCard(
    exerciseName: String,
    exerciseCategory: String,
    isAdded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val cardBorder = if (isAdded) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    val cardBackground = if (isAdded) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val checkScale by animateFloatAsState(
        targetValue = if (isAdded) 1f else 0.8f,
        animationSpec = tween(durationMillis = 200),
        label = "check_scale"
    )

    BaseCard(
        modifier = modifier,
        enabled = enabled,
        onClick = onToggle,
        border = cardBorder,
        backgroundColor = cardBackground,
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
    }
}
