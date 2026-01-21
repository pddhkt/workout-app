package com.workout.app.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.Primary

/**
 * Card variant with elevated appearance (higher shadow)
 * Use for important content that should stand out
 */
@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    BaseCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        elevation = 4.dp,
        content = content
    )
}

/**
 * Card variant with no elevation (flat appearance)
 * Use for grouped or nested cards
 */
@Composable
fun FlatCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    BaseCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        elevation = 0.dp,
        content = content
    )
}

/**
 * Card variant with border, typically for selected/highlighted state
 * @param borderColor Color of the border. Defaults to primary theme color
 * @param borderWidth Width of the border in dp. Defaults to 2dp
 */
@Composable
fun OutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    borderColor: Color = Primary,
    borderWidth: Float = 2f,
    content: @Composable ColumnScope.() -> Unit
) {
    BaseCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        elevation = 0.dp,
        border = BorderStroke(borderWidth.dp, borderColor),
        content = content
    )
}

/**
 * Card variant with colored surface background
 * Use for special status cards (active workout, achievements, etc.)
 * @param surfaceColor Background color for the card
 */
@Composable
fun ColoredCard(
    surfaceColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    BaseCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        backgroundColor = surfaceColor,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        content = content
    )
}

/**
 * Card variant for selected state with primary color border
 * Combines outlined appearance with elevated effect
 */
@Composable
fun SelectedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    BaseCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        elevation = 2.dp,
        border = BorderStroke(2.dp, Primary),
        content = content
    )
}
