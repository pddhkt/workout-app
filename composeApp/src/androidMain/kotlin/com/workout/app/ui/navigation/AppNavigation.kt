package com.workout.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.workout.app.ui.components.navigation.BottomNavBar
import com.workout.app.data.repository.ThemeMode
import com.workout.app.domain.model.Result
import com.workout.app.presentation.active.ActiveSessionViewModel
import com.workout.app.presentation.planning.SessionPlanningViewModel
import com.workout.app.presentation.workout.WorkoutState
import com.workout.app.presentation.workout.WorkoutViewModel
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
import com.workout.app.ui.components.overlays.BottomSheetComparisonScreen
import com.workout.app.ui.components.workout.WorkoutOverlay
import com.workout.app.presentation.library.ExerciseLibraryViewModel
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
    // Observe active session state for overlay control
    val activeSessionViewModel: ActiveSessionViewModel = koinInject()
    val activeSessionState by activeSessionViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Inject WorkoutViewModel when there's an active session (managed at this level for overlay)
    val workoutViewModel: WorkoutViewModel? = if (activeSessionState.sessionId != null) {
        koinInject { parametersOf(activeSessionState.sessionId!!) }
    } else null

    val workoutState: WorkoutState by workoutViewModel?.state?.collectAsStateWithLifecycle()
        ?: remember { androidx.compose.runtime.mutableStateOf(WorkoutState()) }

    // Sync workout progress to ActiveSessionViewModel for minimized bar display
    LaunchedEffect(workoutState.currentExercise, workoutState.exercises.size) {
        if (workoutViewModel != null) {
            activeSessionViewModel.updateWorkoutProgress(
                currentExerciseName = workoutState.currentExercise?.name,
                currentIndex = workoutState.currentExerciseIndex,
                totalExercises = workoutState.exercises.size
            )
        }
    }

    // Callback for expanding back to full workout (from minimized bar)
    val onExpandWorkout: () -> Unit = {
        activeSessionViewModel.expand()
    }

    // Callback for minimizing workout (swipe back)
    val onMinimizeWorkout: () -> Unit = {
        activeSessionViewModel.minimize()
    }

    // Show overlay when there's an active session
    val showWorkoutOverlay = activeSessionState.hasActiveSession

    // Observe current route for centralized bottom nav
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes that should show the bottom nav bar
    val navBarRoutes = setOf(
        Route.Home.route,
        Route.ExerciseLibrary.route,
        Route.Templates.route
    )

    // Determine if navbar should be visible (hide when workout overlay is expanded)
    val showNavBar = currentRoute in navBarRoutes && (activeSessionState.isMinimized || !showWorkoutOverlay)

    // Derive selected index from current route
    val selectedNavIndex = BottomNavDestinations.getIndexForRoute(currentRoute)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showNavBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(200)),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(200))
            ) {
                BottomNavBar(
                    selectedIndex = selectedNavIndex,
                    onItemSelected = { index ->
                        val route = BottomNavDestinations.getRouteForIndex(index)
                        navController.navigate(route) {
                            popUpTo(Route.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddClick = {
                        navController.navigateToSessionPlanning()
                    }
                )
            }
        }
    ) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            // Default transitions: simple crossfade for bottom nav tabs
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(200)) }
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
                }
            )
        }

        // Templates Screen
        composable(Route.Templates.route) {
            TemplatesScreen(
                onTemplateClick = { templateId ->
                    // Navigate to session planning with template pre-selected
                    navController.navigateToSessionPlanningWithTemplate(templateId)
                }
            )
        }

        // Session Planning (FT-013)
        composable(
            route = Route.SessionPlanning.route,
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
        ) {
            val viewModel: SessionPlanningViewModel = koinInject()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val planningScope = rememberCoroutineScope()

            SessionPlanningScreen(
                state = state,
                onBackClick = {
                    navController.popBackStack()
                },
                onTemplatesClick = {
                    navController.navigateToTemplates()
                },
                onStartSession = {
                    planningScope.launch {
                        when (val result = viewModel.createSession("Workout")) {
                            is Result.Success -> {
                                // Session created - overlay will automatically appear
                                // Just pop back to home, the workout overlay handles display
                                navController.popBackStack(Route.Home.route, inclusive = false)
                            }
                            is Result.Error -> {
                                // Error handled by ViewModel state
                            }
                            is Result.Loading -> { }
                        }
                    }
                },
                onToggleExercise = viewModel::toggleExercise,
                onAddExercise = viewModel::addExercise,
                onToggleTimeRange = viewModel::toggleTimeRange
            )
        }

        // Session Planning with Template (FT-013)
        composable(
            route = Route.SessionPlanningWithTemplate.ROUTE,
            arguments = listOf(
                navArgument(Route.SessionPlanningWithTemplate.ARG_TEMPLATE_ID) {
                    type = NavType.StringType
                }
            ),
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString(Route.SessionPlanningWithTemplate.ARG_TEMPLATE_ID)
            val viewModel: SessionPlanningViewModel = koinInject()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val planningScope = rememberCoroutineScope()

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
                    planningScope.launch {
                        when (val result = viewModel.createSession("Workout")) {
                            is Result.Success -> {
                                // Session created - overlay will automatically appear
                                // Just pop back to home, the workout overlay handles display
                                navController.popBackStack(Route.Home.route, inclusive = false)
                            }
                            is Result.Error -> {
                                // Error handled by ViewModel state
                            }
                            is Result.Loading -> { }
                        }
                    }
                },
                onToggleExercise = viewModel::toggleExercise,
                onAddExercise = viewModel::addExercise,
                onToggleTimeRange = viewModel::toggleTimeRange
            )
        }

        // NOTE: Active Workout (FT-014) is now handled by WorkoutOverlay outside NavHost

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
            ),
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
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

            val exercises = state.displayedExercises.map { exercise ->
                com.workout.app.ui.components.exercise.LibraryExercise(
                    id = exercise.id,
                    name = exercise.name,
                    muscleGroup = exercise.muscleGroup,
                    category = exercise.category ?: "Other",
                    isCustom = exercise.isCustom == 1L,
                    isFavorite = exercise.isFavorite == 1L
                )
            }

            ExerciseLibraryScreen(
                onExerciseClick = { exerciseId ->
                    navController.navigateToExerciseDetail(exerciseId)
                },
                onFavoriteToggle = { exerciseId ->
                    viewModel.toggleFavorite(exerciseId)
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
                showAddExerciseSheet = state.showAddExerciseSheet,
                onShowAddExerciseSheet = viewModel::showAddExerciseSheet,
                onHideAddExerciseSheet = viewModel::hideAddExerciseSheet,
                exercises = exercises,
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
            ),
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
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
        composable(
            route = Route.Settings.route,
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
        ) {
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
        composable(
            route = Route.SessionHistory.route,
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
        ) {
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
            ),
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
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

        // Handle back press when workout overlay is expanded
        BackHandler(enabled = showWorkoutOverlay && !activeSessionState.isMinimized) {
            onMinimizeWorkout()
        }

        // Workout Overlay - animated shrink/expand view
        // Appears when there's an active session, handles both expanded and minimized states
        AnimatedVisibility(
            visible = showWorkoutOverlay,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (workoutViewModel != null && activeSessionState.startTime != null) {
                WorkoutOverlay(
                    isExpanded = !activeSessionState.isMinimized,
                    workoutState = workoutState,
                    startTime = activeSessionState.startTime!!,
                    onMinimize = onMinimizeWorkout,
                    onExpand = onExpandWorkout,
                    onCompleteSet = { exerciseId, setNumber, reps, weight, rpe ->
                        workoutViewModel.updateReps(reps)
                        workoutViewModel.updateWeight(weight)
                        workoutViewModel.updateRPE(rpe)
                        workoutViewModel.completeSet(exerciseId, setNumber)
                    },
                    onSkipSet = { _ ->
                        workoutViewModel.skipSet()
                    },
                    onAddExercises = { exerciseIds ->
                        workoutViewModel.addExercises(exerciseIds)
                    },
                    onRemoveExercise = { exerciseId ->
                        workoutViewModel.deleteExercise(exerciseId)
                    },
                    onReplaceExercise = { exerciseId, newExercise ->
                        workoutViewModel.replaceExercise(
                            sessionExerciseId = exerciseId,
                            newExerciseId = newExercise.id,
                            newExerciseName = newExercise.name,
                            newMuscleGroup = newExercise.muscleGroup
                        )
                    },
                    onAddSet = { exerciseId ->
                        workoutViewModel.addSetToExercise(exerciseId)
                    },
                    onReorderExercise = { fromIndex, toIndex ->
                        workoutViewModel.reorderExercises(fromIndex, toIndex)
                    },
                    onCreateExercise = { name, muscleGroup, equipment, instructions ->
                        scope.launch {
                            workoutViewModel.createCustomExercise(
                                name = name,
                                muscleGroup = muscleGroup,
                                equipment = equipment,
                                instructions = instructions
                            )
                        }
                    },
                    onGetHistoricalWeights = { exerciseId ->
                        workoutViewModel.getHistoricalWeights(exerciseId)
                    },
                    onGetHistoricalReps = { exerciseId ->
                        workoutViewModel.getHistoricalReps(exerciseId)
                    },
                    onSetActiveSet = { exerciseId, setIndex ->
                        workoutViewModel.setActiveSet(exerciseId, setIndex)
                    },
                    onEnterReorderMode = {
                        workoutViewModel.enterReorderMode()
                    },
                    onExitReorderMode = {
                        workoutViewModel.exitReorderMode()
                    },
                    onEndWorkout = {
                        scope.launch {
                            val sessionId = activeSessionState.sessionId
                            when (workoutViewModel.finishWorkout()) {
                                is Result.Success -> {
                                    activeSessionViewModel.clearMinimizedState()
                                    if (sessionId != null) {
                                        navController.navigateToWorkoutComplete(sessionId) {
                                            popUpTo(Route.Home.route)
                                        }
                                    }
                                }
                                else -> { }
                            }
                        }
                    }
                )
            }
        }
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
