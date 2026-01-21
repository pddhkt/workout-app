package com.workout.app.ui.components.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.Error
import com.workout.app.ui.theme.Info
import com.workout.app.ui.theme.Success
import com.workout.app.ui.theme.Warning

/**
 * Badge variant types for different status indicators
 */
enum class BadgeVariant {
    /** Success/positive status (green) */
    SUCCESS,
    /** Warning/caution status (yellow) */
    WARNING,
    /** Error/negative status (red) */
    ERROR,
    /** Informational status (blue) */
    INFO,
    /** Neutral/default status (gray) */
    NEUTRAL
}

/**
 * Badge component for status indicators
 * Small, compact label for displaying status or counts
 * Based on elements EL-31, EL-81, EL-95 from mockups
 *
 * @param text The text to display in the badge
 * @param variant The visual variant/style of the badge
 * @param showDot Whether to show a status dot indicator
 * @param modifier Optional modifier for customization
 */
@Composable
fun Badge(
    text: String,
    variant: BadgeVariant = BadgeVariant.NEUTRAL,
    showDot: Boolean = false,
    modifier: Modifier = Modifier
) {
    val variantColor = when (variant) {
        BadgeVariant.SUCCESS -> Success
        BadgeVariant.WARNING -> Warning
        BadgeVariant.ERROR -> Error
        BadgeVariant.INFO -> Info
        BadgeVariant.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val backgroundColor = variantColor.copy(alpha = 0.15f)
    val textColor = variantColor

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(
                horizontal = AppTheme.spacing.sm,
                vertical = AppTheme.spacing.xs
            ),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDot) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(variantColor)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * Numeric badge for displaying counts
 * Typically used for notification counts or item counts
 *
 * @param count The number to display
 * @param variant The visual variant/style of the badge
 * @param modifier Optional modifier for customization
 */
@Composable
fun CountBadge(
    count: Int,
    variant: BadgeVariant = BadgeVariant.ERROR,
    modifier: Modifier = Modifier
) {
    val variantColor = when (variant) {
        BadgeVariant.SUCCESS -> Success
        BadgeVariant.WARNING -> Warning
        BadgeVariant.ERROR -> Error
        BadgeVariant.INFO -> Info
        BadgeVariant.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val displayText = if (count > 99) "99+" else count.toString()

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(CircleShape)
            .background(variantColor)
            .padding(
                horizontal = if (count > 9) AppTheme.spacing.xs else 6.dp,
                vertical = AppTheme.spacing.xs
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
