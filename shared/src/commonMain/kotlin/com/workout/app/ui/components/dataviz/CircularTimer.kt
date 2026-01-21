package com.workout.app.ui.components.dataviz

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.theme.AppTheme
import kotlin.math.PI

/**
 * CircularTimer component with animated countdown arc
 * Based on mockup element EL-82
 *
 * Displays a circular progress arc that counts down from a total duration.
 * Shows time in MM:SS format with quick adjust buttons.
 *
 * @param remainingSeconds Current remaining time in seconds
 * @param totalSeconds Total duration in seconds (for progress calculation)
 * @param onAddSeconds Callback when +15s button is clicked
 * @param onSubtractSeconds Callback when -15s button is clicked
 * @param modifier Optional modifier for customization
 * @param size Size of the circular timer
 * @param strokeWidth Width of the progress arc stroke
 * @param backgroundColor Color of the background arc
 * @param progressColor Color of the progress arc
 * @param animate Whether to animate progress changes
 */
@Composable
fun CircularTimer(
    remainingSeconds: Int,
    totalSeconds: Int,
    onAddSeconds: () -> Unit,
    onSubtractSeconds: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 12.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    animate: Boolean = true
) {
    // Calculate progress (0.0 to 1.0)
    val progress = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else {
        0f
    }.coerceIn(0f, 1f)

    // Animate progress changes
    val animatedProgress by animateFloatAsState(
        targetValue = if (animate) progress else progress,
        animationSpec = tween(durationMillis = 300),
        label = "timer_progress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
    ) {
        // Circular timer with time display
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val canvasSize = this.size.minDimension
                val radius = (canvasSize - strokeWidth.toPx()) / 2f
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val topCenter = Offset(center.x, center.y - radius)

                // Background arc (full circle)
                drawArc(
                    color = backgroundColor,
                    startAngle = -90f, // Start from top
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )

                // Progress arc
                if (animatedProgress > 0f) {
                    drawArc(
                        color = progressColor,
                        startAngle = -90f, // Start from top
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = Offset(
                            center.x - radius,
                            center.y - radius
                        ),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Time display in center
            Text(
                text = formatTime(remainingSeconds),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Quick adjust buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SecondaryButton(
                text = "-15s",
                onClick = onSubtractSeconds,
                modifier = Modifier.weight(1f),
                enabled = remainingSeconds > 0
            )

            SecondaryButton(
                text = "+15s",
                onClick = onAddSeconds,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Compact version of CircularTimer without quick adjust buttons
 * Useful for displaying timer in smaller spaces
 *
 * @param remainingSeconds Current remaining time in seconds
 * @param totalSeconds Total duration in seconds (for progress calculation)
 * @param modifier Optional modifier for customization
 * @param size Size of the circular timer
 * @param strokeWidth Width of the progress arc stroke
 * @param backgroundColor Color of the background arc
 * @param progressColor Color of the progress arc
 * @param showTime Whether to display time text in center
 */
@Composable
fun CompactCircularTimer(
    remainingSeconds: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 8.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    showTime: Boolean = true
) {
    val progress = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else {
        0f
    }.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "compact_timer_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokeWidth.toPx()) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Background arc
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        if (showTime) {
            Text(
                text = formatTime(remainingSeconds),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Formats seconds into MM:SS format
 * @param seconds Total seconds
 * @return Formatted string in MM:SS format
 */
private fun formatTime(seconds: Int): String {
    val absSeconds = seconds.coerceAtLeast(0)
    val minutes = absSeconds / 60
    val secs = absSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}
