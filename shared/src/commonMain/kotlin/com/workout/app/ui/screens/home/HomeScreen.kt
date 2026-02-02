package com.workout.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.cards.ElevatedCard
import com.workout.app.ui.components.dataviz.ConsistencyHeatmap
import com.workout.app.ui.components.dataviz.HeatmapDay
import com.workout.app.ui.components.headers.SectionHeaderWithAction
import com.workout.app.ui.components.navigation.BottomNavBar
import com.workout.app.ui.theme.AppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Mock data for a workout template
 */
data class WorkoutTemplate(
    val id: String,
    val name: String,
    val exerciseCount: Int,
    val estimatedDuration: String
)

/**
 * Mock data for a recent session
 */
data class RecentSession(
    val id: String,
    val workoutName: String,
    val date: String,
    val duration: String,
    val exerciseCount: Int,
    val totalSets: Int
)

/**
 * Home Screen of the workout app.
 * Based on mockups - main dashboard showing overview, templates, and recent sessions.
 *
 * Features:
 * - HomeHeader with greeting and date
 * - ConsistencyHeatmap widget
 * - Quick Start Templates section with horizontal scroll
 * - Recent Sessions section with session cards
 * - BottomNavBar with Home selected
 *
 * @param templates List of workout templates to display
 * @param recentSessions List of recent sessions to display
 * @param onTemplateClick Callback when a template is selected
 * @param onSessionClick Callback when a recent session is selected
 * @param onViewAllTemplates Callback for "View All" templates action
 * @param onViewAllSessions Callback for "View All" sessions action
 * @param onNavigate Callback for bottom navigation
 * @param onAddClick Callback for center Add button
 * @param modifier Optional modifier for customization
 */
@Composable
fun HomeScreen(
    templates: List<WorkoutTemplate>,
    recentSessions: List<RecentSession>,
    onTemplateClick: (String) -> Unit = {},
    onSessionClick: (String) -> Unit = {},
    onViewAllTemplates: () -> Unit = {},
    onViewAllSessions: () -> Unit = {},
    onNavigate: (Int) -> Unit = {},
    onAddClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedNavIndex,
                onItemSelected = { index ->
                    selectedNavIndex = index
                    onNavigate(index)
                },
                onAddClick = onAddClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with greeting and date
            HomeHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg)
                    .padding(top = AppTheme.spacing.xl)
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

            // Consistency Heatmap (full width with internal scroll)
            ConsistencyHeatmapSection(
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

            // Quick Start Templates
            QuickStartTemplatesSection(
                templates = templates,
                onTemplateClick = onTemplateClick,
                onViewAll = onViewAllTemplates,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

            // Recent Sessions
            RecentSessionsSection(
                sessions = recentSessions,
                onSessionClick = onSessionClick,
                onViewAll = onViewAllSessions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg)
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
        }
    }
}

/**
 * Header section with greeting and current date
 */
@Composable
private fun HomeHeader(
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

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = currentDate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Consistency heatmap section with title
 */
@Composable
private fun ConsistencyHeatmapSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        Text(
            text = "Workout Consistency",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
        )

        ConsistencyHeatmap(
            days = getMockHeatmapData(),
            showDayLabels = true,
            title = null,
            modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
        )
    }
}

/**
 * Quick start templates section with horizontal scrolling cards
 */
@Composable
private fun QuickStartTemplatesSection(
    templates: List<WorkoutTemplate>,
    onTemplateClick: (String) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        SectionHeaderWithAction(
            title = "Quick Start",
            actionText = "View All",
            onActionClick = onViewAll,
            modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
        )

        if (templates.isEmpty()) {
            // Empty state - show add template card
            AddTemplateCard(
                onClick = onViewAll,
                modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = AppTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                items(templates, key = { it.id }) { template ->
                    TemplateCard(
                        template = template,
                        onClick = { onTemplateClick(template.id) }
                    )
                }
            }
        }
    }
}

/**
 * Add template card for empty state - encourages users to create their first template
 */
@Composable
private fun AddTemplateCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .width(180.dp)
            .height(120.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add template",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            Text(
                text = "Add Template",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Create your first workout",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Template card component
 */
@Composable
private fun TemplateCard(
    template: WorkoutTemplate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.padding(vertical = AppTheme.spacing.xs),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
            ) {
                Column {
                    Text(
                        text = "${template.exerciseCount}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Exercises",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column {
                    Text(
                        text = template.estimatedDuration,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Recent sessions section with session cards
 */
@Composable
private fun RecentSessionsSection(
    sessions: List<RecentSession>,
    onSessionClick: (String) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        SectionHeaderWithAction(
            title = "Recent Sessions",
            actionText = "View All",
            onActionClick = onViewAll
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            sessions.forEach { session ->
                RecentSessionCard(
                    session = session,
                    onClick = { onSessionClick(session.id) }
                )
            }
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
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Mock data: 28-day consistency heatmap
 * Simulates workout frequency with varied intensity
 * TODO: Load from database via ViewModel
 */
private fun getMockHeatmapData(): List<HeatmapDay> = buildList {
    // Week 1 - High consistency
    add(HeatmapDay(1, 2))
    add(HeatmapDay(2, 0))
    add(HeatmapDay(3, 3))
    add(HeatmapDay(4, 0))
    add(HeatmapDay(5, 2))
    add(HeatmapDay(6, 1))
    add(HeatmapDay(7, 0))

    // Week 2 - Medium consistency
    add(HeatmapDay(8, 1))
    add(HeatmapDay(9, 0))
    add(HeatmapDay(10, 2))
    add(HeatmapDay(11, 1))
    add(HeatmapDay(12, 0))
    add(HeatmapDay(13, 0))
    add(HeatmapDay(14, 1))

    // Week 3 - Low consistency
    add(HeatmapDay(15, 0))
    add(HeatmapDay(16, 1))
    add(HeatmapDay(17, 0))
    add(HeatmapDay(18, 0))
    add(HeatmapDay(19, 1))
    add(HeatmapDay(20, 0))
    add(HeatmapDay(21, 0))

    // Week 4 - High consistency
    add(HeatmapDay(22, 3))
    add(HeatmapDay(23, 0))
    add(HeatmapDay(24, 4))
    add(HeatmapDay(25, 2))
    add(HeatmapDay(26, 0))
    add(HeatmapDay(27, 3))
    add(HeatmapDay(28, 1))
}
