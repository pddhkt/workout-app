package com.workout.app.ui.components.workout

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
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
    bottomNavHeight: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val minimizedHeight = 56.dp

    // Use BoxWithConstraints to get the available height
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val maxHeightDp = maxHeight

        // Coordinated transition for all animations
        val transition = updateTransition(targetState = isExpanded, label = "workoutOverlay")

        val targetHeight by transition.animateDp(
            transitionSpec = {
                tween(durationMillis = 350, easing = FastOutSlowInEasing)
            },
            label = "overlayHeight"
        ) { expanded -> if (expanded) maxHeightDp else minimizedHeight }

        // Expanded content: fade out quickly when minimizing, fade in after height starts expanding
        val expandedAlpha by transition.animateFloat(
            transitionSpec = {
                if (false isTransitioningTo true) {
                    // Expanding: fade in with slight delay so height leads
                    tween(durationMillis = 250, delayMillis = 100)
                } else {
                    // Minimizing: fade out immediately
                    tween(durationMillis = 200)
                }
            },
            label = "expandedAlpha"
        ) { expanded -> if (expanded) 1f else 0f }

        // Minimized bar: fade in after content fades out, fade out immediately when expanding
        val minimizedAlpha by transition.animateFloat(
            transitionSpec = {
                if (true isTransitioningTo false) {
                    // Expanding: fade out minimized bar immediately
                    tween(durationMillis = 150)
                } else {
                    // Minimizing: fade in after expanded content fades out
                    tween(durationMillis = 200, delayMillis = 150)
                }
            },
            label = "minimizedAlpha"
        ) { expanded -> if (expanded) 0f else 1f }

        // Animate bottom padding for proper positioning above bottom nav
        val bottomPadding by transition.animateDp(
            transitionSpec = {
                tween(durationMillis = 350, easing = FastOutSlowInEasing)
            },
            label = "bottomPadding"
        ) { expanded -> if (expanded) 0.dp else bottomNavHeight }

        // Animate background color
        val bgColor by transition.animateColor(
            transitionSpec = {
                tween(durationMillis = 350)
            },
            label = "backgroundColor"
        ) { expanded ->
            if (expanded) MaterialTheme.colorScheme.background
            else MaterialTheme.colorScheme.surface
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomPadding)
                .height(targetHeight)
                .background(bgColor)
                .align(Alignment.BottomCenter)
        ) {
            // Full workout content (fades out when collapsing)
            if (expandedAlpha > 0.01f) {
                Box(modifier = Modifier.graphicsLayer { alpha = expandedAlpha }) {
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
                        .graphicsLayer { alpha = minimizedAlpha }
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
