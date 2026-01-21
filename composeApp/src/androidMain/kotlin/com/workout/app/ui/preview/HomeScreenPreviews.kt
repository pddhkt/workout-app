package com.workout.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.workout.app.ui.screens.home.HomeScreen
import com.workout.app.ui.theme.WorkoutAppTheme

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
        HomeScreen()
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
