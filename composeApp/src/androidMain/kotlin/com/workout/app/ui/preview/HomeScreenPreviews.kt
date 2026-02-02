package com.workout.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.workout.app.ui.screens.home.HomeScreen
import com.workout.app.ui.screens.home.RecentSession
import com.workout.app.ui.screens.home.WorkoutTemplate
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Sample data for previews
 */
private val sampleTemplates = listOf(
    WorkoutTemplate(
        id = "1",
        name = "Push Day",
        exerciseCount = 6,
        estimatedDuration = "60 min"
    ),
    WorkoutTemplate(
        id = "2",
        name = "Pull Day",
        exerciseCount = 5,
        estimatedDuration = "55 min"
    ),
    WorkoutTemplate(
        id = "3",
        name = "Leg Day",
        exerciseCount = 7,
        estimatedDuration = "70 min"
    )
)

private val sampleSessions = listOf(
    RecentSession(
        id = "1",
        workoutName = "Push Day",
        date = "Today",
        duration = "58 min",
        exerciseCount = 6,
        totalSets = 18
    ),
    RecentSession(
        id = "2",
        workoutName = "Pull Day",
        date = "2 days ago",
        duration = "52 min",
        exerciseCount = 5,
        totalSets = 15
    ),
    RecentSession(
        id = "3",
        workoutName = "Leg Day",
        date = "4 days ago",
        duration = "68 min",
        exerciseCount = 7,
        totalSets = 21
    )
)

/**
 * Preview for the Home Screen
 */
@Preview(
    name = "Home Screen",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    heightDp = 2000 // Tall preview to show scrolling content
)
@Composable
private fun HomeScreenPreview() {
    WorkoutAppTheme {
        HomeScreen(
            templates = sampleTemplates,
            recentSessions = sampleSessions
        )
    }
}

/**
 * Interactive preview with navigation
 */
@Preview(
    name = "Home Screen - Interactive",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    heightDp = 2000
)
@Composable
private fun HomeScreenInteractivePreview() {
    WorkoutAppTheme {
        HomeScreen(
            templates = sampleTemplates,
            recentSessions = sampleSessions,
            onTemplateClick = { templateId ->
                println("Template clicked: $templateId")
            },
            onSessionClick = { sessionId ->
                println("Session clicked: $sessionId")
            },
            onViewAllTemplates = {
                println("View all templates")
            },
            onViewAllSessions = {
                println("View all sessions")
            },
            onNavigate = { index ->
                println("Navigate to index: $index")
            }
        )
    }
}
