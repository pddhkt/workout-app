package com.workout.app.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.workout.app.presentation.home.HomeViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

/**
 * Home Screen with ViewModel integration.
 * Loads real data from the database and passes it to HomeScreen.
 */
@Composable
fun HomeScreenWithViewModel(
    viewModel: HomeViewModel = koinInject(),
    onTemplateClick: (String) -> Unit = {},
    onSessionClick: (String) -> Unit = {},
    onViewAllTemplates: () -> Unit = {},
    onViewAllSessions: () -> Unit = {}
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
            duration = formatDuration(workout.duration),
            exerciseCount = workout.exerciseCount.toInt(),
            totalSets = workout.totalSets.toInt(),
            exerciseNames = workout.exerciseNames
        )
    }

    // Pass real data to HomeScreen
    HomeScreen(
        templates = templates,
        recentSessions = sessions,
        onTemplateClick = onTemplateClick,
        onSessionClick = onSessionClick,
        onViewAllTemplates = onViewAllTemplates,
        onViewAllSessions = onViewAllSessions,
        heatmapData = state.heatmapData
    )
}

/**
 * Format duration in seconds to readable string.
 */
private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    return "$minutes min"
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
