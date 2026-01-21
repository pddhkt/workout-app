package com.workout.app.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.navigation.BottomActionBar
import com.workout.app.ui.components.navigation.BottomNavBar
import com.workout.app.ui.components.navigation.SessionSummary
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview for BottomNavBar with Home selected.
 */
@Preview(name = "Bottom Nav Bar - Home Selected")
@Composable
private fun BottomNavBarHomePreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BottomNavBar(
                selectedIndex = 0,
                onItemSelected = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Preview for BottomNavBar with Library selected.
 */
@Preview(name = "Bottom Nav Bar - Library Selected")
@Composable
private fun BottomNavBarLibraryPreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BottomNavBar(
                selectedIndex = 1,
                onItemSelected = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Preview for BottomNavBar with Workout selected.
 */
@Preview(name = "Bottom Nav Bar - Workout Selected")
@Composable
private fun BottomNavBarWorkoutPreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BottomNavBar(
                selectedIndex = 2,
                onItemSelected = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Preview for BottomNavBar with Profile selected.
 */
@Preview(name = "Bottom Nav Bar - Profile Selected")
@Composable
private fun BottomNavBarProfilePreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BottomNavBar(
                selectedIndex = 3,
                onItemSelected = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Interactive preview for BottomNavBar with state.
 */
@Preview(name = "Bottom Nav Bar - Interactive")
@Composable
private fun BottomNavBarInteractivePreview() {
    WorkoutAppTheme {
        var selectedIndex by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                BottomNavBar(
                    selectedIndex = selectedIndex,
                    onItemSelected = { selectedIndex = it }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (selectedIndex) {
                        0 -> "Home Screen"
                        1 -> "Library Screen"
                        2 -> "Workout Screen"
                        3 -> "Profile Screen"
                        else -> "Unknown"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

/**
 * Preview for BottomActionBar with session summary.
 */
@Preview(name = "Bottom Action Bar - With Summary")
@Composable
private fun BottomActionBarWithSummaryPreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BottomActionBar(
                actionText = "Finish Workout",
                onActionClick = {},
                sessionSummary = SessionSummary(
                    duration = "45:30",
                    sets = 12,
                    exercises = 5
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Preview for BottomActionBar without session summary.
 */
@Preview(name = "Bottom Action Bar - No Summary")
@Composable
private fun BottomActionBarNoSummaryPreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BottomActionBar(
                actionText = "Start Workout",
                onActionClick = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Preview for BottomActionBar disabled state.
 */
@Preview(name = "Bottom Action Bar - Disabled")
@Composable
private fun BottomActionBarDisabledPreview() {
    WorkoutAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BottomActionBar(
                actionText = "Complete Workout",
                onActionClick = {},
                sessionSummary = SessionSummary(
                    duration = "00:00",
                    sets = 0,
                    exercises = 0
                ),
                actionEnabled = false,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Preview for BottomActionBar in active workout context.
 */
@Preview(name = "Bottom Action Bar - Active Workout")
@Composable
private fun BottomActionBarActiveWorkoutPreview() {
    WorkoutAppTheme {
        var actionEnabled by remember { mutableStateOf(true) }

        Scaffold(
            bottomBar = {
                BottomActionBar(
                    actionText = "Finish Workout",
                    onActionClick = { actionEnabled = false },
                    sessionSummary = SessionSummary(
                        duration = "1:23:45",
                        sets = 24,
                        exercises = 8
                    ),
                    actionEnabled = actionEnabled
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Workout in Progress",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Session summary shown in action bar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Comprehensive preview showing all navigation components.
 */
@Preview(name = "All Navigation Components", heightDp = 1200)
@Composable
private fun AllNavigationComponentsPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section: Bottom Nav Bar States
            Text(
                text = "Bottom Navigation Bar",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Home selected
            BottomNavBar(
                selectedIndex = 0,
                onItemSelected = {}
            )

            // Library selected
            BottomNavBar(
                selectedIndex = 1,
                onItemSelected = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Bottom Action Bar
            Text(
                text = "Bottom Action Bar",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // With summary
            BottomActionBar(
                actionText = "Finish Workout",
                onActionClick = {},
                sessionSummary = SessionSummary(
                    duration = "45:30",
                    sets = 12,
                    exercises = 5
                )
            )

            // Without summary
            BottomActionBar(
                actionText = "Start Workout",
                onActionClick = {}
            )

            // Disabled state
            BottomActionBar(
                actionText = "Complete Workout",
                onActionClick = {},
                sessionSummary = SessionSummary(
                    duration = "00:00",
                    sets = 0,
                    exercises = 0
                ),
                actionEnabled = false
            )
        }
    }
}
