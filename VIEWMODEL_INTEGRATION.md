# ViewModel Integration Guide

This guide shows how to integrate the ViewModels with existing screens.

## Table of Contents

1. [Android Integration](#android-integration)
2. [Common Integration](#common-integration)
3. [Navigation with Parameters](#navigation-with-parameters)
4. [Error Handling](#error-handling)
5. [Loading States](#loading-states)

## Android Integration

### Using koinViewModel in Android

For Android screens, use `koinViewModel()` from Koin Compose to get ViewModels with lifecycle support:

```kotlin
// composeApp/src/androidMain/kotlin/com/workout/app/ui/screens/home/HomeRoute.kt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workout.app.presentation.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoute(
    onNavigate: (Int) -> Unit,
    onTemplateClick: (String) -> Unit,
    onSessionClick: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreenContent(
        state = state,
        onRefresh = viewModel::refresh,
        onTemplateClick = onTemplateClick,
        onSessionClick = onSessionClick,
        onNavigate = onNavigate
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeState,
    onRefresh: () -> Unit,
    onTemplateClick: (String) -> Unit,
    onSessionClick: (String) -> Unit,
    onNavigate: (Int) -> Unit
) {
    when {
        state.isLoading -> LoadingScreen()
        state.error != null -> ErrorScreen(
            message = state.error,
            onRetry = onRefresh
        )
        else -> HomeScreenSuccess(
            templates = state.templates,
            recentSessions = state.recentSessions,
            heatmapData = state.heatmapData,
            onTemplateClick = onTemplateClick,
            onSessionClick = onSessionClick,
            onNavigate = onNavigate
        )
    }
}
```

### Add to Navigation

Update your `AppNavigation.kt` to use the new routes:

```kotlin
// composeApp/src/androidMain/kotlin/com/workout/app/ui/navigation/AppNavigation.kt
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.route
    ) {
        composable(Route.Home.route) {
            HomeRoute(
                onNavigate = { index ->
                    when (index) {
                        0 -> navController.navigate(Route.Home.route)
                        1 -> navController.navigate(Route.ExerciseLibrary.route)
                        2 -> navController.navigate(Route.SessionPlanning.route)
                        3 -> navController.navigate(Route.Profile.route)
                    }
                },
                onTemplateClick = { templateId ->
                    navController.navigate(Route.SessionPlanning.route)
                },
                onSessionClick = { sessionId ->
                    navController.navigate(Route.WorkoutDetail(sessionId).route)
                }
            )
        }

        composable(Route.SessionPlanning.route) {
            SessionPlanningRoute(
                onBackClick = { navController.popBackStack() },
                onStartSession = { sessionId ->
                    navController.navigate(Route.Workout(sessionId).route)
                }
            )
        }

        // More routes...
    }
}
```

## Common Integration

### Using koinInject in Common

For common screens, use `koinInject()`:

```kotlin
// shared/src/commonMain/kotlin/com/workout/app/ui/screens/library/ExerciseLibraryScreen.kt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.workout.app.presentation.library.ExerciseLibraryViewModel
import org.koin.compose.koinInject

@Composable
fun ExerciseLibraryScreenWithViewModel(
    onExerciseClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ExerciseLibraryViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    ExerciseLibraryContent(
        exercises = state.displayedExercises,
        searchQuery = state.searchQuery,
        selectedMuscleGroup = state.selectedMuscleGroup,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onMuscleGroupSelect = viewModel::selectMuscleGroup,
        onFavoriteToggle = viewModel::toggleFavorite,
        onExerciseClick = onExerciseClick,
        onBackClick = onBackClick
    )
}
```

## Navigation with Parameters

### ViewModels that require parameters

For ViewModels with parameters (like `WorkoutViewModel` and `ExerciseDetailViewModel`), use `parametersOf`:

```kotlin
// Android
@Composable
fun WorkoutRoute(
    sessionId: String,
    onBackClick: () -> Unit,
    onFinish: () -> Unit,
    viewModel: WorkoutViewModel = koinViewModel { parametersOf(sessionId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            onFinish()
        }
    }

    WorkoutScreenContent(
        state = state,
        onRepsChange = viewModel::updateReps,
        onWeightChange = viewModel::updateWeight,
        onRPEChange = viewModel::updateRPE,
        onCompleteSet = viewModel::completeSet,
        onSkipSet = viewModel::skipSet,
        onFinish = {
            viewModel.finishWorkout()
        },
        onBackClick = onBackClick
    )
}

// Navigation setup
composable(
    route = Route.Workout.ROUTE,
    arguments = listOf(navArgument(Route.Workout.ARG_SESSION_ID) { type = NavType.StringType })
) { backStackEntry ->
    val sessionId = backStackEntry.arguments?.getString(Route.Workout.ARG_SESSION_ID) ?: return@composable

    WorkoutRoute(
        sessionId = sessionId,
        onBackClick = { navController.popBackStack() },
        onFinish = {
            navController.navigate(Route.WorkoutComplete(sessionId).route) {
                popUpTo(Route.Home.route)
            }
        }
    )
}
```

### Exercise Detail Example

```kotlin
@Composable
fun ExerciseDetailRoute(
    exerciseId: String,
    onBackClick: () -> Unit,
    onAddToWorkout: () -> Unit,
    viewModel: ExerciseDetailViewModel = koinViewModel { parametersOf(exerciseId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ExerciseDetailContent(
        exercise = state.exercise,
        selectedView = state.selectedView,
        isInstructionsExpanded = state.isInstructionsExpanded,
        performanceStats = state.performanceStats,
        workoutHistory = state.workoutHistory,
        onViewChange = viewModel::selectView,
        onInstructionsToggle = viewModel::toggleInstructionsExpanded,
        onHistoryToggle = viewModel::toggleHistoryItemExpanded,
        onFavoriteToggle = viewModel::toggleFavorite,
        onBackClick = onBackClick,
        onAddToWorkout = onAddToWorkout
    )
}
```

## Error Handling

### Centralized Error Display

```kotlin
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
        PrimaryButton(
            text = "Try Again",
            onClick = onRetry
        )
        if (onDismiss != null) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            SecondaryButton(
                text = "Dismiss",
                onClick = onDismiss
            )
        }
    }
}
```

### Snackbar for Non-Critical Errors

```kotlin
@Composable
fun SessionPlanningRoute(
    onBackClick: () -> Unit,
    onStartSession: (String) -> Unit,
    viewModel: SessionPlanningViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show errors in snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Dismiss"
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        SessionPlanningContent(
            state = state,
            onAddExercise = viewModel::addExercise,
            onRemoveExercise = viewModel::removeExercise,
            onStartSession = { sessionName ->
                viewModel.createSession(sessionName).also { result ->
                    if (result is Result.Success) {
                        onStartSession(result.data)
                    }
                }
            },
            modifier = Modifier.padding(padding)
        )
    }
}
```

## Loading States

### Skeleton Loading

```kotlin
@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.spacing.lg)
    ) {
        // Skeleton cards
        repeat(3) {
            ShimmerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
        }
    }
}

@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(shimmerColors),
                shape = MaterialTheme.shapes.medium
            )
    )
}
```

### Inline Loading

```kotlin
@Composable
fun HomeScreenContent(
    state: HomeState,
    onRefresh: () -> Unit,
    ...
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Show content with loading indicator at top
        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Content
        LazyColumn {
            items(state.templates) { template ->
                TemplateCard(template = template)
            }
        }
    }
}
```

### Pull-to-Refresh (Android)

```kotlin
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreenContent(
    state: HomeState,
    onRefresh: () -> Unit,
    ...
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = onRefresh
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn {
            // Content
        }

        PullRefreshIndicator(
            refreshing = state.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
```

## Complete Example: Session Planning

Here's a complete example showing all patterns together:

```kotlin
// Android route composable
@Composable
fun SessionPlanningRoute(
    onBackClick: () -> Unit,
    onTemplatesClick: () -> Unit,
    onStartSession: (String) -> Unit,
    viewModel: SessionPlanningViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle errors
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Handle session creation success
    var showSessionNameDialog by remember { mutableStateOf(false) }

    if (showSessionNameDialog) {
        SessionNameDialog(
            onDismiss = { showSessionNameDialog = false },
            onConfirm = { sessionName ->
                viewModel.createSession(sessionName).also { result ->
                    if (result is Result.Success) {
                        onStartSession(result.data)
                    }
                }
                showSessionNameDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen()
        } else {
            SessionPlanningContent(
                exercises = state.filteredExercises,
                addedExercises = state.addedExercises,
                selectedMuscleGroup = state.selectedMuscleGroup,
                totalSets = state.totalSets,
                canStartSession = state.canStartSession,
                onMuscleGroupSelect = viewModel::selectMuscleGroup,
                onExerciseToggle = viewModel::toggleExercise,
                onSetCountChange = viewModel::updateExerciseSets,
                onStartClick = { showSessionNameDialog = true },
                onBackClick = onBackClick,
                onTemplatesClick = onTemplatesClick,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
```

## Migration Checklist

When migrating a screen to use ViewModels:

- [ ] Create ViewModel in `presentation/` package
- [ ] Add ViewModel to `ViewModelModule.kt`
- [ ] Create Route composable with ViewModel injection
- [ ] Collect state with `collectAsState()` or `collectAsStateWithLifecycle()`
- [ ] Extract content to stateless composable
- [ ] Handle loading state
- [ ] Handle error state
- [ ] Handle empty state
- [ ] Add error snackbar or dialog
- [ ] Test reactive updates
- [ ] Remove mock data
- [ ] Update navigation
- [ ] Test back navigation
- [ ] Test parameter passing (if applicable)

## Best Practices

1. **Separate Route from Content**: Route handles ViewModel, Content is stateless
2. **Use collectAsStateWithLifecycle()**: On Android for automatic cleanup
3. **Handle All States**: Loading, Error, Empty, Success
4. **Clear Errors**: Call `clearError()` after showing error
5. **Use LaunchedEffect**: For one-time events and side effects
6. **Parameter Validation**: Check navigation parameters for null
7. **Loading Indicators**: Show appropriate feedback during async operations
8. **Error Recovery**: Always provide a retry mechanism
9. **State Preservation**: ViewModels survive configuration changes
10. **Testing**: Test ViewModels in isolation with mock repositories
