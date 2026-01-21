# FT-007 Implementation Summary - ExerciseCard Component

**Task:** Compound Components - Exercise Cards
**Status:** Completed
**Date:** 2026-01-21

## Files Created

| File | Purpose |
|------|---------|
| `shared/src/commonMain/kotlin/com/workout/app/ui/components/exercise/ExerciseCard.kt` | Main ExerciseCard component with three states |
| `shared/src/commonMain/kotlin/com/workout/app/ui/components/exercise/ExerciseCardPreviews.kt` | Common showcase composable |
| `composeApp/src/androidMain/kotlin/com/workout/app/ui/preview/ExerciseCardPreviews.kt` | Android @Preview annotations |

## Implementation Details

### ExerciseCard Component

**Three States Implemented:**

1. **Completed State**
   - Green border accent (Success color with 50% alpha)
   - Checkmark icon (Icons.Default.Check)
   - Normal text color
   - Non-clickable

2. **Active State**
   - Bright green 2dp border (Active color)
   - Clickable to expand/collapse
   - When expanded: Shows set input form
   - Form includes: NumberStepper (reps) + DecimalNumberStepper (weight with kg unit)
   - Animated expansion using fadeIn/fadeOut + expandVertically/shrinkVertically

3. **Pending State**
   - No border
   - Muted gray text color (OnSurfaceVariant)
   - Non-clickable

### Key Features

- **SetChip Row**: Displays all sets with their current state (Completed/Active/Pending)
- **Set Input Form**: Active state expands to show reps and weight steppers side-by-side
- **BaseCard Foundation**: Uses existing BaseCard from FT-005 for consistent styling
- **State Hoisting**: All state managed by parent composable
- **Animations**: Smooth expand/collapse transitions

### Data Models

**ExerciseCardState Enum:**
```kotlin
enum class ExerciseCardState {
    COMPLETED,  // All sets done
    ACTIVE,     // Currently working on this exercise
    PENDING     // Not started yet
}
```

**SetInfo Data Class:**
```kotlin
data class SetInfo(
    val setNumber: Int,
    val reps: Int,
    val weight: Float,
    val state: SetState  // from SetChip component
)
```

### Dependencies Used

| Component | Source | Usage |
|-----------|--------|-------|
| BaseCard | FT-005 (cards package) | Foundation for card styling |
| SetChip | FT-003 (chips package) | Display set progress row |
| NumberStepper | FT-004 (inputs package) | Reps input in active form |
| DecimalNumberStepper | FT-004 (inputs package) | Weight input in active form |
| Success, Active, Pending colors | Design system | State-based theming |

## Preview Composables

### Common Preview (shared module)
- `ExerciseCardShowcase()`: Demonstrates all states
- Used as reference or demo screen
- Shows 4 examples: Completed, Active (collapsed), Active (expanded), Pending

### Android Previews (composeApp module)
- `ExerciseCardCompletedPreview`: Single completed card
- `ExerciseCardActiveCollapsedPreview`: Active card in collapsed state
- `ExerciseCardActiveExpandedPreview`: Active card with form expanded
- `ExerciseCardPendingPreview`: Single pending card
- `AllExerciseCardsPreview`: Complete showcase (1200dp height)

## Patterns Applied

### From Android Skill (SKILL.md)
- Stateless component pattern
- State hoisting with callbacks
- Modifier parameter applied last
- @Preview annotations in androidMain
- Interactive state examples with remember/mutableState

### From KMP Skill (SKILL.md)
- Pure commonMain implementation
- No platform-specific APIs
- Consistent with existing component patterns

### From Project Conventions
- BaseCard as foundation (matching FT-005 pattern)
- SetChip integration (matching FT-003 pattern)
- NumberStepper integration (matching FT-004 pattern)
- Theme color usage for states
- Accessibility with contentDescription

## Testing Recommendations

### UI Testing
- Verify all three states render correctly
- Test expand/collapse animation for active state
- Verify set chips display correctly
- Test reps/weight stepper interactions in expanded form
- Verify border colors and checkmark visibility

### ViewModel Testing
- Test state transitions (Pending -> Active -> Completed)
- Test set progress tracking
- Test reps/weight value updates
- Test expand/collapse state management

### Integration Testing
- Test multiple ExerciseCards in a list
- Test switching active exercise
- Test completing all sets of an exercise
- Test navigation from exercise card

## Key Decisions

1. **Active state only expandable**: Only ACTIVE cards are clickable and expandable. COMPLETED and PENDING cards are static.

2. **Border differentiation**:
   - Completed: 1dp border with 50% alpha for subtle accent
   - Active: 2dp solid border for strong visual emphasis
   - Pending: No border for minimal presence

3. **Form layout**: Reps and weight steppers side-by-side with equal weight (1f) for balanced layout

4. **Animation**: Used Compose's built-in AnimatedVisibility with fade + expand for smooth UX

5. **Set info as data class**: Encapsulates set data (number, reps, weight, state) for clean API

6. **BaseCard integration**: Leverages existing BaseCard for onClick, border, and padding logic

## Accessibility Considerations

- Checkmark icon has contentDescription "Exercise completed"
- Clickable cards have natural focus handling via BaseCard
- Set chips have individual click handlers (optional)
- Steppers have accessibility descriptions for +/- buttons

## Build Verification

- Compiled successfully: `./gradlew :shared:compileDebugKotlinAndroid`
- Android app compiled: `./gradlew :composeApp:compileDebugKotlin`
- No build errors or warnings
- Ready for preview in Android Studio

## Next Integration Steps

1. Add ExerciseCard to inventory.md under "Compound Components - Exercise"
2. Create WorkoutCard that uses ExerciseCard (potential future task)
3. Integrate with workout session screen
4. Add unit tests for ExerciseCardState logic
5. Add screenshot tests for all states
