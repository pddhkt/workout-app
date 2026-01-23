package com.workout.app.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Type-safe navigation routes for the Workout App.
 * Defines all app destinations with proper argument handling.
 *
 * Based on Android skill navigation patterns and MVP screen requirements:
 * - Home Screen (FT-012)
 * - Session Planning (FT-013)
 * - Workout Screen (FT-014)
 * - Rest Timer (FT-015)
 * - Workout Complete (FT-016)
 * - Exercise Library (FT-017)
 * - Exercise Detail (FT-018)
 * - Onboarding (FT-019)
 */
sealed class Route(val route: String) {

    /**
     * Onboarding flow for new users
     */
    data object Onboarding : Route("onboarding")

    /**
     * Home/Dashboard screen - main entry point
     */
    data object Home : Route("home")

    /**
     * Session planning screen - create workout from exercises
     */
    data object SessionPlanning : Route("session_planning")

    /**
     * Active workout session screen
     * @param sessionId Optional session ID for resuming
     */
    data class Workout(val sessionId: String? = null) : Route(
        if (sessionId != null) "workout/$sessionId" else "workout"
    ) {
        companion object {
            const val ROUTE = "workout?sessionId={sessionId}"
            const val ARG_SESSION_ID = "sessionId"
        }
    }

    /**
     * Rest timer overlay during workout
     * Note: Typically shown as a full-screen dialog/overlay
     */
    data object RestTimer : Route("rest_timer")

    /**
     * Workout completion summary screen
     * @param sessionId The completed session ID
     */
    data class WorkoutComplete(val sessionId: String) : Route("workout_complete/$sessionId") {
        companion object {
            const val ROUTE = "workout_complete/{sessionId}"
            const val ARG_SESSION_ID = "sessionId"
        }
    }

    /**
     * Exercise library browsing screen
     */
    data object ExerciseLibrary : Route("exercise_library")

    /**
     * Exercise detail screen
     * @param exerciseId The exercise ID to display
     */
    data class ExerciseDetail(val exerciseId: String) : Route("exercise_detail/$exerciseId") {
        companion object {
            const val ROUTE = "exercise_detail/{exerciseId}"
            const val ARG_EXERCISE_ID = "exerciseId"
        }
    }

    /**
     * Settings screen
     */
    data object Settings : Route("settings")

    /**
     * Debug: BottomSheet comparison screen
     */
    data object BottomSheetComparison : Route("debug/bottomsheet_comparison")
}

/**
 * Extension functions for type-safe navigation
 */

fun NavController.navigateToOnboarding(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(Route.Onboarding.route, builder)
}

fun NavController.navigateToHome(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(Route.Home.route, builder)
}

fun NavController.navigateToSessionPlanning(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(Route.SessionPlanning.route, builder)
}

fun NavController.navigateToWorkout(sessionId: String? = null, builder: NavOptionsBuilder.() -> Unit = {}) {
    val route = if (sessionId != null) "workout/$sessionId" else "workout"
    navigate(route, builder)
}

fun NavController.navigateToRestTimer(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(Route.RestTimer.route, builder)
}

fun NavController.navigateToWorkoutComplete(sessionId: String, builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(Route.WorkoutComplete(sessionId).route, builder)
}

fun NavController.navigateToExerciseLibrary(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(Route.ExerciseLibrary.route, builder)
}

fun NavController.navigateToExerciseDetail(exerciseId: String, builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(Route.ExerciseDetail(exerciseId).route, builder)
}

fun NavController.navigateToSettings(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(Route.Settings.route, builder)
}

fun NavController.navigateToBottomSheetComparison(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(Route.BottomSheetComparison.route, builder)
}

/**
 * Bottom navigation indices mapping
 * Used for BottomNavBar component integration
 */
object BottomNavDestinations {
    const val HOME = 0
    const val LIBRARY = 1
    const val WORKOUT = 2
    const val SETTINGS = 3

    /**
     * Get navigation route for bottom nav index
     */
    fun getRouteForIndex(index: Int): String = when (index) {
        HOME -> Route.Home.route
        LIBRARY -> Route.ExerciseLibrary.route
        WORKOUT -> Route.SessionPlanning.route
        SETTINGS -> Route.Settings.route
        else -> Route.Home.route
    }

    /**
     * Get bottom nav index for current route
     */
    fun getIndexForRoute(route: String?): Int = when {
        route == null -> HOME
        route.startsWith(Route.Home.route) -> HOME
        route.startsWith(Route.ExerciseLibrary.route) -> LIBRARY
        route.startsWith(Route.ExerciseDetail.ROUTE.substringBefore("{")) -> LIBRARY
        route.startsWith(Route.SessionPlanning.route) -> WORKOUT
        route.startsWith(Route.Workout.ROUTE.substringBefore("?")) -> WORKOUT
        route.startsWith(Route.Settings.route) -> SETTINGS
        else -> HOME
    }
}
