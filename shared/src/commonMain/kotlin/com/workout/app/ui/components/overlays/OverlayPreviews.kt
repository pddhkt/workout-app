package com.workout.app.ui.components.overlays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview composable showcasing BottomSheet component variants and usage patterns.
 * Demonstrates:
 * - Basic bottom sheet with simple content
 * - Bottom sheet with action buttons
 * - Bottom sheet with form inputs
 * - Drag-to-dismiss interaction
 */
@Composable
fun BottomSheetShowcase() {
    WorkoutAppTheme {
        var showSimpleSheet by remember { mutableStateOf(false) }
        var showActionSheet by remember { mutableStateOf(false) }
        var showFormSheet by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Text(
                text = "Bottom Sheet Variants",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            // Simple content sheet trigger
            PrimaryButton(
                text = "Show Simple Sheet",
                onClick = { showSimpleSheet = true },
                fullWidth = true
            )

            // Action sheet trigger
            PrimaryButton(
                text = "Show Action Sheet",
                onClick = { showActionSheet = true },
                fullWidth = true
            )

            // Form sheet trigger
            PrimaryButton(
                text = "Show Form Sheet",
                onClick = { showFormSheet = true },
                fullWidth = true
            )
        }

        // Simple bottom sheet
        BottomSheet(
            visible = showSimpleSheet,
            onDismiss = { showSimpleSheet = false }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Text(
                    text = "Simple Bottom Sheet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "This is a basic bottom sheet with simple content. " +
                            "You can drag it down to dismiss or tap the scrim background.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                PrimaryButton(
                    text = "Got it",
                    onClick = { showSimpleSheet = false },
                    fullWidth = true
                )
            }
        }

        // Action sheet
        BottomSheet(
            visible = showActionSheet,
            onDismiss = { showActionSheet = false }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Text(
                    text = "Choose an Action",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Select one of the options below:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                PrimaryButton(
                    text = "Primary Action",
                    onClick = { showActionSheet = false },
                    fullWidth = true
                )

                SecondaryButton(
                    text = "Secondary Action",
                    onClick = { showActionSheet = false },
                    fullWidth = true
                )

                SecondaryButton(
                    text = "Cancel",
                    onClick = { showActionSheet = false },
                    fullWidth = true
                )
            }
        }

        // Form sheet
        BottomSheet(
            visible = showFormSheet,
            onDismiss = { showFormSheet = false }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Text(
                    text = "Add Notes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "This bottom sheet could contain form inputs, " +
                            "text fields, or other interactive content.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

                // Placeholder for form content
                Text(
                    text = "[Form inputs would go here]",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

                PrimaryButton(
                    text = "Save",
                    onClick = { showFormSheet = false },
                    fullWidth = true
                )

                SecondaryButton(
                    text = "Cancel",
                    onClick = { showFormSheet = false },
                    fullWidth = true
                )
            }
        }
    }
}

/**
 * Demo of bottom sheet with custom colors
 */
@Composable
fun CustomStyledBottomSheetDemo() {
    WorkoutAppTheme {
        var showSheet by remember { mutableStateOf(true) }

        if (!showSheet) {
            PrimaryButton(
                text = "Show Custom Sheet",
                onClick = { showSheet = true }
            )
        }

        BottomSheet(
            visible = showSheet,
            onDismiss = { showSheet = false },
            scrimColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            sheetColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Text(
                    text = "Custom Styled Sheet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "This bottom sheet uses custom scrim and background colors.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PrimaryButton(
                    text = "Close",
                    onClick = { showSheet = false },
                    fullWidth = true
                )
            }
        }
    }
}

/**
 * Demo of bottom sheet with adjusted drag threshold
 */
@Composable
fun DragThresholdDemo() {
    WorkoutAppTheme {
        var showSheet by remember { mutableStateOf(true) }

        if (!showSheet) {
            PrimaryButton(
                text = "Show Sensitive Drag Sheet",
                onClick = { showSheet = true }
            )
        }

        BottomSheet(
            visible = showSheet,
            onDismiss = { showSheet = false },
            dragDismissThreshold = 0.15f // More sensitive - dismisses with less drag
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Text(
                    text = "Sensitive Drag Threshold",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "This sheet has a lower drag threshold (15%), " +
                            "so it dismisses more easily when dragged down.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PrimaryButton(
                    text = "Close",
                    onClick = { showSheet = false },
                    fullWidth = true
                )
            }
        }
    }
}
