package com.workout.app.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme

/**
 * Number stepper for weight/reps with +/- buttons
 * Element: EL-13
 */
@Composable
fun NumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    minValue: Int = 0,
    maxValue: Int = Int.MAX_VALUE,
    step: Int = 1,
    unit: String = "",
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = AppTheme.spacing.xs)
            )
        }

        // Stepper controls
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(AppTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minus button
            StepperButton(
                onClick = {
                    val newValue = (value - step).coerceAtLeast(minValue)
                    if (newValue != value) onValueChange(newValue)
                },
                enabled = enabled && value > minValue,
                contentDescription = "Decrease $label"
            ) {
                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled && value > minValue)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Value display
            Text(
                text = if (unit.isNotEmpty()) "$value $unit" else value.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.sm)
            )

            // Plus button
            StepperButton(
                onClick = {
                    val newValue = (value + step).coerceAtMost(maxValue)
                    if (newValue != value) onValueChange(newValue)
                },
                enabled = enabled && value < maxValue,
                contentDescription = "Increase $label"
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled && value < maxValue)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepperButton(
    onClick: () -> Unit,
    enabled: Boolean,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (enabled) surfaceVariant else surfaceVariant.copy(alpha = 0.5f))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Decimal number stepper for weight with decimal precision
 */
@Composable
fun DecimalNumberStepper(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    minValue: Float = 0f,
    maxValue: Float = Float.MAX_VALUE,
    step: Float = 0.5f,
    unit: String = "",
    decimalPlaces: Int = 1,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = AppTheme.spacing.xs)
            )
        }

        // Stepper controls
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(AppTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minus button
            StepperButton(
                onClick = {
                    val newValue = (value - step).coerceAtLeast(minValue)
                    if (newValue != value) onValueChange(newValue)
                },
                enabled = enabled && value > minValue,
                contentDescription = "Decrease $label"
            ) {
                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled && value > minValue)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Value display
            val multiplier = when (decimalPlaces) {
                0 -> 1
                1 -> 10
                2 -> 100
                else -> 1000
            }
            val rounded = (value * multiplier).toInt().toFloat() / multiplier
            val formattedValue = if (decimalPlaces == 0) {
                rounded.toInt().toString()
            } else {
                rounded.toString()
            }
            Text(
                text = if (unit.isNotEmpty()) "$formattedValue $unit" else formattedValue,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.sm)
            )

            // Plus button
            StepperButton(
                onClick = {
                    val newValue = (value + step).coerceAtMost(maxValue)
                    if (newValue != value) onValueChange(newValue)
                },
                enabled = enabled && value < maxValue,
                contentDescription = "Increase $label"
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled && value < maxValue)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
