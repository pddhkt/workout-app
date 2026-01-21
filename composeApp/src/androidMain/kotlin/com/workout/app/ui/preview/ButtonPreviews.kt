package com.workout.app.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.AppFloatingActionButton
import com.workout.app.ui.components.buttons.AppIconButton
import com.workout.app.ui.components.buttons.FilledIconButton
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.components.buttons.ToggleButton
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Android Studio preview for PrimaryButton variants.
 */
@Preview(name = "Primary Buttons", showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    WorkoutAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryButton(
                    text = "Primary Button",
                    onClick = { }
                )

                PrimaryButton(
                    text = "Full Width Primary",
                    onClick = { },
                    fullWidth = true
                )

                PrimaryButton(
                    text = "Disabled Primary",
                    onClick = { },
                    enabled = false
                )
            }
        }
    }
}

/**
 * Android Studio preview for SecondaryButton variants.
 */
@Preview(name = "Secondary Buttons", showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    WorkoutAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryButton(
                    text = "Secondary Button",
                    onClick = { }
                )

                SecondaryButton(
                    text = "Full Width Secondary",
                    onClick = { },
                    fullWidth = true
                )

                SecondaryButton(
                    text = "Disabled Secondary",
                    onClick = { },
                    enabled = false
                )
            }
        }
    }
}

/**
 * Android Studio preview for IconButton variants.
 */
@Preview(name = "Icon Buttons", showBackground = true)
@Composable
private fun IconButtonPreview() {
    WorkoutAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Standard Icon Buttons",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                }

                Text(
                    text = "Filled Icon Buttons",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(
                        icon = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        onClick = { }
                    )

                    FilledIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Add",
                        onClick = { },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )

                    FilledIconButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete",
                        onClick = { },
                        enabled = false
                    )
                }
            }
        }
    }
}

/**
 * Android Studio preview for FAB.
 */
@Preview(name = "Floating Action Button", showBackground = true)
@Composable
private fun FABPreview() {
    WorkoutAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppFloatingActionButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Add",
                    onClick = { }
                )
            }
        }
    }
}

/**
 * Android Studio preview for ToggleButton variants.
 */
@Preview(name = "Toggle Buttons", showBackground = true)
@Composable
private fun ToggleButtonPreview() {
    WorkoutAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Interactive Toggles",
                    style = MaterialTheme.typography.labelLarge
                )

                var selected1 by remember { mutableStateOf(false) }
                var selected2 by remember { mutableStateOf(true) }
                var selected3 by remember { mutableStateOf(false) }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                Text(
                    text = "Disabled State",
                    style = MaterialTheme.typography.labelLarge
                )

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

/**
 * Combined preview showing all button types.
 */
@Preview(name = "All Buttons", showBackground = true)
@Composable
private fun AllButtonsPreview() {
    WorkoutAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
            ) {
                // Primary
                PrimaryButton(
                    text = "Primary",
                    onClick = { }
                )

                // Secondary
                SecondaryButton(
                    text = "Secondary",
                    onClick = { }
                )

                // Icon buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    AppIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit",
                        onClick = { }
                    )

                    FilledIconButton(
                        icon = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        onClick = { }
                    )

                    AppFloatingActionButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Add",
                        onClick = { }
                    )
                }

                // Toggle
                var selected by remember { mutableStateOf(true) }
                ToggleButton(
                    text = "Toggle",
                    selected = selected,
                    onClick = { selected = !selected }
                )
            }
        }
    }
}
