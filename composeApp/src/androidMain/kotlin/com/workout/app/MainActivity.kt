package com.workout.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workout.app.data.repository.ThemeMode
import com.workout.app.presentation.settings.ThemeViewModel
import com.workout.app.ui.navigation.AppNavigation
import com.workout.app.ui.navigation.Route
import com.workout.app.ui.theme.WorkoutAppTheme
import org.koin.compose.koinInject

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
            val themeViewModel: ThemeViewModel = koinInject()
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            WorkoutAppTheme(darkTheme = darkTheme) {
                AppNavigation(
                    modifier = Modifier.fillMaxSize(),
                    // Start with Home for development, change to Onboarding for production
                    startDestination = Route.Home.route,
                    themeMode = themeMode,
                    onThemeModeChange = { mode -> themeViewModel.setThemeMode(mode) }
                )
            }
        }
    }
}
