package com.workout.app.ui.components.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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

/**
 * FilterChip component with active/inactive states
 * Used for filtering content (e.g., exercise categories, difficulty levels)
 * Based on elements EL-26/27 from mockups
 *
 * @param text The label to display on the chip
 * @param isActive Whether the chip is in active/selected state
 * @param onClick Callback invoked when chip is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun FilterChip(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = if (isActive) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = AppTheme.spacing.lg,
                vertical = AppTheme.spacing.sm
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}
