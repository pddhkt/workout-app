package com.workout.app.ui.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview composable demonstrating all button variants.
 * This can be used in Android Studio preview or as a showcase screen.
 */
@Composable
fun ButtonShowcase() {
    WorkoutAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xl)
            ) {
                // Primary Buttons
                ButtonSection(title = "Primary Buttons") {
                    PrimaryButton(
                        text = "Primary Button",
                        onClick = { }
                    )

                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                    PrimaryButton(
                        text = "Full Width Primary",
                        onClick = { },
                        fullWidth = true
                    )

                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                    PrimaryButton(
                        text = "Disabled Primary",
                        onClick = { },
                        enabled = false
                    )
                }

                // Secondary Buttons
                ButtonSection(title = "Secondary Buttons") {
                    SecondaryButton(
                        text = "Secondary Button",
                        onClick = { }
                    )

                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                    SecondaryButton(
                        text = "Full Width Secondary",
                        onClick = { },
                        fullWidth = true
                    )

                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                    SecondaryButton(
                        text = "Disabled Secondary",
                        onClick = { },
                        enabled = false
                    )
                }

                // Icon Buttons
                ButtonSection(title = "Icon Buttons") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                    ) {
                        AppIconButton(
                            icon = Icons.Default.Edit,
                            contentDescription = "Edit",
                            onClick = { }
                        )

                        AppIconButton(
                            icon = Icons.Default.Delete,
                            contentDescription = "Delete",
                            onClick = { },
                            tint = MaterialTheme.colorScheme.error
                        )

                        AppIconButton(
                            icon = Icons.Default.Settings,
                            contentDescription = "Settings",
                            onClick = { },
                            enabled = false
                        )

                        FilledIconButton(
                            icon = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            onClick = { }
                        )
                    }
                }

                // FAB
                ButtonSection(title = "Floating Action Button") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                    ) {
                        AppFloatingActionButton(
                            icon = Icons.Default.Add,
                            contentDescription = "Add",
                            onClick = { }
                        )
                    }
                }

                // Toggle Buttons
                ButtonSection(title = "Toggle Buttons") {
                    var selected1 by remember { mutableStateOf(false) }
                    var selected2 by remember { mutableStateOf(true) }
                    var selected3 by remember { mutableStateOf(false) }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                    ) {
                        ToggleButton(
                            text = "Option 1",
                            selected = selected1,
                            onClick = { selected1 = !selected1 }
                        )

                        ToggleButton(
                            text = "Option 2",
                            selected = selected2,
                            onClick = { selected2 = !selected2 }
                        )

                        ToggleButton(
                            text = "Option 3",
                            selected = selected3,
                            onClick = { selected3 = !selected3 }
                        )
                    }

                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                    ToggleButton(
                        text = "Disabled Toggle",
                        selected = false,
                        onClick = { },
                        enabled = false
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
        content()
    }
}
