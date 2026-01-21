# Workout App - Code Inventory

Last Updated: 2026-01-21 (FT-024 completed - Exercise Library Seed Data)

## Project Structure

### Shared Module (KMP)
Pure Kotlin code that runs on all platforms (Android, iOS, JVM, JS).

#### Design System
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/theme/`

**Color.kt**
- Primary colors: Green accent (#13EC5B) matching mockup design
- Background: Dark theme (#0A0A0A, #111111, #1A1A1A)
- Status colors: Success, Warning, Error, Info
- Exercise states: Completed, Active, Pending
- Tag colors: 9 semantic colors from mockup elements.json

**Type.kt**
- Typography system using Material3 Typography
- Heading styles: Large (28sp), Medium (24sp), Small (20sp)
- Body styles: Large (16sp), Medium (14sp), Small (12sp)
- Label styles: Large (14sp), Medium (12sp), Small (10sp)
- Font weights: Bold for headlines, SemiBold for labels, Normal for body

**Spacing.kt**
- Spacing scale using CompositionLocal
- Values: xs=4dp, sm=8dp, md=12dp, lg=16dp, xl=24dp, xxl=32dp
- Accessible via `AppTheme.spacing`

**Theme.kt**
- WorkoutAppTheme composable
- Dark color scheme as default
- Material3 integration
- AppTheme object for accessing spacing

#### Atomic Components - Buttons
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/components/buttons/`

**PrimaryButton.kt**
- Filled button with primary green color (#13EC5B)
- Supports enabled/disabled states
- Full-width variant available
- Height: 48dp, Corner radius: 8dp
- Based on mockup elements EL-14, EL-15, EL-34, EL-35

**SecondaryButton.kt**
- Outlined button style with primary border
- Transparent background
- Supports enabled/disabled states
- Full-width variant available
- Height: 48dp, Corner radius: 8dp
- Based on mockup elements EL-38, EL-83

**IconButton.kt**
- Two variants: AppIconButton (transparent) and FilledIconButton (with background)
- Accepts ImageVector icons
- Customizable tint/colors
- Size: 40dp, Icon: 24dp
- Supports enabled/disabled states
- Based on mockup elements EL-85, EL-93, EL-94, EL-101

**FAB.kt**
- Floating Action Button with primary color
- Icon slot for customization
- Material3 elevation (6dp default, 8dp pressed)
- Corner radius: 16dp
- Based on mockup element EL-102

**ToggleButton.kt**
- Button with selected/unselected states
- Animated color transitions
- Height: 40dp, Corner radius: 8dp
- Selected: Primary fill, Unselected: Outlined
- Supports enabled/disabled states

**ButtonPreviews.kt**
- Showcase composable with all button variants
- Can be used as reference or demo screen

#### Atomic Components - Chips & Badges
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/components/chips/`

**FilterChip.kt**
- Filter chip with active/inactive visual states
- Active: Primary green fill, Inactive: Outlined with gray text
- Rounded corners (20dp), clickable
- Typography: labelMedium (12sp, SemiBold)
- Padding: horizontal 16dp, vertical 8dp
- Based on mockup elements EL-26, EL-27

**SetChip.kt**
- Workout set status chip with three states: Completed, Active, Pending
- Color-coded indicator dot (8dp) + set number text
- Completed: Green background/text, Active: Bright green, Pending: Gray
- Rounded corners (20dp), optional click handler
- Padding: horizontal 12dp, vertical 8dp
- Based on mockup elements EL-17, EL-18, EL-19

**Badge.kt**
- Status indicator badges with five variants: Success, Warning, Error, Info, Neutral
- Optional status dot indicator (6dp)
- Compact design with 15% opacity backgrounds
- Typography: labelSmall (10sp, SemiBold)
- Corner radius: 12dp
- CountBadge variant for numeric counts (1-99+)
- Based on mockup elements EL-31, EL-81, EL-95

**ProgressDots.kt**
- Two variants: standard and with active indicator
- Shows progress through a sequence with filled/empty dots
- Standard: All completed dots same size
- Active indicator: Current dot is larger than others
- Customizable colors, dot sizes, spacing
- Useful for workout sets, tutorial steps, pagination

#### Atomic Components - Inputs
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/components/inputs/`

**TextField.kt**
- Custom text field with icon slot and error state support
- Optional label and placeholder text
- Leading icon slot (ImageVector)
- Error state with red border and error message display
- Configurable keyboard types (Text, Email, Number, etc.)
- Single/multi-line support
- Enabled/disabled states
- Border radius: 8dp, uses AppTheme.spacing
- Based on mockup elements EL-11, EL-12

**NumberStepper.kt**
- Integer and decimal number steppers for weight/reps tracking
- +/- buttons with circular design (40dp)
- Configurable min/max values and step size
- Optional unit display (kg, lbs, etc.)
- NumberStepper: Integer values
- DecimalNumberStepper: Float values with configurable decimal places
- Visual feedback for min/max limits
- Based on mockup element EL-13

**SearchBar.kt**
- Rounded search bar with pill-shaped design (24dp corner radius)
- Search icon slot on left
- Clear button (X) appears when text entered
- Search action via keyboard IME
- Dark surface background with border
- Based on mockup element EL-25

**NotesInput.kt**
- Multi-line text field for workout/exercise notes
- Configurable min/max lines (default 4-8)
- Optional character limit with counter display
- Auto-scrolling for overflow content
- Error state support
- Sentence capitalization by default
- Label with character count display
- Based on mockup element EL-97

**RPESelector.kt**
- Rate of Perceived Exertion (RPE) 1-10 scale selector
- Two variants: Full grid and compact horizontal
- Color-coded by intensity: Easy (Blue), Moderate (Green), Hard (Yellow), Maximum (Red)
- Full variant: 56dp buttons in 2 rows, shows descriptions
- Compact variant: 32dp circular buttons in single row
- Selected state with bold text and colored background
- Descriptive text for each RPE level
- Semantic colors based on design system

#### Compound Components - Headers
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/components/headers/`

**SectionHeader.kt**
- Section header component with four variants for content organization
- Simple variant: Title only with headlineSmall typography
- With Count Badge: Title + CountBadge for numeric indicators
- With Action: Title + trailing TextButton (default "View All")
- With Count & Action: Combines count badge and action button
- Uses Material3 TextButton for actions (no custom button component)
- Consistent typography: headlineSmall for title, labelLarge for actions
- Flexible badge variants: Success, Warning, Error, Info, Neutral
- Customizable action text parameter
- Based on mockup elements EL-28, EL-44, EL-50, EL-78

#### Compound Components - Exercise
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/components/exercise/`

**ExerciseSelectionCard.kt**
- Exercise selection card for session planning with two states: default and added
- Default state: Shows exercise info (name, category) with add button
- Added state: Shows checkmark, remove button, and set count stepper
- Uses FilledIconButton from buttons package for add/remove actions
- Uses NumberStepper from inputs package for set count adjustment
- Uses BaseCard as foundation with standard 16dp padding
- Add button: Primary color fill, Plus icon
- Remove button: SurfaceVariant fill, Close icon
- Success-colored checkmark when added
- Configurable min/max sets (default 1-10)
- Supports enabled/disabled states
- Based on mockup elements EL-39, EL-40

#### Compound Components - Overlays
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/components/overlays/`

**BottomSheet.kt**
- Bottom sheet overlay component with drag-to-dismiss and scrim background
- Smooth slide-up animation on show using spring animation
- Drag gesture handling with configurable dismiss threshold (default 30%)
- Semi-transparent scrim background (OnSurface 60% alpha) dismisses on tap
- Customizable scrim and sheet colors
- Visual drag handle indicator at top of sheet
- Snap-back animation if drag doesn't meet threshold
- Content slot for custom drawer content (ColumnScope)
- Based on mockup element EL-23


**NavigationPreviews.kt**
- Android Studio @Preview annotations for all navigation components
- Individual previews: BottomNavBar (all 4 selected states), BottomActionBar variants
- Interactive preview with state management for navigation switching
- BottomActionBar previews: with/without summary, enabled/disabled states
- Active workout context preview showing session summary integration
- AllNavigationComponentsPreview: Comprehensive showcase of all variants
- Demonstrates WindowInsets handling for system navigation bars
- Shows session summary metrics display
**OverlayPreviews.kt**
- Showcase composable with multiple bottom sheet usage patterns
- Simple content sheet demo
- Action sheet with multiple buttons
- Form sheet with save/cancel actions
- Custom styled sheet with different colors
- Drag threshold sensitivity demo
- Reusable demo composables for testing


#### Compound Components - Navigation
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/components/navigation/`

**BottomNavBar.kt**
- Bottom navigation bar with 4 navigation items (Home, Library, Workout, Profile)
- Selected/unselected states with primary color for selected items
- Material3 filled/outlined icon variants for visual distinction
- Label text below icons using labelSmall typography
- Height: 80dp with padding for system navigation insets
- WindowInsets.navigationBars for safe area handling
- Tab role for accessibility with content descriptions
- NavItem data class for customizable navigation items
- Border styling consistent with design system
- Based on mockup element EL-04

**BottomActionBar.kt**
- Bottom action bar with primary action button and optional session summary
- SessionSummary data class for duration, sets, and exercises
- Three-column layout for summary metrics with vertical dividers
- Integrated PrimaryButton component for main action
- Full-width button layout
- Safe area handling with WindowInsets.navigationBars
- Disabled state support for the action button
- Summary items: centered with bold value and small label
- Typical use: Active workout sessions showing progress
- Based on mockup element EL-46

#### Domain Layer (FT-022)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/domain/model/`

**Result.kt**
- Generic sealed class for handling operation results
- Three states: Success, Error, Loading
- Extension functions: onSuccess, onError, map, getOrNull, getOrDefault
- Used throughout data layer for consistent error handling

#### Data Layer (FT-021)
**Location:** `shared/src/commonMain/sqldelight/com/workout/app/database/`

**Database Schema - SQLDelight**
- WorkoutDatabase: Main database interface with all query classes
- Package: com.workout.app.database
- Version: 1 (initial migration)

**Exercise.sq**
- Exercise table: Stores exercises (standard and custom)
- Fields: id, name, muscleGroup, category, equipment, difficulty, instructions, videoUrl, isCustom, isFavorite, createdAt, updatedAt
- Indexes: muscleGroup, category, favorite, custom
- Queries: selectAll, selectById, selectByMuscleGroup, selectFavorites, selectCustom, search, insert, update, toggleFavorite, delete, countByMuscleGroup
- 52 seed exercises across 7 muscle groups: Chest (9), Back (10), Legs (11), Shoulders (8), Arms (7), Core (7)

**Workout.sq**
- Workout table: Stores completed workouts (historical records)
- Fields: id, name, createdAt, duration, notes, isPartnerWorkout, totalVolume, totalSets, exerciseCount
- Index: createdAt (for recent workouts)
- Queries: selectAll, selectById, selectRecent, selectByDateRange, insert, update, delete, getStats, selectForHeatmap
- Statistics queries for total workouts, volume, duration

**Session.sq**
- Session table: Active or completed workout sessions
- Fields: id, workoutId, templateId, name, startTime, endTime, status, notes, isPartnerWorkout, currentExerciseIndex, createdAt, updatedAt
- Status values: 'active', 'paused', 'completed', 'draft'
- Indexes: status, startTime
- Queries: selectAll, selectById, selectActive, selectByStatus, selectWithExercises (join), insert, update, updateStatus, complete, delete, countByStatus

**SessionExercise.sq**
- SessionExercise table: Links exercises to sessions with ordering
- Fields: id, sessionId, exerciseId, orderIndex, targetSets, completedSets, notes, createdAt, updatedAt
- Foreign keys: sessionId (ON DELETE CASCADE), exerciseId
- Indexes: sessionId, (sessionId, orderIndex)
- Queries: selectBySession (with exercise details), selectById, insert, update, updateCompletedSets, incrementCompletedSets, delete, deleteBySession, countBySession

**Set.sq (WorkoutSet table)**
- WorkoutSet table: Individual set records for exercises
- Fields: id, sessionId, sessionExerciseId, exerciseId, setNumber, weight, reps, rpe, isWarmup, restTime, notes, completedAt, createdAt
- Foreign keys: sessionId (CASCADE), sessionExerciseId (CASCADE), exerciseId
- Indexes: sessionId, sessionExerciseId, exerciseId, completedAt
- Queries: selectAll, selectById, selectBySession, selectBySessionExercise, selectByExercise, selectRecentByExercise, insert, update, delete, deleteBySession
- Analytics queries: calculateSessionVolume, calculateExerciseVolume, getPersonalRecord (1RM), getExerciseStats, countBySession

**Template.sq**
- Template table: Workout templates for quick session creation
- Fields: id, name, description, exercises (JSON), estimatedDuration, isDefault, isFavorite, lastUsed, useCount, createdAt, updatedAt
- Exercises field: JSON array of exercise IDs with sets/order
- Indexes: favorite, lastUsed, useCount
- Queries: selectAll, selectById, selectFavorites, selectRecentlyUsed, selectMostUsed, selectDefaults, selectCustom, insert, update, toggleFavorite, updateLastUsed, delete, countAll, countByType
- 5 default templates: Push Day, Pull Day, Leg Day, Upper Body, Quick Full Body

**migrations/1.sqm**
- Initial schema migration
- Seeds 52 standard exercises across 7 muscle groups (FT-024)
  - Chest: 9 exercises (bench press variations, flyes, dips, etc.)
  - Back: 10 exercises (deadlift, rows, pullups, etc.)
  - Legs: 11 exercises (squats, lunges, leg press, etc.)
  - Shoulders: 8 exercises (presses, raises, etc.)
  - Arms: 7 exercises (curls, tricep work)
  - Core: 7 exercises (planks, crunches, etc.)
- Seeds 5 default workout templates
- All tables created with proper indexes and foreign keys
- Each exercise includes muscle group, category, equipment, difficulty, and instructions

**Key Design Decisions:**
- TEXT IDs for cross-platform compatibility (UUID strings)
- INTEGER timestamps (epoch milliseconds) for datetime
- INTEGER for booleans (0/1) following SQLite conventions
- Soft delete pattern not used (hard deletes for now)
- JSON for template exercises array (flexible structure)
- Cascading deletes for session relationships
- Comprehensive indexes on frequently queried columns

#### Data Layer - Repositories (FT-022)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/data/repository/`

**ExerciseRepository.kt**
- Interface for exercise data access
- CRUD operations: create, update, delete (custom only)
- Reactive queries: observeAll, observeByMuscleGroup, observeFavorites, observeCustom
- Search functionality: search by name/muscle/category
- Toggle favorite status
- Get count by muscle group

**ExerciseRepositoryImpl.kt**
- Implementation using SQLDelight ExerciseQueries
- Flow-based reactive queries using asFlow() and mapToList()
- Timestamp management with Clock.System.now()
- ID generation: "exercise_{timestamp}_{random}"
- Dispatchers.Default for database operations

**WorkoutRepository.kt**
- Interface for workout history data access
- CRUD operations: create, update, delete
- Reactive queries: observeAll, observeRecent (with limit)
- Date range queries: getByDateRange
- Statistics: getStats (totals), getHeatmapData (consistency)

**WorkoutRepositoryImpl.kt**
- Implementation using SQLDelight WorkoutQueries
- Flow-based reactive queries
- ID generation: "workout_{timestamp}_{random}"
- Heatmap data for workout frequency visualization

**SessionRepository.kt**
- Interface for active session management
- CRUD operations: create, update, delete
- Reactive queries: observeAll, observeActive, observeByStatus
- Status management: updateStatus, complete
- Join query: getWithExercises (session with exercises)
- Count by status aggregation

**SessionRepositoryImpl.kt**
- Implementation using SQLDelight SessionQueries
- Flow-based reactive queries
- ID generation: "session_{timestamp}_{random}"
- Auto timestamp on completion

**TemplateRepository.kt**
- Interface for workout template management
- CRUD operations: create, update, delete (custom only)
- Reactive queries: observeAll, observeFavorites, observeRecentlyUsed, observeMostUsed
- Usage tracking: updateLastUsed (increments count)
- Toggle favorite status
- Get default vs custom templates

**TemplateRepositoryImpl.kt**
- Implementation using SQLDelight TemplateQueries
- Flow-based reactive queries
- ID generation: "template_{timestamp}_{random}"
- Tracks usage statistics

#### DI (Koin) (FT-022)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/di/`

**DatabaseModule.kt**
- Provides SqlDriver (expect/actual)
- WorkoutDatabase singleton
- All query objects (exerciseQueries, workoutQueries, sessionQueries, etc.)
- createDatabaseDriver() - platform-specific

**DatabaseModule.android.kt**
- Android-specific database driver
- Uses AndroidSqliteDriver
- Database name: "workout.db"
- Requires Context from Koin

**DatabaseModule.ios.kt**
- iOS-specific database driver
- Uses NativeSqliteDriver
- Database name: "workout.db"

**DataModule.kt**
- Provides all repository implementations
- ExerciseRepository → ExerciseRepositoryImpl
- WorkoutRepository → WorkoutRepositoryImpl
- SessionRepository → SessionRepositoryImpl
- TemplateRepository → TemplateRepositoryImpl
- All singleton instances

**Modules.kt**
- sharedModules list combining all modules
- Order: databaseModule, dataModule
- Imported by platform-specific app initialization

### ComposeApp Module (Android)
Android-specific application code.

**MainActivity.kt**
- Entry point for Android app
- Sets up WorkoutAppTheme
- Integrates AppNavigation as root composable
- Uses edge-to-edge display
- Starts with Home route (configurable for Onboarding in production)

**App.kt**
- Application class
- Initializes Koin DI with sharedModules (FT-022)
- Enables androidLogger for debugging
- Provides Android Context to Koin

#### Navigation (FT-020)
**Location:** `composeApp/src/androidMain/kotlin/com/workout/app/ui/navigation/`

**Routes.kt**
- Type-safe route definitions for all MVP screens
- Sealed class hierarchy for compile-time safety
- Route constants and argument names
- Extension functions for type-safe navigation
- BottomNavDestinations object for bottom nav integration
- Routes defined:
  - Onboarding: User onboarding flow
  - Home: Main dashboard
  - SessionPlanning: Create workout from exercises
  - Workout: Active workout session (with optional sessionId)
  - RestTimer: Rest timer overlay
  - WorkoutComplete: Workout summary (requires sessionId)
  - ExerciseLibrary: Browse exercises
  - ExerciseDetail: Exercise details (requires exerciseId)

**AppNavigation.kt**
- Main NavHost configuration with all MVP screens
- Compose Navigation integration
- Argument passing for exercise IDs and session IDs
- Proper back stack handling:
  - Onboarding cleared after completion
  - Bottom nav state preservation
  - Single top instances for bottom nav
- Navigation callbacks for all screens
- Helper function for current bottom nav index
- Based on Android skill navigation patterns

**NavigationWrappers.kt**
- State management wrappers for screens requiring state
- WorkoutScreen wrapper with mock session data
- RestTimerScreen wrapper with countdown state
- WorkoutCompleteScreen wrapper with form state
- Bridges navigation callbacks to screen implementations

**DeepLinks.kt**
- Deep link URI structure definition
- Deep link patterns for all routes:
  - workoutapp://app/home
  - workoutapp://app/exercise/{exerciseId}
  - workoutapp://app/workout/{sessionId}
  - workoutapp://app/library
  - workoutapp://app/onboarding
  - workoutapp://app/planning
  - workoutapp://app/complete/{sessionId}
- Helper functions to create and parse deep links
- AppShortcuts constants for launcher integration
- Documentation for AndroidManifest.xml integration
- Documentation for shortcuts.xml configuration

#### Android Previews
**Location:** `composeApp/src/androidMain/kotlin/com/workout/app/ui/preview/`

**ButtonPreviews.kt**
- Android Studio @Preview annotations for all button variants
- Individual previews: PrimaryButton, SecondaryButton, IconButton, FAB, ToggleButton
- Combined preview showing all button types
- Interactive toggle button states in previews

**ChipPreviews.kt**
- Android Studio @Preview annotations for all chip and badge components
- Individual previews: FilterChip, SetChip, Badge, CountBadge, ProgressDots
- AllChipsPreview: Comprehensive showcase of all variants and states
- Organized by component type with section headers
- Dark theme preview demonstrating all states

**InputPreviews.kt**
- Android Studio @Preview annotations for all input components
- Individual previews: TextField, NumberStepper, SearchBar, NotesInput, RPESelector
- Demonstrates various states: empty, filled, error, disabled
- Interactive state examples with remember/mutableState
- AllInputsPreview: Complete form example with all input types
- Shows both integer and decimal steppers
- RPE selector in both full and compact variants

**HeaderPreviews.kt**
- Android Studio @Preview annotations for all section header variants
- Individual previews: SectionHeader, SectionHeaderWithCount, SectionHeaderWithAction, SectionHeaderWithCountAndAction
- AllSectionHeadersPreview: Complete showcase with all variants and badge types
- Demonstrates different badge variants: Success, Info, Error, Neutral
- Shows custom action text examples ("View All", "See All", "Browse All")
- Organized sections with dividers between variants
- Dark theme preview with proper spacing

**ExerciseSelectionCardPreviews.kt**
- Android Studio @Preview annotations for ExerciseSelectionCard component
- Individual previews: Default state, Added state, Interactive toggle
- Multiple exercises list preview showing mixed states
- Disabled state previews for both default and added states
- Edge cases preview: Long names, min/max sets boundaries
- AllExerciseSelectionCardStatesPreview: Comprehensive showcase
- Interactive state with remember/mutableState for add/remove actions
- Demonstrates set count stepper integration

**NavigationPreviews.kt**
- Android Studio @Preview annotations for all navigation components
- Individual previews: BottomNavBar (all 4 selected states), BottomActionBar variants
- Interactive preview with state management for navigation switching
- BottomActionBar previews: with/without summary, enabled/disabled states
- Active workout context preview showing session summary integration
- AllNavigationComponentsPreview: Comprehensive showcase of all variants
- Demonstrates WindowInsets handling for system navigation bars
- Shows session summary metrics display
**OverlayPreviews.kt**
- Android Studio @Preview annotations for all bottom sheet variants
- Individual previews: SimpleBottomSheet, ActionSheet, FormBottomSheet
- CustomStyledSheet: Demonstrates custom scrim and sheet colors
- DragThreshold: Shows sensitive drag threshold configuration
- AllBottomSheetsPreview: Comprehensive showcase of all overlay patterns
- Interactive state management with remember/mutableState
- Demonstrates scrim tap dismissal and drag-to-dismiss gestures

### Build Configuration

**gradle/libs.versions.toml**
- Kotlin 2.0.0
- Compose Multiplatform 1.6.10
- Koin 3.5.6
- Coroutines 1.8.0
- Kotlinx DateTime 0.6.0
- AGP 8.2.2
- Ktor 2.3.9 (not yet used)
- SQLDelight 2.0.1 (FT-021)
- Navigation Compose 2.8.5 (FT-020)
- Lifecycle 2.8.0 (FT-023)
- SQLDelight libraries: runtime, coroutines-extensions, android-driver, native-driver, sqlite-driver (FT-022)
- AndroidX Lifecycle: viewmodel, viewmodel-compose (FT-023)
- Testing libraries: kotlin-test, kotlinx-coroutines-test (FT-022)

**shared/build.gradle.kts**
- Android target (minSdk 24, compileSdk 34)
- iOS targets: x64, arm64, simulatorArm64
- Framework: Static library named "Shared"
- Dependencies: Compose Multiplatform, Koin, Coroutines, Kotlinx DateTime, SQLDelight (FT-021)
- AndroidMain dependencies: androidx-lifecycle-viewmodel (FT-023)
- Test dependencies: kotlin-test, kotlinx-coroutines-test, sqldelight-sqlite (FT-022)
- SQLDelight plugin configured with WorkoutDatabase
- Database package: com.workout.app.database
- Schema directory: src/commonMain/sqldelight

**composeApp/build.gradle.kts**
- Android application
- Depends on :shared module
- Version: 1.0.0 (versionCode 1)
- ProGuard enabled for release builds
- Compose UI Tooling for @Preview support
- Navigation Compose for routing (FT-020)

## Patterns in Use

### KMP Patterns
- commonMain for shared code
- androidMain for Android-specific implementations
- Source set hierarchy: commonMain -> androidMain/iosMain

### Design System Patterns
- Material3 ColorScheme for theming
- CompositionLocal for spacing system
- Static object for accessing theme values

### Component Patterns
- **Stateless components**: All UI components are stateless, accept state via parameters
- **State hoisting**: State managed by parent composables or ViewModels
- **Modifier parameter**: All components accept optional Modifier parameter, applied last
- **Preview composables**: @Preview annotations in androidMain for visual testing
- **Platform compatibility**: Avoided platform-specific APIs (ripple, String.format) in commonMain
- **Semantic descriptions**: contentDescription for accessibility
- **Keyboard types**: Proper KeyboardType configuration for inputs

### Dependency Injection
- Koin initialization in Application class
- Lazy module loading approach
- Android-specific context injection

#### Unit Tests (FT-022)
**Location:** `shared/src/commonTest/kotlin/com/workout/app/data/repository/`

**ExerciseRepositoryTest.kt**
- Tests all CRUD operations for exercises
- Tests reactive queries (observeAll, observeFavorites, etc.)
- Tests search functionality
- Tests toggle favorite
- Tests filtering by muscle group
- Tests delete (custom exercises only)
- Uses in-memory SQLite (JdbcSqliteDriver)
- Verifies seeded data from migration

**WorkoutRepositoryTest.kt**
- Tests all CRUD operations for workouts
- Tests reactive queries and limits
- Tests date range filtering
- Tests statistics aggregation (getStats)
- Tests heatmap data generation
- Tests partner workout flag

**SessionRepositoryTest.kt**
- Tests all CRUD operations for sessions
- Tests reactive queries by status
- Tests status updates and completion
- Tests observeActive for active sessions
- Tests getWithExercises join query
- Tests count by status aggregation

**TemplateRepositoryTest.kt**
- Tests all CRUD operations for templates
- Tests reactive queries (favorites, recently used, most used)
- Tests toggle favorite
- Tests usage tracking (updateLastUsed)
- Tests filtering (defaults vs custom)
- Tests delete (custom templates only)
- Tests count by type aggregation
- Verifies seeded default templates

#### Presentation Layer - ViewModels (FT-023)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/presentation/`

**Base ViewModel (expect/actual)**
- `base/ViewModel.kt` - Common ViewModel interface with viewModelScope
- `androidMain/.../ViewModel.android.kt` - AndroidX ViewModel implementation
- `iosMain/.../ViewModel.ios.kt` - Custom iOS implementation with manual scope

**HomeViewModel.kt**
- Manages home screen data (templates, recent sessions, heatmap)
- Dependencies: WorkoutRepository, TemplateRepository, SessionRepository
- Reactive data loading via Flow.combine()
- State: templates, recentSessions, heatmapData, loading, error
- Methods: refresh(), loadHomeData(), loadHeatmapData()
- Loads 4 recent templates and 3 recent workouts
- Generates 28-day workout consistency heatmap

**SessionPlanningViewModel.kt**
- Manages exercise selection and session creation
- Dependencies: ExerciseRepository, SessionRepository
- Reactive exercise loading via observeAll()
- State: allExercises, addedExercises (Map<ID, AddedExerciseData>), selectedMuscleGroup, totalSets
- Methods: selectMuscleGroup(), addExercise(), removeExercise(), updateExerciseSets(), toggleExercise(), createSession()
- Computed properties: filteredExercises, canStartSession
- Validation: Requires at least one exercise to start session

**WorkoutViewModel.kt**
- Manages active workout session state machine
- Dependencies: SessionRepository
- Parameter: sessionId (from navigation)
- State: sessionName, exercises, currentExerciseIndex, elapsedSeconds, currentReps/Weight/RPE/Notes, restTimer
- Methods: updateReps/Weight/RPE/Notes(), completeSet(), skipSet(), skipRest(), addRestTime(), finishWorkout(), saveAndExit()
- Features: Elapsed timer, rest timer countdown, auto progression, set completion tracking
- Computed properties: currentExercise, nextExercise, isWorkoutComplete, canCompleteSet, totalSetsCompleted

**ExerciseLibraryViewModel.kt**
- Manages exercise browsing, search, and filtering
- Dependencies: ExerciseRepository
- Reactive exercise loading via observeAll()
- State: allExercises, searchQuery, searchResults, selectedMuscleGroup
- Methods: updateSearchQuery(), selectMuscleGroup(), toggleFavorite(), clearSearch()
- Computed properties: displayedExercises, exercisesByCategory, availableMuscleGroups, isSearchActive
- Search integration with repository.search()

**ExerciseDetailViewModel.kt**
- Manages exercise details, stats, and history
- Dependencies: ExerciseRepository
- Parameter: exerciseId (from navigation)
- State: exercise, selectedView (ME/PARTNER), isInstructionsExpanded, expandedHistoryItems, performanceStats, workoutHistory
- Methods: toggleInstructionsExpanded(), toggleHistoryItemExpanded(), selectView(), toggleFavorite()
- Computed properties: hasInstructions, hasHistory, isFavorite
- Data classes: PerformanceStats, WorkoutHistoryItem
- Enum: ExerciseView (ME, PARTNER)

**OnboardingViewModel.kt**
- Manages multi-step onboarding flow (4 steps)
- No dependencies (preferences storage TODO)
- State: currentStep, name, selectedGoals (Set<String>), weightUnit
- Methods: updateName(), toggleGoal(), selectWeightUnit(), nextStep(), previousStep(), goToStep(), completeOnboarding()
- Enums: OnboardingStep (WELCOME, NAME_INPUT, GOAL_SELECTION, UNIT_PREFERENCE), WeightUnit (KILOGRAMS, POUNDS)
- Computed properties: totalSteps, currentStepIndex, isFirstStep, isLastStep, canProceed
- Validation: name required for step 2, at least one goal for step 3

**ViewModelModule.kt**
- Koin DI module for all ViewModels
- Factory scope for all ViewModels (new instance per injection)
- Parametrized ViewModels: WorkoutViewModel(sessionId), ExerciseDetailViewModel(exerciseId)
- Auto-injected dependencies from dataModule repositories
- Included in sharedModules list

**README.md**
- Comprehensive documentation of ViewModel architecture
- Usage examples for Android and Common composables
- State management patterns with StateFlow
- Reactive data loading patterns
- Error handling conventions
- Testing guidelines
- Migration guide from mock data to ViewModels
- TODO list for pending features

## Next Steps (Not Implemented)

1. SessionExercise repository for linking exercises to sessions
2. WorkoutSet repository for set tracking and history
3. User preferences repository for onboarding data
4. Template exercise JSON parsing
5. Performance stats calculation in ExerciseDetailViewModel
6. Partner mode stats loading
7. ViewModel unit tests in commonTest

## Key Decisions

1. **Dark theme only**: Mockups show dark UI, no light theme planned
2. **Material3**: Using official Material Design 3 for components
3. **Pure Kotlin commonMain**: No platform-specific code in shared
4. **Koin for DI**: Chosen over manual DI for KMP support
5. **Static framework**: iOS framework is static library for simplicity

#### Compound Components - Data Visualization (FT-010)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/components/dataviz/`

**CircularTimer.kt**
- Circular timer with animated countdown arc
- Full variant with quick adjust buttons (+15s/-15s)
- Compact variant without buttons for smaller spaces
- Displays time in MM:SS format at center
- Canvas-based arc drawing with StrokeCap.Round
- Animated progress changes with 300ms tween
- Configurable size, stroke width, colors
- Uses SecondaryButton for quick adjustments
- Progress arc starts from top (-90 degrees)
- Based on mockup element EL-82

**ConsistencyHeatmap.kt**
- Workout frequency heatmap with grid layout
- Data-driven via HeatmapDay data class (day, count)
- Color-coded intensity levels: Empty, Low (1), Medium (2-3), High (4+)
- Full variant with day labels (M-T-W-T-F-S-S), title, and legend
- Compact variant for dashboard cards (no labels/legend)
- Configurable columns (default 7 for weekly view)
- Intensity colors: Warning (low), Success (medium/high), SurfaceVariant (empty)
- Legend with "Less" to "More" gradient visualization
- Cell size and spacing customizable
- Rounded corners on cells (4dp radius)
- Based on mockup element EL-21

**DataVizPreviews.kt**
- Showcase composable for all data visualization components
- Demonstrates CircularTimer with interactive state
- Shows multiple CompactCircularTimer variants
- ConsistencyHeatmap with 28-day sample data
- Different intensity pattern examples (high, low, mixed consistency)
- Can be used as reference or demo screen

**Android Previews - DataVizPreviews.kt**
- Location: `composeApp/src/androidMain/kotlin/com/workout/app/ui/preview/DataVizPreviews.kt`
- CircularTimerPreview: Standard timer with interactive controls
- CompactCircularTimerPreview: Three compact timers with different states
- ConsistencyHeatmapPreview: Full heatmap with 28-day sample data
- CompactConsistencyHeatmapPreview: Compact variant without labels
- AllDataVizPreview: Comprehensive showcase of all components (1200dp height)
- Interactive state management for timer demonstrations

#### Feature Screens (FT-012)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/screens/home/`

**HomeScreen.kt**
- Main dashboard/home screen composition using atomic and compound components
- Mock data classes: WorkoutTemplate and RecentSession for showcase
- Scaffold layout with BottomNavBar integrated
- Scrollable content area with multiple sections
- HomeHeader: Greeting with dynamic date display (uses kotlinx-datetime)
- ConsistencyHeatmapSection: 28-day workout frequency heatmap with title
- QuickStartTemplatesSection: Horizontal scrolling LazyRow of TemplateCard components
- RecentSessionsSection: Vertical list of RecentSessionCard components
- TemplateCard: Shows template name, exercise count, estimated duration
- RecentSessionCard: Shows workout name, date, duration, exercises, and sets
- SectionHeaderWithAction used for section titles with "View All" actions
- Uses ElevatedCard from cards package
- Static mock data for all content (4 templates, 3 sessions, 28 heatmap days)
- Callbacks for navigation: onTemplateClick, onSessionClick, onViewAll actions, onNavigate
- Based on mockup layout with proper spacing via AppTheme.spacing

**Mock Data Functions:**
- getMockTemplates(): 4 workout templates (Push, Pull, Leg, Upper)
- getMockSessions(): 3 recent sessions with varied metrics
- getMockHeatmapData(): 28-day consistency data with varied intensity

**Android Previews - HomeScreenPreviews.kt**
- Location: `composeApp/src/androidMain/kotlin/com/workout/app/ui/preview/HomeScreenPreviews.kt`
- HomeScreenPreview: Standard screen preview (2000dp height for scrolling)
- HomeScreenInteractivePreview: Preview with all callbacks wired to println
- Dark theme background (0xFF0A0A0A)
- Demonstrates full screen composition and navigation integration

#### Feature Screens - Workout Complete (FT-016)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/screens/complete/`

**WorkoutCompleteScreen.kt**
- Celebration screen shown after completing a workout session
- WorkoutSummary data class with duration, totalVolume, exerciseCount, setCount, muscleGroups
- CelebrationHeader: Emoji celebration with title and subtitle
- WorkoutStatsSection: 2x2 grid of stat cards showing duration, volume, exercises, and sets
- StatCard: Individual card component with primary-colored value and label
- PartnerModeSection: Toggle between Solo/Partner workout modes with helper text
- Muscle groups worked: FlowRow of FilterChip components for each muscle group
- Session notes: NotesInput with 500 character limit
- Action buttons: Save Draft (SecondaryButton) and Done (PrimaryButton) in a Row
- formatDuration(): Helper function converting seconds to MM:SS format
- Uses existing components: SectionHeader, FilterChip, ToggleButton, NotesInput, PrimaryButton, SecondaryButton
- Scrollable content with AppTheme.spacing for consistent padding
- Based on mockup screen AN-11 and elements EL-16, EL-22, EL-78, EL-12

**Android Previews - WorkoutCompleteScreenPreviews.kt**
- Location: `composeApp/src/androidMain/kotlin/com/workout/app/ui/preview/WorkoutCompleteScreenPreviews.kt`
- WorkoutCompleteScreenPreview: Standard workout completion (62 min, 6 exercises, 18 sets)
- WorkoutCompleteScreenPartnerModePreview: Partner mode enabled with leg day example
- WorkoutCompleteScreenShortPreview: Short 20-minute session
- WorkoutCompleteScreenLongPreview: Long 99-minute full body session with 8 muscle groups
- WorkoutCompleteScreenAllStatesPreview: Interactive preview with state management
- Interactive state: muscle group selection, partner mode toggle, notes input
- Multiple scenarios: different workout lengths, muscle group counts, with/without notes

#### Feature Screens - Rest Timer (FT-015)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/screens/timer/`

**RestTimerScreen.kt**
- Full-screen rest timer overlay for workout sessions
- Composed of multiple elements (EL-80 through EL-85 from mockups)
- RestTimerState data class: remainingSeconds, totalSeconds, exerciseContext, upNext
- ExerciseContext data class: currentExerciseName, currentSetNumber, totalSets
- UpNextExercise data class: name, sets, reps (optional), weight (optional)
- Auto-dismisses when timer reaches zero via LaunchedEffect
- Callbacks: onDismiss, onSkipRest, onAddTime, onTimerComplete

**Screen Components:**
- TimerHeaderBar (EL-80): Title and close button in top bar
- ContextBadge (EL-81): Exercise context badge with set progress (Info badge with dot)
- CircularTimerWidget (EL-82): Large 240dp circular timer with 16dp stroke
- QuickAdjustButtons (EL-83): -15s/+15s buttons integrated in CircularTimer
- UpNextCard (EL-84): ElevatedCard showing next exercise details with dividers
- TimerActionButtons (EL-85): "Skip Rest" (Secondary) and "Add 30s" (Primary)

**UpNextCard Details:**
- Shows next exercise name, sets, optional reps and weight
- Three-column layout with vertical dividers between items
- ExerciseDetailItem composable: Label and bold value display
- Conditionally shows reps/weight columns if data is present
- Uses ElevatedCard from cards package

**Layout:**
- Full-screen background with Material background color
- Vertical arrangement with SpaceBetween for proper spacing
- Header at top, context badge below, timer in center
- UpNext card and action buttons at bottom
- All content wrapped in xxl (32dp) padding

**RestTimerPreviews.kt**
- Location: `shared/src/commonMain/kotlin/com/workout/app/ui/screens/timer/RestTimerPreviews.kt`
- RestTimerPreview: Active countdown from 90 seconds with LaunchedEffect simulation
- RestTimerNearCompletionPreview: 5 seconds remaining state
- RestTimerLastExercisePreview: No next exercise (upNext = null)
- RestTimerStartPreview: Full 120 seconds at start
- InteractiveRestTimerPreview: Fully interactive with working callbacks

**Android Previews - RestTimerPreviews.kt**
- Location: `composeApp/src/androidMain/kotlin/com/workout/app/ui/preview/RestTimerPreviews.kt`
- RestTimerActivePreview: Mid-countdown state (75s/90s)
- RestTimerNearCompletionPreview: Warning state (8s remaining)
- RestTimerFullTimePreview: Just started (120s/120s)
- RestTimerLastExercisePreview: No next exercise scenario
- RestTimerMinimalInfoPreview: Shows handling of missing reps/weight
- RestTimerLongNamePreview: Tests long exercise names
- RestTimerInteractivePreview: Countdown simulation with working buttons
- AllRestTimerStatesPreview: Comprehensive showcase (shows active state)
- All previews use 393x852dp device spec (standard phone)

#### Feature Screens - Workout (FT-014)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/screens/workout/`

**WorkoutScreen.kt**
- Active workout session screen with comprehensive exercise tracking
- Data Models:
  - WorkoutSession: Contains workout name, exercises list, start time
  - ExerciseData: Exercise details with id, name, muscle group, target/completed sets
- Session Header (EL-02): Workout name, elapsed time, exercise progress counter
- State management: Current exercise tracking, reps/weight input, RPE, notes, timers
- LazyColumn exercise list with itemsIndexed for proper indexing
- ExerciseCard integration with three states: COMPLETED, ACTIVE, PENDING
- Set chips showing progress (EL-17/18/19)
- Expanded form with NumberStepper and DecimalNumberStepper for reps/weight (EL-11)
- Compact RPE selector shown after completing sets (EL-13)
- Rest timer with CompactCircularTimer and countdown logic (EL-20)
- NotesInput field with 500 character limit (EL-12)
- Action buttons: Complete Set (primary) and Skip Set (secondary) (EL-14)
- BottomSheet drawer for workout options: Finish, Save & Exit, Cancel (EL-23)
- Elapsed time tracking with LaunchedEffect and delay loop
- Rest timer countdown with automatic reset on completion
- Automatic progression to next exercise when current is complete
- SessionHeader private composable: Workout title, time/exercise count, more options button
- ActionButtons private composable: Complete/Skip buttons with enabled state
- formatTime helper function for MM:SS display
- createMockWorkoutSession function for preview data
- Four exercises mock data: Bench Press, Overhead Press, Barbell Row, Bicep Curl
- Proper state hoisting for all interactive elements

**Android Previews - WorkoutScreenPreviews.kt**
- Location: `composeApp/src/androidMain/kotlin/com/workout/app/ui/preview/WorkoutScreenPreviews.kt`
- WorkoutScreenPreview: Active workout with mock data (800dp height)
- WorkoutScreenStartPreview: Fresh workout at start (Leg Day, 3 exercises)
- WorkoutScreenNearCompletePreview: Near completion (Push Day, 30 min elapsed, 2/3 exercises done)
- WorkoutScreenLongNamePreview: Tests long workout and exercise names
- WorkoutScreenInteractivePreview: Interactive preview with state management for set completion
- AllWorkoutScreenStatesPreview: Comprehensive showcase (2400dp height)
- Multiple test scenarios: different workout types, progress states, time elapsed
- Demonstrates all workout screen features in action

#### Feature Screens - Exercise Library (FT-017)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/screens/library/`

**ExerciseLibraryScreen.kt**
- Exercise library browsing screen with comprehensive exercise database
- Data Models:
  - LibraryExercise: Contains id, name, muscleGroup, category, isCustom, isFavorite
- LibraryHeader: Title and subtitle describing the library (EL-24)
- SearchBar integration for filtering exercises by name/muscle/category (EL-25)
- MuscleGroupFilters: Horizontal scrolling filter chips with 8 categories (EL-26/27)
- Exercise list grouped by category with SectionHeader for each group (EL-28)
- ExerciseLibraryItem: Individual exercise card component (EL-29)
  - Exercise name with optional "Custom" badge for user-created exercises (EL-31)
  - Muscle group display below name
  - Favorite star icon toggle (filled/outlined) with primary color when active (EL-32/33)
  - More options button (vertical ellipsis icon) (EL-34)
- FloatingActionButton for "Add to workout" functionality (EL-35)
- BottomNavBar with Library tab selected
- State management: Search query, selected muscle group filter, favorite exercises set
- Filtering logic: By search query and muscle group selection
- Grouping logic: Exercises grouped by category for organized display
- Mock data: 34 exercises across 7 muscle groups (Chest, Back, Legs, Shoulders, Arms, Core, Cardio)
- Categories: Compound, Isolation, Stability, HIIT, Steady State, Full Body, Rotation
- Mix of standard and custom exercises with varied favorite states
- BaseCard with click handlers for navigation
- AppIconButton components for favorite and options actions
- Proper state hoisting for favorite toggles and navigation

**Android Previews - ExerciseLibraryPreviews.kt**
- Location: `composeApp/src/androidMain/kotlin/com/workout/app/ui/preview/ExerciseLibraryPreviews.kt`
- ExerciseLibraryScreenPreview: Standard library view with all features
- ExerciseLibraryScreenInteractivePreview: Preview with console logging for all interactions
- AllExerciseLibraryStatesPreview: Comprehensive showcase (2000dp height for scrolling)
- Demonstrates search functionality, filtering, favorites, and navigation
- Shows all exercise categories and muscle group filters

#### Feature Screens - Exercise Detail (FT-018)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/screens/detail/`

**ExerciseDetailScreen.kt**
- Comprehensive exercise detail screen with all information about a single exercise
- Data Models:
  - ExerciseDetail: Exercise info with name, muscle group, category, equipment, difficulty, instructions, video
  - QuickStats: Target sets, reps, primary muscle for overview
  - PerformanceStats: 1RM, total volume, total sets, last performed date
  - MuscleTarget: Primary and secondary muscles targeted
  - HistoryItem: Individual workout history entry with sets, reps, weight, volume, RPE
- TopAppBar with back navigation and exercise name (EL-03)
- HeroImage component: 240dp height with video thumbnail and play button overlay (EL-86)
- QuickStatsSection: Three-column grid of stat tiles showing sets, reps, muscle (EL-87)
- ExerciseInfo: Badges for category, equipment, and difficulty level
- InstructionsCard: Collapsible card with animated arrow and expandable instructions text (EL-88)
- MePartnerToggle: Toggle buttons to switch between personal and partner stats view (EL-94)
- PerformanceStatsCard: Card with 2x2 grid showing 1RM, volume, sets, last performed (EL-89)
- MuscleTargetCard: Shows muscle diagram placeholder and lists of primary/secondary muscles (EL-90)
- HistoryItems: Expandable cards showing workout history with date, sets, volume (EL-91/92)
- StickyFooterButton: Bottom bar with divider and "Add to Workout" button (EL-93)
- State management: Toggle between Me/Partner stats, expand/collapse instructions and history
- Lazy scrolling with proper content padding
- Uses existing components: BaseCard, ElevatedCard, Badge, ToggleButton, PrimaryButton, SectionHeader
- Mock data functions for all sections with differentiated Me vs Partner stats
- Callbacks: onBackClick, onAddToWorkout, onPlayVideo
- Based on mockup screen AN-13 and elements EL-03, EL-86 through EL-94

**Screen Components:**
- HeroImage: Video thumbnail with centered play button (64dp circle with primary color)
- QuickStatsSection: Row of three QuickStatTile components
- QuickStatTile: Card with centered value (primary color, bold) and label
- InstructionsCard: Collapsible card with animated chevron rotation (180 degrees when expanded)
- MePartnerToggle: Two ToggleButtons in a row for switching stat views
- PerformanceStatsCard: Stats grid with dividers between rows
- StatItem: Label and value in column layout for performance metrics
- MuscleTargetCard: Diagram placeholder (180dp height) with primary/secondary muscle lists
- MuscleList: Label + comma-separated muscle names
- HistoryItemCard: Expandable card with date, sets summary, and detailed reps/weight/RPE
- DetailItem: Centered label/value for history details
- StickyFooterButton: Fixed bottom bar with divider and full-width button

**Android Previews - ExerciseDetailPreviews.kt**
- Location: `composeApp/src/androidMain/kotlin/com/workout/app/ui/preview/ExerciseDetailPreviews.kt`
- ExerciseDetailScreenPreview: Standard detail view (2000dp height for scrolling)
- ExerciseDetailScreenInteractivePreview: Preview with console logging for all interactions
- AllExerciseDetailStatesPreview: Comprehensive showcase (2400dp height)
- Mock exercise: Barbell Bench Press with full data
- Demonstrates all collapsible sections, toggle states, and navigation

**ExerciseDetailPreviews.kt (Common)**
- Location: `shared/src/commonMain/kotlin/com/workout/app/ui/screens/detail/ExerciseDetailPreviews.kt`
- ExerciseDetailPreview: Standard preview composable
- ExerciseDetailInteractivePreview: Interactive preview with callbacks

#### Feature Screens - Onboarding Flow (FT-019)
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/screens/onboarding/`

**OnboardingScreen.kt**
- Multi-step onboarding flow for new user setup with 4 steps
- Data Models:
  - FitnessGoal: Goal data with id, title, description, emoji
  - WeightUnit: Enum for KILOGRAMS or POUNDS preference
  - OnboardingData: Complete form data with name, goals, weight unit
- ProgressDots showing current step out of 4 total steps (EL-95)
- AnimatedContent with slide/fade transitions between steps (forward and backward)
- Skip button in top bar for all steps
- Step 1 - WelcomeStep: Hero card with emoji illustration, title, subtitle, "Get Started" button (EL-96)
- Step 2 - NameInputStep: Text field for name input with Person icon, validation (EL-97)
- Step 3 - GoalSelectionStep: Multiple selectable goal cards with 6 fitness goals (EL-98/99/100)
- Step 4 - UnitPreferenceStep: Toggle between kg and lbs for weight tracking (EL-101)
- GoalCard: Selectable card component with emoji, title, description, selection indicator
- Primary border on selected goal cards, checkmark icon when selected
- Navigation: Back (Secondary) and Continue/Complete (Primary) buttons on each step
- State management: currentStep, onboardingData with all form fields
- Callbacks: onComplete with OnboardingData, onSkip for bypassing flow
- Validation: Name required to proceed, at least one goal required
- Scroll support for smaller screens with verticalScroll
- Based on mockup screen AN-17 and elements EL-95 through EL-102
- Available goals: Build Muscle, Lose Weight, Improve Endurance, Stay Active, Increase Flexibility, Sport Performance

**Screen Components:**
- WelcomeStep: Hero with 120dp emoji, headline, body text, primary CTA
- NameInputStep: AppTextField with label, icon, continue button enabled when name not blank
- GoalSelectionStep: Column of GoalCard components, multiple selection allowed
- GoalCard: BaseCard with border on selection, Row with emoji, text column, checkmark icon
- UnitPreferenceStep: Row with two ToggleButtons for kg/lbs selection

**Android Previews - OnboardingPreviews.kt**
- Location: `composeApp/src/androidMain/kotlin/com/workout/app/ui/preview/OnboardingPreviews.kt`
- OnboardingScreenPreview: Standard onboarding flow preview (852dp height)
- OnboardingScreenInteractivePreview: Preview with console logging for callbacks (1200dp height)
- AllOnboardingStatesPreview: Comprehensive showcase (1500dp height)
- Demonstrates all steps, transitions, validation, and completion flow

**OnboardingPreviews.kt (Common)**
- Location: `shared/src/commonMain/kotlin/com/workout/app/ui/screens/onboarding/OnboardingPreviews.kt`
- OnboardingPreview: Standard preview composable
- OnboardingInteractivePreview: Interactive preview with callbacks and console logging

