package com.workout.app.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.workout.app.ui.components.chips.BadgeVariant
import com.workout.app.ui.components.headers.SectionHeader
import com.workout.app.ui.components.headers.SectionHeaderWithAction
import com.workout.app.ui.components.headers.SectionHeaderWithCount
import com.workout.app.ui.components.headers.SectionHeaderWithCountAndAction
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview showcase for all section header variants
 */
@Preview(name = "All Section Headers - Dark Theme", showBackground = true)
@Composable
private fun AllSectionHeadersPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xl)
        ) {
            // Simple Section Header
            PreviewSection(title = "Simple Header") {
                SectionHeader(
                    title = "Recent Workouts"
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Section Header with Count Badge
            PreviewSection(title = "Header with Count Badge") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    SectionHeaderWithCount(
                        title = "Active Programs",
                        count = 3,
                        badgeVariant = BadgeVariant.SUCCESS
                    )

                    SectionHeaderWithCount(
                        title = "New Exercises",
                        count = 12,
                        badgeVariant = BadgeVariant.INFO
                    )

                    SectionHeaderWithCount(
                        title = "Notifications",
                        count = 5,
                        badgeVariant = BadgeVariant.ERROR
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Section Header with Action
            PreviewSection(title = "Header with Action") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    SectionHeaderWithAction(
                        title = "Exercise Library",
                        onActionClick = {},
                        modifier = Modifier.fillMaxWidth()
                    )

                    SectionHeaderWithAction(
                        title = "Training History",
                        actionText = "See All",
                        onActionClick = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Section Header with Count and Action
            PreviewSection(title = "Header with Count & Action") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    SectionHeaderWithCountAndAction(
                        title = "My Workouts",
                        count = 8,
                        onActionClick = {},
                        badgeVariant = BadgeVariant.SUCCESS,
                        modifier = Modifier.fillMaxWidth()
                    )

                    SectionHeaderWithCountAndAction(
                        title = "Saved Templates",
                        count = 15,
                        actionText = "Browse All",
                        onActionClick = {},
                        badgeVariant = BadgeVariant.NEUTRAL,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Preview for simple SectionHeader
 */
@Preview(name = "Simple Section Header", showBackground = true)
@Composable
private fun SectionHeaderPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.spacing.lg)
        ) {
            SectionHeader(title = "Recent Workouts")
        }
    }
}

/**
 * Preview for SectionHeaderWithCount
 */
@Preview(name = "Section Header with Count", showBackground = true)
@Composable
private fun SectionHeaderWithCountPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            SectionHeaderWithCount(
                title = "Active Programs",
                count = 3,
                badgeVariant = BadgeVariant.SUCCESS
            )

            SectionHeaderWithCount(
                title = "Notifications",
                count = 99,
                badgeVariant = BadgeVariant.ERROR
            )
        }
    }
}

/**
 * Preview for SectionHeaderWithAction
 */
@Preview(name = "Section Header with Action", showBackground = true)
@Composable
private fun SectionHeaderWithActionPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            SectionHeaderWithAction(
                title = "Exercise Library",
                onActionClick = {},
                modifier = Modifier.fillMaxWidth()
            )

            SectionHeaderWithAction(
                title = "Training History",
                actionText = "See All",
                onActionClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preview for SectionHeaderWithCountAndAction
 */
@Preview(name = "Section Header with Count & Action", showBackground = true)
@Composable
private fun SectionHeaderWithCountAndActionPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            SectionHeaderWithCountAndAction(
                title = "My Workouts",
                count = 8,
                onActionClick = {},
                badgeVariant = BadgeVariant.SUCCESS,
                modifier = Modifier.fillMaxWidth()
            )

            SectionHeaderWithCountAndAction(
                title = "Saved Templates",
                count = 100,
                actionText = "Browse All",
                onActionClick = {},
                badgeVariant = BadgeVariant.NEUTRAL,
                modifier = Modifier.fillMaxWidth()
            )
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
