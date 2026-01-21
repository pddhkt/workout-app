package com.workout.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.workout.app.ui.screens.planning.SessionPlanningScreen
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview for Session Planning Screen
 * Shows the complete screen layout with all components
 */
@Preview(
    name = "Session Planning Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun SessionPlanningScreenPreview() {
    WorkoutAppTheme {
        SessionPlanningScreen(
            onBackClick = {},
            onTemplatesClick = {},
            onStartSession = {}
        )
    }
}

/**
 * Preview showing screen with exercises added
 */
@Preview(
    name = "Session Planning - With Exercises",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun SessionPlanningScreenWithExercisesPreview() {
    WorkoutAppTheme {
        SessionPlanningScreen(
            onBackClick = {},
            onTemplatesClick = {},
            onStartSession = {}
        )
    }
}

/**
 * Preview showing screen with chest filter selected
 */
@Preview(
    name = "Session Planning - Filtered",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun SessionPlanningScreenFilteredPreview() {
    WorkoutAppTheme {
        SessionPlanningScreen(
            onBackClick = {},
            onTemplatesClick = {},
            onStartSession = {}
        )
    }
}
