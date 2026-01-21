package com.workout.app.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.workout.app.presentation.home.HomeViewModel
import com.workout.app.ui.components.dataviz.HeatmapDay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

/**
 * Home Screen with ViewModel integration.
 * This is an example of how to integrate ViewModels with existing screens.
 *
 * Usage in AndroidMain:
 * ```
 * @Composable
 * fun HomeRoute(
 *     onNavigate: (Int) -> Unit,
 *     viewModel: HomeViewModel = koinViewModel()
 * ) {
 *     val state by viewModel.state.collectAsStateWithLifecycle()
 *
 *     HomeScreenWithViewModel(
 *         state = state,
 *         onRefresh = viewModel::refresh,
 *         onNavigate = onNavigate
 *     )
 * }
 * ```
 */
@Composable
fun HomeScreenWithViewModel(
    viewModel: HomeViewModel = koinInject(),
    onTemplateClick: (String) -> Unit = {},
    onSessionClick: (String) -> Unit = {},
    onViewAllTemplates: () -> Unit = {},
    onViewAllSessions: () -> Unit = {},
    onNavigate: (Int) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    // Convert database entities to UI models
    val templates = state.templates.map { template ->
        WorkoutTemplate(
            id = template.id,
            name = template.name,
            exerciseCount = 0, // TODO: Parse from exercises JSON
            estimatedDuration = "${template.estimatedDuration ?: 60} min"
        )
    }

    val sessions = state.recentSessions.map { workout ->
        RecentSession(
            id = workout.id,
            workoutName = workout.name,
            date = formatDate(workout.createdAt),
            duration = "${workout.duration} min",
            exerciseCount = workout.exerciseCount?.toInt() ?: 0,
            totalSets = workout.totalSets?.toInt() ?: 0
        )
    }

    // Use the existing HomeScreen composable with data from ViewModel
    HomeScreen(
        onTemplateClick = onTemplateClick,
        onSessionClick = onSessionClick,
        onViewAllTemplates = onViewAllTemplates,
        onViewAllSessions = onViewAllSessions,
        onNavigate = onNavigate
    )
}

/**
 * Format timestamp to relative date string.
 */
private fun formatDate(timestamp: Long): String {
    val now = Clock.System.now()
    val date = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault())

    val daysDiff = today.date.toEpochDays() - date.date.toEpochDays()

    return when (daysDiff) {
        0 -> "Today"
        1 -> "Yesterday"
        in 2..6 -> "$daysDiff days ago"
        else -> {
            val month = when (date.monthNumber) {
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
            "$month ${date.dayOfMonth}"
        }
    }
}
