package com.workout.app.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.workout.app.domain.model.GoalPeriodEntry
import com.workout.app.presentation.goals.GoalDetailViewModel
import com.workout.app.ui.components.goals.GoalProgressBar
import com.workout.app.ui.theme.AccentGreen
import com.workout.app.ui.theme.AppTheme
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * Goal Detail Screen showing current progress, history, and management actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GoalDetailViewModel = koinInject { parametersOf(goalId) }
    val state by viewModel.state.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.goal?.name ?: "Goal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(goalId) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            state.goal == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "Goal not found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                val goal = state.goal!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = AppTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
                ) {
                    // Current period progress
                    item {
                        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                        CurrentPeriodSection(
                            progressFraction = goal.progressFraction,
                            progressPercent = goal.progressPercent,
                            currentValue = goal.currentPeriodValue,
                            targetValue = goal.targetValue,
                            targetUnit = goal.targetUnit,
                            isCompleted = goal.currentPeriodCompleted,
                            frequency = goal.frequency.label
                        )
                    }

                    // Streak
                    if (state.streakCount > 0) {
                        item {
                            StreakSection(streakCount = state.streakCount, frequency = goal.frequency.label)
                        }
                    }

                    // Goal info
                    item {
                        GoalInfoSection(
                            frequency = goal.frequency.label,
                            metric = goal.metric.label,
                            isOngoing = goal.isOngoing,
                            endDate = goal.endDate,
                            isActive = goal.isActive
                        )
                    }

                    // Period history
                    if (state.periodHistory.isNotEmpty()) {
                        item {
                            Text(
                                text = "History",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        items(state.periodHistory) { entry ->
                            PeriodHistoryRow(entry = entry)
                        }
                    }

                    // Actions
                    item {
                        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                        ) {
                            TextButton(
                                onClick = { viewModel.toggleActive() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (goal.isActive) "Pause Goal" else "Resume Goal",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = {
                                    viewModel.deleteGoal { onBackClick() }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Delete Goal",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentPeriodSection(
    progressFraction: Float,
    progressPercent: Int,
    currentValue: Double,
    targetValue: Double,
    targetUnit: String,
    isCompleted: Boolean,
    frequency: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(AppTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        Text(
            text = "This ${frequency.lowercase().removeSuffix("ly")}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Large percentage
        Text(
            text = "$progressPercent%",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (isCompleted) AccentGreen else MaterialTheme.colorScheme.onSurface
        )

        // Progress bar
        GoalProgressBar(
            progress = progressFraction,
            isCompleted = isCompleted,
            modifier = Modifier.fillMaxWidth()
        )

        // Value text
        Text(
            text = "${formatValue(currentValue)} / ${formatValue(targetValue)} $targetUnit",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StreakSection(
    streakCount: Int,
    frequency: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.LocalFireDepartment,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = " $streakCount ${frequency.lowercase()} streak",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun GoalInfoSection(
    frequency: String,
    metric: String,
    isOngoing: Boolean,
    endDate: Long?,
    isActive: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(AppTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        InfoRow(label = "Frequency", value = frequency)
        InfoRow(label = "Metric", value = metric)
        InfoRow(
            label = "Duration",
            value = if (isOngoing) "Ongoing" else {
                if (endDate != null) {
                    val date = Instant.fromEpochMilliseconds(endDate)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    "Until ${formatMonth(date.monthNumber)} ${date.dayOfMonth}, ${date.year}"
                } else "Ongoing"
            }
        )
        InfoRow(
            label = "Status",
            value = if (isActive) "Active" else "Paused"
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PeriodHistoryRow(entry: GoalPeriodEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        // Date
        Text(
            text = formatPeriodDate(entry.periodStart),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.3f)
        )

        // Mini progress bar
        GoalProgressBar(
            progress = entry.progressFraction,
            isCompleted = entry.isCompleted,
            modifier = Modifier.weight(0.5f)
        )

        // Percentage
        Text(
            text = "${entry.progressPercent}%",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (entry.isCompleted) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.2f)
        )
    }
}

private fun formatValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        "%.1f".format(value)
    }
}

private fun formatPeriodDate(timestamp: Long): String {
    val date = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${formatMonth(date.monthNumber)} ${date.dayOfMonth}"
}

private fun formatMonth(month: Int): String = when (month) {
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
