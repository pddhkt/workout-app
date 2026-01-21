package com.workout.app.ui.screens.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.components.cards.ElevatedCard
import com.workout.app.ui.components.chips.Badge
import com.workout.app.ui.components.chips.BadgeVariant
import com.workout.app.ui.components.dataviz.CircularTimer
import com.workout.app.ui.theme.AppTheme
import kotlinx.coroutines.delay

/**
 * Data class representing exercise context for the rest timer
 */
data class ExerciseContext(
    val currentExerciseName: String,
    val currentSetNumber: Int,
    val totalSets: Int
)

/**
 * Data class representing the next exercise in the workout
 */
data class UpNextExercise(
    val name: String,
    val sets: Int,
    val reps: String? = null,
    val weight: String? = null
)

/**
 * Rest Timer screen state
 */
data class RestTimerState(
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val exerciseContext: ExerciseContext,
    val upNext: UpNextExercise?
)

/**
 * Full-screen Rest Timer overlay screen
 * Based on mockup elements EL-80 (TimerHeaderBar), EL-81 (ContextBadge),
 * EL-82 (CircularTimerWidget), EL-83 (QuickAdjustButtons),
 * EL-84 (UpNextCard), EL-85 (TimerActionButtons)
 *
 * @param state Current timer state
 * @param onDismiss Callback when timer is dismissed
 * @param onSkipRest Callback when skip rest button is clicked
 * @param onAddTime Callback to add more time to the timer
 * @param onTimerComplete Callback when timer naturally completes
 * @param modifier Optional modifier for customization
 */
@Composable
fun RestTimerScreen(
    state: RestTimerState,
    onDismiss: () -> Unit,
    onSkipRest: () -> Unit,
    onAddTime: (seconds: Int) -> Unit,
    onTimerComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss when timer completes
    LaunchedEffect(state.remainingSeconds) {
        if (state.remainingSeconds <= 0) {
            onTimerComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with close button
            TimerHeaderBar(
                onDismiss = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

            // Context badge showing current exercise
            ContextBadge(
                context = state.exerciseContext,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))

            // Large circular countdown timer with quick adjust buttons
            CircularTimerWidget(
                remainingSeconds = state.remainingSeconds,
                totalSeconds = state.totalSeconds,
                onAddSeconds = { onAddTime(15) },
                onSubtractSeconds = { onAddTime(-15) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Up Next card
            if (state.upNext != null) {
                UpNextCard(
                    exercise = state.upNext,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
            }

            // Action buttons
            TimerActionButtons(
                onSkipRest = onSkipRest,
                onAddTime = { onAddTime(30) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Timer header bar with title and close button
 * Element EL-80
 */
@Composable
private fun TimerHeaderBar(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rest Timer",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close timer",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Context badge showing current exercise and set number
 * Element EL-81
 */
@Composable
private fun ContextBadge(
    context: ExerciseContext,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        Badge(
            text = "Set ${context.currentSetNumber} of ${context.totalSets}",
            variant = BadgeVariant.INFO,
            showDot = true
        )

        Text(
            text = context.currentExerciseName,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Large circular countdown timer with quick adjust buttons
 * Element EL-82 (timer) + EL-83 (quick adjust buttons)
 */
@Composable
private fun CircularTimerWidget(
    remainingSeconds: Int,
    totalSeconds: Int,
    onAddSeconds: () -> Unit,
    onSubtractSeconds: () -> Unit,
    modifier: Modifier = Modifier
) {
    CircularTimer(
        remainingSeconds = remainingSeconds,
        totalSeconds = totalSeconds,
        onAddSeconds = onAddSeconds,
        onSubtractSeconds = onSubtractSeconds,
        modifier = modifier,
        size = 240.dp,
        strokeWidth = 16.dp
    )
}

/**
 * Up Next card showing the next exercise details
 * Element EL-84
 */
@Composable
private fun UpNextCard(
    exercise: UpNextExercise,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Text(
                text = "Up Next",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            // Exercise details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExerciseDetailItem(
                    label = "Sets",
                    value = exercise.sets.toString()
                )

                if (exercise.reps != null) {
                    VerticalDivider()
                    ExerciseDetailItem(
                        label = "Reps",
                        value = exercise.reps
                    )
                }

                if (exercise.weight != null) {
                    VerticalDivider()
                    ExerciseDetailItem(
                        label = "Weight",
                        value = exercise.weight
                    )
                }
            }
        }
    }
}

/**
 * Individual exercise detail item (label + value)
 */
@Composable
private fun ExerciseDetailItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Vertical divider for separating exercise details
 */
@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

/**
 * Action buttons for timer control
 * Element EL-85
 */
@Composable
private fun TimerActionButtons(
    onSkipRest: () -> Unit,
    onAddTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        SecondaryButton(
            text = "Skip Rest",
            onClick = onSkipRest,
            modifier = Modifier.weight(1f)
        )

        PrimaryButton(
            text = "Add 30s",
            onClick = onAddTime,
            modifier = Modifier.weight(1f)
        )
    }
}
