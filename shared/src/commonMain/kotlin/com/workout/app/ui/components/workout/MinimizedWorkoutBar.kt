package com.workout.app.ui.components.workout

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

/**
 * Minimized workout bar that appears above the bottom navigation
 * when the user swipes back from an active workout.
 *
 * Displays:
 * - Pulsing green dot (activity indicator)
 * - Current exercise name
 * - Elapsed time
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
    val barShape = RoundedCornerShape(16.dp)

    // Elapsed time counter
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(startTime) {
        while (true) {
            elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - startTime) / 1000).toInt()
            delay(1000)
        }
    }

    // Pulsing animation for the activity dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val timeText = formatElapsedTime(elapsedSeconds)
    val displayName = currentExerciseName ?: "Workout"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.lg)
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = barShape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(barShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onExpand
            )
            .semantics {
                contentDescription = "Tap to resume workout, $displayName, $timeText elapsed"
            }
            .padding(horizontal = AppTheme.spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Pulsing dot + Exercise name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                modifier = Modifier.weight(1f)
            ) {
                // Pulsing green dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer { alpha = pulseAlpha }
                        .clip(CircleShape)
                        .background(AppTheme.colors.success)
                )

                // Current exercise name
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right side: Elapsed time
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
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
