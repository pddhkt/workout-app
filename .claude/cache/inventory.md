# Workout App - Code Inventory

Last Updated: 2026-01-21 (FT-008, FT-011 completed)

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

#### Domain Layer
*Not yet implemented*

#### Data Layer
*Not yet implemented*

#### DI (Koin)
*Not yet implemented*

### ComposeApp Module (Android)
Android-specific application code.

**MainActivity.kt**
- Entry point for Android app
- Sets up WorkoutAppTheme
- Displays placeholder "Workout App" text with primary color
- Uses edge-to-edge display

**App.kt**
- Application class
- Initializes Koin DI
- Currently has empty modules list (to be populated)

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
- Koin 3.5.3
- Coroutines 1.8.0
- AGP 8.2.2
- Ktor 2.3.9 (not yet used)
- SQLDelight 2.0.1 (not yet used)

**shared/build.gradle.kts**
- Android target (minSdk 24, compileSdk 34)
- iOS targets: x64, arm64, simulatorArm64
- Framework: Static library named "Shared"
- Dependencies: Compose Multiplatform, Koin, Coroutines

**composeApp/build.gradle.kts**
- Android application
- Depends on :shared module
- Version: 1.0.0 (versionCode 1)
- ProGuard enabled for release builds
- Compose UI Tooling for @Preview support

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

## Next Steps (Not Implemented)

1. Domain models and use cases
2. Repository interfaces
3. Data layer with expect/actual
4. Koin modules for DI
5. Navigation setup
6. Feature screens
7. Unit tests in commonTest
8. Platform-specific tests

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
