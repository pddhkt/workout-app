package com.workout.app.ui.components.inputs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme

/**
 * Multi-line text field for notes
 * Element: EL-97
 */
@Composable
fun NotesInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Add notes...",
    minLines: Int = 4,
    maxLines: Int = 8,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    maxCharacters: Int? = null
) {
    Column(modifier = modifier) {
        // Label with character count
        if (label != null || maxCharacters != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppTheme.spacing.xs)
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (maxCharacters != null) {
                    Text(
                        text = "${value.length}/$maxCharacters",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (value.length > maxCharacters) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterEnd)
                    )
                }
            }
        }

        // Multi-line text field
        val scrollState = rememberScrollState()

        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                if (maxCharacters == null || newValue.length <= maxCharacters) {
                    onValueChange(newValue)
                }
            },
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = (minLines * 20).dp,
                    max = (maxLines * 20).dp
                ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = when {
                                isError -> MaterialTheme.colorScheme.error
                                maxCharacters != null && value.length > maxCharacters -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(AppTheme.spacing.md)
                        .verticalScroll(scrollState)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = AppTheme.spacing.xs)
            )
        }
    }
}
