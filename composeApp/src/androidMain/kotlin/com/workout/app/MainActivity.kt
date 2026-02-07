package com.workout.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.workout.app.ui.navigation.AppNavigation
import com.workout.app.ui.navigation.Route
import com.workout.app.ui.theme.WorkoutAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            WorkoutAppTheme {
                AppNavigation(
                    modifier = Modifier.fillMaxSize(),
                    startDestination = Route.Home.route
                )
            }
        }
    }
}
