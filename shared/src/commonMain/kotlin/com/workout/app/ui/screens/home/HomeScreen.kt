package com.workout.app.ui.screens.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.workout.app.domain.model.GoalWithProgress
import com.workout.app.ui.components.cards.ElevatedCard
import com.workout.app.ui.components.dataviz.ConsistencyHeatmap
import com.workout.app.ui.components.dataviz.HeatmapDay
import com.workout.app.ui.components.goals.GoalCard
import com.workout.app.ui.components.headers.SectionHeaderWithAction
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Data for a workout template
 */
data class WorkoutTemplate(
    val id: String,
    val name: String,
    val exerciseCount: Int,
    val estimatedDuration: String
)

/**
 * Data for a recent session
 */
data class RecentSession(
    val id: String,
    val workoutName: String,
    val date: String,
    val duration: String,
    val exerciseCount: Int,
    val totalSets: Int,
    val exerciseNames: String? = null
)

/**
 * Home Screen of the workout app.
 * Main dashboard showing overview, heatmap, and recent sessions.
 *
 * @param templates List of workout templates to display
 * @param recentSessions List of recent sessions to display
 * @param onTemplateClick Callback when a template is selected
 * @param onSessionClick Callback when a recent session is selected
 * @param onViewAllTemplates Callback for "View All" templates action
 * @param onViewAllSessions Callback for "View All" sessions action
 * @param heatmapData Workout consistency heatmap data
 * @param modifier Optional modifier for customization
 */
@Composable
fun HomeScreen(
    templates: List<WorkoutTemplate>,
    recentSessions: List<RecentSession>,
    activeGoals: List<GoalWithProgress> = emptyList(),
    onTemplateClick: (String) -> Unit = {},
    onSessionClick: (String) -> Unit = {},
    onViewAllTemplates: () -> Unit = {},
    onViewAllSessions: () -> Unit = {},
    onGoalClick: (String) -> Unit = {},
    onManageGoals: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    heatmapData: List<HeatmapDay> = emptyList(),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // TOP SECTION - Yellow background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(bottom = AppTheme.spacing.xl)
        ) {
            // Header with greeting and date
            HomeHeader(
                onHistoryClick = onHistoryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg)
                    .padding(top = AppTheme.spacing.xl)
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

            // Consistency Heatmap - full width with padding
            ConsistencyHeatmap(
                days = heatmapData,
                showDayLabels = true,
                title = null,
                emptyColor = Color.White.copy(alpha = 0.4f),
                legendTextColor = MaterialTheme.colorScheme.onPrimary,
                dayLabelColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg)
            )
        }

        // BOTTOM SECTION - Light gray background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
                .padding(top = AppTheme.spacing.xl)
        ) {
            // Active Goals
            ActiveGoalsSection(
                goals = activeGoals,
                onGoalClick = onGoalClick,
                onManageGoals = onManageGoals,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppTheme.spacing.lg)
            )
        }
    }

    // AI Assistant FAB
    FloatingActionButton(
        onClick = onChatClick,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = AppTheme.spacing.lg, bottom = AppTheme.spacing.lg),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = "AI Assistant",
            modifier = Modifier.size(24.dp)
        )
    }
    }
}

/**
 * Header section with greeting and current date
 */
@Composable
private fun HomeHeader(
    onHistoryClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentDate = remember {
        val now = Clock.System.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val dayOfWeek = when (localDate.dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> "Monday"
            kotlinx.datetime.DayOfWeek.TUESDAY -> "Tuesday"
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Wednesday"
            kotlinx.datetime.DayOfWeek.THURSDAY -> "Thursday"
            kotlinx.datetime.DayOfWeek.FRIDAY -> "Friday"
            kotlinx.datetime.DayOfWeek.SATURDAY -> "Saturday"
            kotlinx.datetime.DayOfWeek.SUNDAY -> "Sunday"
            else -> ""
        }
        val month = when (localDate.monthNumber) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> ""
        }
        "$dayOfWeek, $month ${localDate.dayOfMonth}"
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        }
        IconButton(onClick = onHistoryClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "Session History",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Active goals section showing goal progress cards.
 */
@Composable
private fun ActiveGoalsSection(
    goals: List<GoalWithProgress>,
    onGoalClick: (String) -> Unit,
    onManageGoals: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        SectionHeaderWithAction(
            title = "Goals",
            actionText = "Manage",
            onActionClick = onManageGoals
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        if (goals.isEmpty()) {
            GoalsEmptyState(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppTheme.spacing.xxl)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { onGoalClick(goal.id) }
                    )
                }
            }
        }
    }
}

/**
 * Empty state for when there are no active goals.
 */
@Composable
private fun GoalsEmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            Text(
                text = "No active goals",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap Manage to create your first goal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Recent session card component
 */
@Composable
private fun RecentSessionCard(
    session: RecentSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            // Workout name and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = session.workoutName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = session.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Exercise names or fallback to count
            Text(
                text = if (!session.exerciseNames.isNullOrBlank()) {
                    session.exerciseNames
                } else {
                    "${session.exerciseCount} exercises"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Session stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
            ) {
                SessionStat(
                    label = "Duration",
                    value = session.duration
                )
                SessionStat(
                    label = "Exercises",
                    value = "${session.exerciseCount}"
                )
                SessionStat(
                    label = "Sets",
                    value = "${session.totalSets}"
                )
            }
        }
    }
}

/**
 * Session stat component - label and value pair
 */
@Composable
private fun SessionStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = AppTheme.colors.primaryText
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
