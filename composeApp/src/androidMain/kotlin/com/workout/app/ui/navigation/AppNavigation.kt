package com.workout.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.workout.app.ui.components.navigation.BottomNavBar
import com.workout.app.domain.model.Result
import com.workout.app.presentation.active.ActiveSessionViewModel
import com.workout.app.presentation.planning.SessionPlanningViewModel
import com.workout.app.presentation.workout.WorkoutState
import com.workout.app.presentation.workout.WorkoutViewModel
import com.workout.app.ui.screens.complete.EnhancedWorkoutCompleteScreen
import com.workout.app.ui.screens.detail.ExerciseDetailScreen
import com.workout.app.ui.screens.goals.GoalCreateEditScreen
import com.workout.app.ui.screens.goals.GoalDetailScreen
import com.workout.app.ui.screens.goals.GoalsScreenWithViewModel
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
import com.workout.app.ui.screens.experiment.WorkoutLayoutExperimentScreen
import com.workout.app.ui.screens.chat.ChatScreen
import com.workout.app.presentation.chat.ChatViewModel
import com.workout.app.ui.components.workout.WorkoutOverlay
import com.workout.app.data.repository.SessionRepository
import com.workout.app.data.repository.WorkoutRepository
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
) {
    // Observe active session state for overlay control
    val activeSessionViewModel: ActiveSessionViewModel = koinInject()
    val activeSessionState by activeSessionViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val workoutRepository: WorkoutRepository = koinInject()
    val sessionRepository: SessionRepository = koinInject()

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

    // Observe current route for centralized bottom nav
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show overlay when there's an active session
    // Minimized bar only shows on the Home screen; expanded overlay shows anywhere
    val isOnHomeScreen = currentRoute == Route.Home.route
    val showWorkoutOverlay = activeSessionState.hasActiveSession && (!activeSessionState.isMinimized || isOnHomeScreen)

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

    // Confirmation dialog for starting a new workout while one is active
    var showCancelSessionDialog by remember { mutableStateOf(false) }

    // Measure actual bottom nav bar height for overlay positioning
    val density = LocalDensity.current
    var measuredNavBarHeight by remember { mutableStateOf(0.dp) }

    if (showCancelSessionDialog) {
        AlertDialog(
            onDismissRequest = { showCancelSessionDialog = false },
            title = { Text("Active Session") },
            text = { Text("You have a workout in progress. Cancel it and start a new one?") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelSessionDialog = false
                    scope.launch {
                        workoutViewModel?.cancelWorkout()
                        activeSessionViewModel.clearMinimizedState()
                        navController.navigateToSessionPlanning()
                    }
                }) {
                    Text("Yes, cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelSessionDialog = false }) {
                    Text("Keep workout")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().padding(
            top = paddingValues.calculateTopPadding()
        )) {
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
            Box(Modifier.padding(bottom = measuredNavBarHeight)) {
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
                    onGoalClick = { goalId ->
                        navController.navigateToGoalDetail(goalId)
                    },
                    onManageGoals = {
                        navController.navigateToGoals()
                    },
                    onChatClick = {
                        navController.navigateToChat()
                    }
                )
            }
        }

        // Templates Screen
        composable(Route.Templates.route) {
            Box(Modifier.padding(bottom = measuredNavBarHeight)) {
                TemplatesScreen(
                    onTemplateClick = { templateId ->
                        // Navigate to session planning with template pre-selected
                        navController.navigateToSessionPlanningWithTemplate(templateId)
                    }
                )
            }
        }

        // Session Planning (FT-013)
        composable(
            route = Route.SessionPlanning.route,
            enterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(300)) }
        ) {
            val viewModel: SessionPlanningViewModel = koinInject()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val planningScope = rememberCoroutineScope()

            SessionPlanningScreen(
                state = state,
                onBackClick = {
                    navController.popBackStack()
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
                onRemoveExercise = viewModel::removeExercise,
                onAddExercise = viewModel::addExercise,
                onToggleTimeRange = viewModel::toggleTimeRange,
                onExpandExercise = viewModel::expandExercise,
                onCollapseExercise = viewModel::collapseExercise,
                onDismissExpandedExercise = viewModel::dismissExpandedExercise,
                onAddExerciseWithPreset = viewModel::addExerciseWithPreset,
                onModeSelected = viewModel::setSessionMode,
                onAddParticipant = viewModel::addParticipant,
                onRemoveParticipant = viewModel::removeParticipant,
                onShowAddParticipantSheet = viewModel::showAddParticipantSheet,
                onHideAddParticipantSheet = viewModel::hideAddParticipantSheet,
                onUpdateExerciseRecording = viewModel::updateExerciseRecording
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
            enterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(300)) }
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
                onRemoveExercise = viewModel::removeExercise,
                onAddExercise = viewModel::addExercise,
                onToggleTimeRange = viewModel::toggleTimeRange,
                onExpandExercise = viewModel::expandExercise,
                onCollapseExercise = viewModel::collapseExercise,
                onDismissExpandedExercise = viewModel::dismissExpandedExercise,
                onAddExerciseWithPreset = viewModel::addExerciseWithPreset,
                onModeSelected = viewModel::setSessionMode,
                onAddParticipant = viewModel::addParticipant,
                onRemoveParticipant = viewModel::removeParticipant,
                onShowAddParticipantSheet = viewModel::showAddParticipantSheet,
                onHideAddParticipantSheet = viewModel::hideAddParticipantSheet,
                onUpdateExerciseRecording = viewModel::updateExerciseRecording
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

            Box(Modifier.padding(bottom = measuredNavBarHeight)) {
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
        }

        // Exercise Detail (FT-018)
        composable(
            route = Route.ExerciseDetail.ROUTE,
            arguments = listOf(
                navArgument(Route.ExerciseDetail.ARG_EXERCISE_ID) {
                    type = NavType.StringType
                }
            ),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
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
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
        ) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onBottomSheetComparisonClick = {
                    navController.navigateToBottomSheetComparison()
                },
                onWorkoutLayoutExperimentClick = {
                    navController.navigateToWorkoutLayoutExperiment()
                }
            )
        }

        // Debug: BottomSheet Comparison Screen
        composable(Route.BottomSheetComparison.route) {
            BottomSheetComparisonScreen()
        }

        // Debug: Workout Layout Experiment Screen
        composable(Route.WorkoutLayoutExperiment.route) {
            WorkoutLayoutExperimentScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Session History Screen
        composable(
            route = Route.SessionHistory.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
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

        // Goals Management Screen
        composable(
            route = Route.Goals.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
        ) {
            GoalsScreenWithViewModel(
                onGoalClick = { goalId ->
                    navController.navigateToGoalDetail(goalId)
                },
                onCreateGoal = {
                    navController.navigateToGoalCreate()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Goal Detail Screen
        composable(
            route = Route.GoalDetail.ROUTE,
            arguments = listOf(navArgument(Route.GoalDetail.ARG_GOAL_ID) { type = NavType.StringType }),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString(Route.GoalDetail.ARG_GOAL_ID) ?: return@composable
            GoalDetailScreen(
                goalId = goalId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigateToGoalEdit(goalId) }
            )
        }

        // Goal Create Screen
        composable(
            route = Route.GoalCreate.route,
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
        ) {
            GoalCreateEditScreen(
                goalId = null,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // Goal Edit Screen
        composable(
            route = Route.GoalEdit.ROUTE,
            arguments = listOf(navArgument(Route.GoalEdit.ARG_GOAL_ID) { type = NavType.StringType }),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString(Route.GoalEdit.ARG_GOAL_ID) ?: return@composable
            GoalCreateEditScreen(
                goalId = goalId,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // AI Assistant Chat Screen
        composable(
            route = Route.Chat.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
        ) {
            val viewModel: ChatViewModel = koinInject { parametersOf(null) }
            ChatScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = viewModel
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
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(300)) }
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
        } // Close inner padded Box

        // Handle back press when workout overlay is expanded
        BackHandler(enabled = showWorkoutOverlay && !activeSessionState.isMinimized) {
            onMinimizeWorkout()
        }

        // Bottom Nav Bar - overlay positioned at the bottom
        AnimatedVisibility(
            visible = showNavBar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(200)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
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
                    if (activeSessionState.hasActiveSession) {
                        showCancelSessionDialog = true
                    } else {
                        navController.navigateToSessionPlanning()
                    }
                },
                modifier = Modifier.onSizeChanged { size ->
                    with(density) {
                        measuredNavBarHeight = size.height.toDp()
                    }
                }
            )
        }

        // Workout Overlay - animated shrink/expand view
        // Positioned outside the padded content Box so it can fill the full screen
        // when expanded, and correctly position above the nav bar when minimized
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
                    onCompleteSet = { exerciseId, setNumber, fieldValues ->
                        fieldValues.forEach { (key, value) ->
                            workoutViewModel.updateFieldValue(key, value)
                        }
                        workoutViewModel.completeSet(exerciseId, setNumber)
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
                    onEndWorkout = {
                        scope.launch {
                            val sessionId = activeSessionState.sessionId
                            when (workoutViewModel.finishWorkout()) {
                                is Result.Success -> {
                                    // Create the Workout historical record from session data
                                    if (sessionId != null) {
                                        val exerciseNames = workoutState.exercises
                                            .map { it.name }
                                            .joinToString(", ")
                                        val totalVolume = workoutState.exercises.sumOf { exercise ->
                                            exercise.setRecords.sumOf { set ->
                                                (set.weight * set.reps).toLong()
                                            }
                                        }
                                        val totalSets = workoutState.exercises.sumOf { it.completedSets.toLong() }

                                        val createResult = workoutRepository.create(
                                            name = workoutState.sessionName,
                                            duration = workoutState.elapsedSeconds.toLong(),
                                            notes = null,
                                            isPartnerWorkout = false,
                                            totalVolume = totalVolume,
                                            totalSets = totalSets,
                                            exerciseCount = workoutState.exercises.size.toLong(),
                                            exerciseNames = exerciseNames.takeIf { it.isNotBlank() }
                                        )

                                        // Link the workout to the session if creation succeeded
                                        if (createResult is Result.Success) {
                                            sessionRepository.updateWorkoutId(sessionId, createResult.data)
                                        }
                                    }

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
                    },
                    onCancelWorkout = {
                        scope.launch {
                            workoutViewModel.cancelWorkout()
                            activeSessionViewModel.clearMinimizedState()
                        }
                    },
                    onRenameWorkout = { name ->
                        workoutViewModel.renameSession(name)
                    },
                    onRestTimerStart = { workoutViewModel.startRestTimer() },
                    onRestTimerStop = { workoutViewModel.stopRestTimer() },
                    onRestTimerReset = { workoutViewModel.resetRestTimer() },
                    onRestTimerDurationChange = { delta ->
                        val newDuration = workoutState.restTimerDuration + delta
                        workoutViewModel.setRestTimerDuration(newDuration)
                    },
                    onRestTimerAdjust = { seconds ->
                        workoutViewModel.addRestTime(seconds)
                    },
                    onSwitchParticipant = { participantId ->
                        workoutViewModel.switchParticipant(participantId)
                    },
                    bottomNavHeight = measuredNavBarHeight
                )
            }
        }
    } // Close outer full-size Box
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
