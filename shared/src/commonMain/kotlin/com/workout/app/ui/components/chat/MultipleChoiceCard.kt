package com.workout.app.ui.components.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.ChatOption
import com.workout.app.ui.theme.AppTheme

/**
 * Card displaying multiple-choice options for the user to select.
 * Each option is a tappable surface. Once an option is selected,
 * all options become disabled and the selected one is highlighted.
 *
 * @param options List of selectable options
 * @param selectedOptionId ID of the currently selected option, or null if none selected
 * @param onOptionSelected Callback invoked with (optionId, optionLabel) when an option is tapped
 * @param modifier Optional modifier
 */
@Composable
fun MultipleChoiceCard(
    options: List<ChatOption>,
    selectedOptionId: String?,
    onOptionSelected: (optionId: String, optionLabel: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDisabled = selectedOptionId != null

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        options.forEach { option ->
            val isSelected = option.id == selectedOptionId

            OptionChip(
                label = option.label,
                isSelected = isSelected,
                isDisabled = isDisabled,
                onClick = { onOptionSelected(option.id, option.label) }
            )
        }
    }
}

/**
 * Individual option chip within a multiple-choice card.
 *
 * @param label Display text for the option
 * @param isSelected Whether this option is currently selected
 * @param isDisabled Whether interaction is disabled (after a selection has been made)
 * @param onClick Callback when the chip is tapped
 * @param modifier Optional modifier
 */
@Composable
private fun OptionChip(
    label: String,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = !isDisabled,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier.padding(
                horizontal = AppTheme.spacing.md,
                vertical = AppTheme.spacing.sm
            )
        )
    }
}
