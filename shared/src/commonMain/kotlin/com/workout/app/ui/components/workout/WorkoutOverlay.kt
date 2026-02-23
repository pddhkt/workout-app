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
 * When minimized: Collapses to a compact bar (56dp)
 */
@Composable
fun WorkoutOverlay(
    isExpanded: Boolean,
    workoutState: WorkoutState,
    startTime: Long,
    onMinimize: () -> Unit,
    onExpand: () -> Unit,
    onCompleteSet: (exerciseId: String, setNumber: Int, fieldValues: Map<String, String>) -> Unit,
    onAddExercises: (List<String>) -> Unit,
    onRemoveExercise: (exerciseId: String) -> Unit,
    onReplaceExercise: (exerciseId: String, newExercise: LibraryExercise) -> Unit,
    onAddSet: (exerciseId: String) -> Unit,
    onReorderExercise: (fromIndex: Int, toIndex: Int) -> Unit,
    onCreateExercise: (name: String, muscleGroup: String, equipment: String?, instructions: String?) -> Unit,
    onEndWorkout: () -> Unit,
    onCancelWorkout: () -> Unit,
    onRenameWorkout: (String) -> Unit,
    onRestTimerStart: () -> Unit = {},
    onRestTimerStop: () -> Unit = {},
    onRestTimerReset: () -> Unit = {},
    onRestTimerDurationChange: (Int) -> Unit = {},
    onRestTimerAdjust: (Int) -> Unit = {},
    onSwitchParticipant: (String) -> Unit = {},
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
                        onAddExercises = onAddExercises,
                        onRemoveExercise = onRemoveExercise,
                        onReplaceExercise = onReplaceExercise,
                        onAddSet = onAddSet,
                        onReorderExercise = onReorderExercise,
                        onCreateExercise = onCreateExercise,
                        onEndWorkout = onEndWorkout,
                        onCancelWorkout = onCancelWorkout,
                        onRenameWorkout = onRenameWorkout,
                        onRestTimerStart = onRestTimerStart,
                        onRestTimerStop = onRestTimerStop,
                        onRestTimerReset = onRestTimerReset,
                        onRestTimerDurationChange = onRestTimerDurationChange,
                        onRestTimerAdjust = onRestTimerAdjust,
                        onSwitchParticipant = onSwitchParticipant
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
