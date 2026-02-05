package com.workout.app.ui.components.recovery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.MuscleRecovery
import com.workout.app.domain.model.RecoveryStatus
import com.workout.app.domain.model.RecoveryTimeRange
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.theme.AppTheme

@Composable
fun MuscleRecoveryCard(
    muscleRecoveryList: List<MuscleRecovery>,
    selectedMuscleGroup: String?,
    onMuscleGroupSelected: (String?) -> Unit,
    timeRange: RecoveryTimeRange,
    onToggleTimeRange: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (muscleRecoveryList.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    BaseCard(
        modifier = modifier,
        onClick = { expanded = !expanded },
        contentPadding = AppTheme.spacing.lg
    ) {
        // Header row - always visible
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                Text(
                    text = "Muscle Groups",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "${timeRange.label} >",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onToggleTimeRange)
            )
        }

        // Expandable content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))

                muscleRecoveryList.forEach { recovery ->
                    MuscleRecoveryRow(
                        recovery = recovery,
                        isSelected = selectedMuscleGroup.equals(recovery.muscleGroup, ignoreCase = true),
                        onClick = {
                            if (selectedMuscleGroup.equals(recovery.muscleGroup, ignoreCase = true)) {
                                onMuscleGroupSelected(null) // Deselect
                            } else {
                                onMuscleGroupSelected(recovery.muscleGroup)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                }
            }
        }
    }
}

@Composable
private fun MuscleRecoveryRow(
    recovery: MuscleRecovery,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barColor = when (recovery.status) {
        RecoveryStatus.REST -> AppTheme.colors.error
        RecoveryStatus.RECOVERING -> AppTheme.colors.warning
        RecoveryStatus.READY -> AppTheme.colors.success
        RecoveryStatus.TRAIN -> AppTheme.colors.info
        RecoveryStatus.NEW -> AppTheme.colors.info
    }

    val statusColor = when (recovery.status) {
        RecoveryStatus.REST -> AppTheme.colors.error
        RecoveryStatus.RECOVERING -> AppTheme.colors.warning
        RecoveryStatus.READY -> AppTheme.colors.success
        RecoveryStatus.TRAIN -> AppTheme.colors.info
        RecoveryStatus.NEW -> AppTheme.colors.info
    }

    // Animate bar fill
    var animationStarted by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationStarted) recovery.progress else 0f,
        animationSpec = tween(durationMillis = 600)
    )
    LaunchedEffect(Unit) { animationStarted = true }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface.copy(alpha = 0f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = AppTheme.spacing.sm, horizontal = AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Muscle group name
        Text(
            text = recovery.muscleGroup,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(76.dp)
        )

        // Days label
        Text(
            text = if (recovery.daysSinceLastTrained != null) "${recovery.daysSinceLastTrained}d" else "--",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(28.dp)
        )

        Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

        // Recovery bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }

        Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

        // Status label
        Text(
            text = recovery.status.label,
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp)
        )

        // Check mark for selected
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = if (isSelected) "Selected" else null,
            tint = if (isSelected) AppTheme.colors.success
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f),
            modifier = Modifier
                .padding(start = AppTheme.spacing.xs)
                .size(16.dp)
        )
    }
}
