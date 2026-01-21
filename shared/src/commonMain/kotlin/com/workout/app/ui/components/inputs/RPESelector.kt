package com.workout.app.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.Border
import com.workout.app.ui.theme.Error
import com.workout.app.ui.theme.Info
import com.workout.app.ui.theme.OnSurfaceVariant
import com.workout.app.ui.theme.Success
import com.workout.app.ui.theme.SurfaceVariant
import com.workout.app.ui.theme.Warning

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
                color = OnSurfaceVariant,
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
    val backgroundColor = when {
        !enabled -> SurfaceVariant.copy(alpha = 0.5f)
        isSelected -> getRPEColor(rpe)
        else -> SurfaceVariant
    }

    val textColor = when {
        !enabled -> OnSurfaceVariant
        isSelected -> Color.Black
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) getRPEColor(rpe) else Border,
                shape = RoundedCornerShape(8.dp)
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
                Text(
                    text = selectedRPE.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getRPEColor(selectedRPE),
                    modifier = Modifier
                        .background(
                            color = getRPEColor(selectedRPE).copy(alpha = 0.2f),
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
                    color = Border,
                    shape = RoundedCornerShape(8.dp)
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
    val backgroundColor = when {
        !enabled -> SurfaceVariant.copy(alpha = 0.5f)
        isSelected -> getRPEColor(rpe)
        else -> SurfaceVariant
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
            color = if (isSelected) Color.Black else OnSurfaceVariant
        )
    }
}

/**
 * Get color based on RPE value
 * 1-3: Easy (Info)
 * 4-6: Moderate (Success)
 * 7-8: Hard (Warning)
 * 9-10: Maximum (Error)
 */
private fun getRPEColor(rpe: Int): Color {
    return when (rpe) {
        in 1..3 -> Info
        in 4..6 -> Success
        in 7..8 -> Warning
        else -> Error
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
