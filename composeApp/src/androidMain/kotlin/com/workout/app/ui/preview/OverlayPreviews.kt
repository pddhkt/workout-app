package com.workout.app.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.components.overlays.BottomSheet
import com.workout.app.ui.components.overlays.BottomSheetShowcase
import com.workout.app.ui.components.overlays.CustomStyledBottomSheetDemo
import com.workout.app.ui.components.overlays.DragThresholdDemo
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview: Basic bottom sheet with simple content
 */
@Preview(name = "Simple Bottom Sheet", showBackground = true)
@Composable
private fun SimpleBottomSheetPreview() {
    WorkoutAppTheme {
        var showSheet by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!showSheet) {
                PrimaryButton(
                    text = "Show Bottom Sheet",
                    onClick = { showSheet = true }
                )
            }

            BottomSheet(
                visible = showSheet,
                onDismiss = { showSheet = false }
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
                        onClick = { showSheet = false },
                        fullWidth = true
                    )
                }
            }
        }
    }
}

/**
 * Preview: Bottom sheet with multiple action buttons
 */
@Preview(name = "Action Sheet", showBackground = true)
@Composable
private fun ActionSheetPreview() {
    WorkoutAppTheme {
        var showSheet by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!showSheet) {
                PrimaryButton(
                    text = "Show Action Sheet",
                    onClick = { showSheet = true }
                )
            }

            BottomSheet(
                visible = showSheet,
                onDismiss = { showSheet = false }
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
                        onClick = { showSheet = false },
                        fullWidth = true
                    )

                    SecondaryButton(
                        text = "Secondary Action",
                        onClick = { showSheet = false },
                        fullWidth = true
                    )

                    SecondaryButton(
                        text = "Cancel",
                        onClick = { showSheet = false },
                        fullWidth = true
                    )
                }
            }
        }
    }
}

/**
 * Preview: Bottom sheet with form-like content
 */
@Preview(name = "Form Bottom Sheet", showBackground = true)
@Composable
private fun FormBottomSheetPreview() {
    WorkoutAppTheme {
        var showSheet by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!showSheet) {
                PrimaryButton(
                    text = "Show Form Sheet",
                    onClick = { showSheet = true }
                )
            }

            BottomSheet(
                visible = showSheet,
                onDismiss = { showSheet = false }
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
                        onClick = { showSheet = false },
                        fullWidth = true
                    )

                    SecondaryButton(
                        text = "Cancel",
                        onClick = { showSheet = false },
                        fullWidth = true
                    )
                }
            }
        }
    }
}

/**
 * Preview: Bottom sheet with custom styling
 */
@Preview(name = "Custom Styled Sheet", showBackground = true)
@Composable
private fun CustomStyledSheetPreview() {
    CustomStyledBottomSheetDemo()
}

/**
 * Preview: Bottom sheet with sensitive drag threshold
 */
@Preview(name = "Sensitive Drag Threshold", showBackground = true)
@Composable
private fun DragThresholdPreview() {
    DragThresholdDemo()
}

/**
 * Preview: All bottom sheet variants showcase
 */
@Preview(name = "All Bottom Sheets", showBackground = true, heightDp = 800)
@Composable
private fun AllBottomSheetsPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BottomSheetShowcase()
        }
    }
}
