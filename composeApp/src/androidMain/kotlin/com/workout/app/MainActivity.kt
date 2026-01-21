package com.workout.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.workout.app.ui.navigation.AppNavigation
import com.workout.app.ui.navigation.Route
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Main Activity for the Workout App.
 * Sets up the navigation and applies the app theme.
 *
 * FT-020: Navigation integration with all MVP screens
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutAppTheme {
                AppNavigation(
                    modifier = Modifier.fillMaxSize(),
                    // Start with Home for development, change to Onboarding for production
                    startDestination = Route.Home.route
                )
            }
        }
    }
}
