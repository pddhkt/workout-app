package com.workout.app.ui.components.chat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.AgentStatus
import com.workout.app.ui.theme.AppTheme

/**
 * Status indicator showing animated dots and a status label.
 * Displayed when the AI assistant is processing (thinking, using tools, etc.).
 * Left-aligned to match assistant message position.
 *
 * @param status Current agent status (Thinking or Working with a label)
 * @param modifier Optional modifier
 */
@Composable
fun AgentStatusIndicator(
    status: AgentStatus,
    modifier: Modifier = Modifier
) {
    if (status is AgentStatus.Idle) return

    val statusText = when (status) {
        is AgentStatus.Thinking -> "Thinking..."
        is AgentStatus.Working -> status.statusText
        is AgentStatus.Idle -> return
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = AppTheme.spacing.md,
                    vertical = AppTheme.spacing.sm
                ),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedDot(delayMillis = 0)
                AnimatedDot(delayMillis = 200)
                AnimatedDot(delayMillis = 400)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * A single animated dot that pulses in alpha.
 *
 * @param delayMillis Delay offset for staggered animation
 * @param modifier Optional modifier
 */
@Composable
internal fun AnimatedDot(
    delayMillis: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                delayMillis = delayMillis
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(
        modifier = modifier
            .size(6.dp)
            .alpha(alpha)
            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = CircleShape
            )
    )
}
