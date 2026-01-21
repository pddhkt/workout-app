# FT-020: Navigation - App Router Setup

## Implementation Summary

**Status**: COMPLETED
**Domain**: frontend
**Complexity**: medium
**Dependencies**: FT-009 (completed)

### Files Created

| File | Purpose |
|------|---------|
| `composeApp/src/androidMain/kotlin/com/workout/app/ui/navigation/Routes.kt` | Type-safe route definitions for all MVP screens with sealed class hierarchy |
| `composeApp/src/androidMain/kotlin/com/workout/app/ui/navigation/AppNavigation.kt` | Main NavHost configuration with all screen routes and back stack handling |
| `composeApp/src/androidMain/kotlin/com/workout/app/ui/navigation/NavigationWrappers.kt` | State management wrappers for screens requiring state hoisting |
| `composeApp/src/androidMain/kotlin/com/workout/app/ui/navigation/DeepLinks.kt` | Deep link configuration and app shortcuts structure |

### Files Modified

| File | Changes |
|------|---------|
| `gradle/libs.versions.toml` | Added Navigation Compose 2.8.5 dependency |
| `composeApp/build.gradle.kts` | Added androidx-navigation-compose implementation |
| `composeApp/src/androidMain/kotlin/com/workout/app/MainActivity.kt` | Integrated AppNavigation as root composable |
| `.claude/cache/inventory.md` | Documented navigation implementation and updated build configuration |

## Key Decisions

### 1. Type-Safe Navigation with Sealed Classes
**Rationale**: Using sealed class hierarchy for routes provides compile-time safety and prevents navigation errors. Extension functions make navigation calls cleaner and IDE-autocomplete friendly.

### 2. Centralized Bottom Nav Integration
**Rationale**: BottomNavDestinations object provides bidirectional mapping between bottom nav indices and routes, enabling seamless integration with BottomNavBar component.

### 3. Navigation Wrappers for State Management
**Rationale**: Screens requiring complex state (WorkoutScreen, RestTimerScreen, WorkoutCompleteScreen) get wrapper composables that handle state hoisting at the navigation layer, keeping the shared module screens reusable.

### 4. Deep Link Structure Preparation
**Rationale**: Defined deep link patterns early for future app shortcuts and external navigation support. URI scheme: `workoutapp://app/{destination}`.

### 5. Back Stack Strategy
**Rationale**:
- Onboarding cleared after completion (one-time flow)
- Bottom nav uses saveState/restoreState for tab switching
- Workout completion clears workout from back stack to prevent re-entry

## Patterns Used

### From Android Skill (SKILL.md)
- **Type-safe routes**: Sealed class with companion object constants for arguments
- **Extension functions**: `NavController.navigateToX()` pattern for cleaner navigation
- **Argument handling**: NavType specifications with nullable support
- **Back stack management**: popUpTo with inclusive/saveState/restoreState

### From KMP Skill (SKILL.md)
- **Shared module integration**: Navigation layer in androidMain, screens in commonMain
- **Platform-specific wrappers**: State management at Android layer, pure composables in shared

### From Project Conventions (LEARNED.md)
- **File organization**: Navigation package in composeApp/androidMain
- **Naming conventions**: PascalCase for sealed classes, camelCase for extension functions
- **Documentation**: KDoc comments for all public functions and classes

## Shared Module Integration

### Screen Composables Used
All screens from `shared/src/commonMain/kotlin/com/workout/app/ui/screens/`:
- `onboarding/OnboardingScreen.kt` - Direct integration with completion callback
- `home/HomeScreen.kt` - With bottom nav integration
- `planning/SessionPlanningScreen.kt` - With exercise selection
- `workout/WorkoutScreen.kt` - Via wrapper with mock session data
- `timer/RestTimerScreen.kt` - Via wrapper with countdown state
- `complete/WorkoutCompleteScreen.kt` - Via wrapper with form state
- `library/ExerciseLibraryScreen.kt` - With bottom nav integration
- `detail/ExerciseDetailScreen.kt` - With exerciseId argument passing

### Navigation Callbacks
All screen callbacks mapped to navigation actions:
- Back navigation: `navController.popBackStack()`
- Forward navigation: Type-safe extension functions
- Bottom nav: Route-based tab switching with state preservation
- Completion flows: Clear back stack to prevent unwanted re-entry

## Acceptance Criteria - VERIFIED

### 1. NavHost with all MVP screen routes
✅ **COMPLETE**: AppNavigation.kt contains NavHost with 8 routes:
- Onboarding (FT-019)
- Home (FT-012)
- Session Planning (FT-013)
- Workout (FT-014) - with optional sessionId argument
- Rest Timer (FT-015)
- Workout Complete (FT-016) - with sessionId argument
- Exercise Library (FT-017)
- Exercise Detail (FT-018) - with exerciseId argument

### 2. Bottom nav integration with route switching
✅ **COMPLETE**:
- BottomNavDestinations provides bidirectional index/route mapping
- Home and Library screens integrated with BottomNavBar
- Route switching uses saveState/restoreState for tab memory
- Single top launch mode prevents duplicate destinations

### 3. Back navigation handling
✅ **COMPLETE**:
- All screens have onBackClick/onNavigateBack callbacks
- popBackStack() used appropriately
- Onboarding cleared from stack after completion
- Workout cleared from stack on completion
- Home as anchor destination for bottom nav

### 4. Argument passing between screens
✅ **COMPLETE**:
- Exercise Detail: exerciseId (NavType.StringType, required)
- Workout: sessionId (NavType.StringType, optional, nullable)
- Workout Complete: sessionId (NavType.StringType, required)
- Type-safe argument retrieval from backStackEntry

### 5. Deep link support structure
✅ **COMPLETE**:
- DeepLinks.kt defines URI patterns for all routes
- Scheme: `workoutapp://app`
- URI patterns documented with examples
- Helper functions for URI creation and parsing
- App shortcuts constants defined for launcher integration
- Documentation for AndroidManifest.xml and shortcuts.xml

## Testing Recommendations

### Navigation Flow Tests
1. **Onboarding to Home**: Verify onboarding clears from back stack
2. **Bottom Nav Switching**: Verify state preservation between tabs
3. **Exercise Detail Navigation**: Verify exerciseId passed correctly
4. **Workout Flow**: Planning → Workout → Complete → Home
5. **Back Stack Behavior**: Verify no unwanted destinations remain

### Deep Link Tests
1. **Home Deep Link**: `workoutapp://app/home`
2. **Exercise Deep Link**: `workoutapp://app/exercise/{id}`
3. **Workout Deep Link**: `workoutapp://app/workout/{id}`
4. **Library Deep Link**: `workoutapp://app/library`

### UI Tests
1. Verify all navigation callbacks trigger correctly
2. Verify bottom nav highlights correct tab
3. Verify back button behavior on each screen
4. Verify argument data displays on target screens

## Potential Issues

### 1. RestTimer as Route vs Dialog
**Issue**: RestTimer is currently a route but could be better as a dialog/overlay.
**Mitigation**: Implementation allows easy refactor to dialog by removing route and using local state.

### 2. State Management in Wrappers
**Issue**: Navigation wrappers use mock data which won't persist.
**Mitigation**: Future implementation will use ViewModels with Koin injection for real state.

### 3. Missing ViewModels
**Issue**: No ViewModel layer yet, state is component-local.
**Mitigation**: Wrappers provide clear integration points for future ViewModel injection.

### 4. Profile Route Not Implemented
**Issue**: Bottom nav has Profile tab but no route yet.
**Mitigation**: Placeholder route returns to Home, easy to implement later.

## Next Steps

1. **FT-021**: Implement ViewModels for state management
2. **FT-022**: Integrate Koin for ViewModel injection
3. **FT-023**: Implement deep link handling in MainActivity
4. **FT-024**: Add navigation transitions/animations
5. **FT-025**: Implement Profile screen
6. **FT-026**: Add workout session persistence
7. **FT-027**: Implement template selection in session planning

## Build Verification

```bash
./gradlew :composeApp:assembleDebug
# Result: BUILD SUCCESSFUL in 3s
```

All files compile successfully. Navigation is fully integrated and ready for development use.

## Screenshots/Evidence

Navigation structure includes:
- 8 sealed route classes with type safety
- 8 navigation extension functions
- 8 composable destinations in NavHost
- 4 deep link patterns with documentation
- 3 state management wrappers
- Bottom nav integration with 4 tabs (Home, Library, Workout, Profile)

---

**Implemented by**: Claude Code Agent (Android Implementation)
**Date**: 2026-01-21
**Build Status**: ✅ Successful
**Test Status**: ⏳ Pending (manual verification recommended)
