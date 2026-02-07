package com.workout.app.ui.components.recovery

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
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
    timeRange: RecoveryTimeRange = RecoveryTimeRange.WEEKLY,
    onToggleTimeRange: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (muscleRecoveryList.isEmpty()) return

    var detailMuscleGroup by remember { mutableStateOf<String?>(null) }
    val detailRecovery = detailMuscleGroup?.let { name ->
        muscleRecoveryList.find { it.muscleGroup.equals(name, ignoreCase = true) }
    }

    BaseCard(
        modifier = modifier,
        contentPadding = AppTheme.spacing.lg
    ) {
        AnimatedContent(
            targetState = detailMuscleGroup,
            transitionSpec = {
                if (targetState != null) {
                    // Summary → Detail: slide in from right
                    (slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    )
                } else {
                    // Detail → Summary: slide in from left
                    (slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    )
                }
            },
            label = "recovery_summary_detail"
        ) { targetDetail ->
            if (targetDetail == null) {
                // Summary list view
                Column {
                    muscleRecoveryList.forEach { recovery ->
                        MuscleRecoveryRow(
                            recovery = recovery,
                            isSelected = selectedMuscleGroup.equals(recovery.muscleGroup, ignoreCase = true),
                            onClick = {
                                if (selectedMuscleGroup.equals(recovery.muscleGroup, ignoreCase = true)) {
                                    onMuscleGroupSelected(null)
                                } else {
                                    onMuscleGroupSelected(recovery.muscleGroup)
                                }
                                detailMuscleGroup = recovery.muscleGroup
                            }
                        )
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                    }
                }
            } else {
                // Detail view
                val recovery = muscleRecoveryList.find {
                    it.muscleGroup.equals(targetDetail, ignoreCase = true)
                }
                if (recovery != null) {
                    MuscleRecoveryDetailView(
                        recovery = recovery,
                        onBackClick = { detailMuscleGroup = null }
                    )
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
    val statusColor = when (recovery.status) {
        RecoveryStatus.REST -> AppTheme.colors.error
        RecoveryStatus.RECOVERING -> AppTheme.colors.primaryText
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface.copy(alpha = 0f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = AppTheme.spacing.sm, horizontal = AppTheme.spacing.xs)
    ) {
        // Line 1: muscle name (left) + status label (right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = recovery.muscleGroup,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) AppTheme.colors.primaryText
                    else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = recovery.status.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Line 2: "Xd · Y sets" (left) + recovery bar (center) + chevron (right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Days + sets compact
            Text(
                text = "${recovery.elapsedLabel} · ${recovery.weeklySetCount} sets",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(76.dp)
            )

            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

            // Recovery bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animatedProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(statusColor)
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

            // Chevron
            Text(
                text = ">",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.width(12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
