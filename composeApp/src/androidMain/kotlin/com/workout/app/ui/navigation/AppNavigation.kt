package com.workout.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.workout.app.data.repository.ThemeMode
import com.workout.app.domain.model.Result
import com.workout.app.presentation.planning.SessionPlanningViewModel
import com.workout.app.presentation.workout.WorkoutViewModel
import com.workout.app.ui.components.chips.SetState
import com.workout.app.ui.components.exercise.SetInfo
import com.workout.app.ui.screens.complete.EnhancedWorkoutCompleteScreen
import com.workout.app.ui.screens.detail.ExerciseDetailScreen
import com.workout.app.ui.screens.home.HomeScreenWithViewModel
import com.workout.app.ui.screens.history.SessionHistoryScreen
import com.workout.app.ui.screens.history.SessionDetailScreen
import com.workout.app.ui.screens.library.ExerciseLibraryScreen
import com.workout.app.ui.screens.onboarding.OnboardingScreen
import com.workout.app.ui.screens.planning.SessionPlanningScreen
import com.workout.app.ui.screens.settings.SettingsScreen
import com.workout.app.ui.screens.templates.TemplatesScreen
import com.workout.app.ui.screens.timer.RestTimerScreen
import com.workout.app.ui.screens.workout.WorkoutScreen
import com.workout.app.ui.screens.workout.WorkoutSession
import com.workout.app.ui.screens.workout.ExerciseData
import com.workout.app.ui.components.overlays.BottomSheetComparisonScreen
import com.workout.app.presentation.library.ExerciseLibraryViewModel
import com.workout.app.ui.components.exercise.CustomExerciseFormState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

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
    startDestination: String = Route.Onboarding.route,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {}
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
            HomeScreenWithViewModel(
                onTemplateClick = { templateId ->
                    // Navigate to session planning with template pre-selected
                    navController.navigateToSessionPlanningWithTemplate(templateId)
                },
                onSessionClick = { sessionId ->
                    // Navigate to session detail to view past session
                    navController.navigateToSessionDetail(sessionId)
                },
                onViewAllTemplates = {
                    navController.navigateToTemplates {
                        popUpTo(Route.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onViewAllSessions = {
                    navController.navigateToSessionHistory()
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
                },
                onAddClick = {
                    navController.navigateToSessionPlanning()
                }
            )
        }

        // Templates Screen
        composable(Route.Templates.route) {
            TemplatesScreen(
                onTemplateClick = { templateId ->
                    // Navigate to session planning with template pre-selected
                    navController.navigateToSessionPlanningWithTemplate(templateId)
                },
                onNavigate = { index ->
                    val route = BottomNavDestinations.getRouteForIndex(index)
                    if (route != Route.Templates.route) {
                        navController.navigate(route) {
                            popUpTo(Route.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onAddClick = {
                    navController.navigateToSessionPlanning()
                }
            )
        }

        // Session Planning (FT-013)
        composable(Route.SessionPlanning.route) {
            val viewModel: SessionPlanningViewModel = koinInject()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()

            SessionPlanningScreen(
                state = state,
                onBackClick = {
                    navController.popBackStack()
                },
                onTemplatesClick = {
                    navController.navigateToTemplates()
                },
                onStartSession = {
                    scope.launch {
                        when (val result = viewModel.createSession("Workout")) {
                            is Result.Success -> {
                                navController.navigateToWorkout(result.data) {
                                    popUpTo(Route.SessionPlanning.route) { inclusive = true }
                                }
                            }
                            is Result.Error -> {
                                // Error handled by ViewModel state
                            }
                            is Result.Loading -> { }
                        }
                    }
                },
                onToggleExercise = viewModel::toggleExercise,
                onAddExercise = viewModel::addExercise
            )
        }

        // Session Planning with Template (FT-013)
        composable(
            route = Route.SessionPlanningWithTemplate.ROUTE,
            arguments = listOf(
                navArgument(Route.SessionPlanningWithTemplate.ARG_TEMPLATE_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString(Route.SessionPlanningWithTemplate.ARG_TEMPLATE_ID)
            val viewModel: SessionPlanningViewModel = koinInject()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()

            SessionPlanningScreen(
                state = state,
                templateId = templateId,
                onBackClick = {
                    navController.popBackStack()
                },
                onTemplatesClick = {
                    navController.navigateToTemplates()
                },
                onStartSession = {
                    scope.launch {
                        when (val result = viewModel.createSession("Workout")) {
                            is Result.Success -> {
                                navController.navigateToWorkout(result.data) {
                                    popUpTo(Route.SessionPlanningWithTemplate.ROUTE) { inclusive = true }
                                }
                            }
                            is Result.Error -> {
                                // Error handled by ViewModel state
                            }
                            is Result.Loading -> { }
                        }
                    }
                },
                onToggleExercise = viewModel::toggleExercise,
                onAddExercise = viewModel::addExercise
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
            val scope = rememberCoroutineScope()

            if (sessionId != null) {
                val viewModel: WorkoutViewModel = koinInject { parametersOf(sessionId) }
                val state by viewModel.state.collectAsStateWithLifecycle()

                // Map ViewModel state to WorkoutSession for the UI
                val session = WorkoutSession(
                    workoutName = state.sessionName,
                    exercises = state.exercises.map { exercise ->
                        ExerciseData(
                            id = exercise.id,
                            name = exercise.name,
                            muscleGroup = exercise.muscleGroup,
                            targetSets = exercise.targetSets,
                            completedSets = exercise.completedSets,
                            sets = List(exercise.targetSets) { index ->
                                SetInfo(
                                    setNumber = index + 1,
                                    reps = 0,
                                    weight = 0f,
                                    state = when {
                                        index < exercise.completedSets -> SetState.COMPLETED
                                        index == exercise.completedSets -> SetState.ACTIVE
                                        else -> SetState.PENDING
                                    }
                                )
                            }
                        )
                    },
                    startTime = System.currentTimeMillis() - (state.elapsedSeconds * 1000L)
                )

                WorkoutScreen(
                    session = session,
                    onCompleteSet = { exerciseId, reps, weight, rpe ->
                        viewModel.updateReps(reps)
                        viewModel.updateWeight(weight)
                        viewModel.updateRPE(rpe)
                        viewModel.completeSet()
                    },
                    onSkipSet = { exerciseId ->
                        viewModel.skipSet()
                    },
                    onEndWorkout = {
                        scope.launch {
                            when (viewModel.finishWorkout()) {
                                is Result.Success -> {
                                    navController.navigateToWorkoutComplete(sessionId) {
                                        popUpTo(Route.Home.route)
                                    }
                                }
                                else -> { }
                            }
                        }
                    }
                )
            } else {
                // Fallback for missing sessionId - navigate back
                navController.popBackStack()
            }
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

            EnhancedWorkoutCompleteScreen(
                sessionId = sessionId,
                onDoneClick = {
                    navController.popBackStack(Route.Home.route, inclusive = false)
                },
                onSaveDraft = {
                    navController.popBackStack(Route.Home.route, inclusive = false)
                }
            )
        }

        // Exercise Library (FT-017)
        composable(Route.ExerciseLibrary.route) {
            val viewModel: ExerciseLibraryViewModel = koinInject()
            val state by viewModel.state.collectAsStateWithLifecycle()

            ExerciseLibraryScreen(
                onExerciseClick = { exerciseId ->
                    navController.navigateToExerciseDetail(exerciseId)
                },
                onFavoriteToggle = { exerciseId ->
                    viewModel.toggleFavorite(exerciseId)
                },
                onAddToWorkoutClick = {
                    // Navigate to session planning
                    navController.navigateToSessionPlanning()
                },
                onCreateExercise = { formState ->
                    viewModel.createCustomExercise(
                        name = formState.name,
                        muscleGroup = formState.muscleGroup ?: "",
                        category = formState.category,
                        equipment = formState.equipment,
                        difficulty = formState.difficulty,
                        instructions = formState.instructions.takeIf { it.isNotBlank() }
                    )
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
                },
                showAddExerciseSheet = state.showAddExerciseSheet,
                onShowAddExerciseSheet = viewModel::showAddExerciseSheet,
                onHideAddExerciseSheet = viewModel::hideAddExerciseSheet,
                isCreatingExercise = state.isCreating
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

        // Settings Screen
        composable(Route.Settings.route) {
            SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                onBackClick = {
                    navController.popBackStack()
                },
                onBottomSheetComparisonClick = {
                    navController.navigateToBottomSheetComparison()
                }
            )
        }

        // Debug: BottomSheet Comparison Screen
        composable(Route.BottomSheetComparison.route) {
            BottomSheetComparisonScreen()
        }

        // Session History Screen
        composable(Route.SessionHistory.route) {
            SessionHistoryScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSessionClick = { sessionId ->
                    navController.navigateToSessionDetail(sessionId)
                }
            )
        }

        // Session Detail Screen
        composable(
            route = Route.SessionDetail.ROUTE,
            arguments = listOf(
                navArgument(Route.SessionDetail.ARG_SESSION_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString(Route.SessionDetail.ARG_SESSION_ID)
                ?: return@composable

            SessionDetailScreen(
                workoutId = sessionId,
                onBackClick = {
                    navController.popBackStack()
                },
                onShareClick = {
                    // TODO: Implement share functionality
                }
            )
        }
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
