package com.workout.app.ui.components.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme

/**
 * ProgressDots component for indicating progress through a sequence
 * Shows a series of dots where some are filled (completed) and others are empty (pending)
 * Useful for workout sets, tutorial steps, or any stepped progress
 *
 * @param total Total number of dots to display
 * @param current Current position (0-based index)
 * @param activeColor Color for completed/active dots
 * @param inactiveColor Color for pending dots
 * @param dotSize Size of each dot
 * @param modifier Optional modifier for customization
 */
@Composable
fun ProgressDots(
    total: Int,
    current: Int,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outline,
    dotSize: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val isActive = index <= current
            val color = if (isActive) activeColor else inactiveColor

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/**
 * Variant with different sizes for active and inactive dots
 * The active dot is larger to draw attention
 *
 * @param total Total number of dots to display
 * @param current Current position (0-based index)
 * @param activeColor Color for the active dot
 * @param completedColor Color for completed dots
 * @param inactiveColor Color for pending dots
 * @param activeDotSize Size of the active dot
 * @param dotSize Size of other dots
 * @param modifier Optional modifier for customization
 */
@Composable
fun ProgressDotsWithActiveIndicator(
    total: Int,
    current: Int,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    completedColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    inactiveColor: Color = MaterialTheme.colorScheme.outline,
    activeDotSize: Dp = 10.dp,
    dotSize: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val color = when {
                index == current -> activeColor
                index < current -> completedColor
                else -> inactiveColor
            }

            val size = if (index == current) activeDotSize else dotSize

            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
