package com.workout.app.ui.components.workout

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.workout.app.presentation.workout.WorkoutState
import com.workout.app.ui.components.exercise.LibraryExercise
import com.workout.app.ui.screens.workout.WorkoutScreen

/**
 * Overlay wrapper for WorkoutScreen that provides shrink/expand animation.
 *
 * When expanded: Full-screen workout view
 * When minimized: Collapses to a compact bar (64dp)
 *
 * The animation creates a continuous shrinking effect where the full workout
 * screen visually "becomes" the minimized bar.
 *
 * @param isExpanded Whether the overlay is expanded (full screen) or minimized (compact bar)
 * @param workoutState The current workout state from ViewModel
 * @param startTime Session start time in epoch milliseconds (for timer display)
 * @param onMinimize Callback when user wants to minimize (swipe back)
 * @param onExpand Callback when user taps minimized bar to expand
 * @param onCompleteSet Callback when user completes a set
 * @param onSkipSet Callback when user skips a set
 * @param onAddExercises Callback when adding exercises to the session
 * @param onRemoveExercise Callback when removing an exercise
 * @param onReplaceExercise Callback when replacing an exercise
 * @param onAddSet Callback when adding a set to an exercise
 * @param onReorderExercise Callback when reordering exercises
 * @param onCreateExercise Callback when creating a custom exercise
 * @param onGetHistoricalWeights Callback to get historical weights for an exercise
 * @param onGetHistoricalReps Callback to get historical reps for an exercise
 * @param onSetActiveSet Callback to set the active set
 * @param onEnterReorderMode Callback when entering reorder mode
 * @param onExitReorderMode Callback when exiting reorder mode
 * @param onEndWorkout Callback when ending the workout
 * @param bottomNavHeight Height of the bottom navigation bar (for proper positioning when minimized)
 * @param modifier Modifier for the overlay container
 */
@Composable
fun WorkoutOverlay(
    isExpanded: Boolean,
    workoutState: WorkoutState,
    startTime: Long,
    onMinimize: () -> Unit,
    onExpand: () -> Unit,
    onCompleteSet: (exerciseId: String, setNumber: Int, reps: Int, weight: Float, rpe: Int?) -> Unit,
    onSkipSet: (exerciseId: String) -> Unit,
    onAddExercises: (List<String>) -> Unit,
    onRemoveExercise: (exerciseId: String) -> Unit,
    onReplaceExercise: (exerciseId: String, newExercise: LibraryExercise) -> Unit,
    onAddSet: (exerciseId: String) -> Unit,
    onReorderExercise: (fromIndex: Int, toIndex: Int) -> Unit,
    onCreateExercise: (name: String, muscleGroup: String, equipment: String?, instructions: String?) -> Unit,
    onGetHistoricalWeights: suspend (String) -> List<String>,
    onGetHistoricalReps: suspend (String) -> List<String>,
    onSetActiveSet: (exerciseId: String, setIndex: Int) -> Unit,
    onEnterReorderMode: () -> Unit,
    onExitReorderMode: () -> Unit,
    onEndWorkout: () -> Unit,
    bottomNavHeight: Int = 80,
    modifier: Modifier = Modifier
) {
    val minimizedHeight = 56.dp
    val bottomNavHeightDp = bottomNavHeight.dp

    // Use BoxWithConstraints to get the available height
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val maxHeightDp = maxHeight

        // Animated height with slower, smoother curve
        val targetHeight by animateDpAsState(
            targetValue = if (isExpanded) maxHeightDp else minimizedHeight,
            animationSpec = tween(
                durationMillis = 450,
                easing = FastOutSlowInEasing
            ),
            label = "overlayHeight"
        )

        // Content alpha for crossfade effect
        // Full workout fades out earlier in the animation
        val expandedAlpha by animateFloatAsState(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = if (isExpanded) 150 else 0
            ),
            label = "expandedAlpha"
        )

        // Minimized bar fades in later in the animation
        val minimizedAlpha by animateFloatAsState(
            targetValue = if (isExpanded) 0f else 1f,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = if (isExpanded) 0 else 150
            ),
            label = "minimizedAlpha"
        )

        // Animate bottom padding for proper positioning above bottom nav
        val bottomPadding by animateDpAsState(
            targetValue = if (isExpanded) 0.dp else bottomNavHeightDp,
            animationSpec = tween(
                durationMillis = 450,
                easing = FastOutSlowInEasing
            ),
            label = "bottomPadding"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomPadding)
                .height(targetHeight)
                .background(if (isExpanded) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface)
                .align(Alignment.BottomCenter)
        ) {
            // Full workout content (fades out when collapsing)
            if (expandedAlpha > 0.01f) {
                Box(modifier = Modifier.alpha(expandedAlpha)) {
                    WorkoutScreen(
                        state = workoutState,
                        onCompleteSet = onCompleteSet,
                        onSkipSet = onSkipSet,
                        onAddExercises = onAddExercises,
                        onRemoveExercise = onRemoveExercise,
                        onReplaceExercise = onReplaceExercise,
                        onAddSet = onAddSet,
                        onReorderExercise = onReorderExercise,
                        onCreateExercise = onCreateExercise,
                        onGetHistoricalWeights = onGetHistoricalWeights,
                        onGetHistoricalReps = onGetHistoricalReps,
                        onSetActiveSet = onSetActiveSet,
                        onEnterReorderMode = onEnterReorderMode,
                        onExitReorderMode = onExitReorderMode,
                        onEndWorkout = onEndWorkout
                    )
                }
            }

            // Minimized bar (fades in when collapsed)
            if (minimizedAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .alpha(minimizedAlpha)
                        .align(Alignment.Center)
                ) {
                    MinimizedWorkoutBar(
                        startTime = startTime,
                        currentExerciseName = workoutState.currentExercise?.name,
                        onExpand = onExpand
                    )
                }
            }
        }

        // Note: Back press handling is done in the platform-specific navigation layer
    }
}
