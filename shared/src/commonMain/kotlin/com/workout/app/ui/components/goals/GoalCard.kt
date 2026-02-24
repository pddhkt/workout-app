package com.workout.app.ui.components.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.GoalWithProgress
import com.workout.app.ui.components.cards.ElevatedCard
import com.workout.app.ui.theme.AccentGreen
import com.workout.app.ui.theme.AppTheme

/**
 * Goal card component for displaying goal progress.
 * Shows goal name, progress bar, metric values, frequency, and optional toggle.
 */
@Composable
fun GoalCard(
    goal: GoalWithProgress,
    onClick: () -> Unit,
    onToggleActive: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            // Top row: name + streak/completion badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (goal.currentPeriodCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Completed",
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (goal.streakCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${goal.streakCount}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Progress bar
            GoalProgressBar(
                progress = goal.progressFraction,
                isCompleted = goal.currentPeriodCompleted
            )

            // Progress text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatGoalValue(goal.currentPeriodValue)} / ${formatGoalValue(goal.targetValue)} ${goal.targetUnit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${goal.progressPercent}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (goal.currentPeriodCompleted) AccentGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bottom row: frequency label + toggle (for ongoing goals)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val durationText = if (goal.isOngoing) "Ongoing" else formatEndDate(goal.endDate)
                Text(
                    text = "${goal.frequency.label} · $durationText",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (goal.isOngoing && onToggleActive != null) {
                    Switch(
                        checked = goal.isActive,
                        onCheckedChange = onToggleActive,
                        modifier = Modifier.height(24.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}

/**
 * Format a goal value for display, stripping unnecessary decimals.
 */
private fun formatGoalValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        "%.1f".format(value)
    }
}

/**
 * Format end date timestamp for display.
 */
private fun formatEndDate(endDate: Long?): String {
    if (endDate == null) return "Ongoing"
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(endDate)
    val localDate = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
    val month = when (localDate.monthNumber) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""
    }
    return "Until $month ${localDate.year}"
}
