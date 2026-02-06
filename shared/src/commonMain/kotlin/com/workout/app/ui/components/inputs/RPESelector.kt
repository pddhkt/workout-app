package com.workout.app.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.ExtendedColors

/**
 * RPE (Rate of Perceived Exertion) selector for 1-10 scale
 * Commonly used in workout tracking to measure effort level
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RPESelector(
    selectedRPE: Int?,
    onRPESelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = "Rate of Perceived Exertion (RPE)",
    enabled: Boolean = true,
    showDescription: Boolean = true
) {
    Column(modifier = modifier) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
            )
        }

        // RPE scale description
        if (showDescription && selectedRPE != null) {
            Text(
                text = getRPEDescription(selectedRPE),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
            )
        }

        // RPE buttons in a grid
        FlowRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            maxItemsInEachRow = 5
        ) {
            for (rpe in 1..10) {
                RPEButton(
                    rpe = rpe,
                    isSelected = selectedRPE == rpe,
                    onClick = { onRPESelected(rpe) },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun RPEButton(
    rpe: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val colors = AppTheme.colors
    val rpeColor = getRPEColor(rpe, colors)
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val backgroundColor = when {
        !enabled -> surfaceVariant.copy(alpha = 0.5f)
        isSelected -> rpeColor
        else -> surfaceVariant
    }

    val textColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
        isSelected -> Color.Black
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) rpeColor else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(2.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics { contentDescription = "RPE $rpe: ${getRPEDescription(rpe)}" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rpe.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Compact RPE selector as a horizontal slider-like component
 */
@Composable
fun CompactRPESelector(
    selectedRPE: Int?,
    onRPESelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = "RPE",
    enabled: Boolean = true
) {
    val colors = AppTheme.colors

    Column(modifier = modifier) {
        // Label with selected value
        Row(
            modifier = Modifier.padding(bottom = AppTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (selectedRPE != null) {
                val rpeColor = getRPEColor(selectedRPE, colors)
                Text(
                    text = selectedRPE.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = rpeColor,
                    modifier = Modifier
                        .background(
                            color = rpeColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.xs)
                )
            }
        }

        // Compact scale
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(AppTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) {
            for (rpe in 1..10) {
                CompactRPEButton(
                    rpe = rpe,
                    isSelected = selectedRPE == rpe,
                    onClick = { onRPESelected(rpe) },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun CompactRPEButton(
    rpe: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val colors = AppTheme.colors
    val rpeColor = getRPEColor(rpe, colors)
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val backgroundColor = when {
        !enabled -> surfaceVariant.copy(alpha = 0.5f)
        isSelected -> rpeColor
        else -> surfaceVariant
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics { contentDescription = "RPE $rpe" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rpe.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * RPE Progress Display component showing a horizontal progress bar with value label.
 * Provides a slider-like interface for selecting or displaying RPE values.
 *
 * @param rpe The current RPE value (1-10)
 * @param onRpeChange Callback when RPE value changes (optional for read-only mode)
 * @param modifier Optional modifier
 * @param label Optional label text displayed above the progress bar
 * @param interactive Whether the component allows user interaction (default true)
 */
@Composable
fun RPEProgressDisplay(
    rpe: Int,
    onRpeChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    label: String? = "How hard was this session?",
    interactive: Boolean = true
) {
    val colors = AppTheme.colors
    val rpeColor = getRPEColor(rpe, colors)
    var trackWidth by remember { mutableStateOf(0f) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
        }

        // Progress bar with value
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress track
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .onSizeChanged { trackWidth = it.width.toFloat() }
                    .then(
                        if (interactive && onRpeChange != null) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val newRpe = ((offset.x / trackWidth) * 10)
                                        .toInt()
                                        .coerceIn(1, 10)
                                    onRpeChange(newRpe)
                                }
                            }.pointerInput(Unit) {
                                detectHorizontalDragGestures { change, _ ->
                                    val newRpe = ((change.position.x / trackWidth) * 10)
                                        .toInt()
                                        .coerceIn(1, 10)
                                    onRpeChange(newRpe)
                                }
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(rpe / 10f)
                        .fillMaxHeight()
                        .background(rpeColor)
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.md))

            // Value label
            Text(
                text = "$rpe/10",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = rpeColor
            )
        }

        // Optional description
        if (rpe > 0) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

            Text(
                text = getRPEDescription(rpe),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Get color based on RPE value using theme-aware semantic colors.
 * 1-3: Easy (Info)
 * 4-6: Moderate (Success)
 * 7-8: Hard (Warning)
 * 9-10: Maximum (Error)
 *
 * @param rpe The RPE value (1-10)
 * @param colors The ExtendedColors from the current theme
 */
private fun getRPEColor(rpe: Int, colors: ExtendedColors): Color {
    return when (rpe) {
        in 1..3 -> colors.info
        in 4..6 -> colors.success
        in 7..8 -> colors.warning
        else -> colors.error
    }
}

/**
 * Get description based on RPE value
 */
private fun getRPEDescription(rpe: Int): String {
    return when (rpe) {
        1 -> "Very Easy - Minimal effort"
        2 -> "Easy - Light effort"
        3 -> "Easy - Could do this all day"
        4 -> "Moderate - Comfortable pace"
        5 -> "Moderate - Still manageable"
        6 -> "Moderate - Getting challenging"
        7 -> "Hard - Difficult but sustainable"
        8 -> "Hard - Very difficult"
        9 -> "Maximum - Extremely hard"
        10 -> "Maximum - Absolute limit"
        else -> ""
    }
}
