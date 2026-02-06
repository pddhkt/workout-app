package com.workout.app.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme

/**
 * Base card component providing consistent elevation, rounded corners, and padding.
 * Foundation for all card types in the app (Template Card, Exercise Card, etc.)
 *
 * @param modifier Modifier to be applied to the card
 * @param onClick Optional click handler. If null, card is not clickable
 * @param enabled Whether the card is enabled (affects click and visual state)
 * @param shape Corner radius shape. Defaults to 12dp rounded corners
 * @param elevation Shadow/elevation depth. Defaults to 1dp
 * @param backgroundColor Card background color. Defaults to Material3 surface color
 * @param contentColor Content color. Defaults to Material3 onSurface color
 * @param border Optional border. Use for selected/highlighted states
 * @param contentPadding Padding inside the card. Defaults to theme spacing.lg (16dp)
 * @param content Card content composable
 */
@Composable
fun BaseCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(2.dp),
    elevation: Dp = 1.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    contentPadding: Dp = AppTheme.spacing.lg,
    content: @Composable ColumnScope.() -> Unit
) {
    // Use neutral shadow colors for better contrast
    val shadowModifier = if (elevation > 0.dp) {
        modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.1f),
            spotColor = Color.Black.copy(alpha = 0.15f)
        )
    } else {
        modifier
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = shadowModifier,
            enabled = enabled,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = contentColor,
                disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
                disabledContentColor = contentColor.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp, // Shadow handled by modifier
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp,
                draggedElevation = 0.dp,
                disabledElevation = 0.dp
            ),
            border = border
        ) {
            Column(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    } else {
        Card(
            modifier = shadowModifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp // Shadow handled by modifier
            ),
            border = border
        ) {
            Column(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}
