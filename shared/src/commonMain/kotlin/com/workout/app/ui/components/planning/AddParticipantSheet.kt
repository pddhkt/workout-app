package com.workout.app.ui.components.planning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.overlays.M3BottomSheet
import com.workout.app.ui.theme.AppTheme

/**
 * Bottom sheet for adding a participant by name.
 * Shows a text input and a list of recent partners.
 */
@Composable
fun AddParticipantSheet(
    visible: Boolean,
    recentPartners: List<String>,
    existingNames: List<String>,
    onAddParticipant: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    M3BottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        var nameInput by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(visible) {
            if (visible) {
                nameInput = ""
                focusRequester.requestFocus()
            }
        }

        Text(
            text = "Add Participant",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(AppTheme.spacing.lg))

        // Name input + add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            BasicTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (nameInput.isNotBlank()) {
                            onAddParticipant(nameInput.trim())
                            nameInput = ""
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(2.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(2.dp)
                            )
                            .padding(
                                horizontal = AppTheme.spacing.lg,
                                vertical = AppTheme.spacing.md
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (nameInput.isEmpty()) {
                                Text(
                                    text = "Type a name...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )

            PrimaryButton(
                text = "Add",
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onAddParticipant(nameInput.trim())
                        nameInput = ""
                    }
                },
                enabled = nameInput.isNotBlank()
            )
        }

        // Recent partners
        val available = recentPartners.filter { recent ->
            existingNames.none { it.equals(recent, ignoreCase = true) }
        }

        if (available.isNotEmpty()) {
            Spacer(Modifier.height(AppTheme.spacing.xl))

            Text(
                text = "Recent Partners",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(AppTheme.spacing.sm))

            available.forEach { name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAddParticipant(name)
                        }
                        .padding(vertical = AppTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(Modifier.height(AppTheme.spacing.xl))
    }
}
