# Presentation Layer - ViewModels

This directory contains ViewModels for all screens in the workout app. ViewModels are implemented using KMP patterns with expect/actual for platform-specific lifecycle management.

## Architecture

```
presentation/
├── base/
│   └── ViewModel.kt (expect/actual)        # Base ViewModel class
├── home/
│   └── HomeViewModel.kt                    # Home screen VM
├── planning/
│   └── SessionPlanningViewModel.kt         # Session planning VM
├── workout/
│   └── WorkoutViewModel.kt                 # Active workout VM
├── library/
│   └── ExerciseLibraryViewModel.kt         # Exercise library VM
├── detail/
│   └── ExerciseDetailViewModel.kt          # Exercise detail VM
└── onboarding/
    └── OnboardingViewModel.kt              # Onboarding flow VM
```

## Base ViewModel

The base `ViewModel` class is implemented using expect/actual:

- **Android**: Uses AndroidX ViewModel with lifecycle-aware viewModelScope
- **iOS**: Custom implementation with manual scope management

```kotlin
expect abstract class ViewModel() {
    val viewModelScope: CoroutineScope
    protected open fun onCleared()
}
```

## ViewModel Patterns

### State Management

All ViewModels use `StateFlow` for UI state:

```kotlin
private val _state = MutableStateFlow(MyState())
val state: StateFlow<MyState> = _state.asStateFlow()
```

### Reactive Data Loading

ViewModels observe repository Flows and map to UI state:

```kotlin
init {
    loadData()
}

private fun loadData() {
    viewModelScope.launch {
        repository.observeAll()
            .catch { error ->
                _state.update { it.copy(error = error.message) }
            }
            .collect { data ->
                _state.update { it.copy(data = data) }
            }
    }
}
```

### Error Handling

Errors are captured in the UI state:

```kotlin
data class MyState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null
)
```

## ViewModels

### HomeViewModel

**Purpose**: Manages home screen data - recent sessions, templates, and consistency heatmap.

**Dependencies**:
- WorkoutRepository
- TemplateRepository
- SessionRepository

**State**:
```kotlin
data class HomeState(
    val isLoading: Boolean = false,
    val templates: List<Template> = emptyList(),
    val recentSessions: List<Workout> = emptyList(),
    val heatmapData: List<HeatmapDay> = emptyList(),
    val error: String? = null
)
```

**Methods**:
- `refresh()`: Reload all home data

### SessionPlanningViewModel

**Purpose**: Manages exercise selection and session creation.

**Dependencies**:
- ExerciseRepository
- SessionRepository

**State**:
```kotlin
data class SessionPlanningState(
    val allExercises: List<Exercise> = emptyList(),
    val addedExercises: Map<String, AddedExerciseData> = emptyMap(),
    val selectedMuscleGroup: String? = null,
    val totalSets: Int,
    val canStartSession: Boolean
)
```

**Methods**:
- `selectMuscleGroup(muscleGroup: String?)`
- `addExercise(exerciseId: String, sets: Int)`
- `removeExercise(exerciseId: String)`
- `updateExerciseSets(exerciseId: String, sets: Int)`
- `toggleExercise(exerciseId: String)`
- `createSession(sessionName: String): Result<String>`

### WorkoutViewModel

**Purpose**: Manages active workout session state machine.

**Dependencies**:
- SessionRepository

**Parameters**:
- `sessionId: String`

**State**:
```kotlin
data class WorkoutState(
    val sessionName: String = "",
    val exercises: List<WorkoutExercise> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val elapsedSeconds: Int = 0,
    val currentReps: Int = 0,
    val currentWeight: Float = 0f,
    val currentRPE: Int? = null,
    val isRestTimerActive: Boolean = false,
    val restTimerRemaining: Int = 0,
    val isWorkoutComplete: Boolean,
    val canCompleteSet: Boolean
)
```

**Methods**:
- `updateReps(reps: Int)`
- `updateWeight(weight: Float)`
- `updateRPE(rpe: Int?)`
- `updateNotes(notes: String)`
- `completeSet()`
- `skipSet()`
- `skipRest()`
- `addRestTime(seconds: Int)`
- `finishWorkout(): Result<Unit>`
- `saveAndExit(): Result<Unit>`

### ExerciseLibraryViewModel

**Purpose**: Manages exercise browsing, search, and filtering.

**Dependencies**:
- ExerciseRepository

**State**:
```kotlin
data class ExerciseLibraryState(
    val allExercises: List<Exercise> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Exercise>? = null,
    val selectedMuscleGroup: String? = null,
    val displayedExercises: List<Exercise>,
    val exercisesByCategory: Map<String, List<Exercise>>,
    val availableMuscleGroups: List<String>
)
```

**Methods**:
- `updateSearchQuery(query: String)`
- `selectMuscleGroup(muscleGroup: String?)`
- `toggleFavorite(exerciseId: String)`
- `clearSearch()`

### ExerciseDetailViewModel

**Purpose**: Manages exercise details, stats, and history.

**Dependencies**:
- ExerciseRepository

**Parameters**:
- `exerciseId: String`

**State**:
```kotlin
data class ExerciseDetailState(
    val exercise: Exercise? = null,
    val selectedView: ExerciseView = ExerciseView.ME,
    val isInstructionsExpanded: Boolean = false,
    val performanceStats: PerformanceStats,
    val workoutHistory: List<WorkoutHistoryItem> = emptyList(),
    val hasInstructions: Boolean,
    val hasHistory: Boolean,
    val isFavorite: Boolean
)
```

**Methods**:
- `toggleInstructionsExpanded()`
- `toggleHistoryItemExpanded(historyId: String)`
- `selectView(view: ExerciseView)`
- `toggleFavorite()`

### OnboardingViewModel

**Purpose**: Manages multi-step onboarding flow.

**Dependencies**: None

**State**:
```kotlin
data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val name: String = "",
    val selectedGoals: Set<String> = emptySet(),
    val weightUnit: WeightUnit = WeightUnit.KILOGRAMS,
    val totalSteps: Int,
    val canProceed: Boolean
)
```

**Methods**:
- `updateName(name: String)`
- `toggleGoal(goalId: String)`
- `selectWeightUnit(unit: WeightUnit)`
- `nextStep()`
- `previousStep()`
- `goToStep(step: OnboardingStep)`
- `completeOnboarding(): OnboardingData`
- `canProceedFromCurrentStep(): Boolean`

## Dependency Injection

ViewModels are provided via Koin in `ViewModelModule.kt`:

```kotlin
val viewModelModule = module {
    // No parameters
    factory { HomeViewModel(get(), get(), get()) }

    // With parameters
    factory { (sessionId: String) ->
        WorkoutViewModel(sessionId, get())
    }
}
```

## Usage in Screens

### Android (with koinViewModel)

```kotlin
@Composable
fun HomeRoute(
    onNavigate: (Int) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreenContent(
        state = state,
        onRefresh = viewModel::refresh,
        onNavigate = onNavigate
    )
}
```

### With Parameters

```kotlin
@Composable
fun WorkoutRoute(
    sessionId: String,
    viewModel: WorkoutViewModel = koinViewModel { parametersOf(sessionId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    WorkoutScreenContent(
        state = state,
        onCompleteSet = viewModel::completeSet,
        onFinish = {
            viewModel.finishWorkout()
        }
    )
}
```

### Common (with koinInject)

```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    // ...
}
```

## Testing

ViewModels can be tested in `commonTest`:

```kotlin
class HomeViewModelTest : KoinTest {
    private val mockWorkoutRepo = mockk<WorkoutRepository>()
    private val testModule = module {
        single<WorkoutRepository> { mockWorkoutRepo }
        factory { HomeViewModel(get(), get(), get()) }
    }

    @Before
    fun setup() {
        startKoin { modules(testModule) }
    }

    @Test
    fun testLoadData() = runTest {
        val viewModel: HomeViewModel by inject()
        // Test state updates
    }
}
```

## Migration Guide

To migrate existing screens to use ViewModels:

1. **Create ViewModel** with repository dependencies
2. **Define State** data class with all UI state
3. **Add to ViewModelModule** in Koin
4. **Update Screen** to accept ViewModel parameter
5. **Collect State** using collectAsState() or collectAsStateWithLifecycle()
6. **Remove Mock Data** from screens
7. **Test** ViewModel in isolation

Example migration:

```kotlin
// Before (mock data in screen)
@Composable
fun HomeScreen() {
    val mockData = getMockData()
    // ...
}

// After (data from ViewModel)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    // Use state.templates, state.sessions, etc.
}
```

## Best Practices

1. **Single Responsibility**: Each ViewModel manages one screen
2. **Immutable State**: Use data classes with val properties
3. **StateFlow for UI State**: Use StateFlow, not LiveData
4. **Error Handling**: Include error state in UI state data class
5. **Loading State**: Include loading state for async operations
6. **Computed Properties**: Use computed properties in state for derived data
7. **Clean Up**: Cancel jobs in onCleared()
8. **Repository Layer**: Never access database directly, always through repositories
9. **No Android Dependencies**: Keep ViewModels in commonMain
10. **Test Coverage**: Write unit tests for ViewModels in commonTest

## TODO

- [ ] Add WorkoutSetRepository for set tracking in WorkoutViewModel
- [ ] Add SessionExerciseRepository for exercise linking in SessionPlanningViewModel
- [ ] Implement performance stats loading in ExerciseDetailViewModel
- [ ] Add user preferences repository for OnboardingViewModel
- [ ] Add workout history loading in ExerciseDetailViewModel
- [ ] Implement partner mode stats loading
- [ ] Add template exercises parsing in HomeViewModel
