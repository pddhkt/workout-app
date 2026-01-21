package com.workout.app.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.chips.Badge
import com.workout.app.ui.components.chips.BadgeVariant
import com.workout.app.ui.components.chips.CountBadge
import com.workout.app.ui.components.chips.FilterChip
import com.workout.app.ui.components.chips.ProgressDots
import com.workout.app.ui.components.chips.ProgressDotsWithActiveIndicator
import com.workout.app.ui.components.chips.SetChip
import com.workout.app.ui.components.chips.SetState
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview showcase for all chip and badge components
 */
@Preview(name = "All Chips - Dark Theme", showBackground = true)
@Composable
private fun AllChipsPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xl)
        ) {
            // Filter Chips Section
            PreviewSection(title = "Filter Chips") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        text = "All",
                        isActive = true,
                        onClick = {}
                    )
                    FilterChip(
                        text = "Strength",
                        isActive = false,
                        onClick = {}
                    )
                    FilterChip(
                        text = "Cardio",
                        isActive = false,
                        onClick = {}
                    )
                }
            }

            // Set Chips Section
            PreviewSection(title = "Set Chips") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SetChip(
                        setNumber = 1,
                        state = SetState.COMPLETED,
                        onClick = {}
                    )
                    SetChip(
                        setNumber = 2,
                        state = SetState.ACTIVE,
                        onClick = {}
                    )
                    SetChip(
                        setNumber = 3,
                        state = SetState.PENDING,
                        onClick = {}
                    )
                }
            }

            // Badges Section
            PreviewSection(title = "Badges") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Badge(text = "Success", variant = BadgeVariant.SUCCESS, showDot = true)
                        Badge(text = "Warning", variant = BadgeVariant.WARNING, showDot = true)
                        Badge(text = "Error", variant = BadgeVariant.ERROR, showDot = true)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Badge(text = "Info", variant = BadgeVariant.INFO, showDot = false)
                        Badge(text = "Neutral", variant = BadgeVariant.NEUTRAL, showDot = false)
                    }
                }
            }

            // Count Badges Section
            PreviewSection(title = "Count Badges") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CountBadge(count = 3, variant = BadgeVariant.ERROR)
                    CountBadge(count = 12, variant = BadgeVariant.SUCCESS)
                    CountBadge(count = 99, variant = BadgeVariant.WARNING)
                    CountBadge(count = 100, variant = BadgeVariant.INFO)
                }
            }

            // Progress Dots Section
            PreviewSection(title = "Progress Dots") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    ProgressDots(total = 5, current = 2)
                    ProgressDotsWithActiveIndicator(total = 5, current = 2)
                }
            }
        }
    }
}

/**
 * Preview for FilterChip states
 */
@Preview(name = "Filter Chips", showBackground = true)
@Composable
private fun FilterChipPreview() {
    WorkoutAppTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            FilterChip(
                text = "Active",
                isActive = true,
                onClick = {}
            )
            FilterChip(
                text = "Inactive",
                isActive = false,
                onClick = {}
            )
        }
    }
}

/**
 * Preview for SetChip states
 */
@Preview(name = "Set Chips", showBackground = true)
@Composable
private fun SetChipPreview() {
    WorkoutAppTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            SetChip(setNumber = 1, state = SetState.COMPLETED)
            SetChip(setNumber = 2, state = SetState.ACTIVE)
            SetChip(setNumber = 3, state = SetState.PENDING)
        }
    }
}

/**
 * Preview for Badge variants
 */
@Preview(name = "Badges", showBackground = true)
@Composable
private fun BadgePreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                Badge(text = "Success", variant = BadgeVariant.SUCCESS, showDot = true)
                Badge(text = "Warning", variant = BadgeVariant.WARNING, showDot = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                Badge(text = "Error", variant = BadgeVariant.ERROR, showDot = false)
                Badge(text = "Info", variant = BadgeVariant.INFO, showDot = false)
            }
        }
    }
}

/**
 * Preview for CountBadge variants
 */
@Preview(name = "Count Badges", showBackground = true)
@Composable
private fun CountBadgePreview() {
    WorkoutAppTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            CountBadge(count = 1)
            CountBadge(count = 12)
            CountBadge(count = 99)
            CountBadge(count = 100)
        }
    }
}

/**
 * Preview for ProgressDots variants
 */
@Preview(name = "Progress Dots", showBackground = true)
@Composable
private fun ProgressDotsPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
        ) {
            // Standard progress dots
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                Text("Standard", style = MaterialTheme.typography.labelMedium)
                ProgressDots(total = 5, current = 0)
                ProgressDots(total = 5, current = 2)
                ProgressDots(total = 5, current = 4)
            }

            // Active indicator variant
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                Text("With Active Indicator", style = MaterialTheme.typography.labelMedium)
                ProgressDotsWithActiveIndicator(total = 5, current = 0)
                ProgressDotsWithActiveIndicator(total = 5, current = 2)
                ProgressDotsWithActiveIndicator(total = 5, current = 4)
            }
        }
    }
}

@Composable
private fun PreviewSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}
