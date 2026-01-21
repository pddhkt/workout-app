package com.workout.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.workout.app.ui.screens.complete.WorkoutCompleteScreen
import com.workout.app.ui.screens.detail.ExerciseDetailScreen
import com.workout.app.ui.screens.home.HomeScreen
import com.workout.app.ui.screens.library.ExerciseLibraryScreen
import com.workout.app.ui.screens.onboarding.OnboardingScreen
import com.workout.app.ui.screens.planning.SessionPlanningScreen
import com.workout.app.ui.screens.timer.RestTimerScreen
import com.workout.app.ui.screens.workout.WorkoutScreen

/**
 * Main navigation configuration for the Workout App.
 * Implements Compose Navigation with type-safe routes and proper back stack handling.
 *
 * Features:
 * - NavHost with all MVP screen routes
 * - Bottom nav integration via route-based navigation
 * - Argument passing (exercise ID, session ID)
 * - Proper back stack management
 * - Deep link support structure
 *
 * Based on Android skill navigation patterns and FT-020 requirements.
 *
 * @param modifier Modifier to be applied to the NavHost
 * @param navController Optional NavController for external control
 * @param startDestination Starting route (defaults to Onboarding)
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Onboarding Flow (FT-019)
        composable(Route.Onboarding.route) {
            OnboardingScreen(
                onComplete = { onboardingData ->
                    // Navigate to home and clear onboarding from back stack
                    navController.navigateToHome {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                },
                onSkip = {
                    // Same as complete - go to home
                    navController.navigateToHome {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen (FT-012)
        composable(Route.Home.route) {
            HomeScreen(
                onTemplateClick = { templateId ->
                    // Navigate to session planning with template pre-selected
                    // TODO: Pass template ID when supported
                    navController.navigateToSessionPlanning()
                },
                onSessionClick = { sessionId ->
                    // Navigate to workout complete to view session details
                    navController.navigateToWorkoutComplete(sessionId)
                },
                onViewAllTemplates = {
                    // Navigate to templates library (future implementation)
                    // For now, navigate to session planning
                    navController.navigateToSessionPlanning()
                },
                onViewAllSessions = {
                    // Navigate to history screen (future implementation)
                    // TODO: Implement history screen
                },
                onNavigate = { index ->
                    // Bottom nav integration
                    val route = BottomNavDestinations.getRouteForIndex(index)
                    if (route != Route.Home.route) {
                        navController.navigate(route) {
                            // Pop to home to avoid deep stack
                            popUpTo(Route.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }

        // Session Planning (FT-013)
        composable(Route.SessionPlanning.route) {
            SessionPlanningScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onTemplatesClick = {
                    // Navigate to templates screen (future implementation)
                    // TODO: Implement templates screen
                },
                onStartSession = { addedExercises ->
                    // Start workout with selected exercises
                    // TODO: Create session and pass session ID
                    navController.navigateToWorkout()
                }
            )
        }

        // Active Workout (FT-014)
        composable(
            route = Route.Workout.ROUTE,
            arguments = listOf(
                navArgument(Route.Workout.ARG_SESSION_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString(Route.Workout.ARG_SESSION_ID)

            WorkoutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRestTimerClick = {
                    // Show rest timer as overlay
                    // Note: Could be implemented as dialog instead of route
                    navController.navigateToRestTimer()
                },
                onCompleteWorkout = { completedSessionId ->
                    // Navigate to completion screen
                    navController.navigateToWorkoutComplete(completedSessionId) {
                        // Clear workout from back stack
                        popUpTo(Route.Home.route)
                    }
                },
                onSaveAndExit = {
                    // Save progress and return home
                    navController.popBackStack(Route.Home.route, inclusive = false)
                },
                onCancelWorkout = {
                    // Discard and return home
                    navController.popBackStack(Route.Home.route, inclusive = false)
                }
            )
        }

        // Rest Timer (FT-015)
        // Note: This could also be implemented as a Dialog instead of a route
        composable(Route.RestTimer.route) {
            RestTimerScreen(
                onDismiss = {
                    navController.popBackStack()
                },
                onSkipRest = {
                    navController.popBackStack()
                },
                onAddTime = { seconds ->
                    // Add time handled by screen state
                },
                onTimerComplete = {
                    navController.popBackStack()
                }
            )
        }

        // Workout Complete (FT-016)
        composable(
            route = Route.WorkoutComplete.ROUTE,
            arguments = listOf(
                navArgument(Route.WorkoutComplete.ARG_SESSION_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString(Route.WorkoutComplete.ARG_SESSION_ID)
                ?: return@composable

            WorkoutCompleteScreen(
                onDoneClick = {
                    // Return to home
                    navController.popBackStack(Route.Home.route, inclusive = false)
                },
                onSaveDraft = {
                    // Save draft and return home
                    navController.popBackStack(Route.Home.route, inclusive = false)
                }
            )
        }

        // Exercise Library (FT-017)
        composable(Route.ExerciseLibrary.route) {
            ExerciseLibraryScreen(
                onExerciseClick = { exerciseId ->
                    navController.navigateToExerciseDetail(exerciseId)
                },
                onAddToWorkoutClick = {
                    // Navigate to session planning
                    navController.navigateToSessionPlanning()
                },
                onNavigate = { index ->
                    // Bottom nav integration
                    val route = BottomNavDestinations.getRouteForIndex(index)
                    if (route != Route.ExerciseLibrary.route) {
                        navController.navigate(route) {
                            popUpTo(Route.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }

        // Exercise Detail (FT-018)
        composable(
            route = Route.ExerciseDetail.ROUTE,
            arguments = listOf(
                navArgument(Route.ExerciseDetail.ARG_EXERCISE_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString(Route.ExerciseDetail.ARG_EXERCISE_ID)
                ?: return@composable

            ExerciseDetailScreen(
                exerciseId = exerciseId,
                onBackClick = {
                    navController.popBackStack()
                },
                onAddToWorkout = {
                    // Navigate to session planning
                    // TODO: Pre-select this exercise
                    navController.navigateToSessionPlanning()
                },
                onPlayVideo = {
                    // Open video player (future implementation)
                    // TODO: Implement video player
                }
            )
        }

        // Future: Profile screen, History, Templates, Settings, etc.
    }
}

/**
 * Helper to get current bottom nav index from navigation state
 */
@Composable
fun NavHostController.currentBottomNavIndex(): Int {
    val currentRoute = currentBackStackEntry?.destination?.route
    return BottomNavDestinations.getIndexForRoute(currentRoute)
}
