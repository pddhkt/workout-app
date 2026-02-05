package com.workout.app.ui.components.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

/**
 * Minimized workout bar that appears above the bottom navigation
 * when the user swipes back from an active workout.
 *
 * Displays:
 * - Workout/exercise name (centered)
 * - Elapsed time below
 *
 * Tapping the bar expands back to the full workout screen.
 *
 * @param startTime Session start time in epoch milliseconds
 * @param currentExerciseName Name of the current exercise being performed
 * @param onExpand Callback when user taps to expand back to full workout
 * @param modifier Modifier to apply to the bar
 */
@Composable
fun MinimizedWorkoutBar(
    startTime: Long,
    currentExerciseName: String?,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Elapsed time counter
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(startTime) {
        while (true) {
            elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - startTime) / 1000).toInt()
            delay(1000)
        }
    }

    val timeText = formatElapsedTime(elapsedSeconds)
    val displayName = currentExerciseName ?: "Workout"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onExpand
            )
            .semantics {
                contentDescription = "Tap to resume workout, $displayName, $timeText elapsed"
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Workout name
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Elapsed time
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Format elapsed seconds to MM:SS display.
 */
private fun formatElapsedTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}
