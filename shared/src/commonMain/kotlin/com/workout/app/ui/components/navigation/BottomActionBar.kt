package com.workout.app.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.theme.AppTheme

/**
 * Session summary data for the action bar.
 *
 * @param duration Session duration display text (e.g., "45:30")
 * @param sets Number of completed sets
 * @param exercises Number of exercises completed
 */
data class SessionSummary(
    val duration: String,
    val sets: Int,
    val exercises: Int
)

/**
 * Bottom action bar with primary action button and session summary.
 * Based on mockup element EL-46.
 *
 * Typically used during active workout sessions to show progress
 * and provide quick access to primary actions (finish workout, etc.).
 *
 * @param actionText Text for the primary action button
 * @param onActionClick Callback invoked when action button is clicked
 * @param modifier Modifier to be applied to the action bar
 * @param sessionSummary Optional session summary information to display
 * @param actionEnabled Whether the action button is enabled
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomActionBar(
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    exerciseNames: List<String> = emptyList(),
    sessionSummary: SessionSummary? = null,
    actionEnabled: Boolean = true,
    isLoading: Boolean = false
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onSurface)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(AppTheme.spacing.lg)
        ) {
            // Selected exercises list
            if (exerciseNames.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
                ) {
                    exerciseNames.forEach { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = AppTheme.spacing.sm, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            }

            // Session summary (if provided)
            if (sessionSummary != null) {
                SessionSummaryRow(
                    summary = sessionSummary,
                    modifier = Modifier.padding(bottom = AppTheme.spacing.md)
                )
            }

            // Primary action button
            PrimaryButton(
                text = actionText,
                onClick = onActionClick,
                enabled = actionEnabled,
                fullWidth = true,
                isLoading = isLoading
            )
        }
    }
}

/**
 * Session summary row showing duration, sets, and exercises.
 *
 * @param summary Session summary data
 * @param modifier Modifier to be applied to the row
 */
@Composable
private fun SessionSummaryRow(
    summary: SessionSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SummaryItem(
            label = "Duration",
            value = summary.duration,
            modifier = Modifier.weight(1f)
        )

        VerticalDivider()

        SummaryItem(
            label = "Sets",
            value = summary.sets.toString(),
            modifier = Modifier.weight(1f)
        )

        VerticalDivider()

        SummaryItem(
            label = "Exercises",
            value = summary.exercises.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual summary item with label and value.
 *
 * @param label Summary item label
 * @param value Summary item value
 * @param modifier Modifier to be applied to the item
 */
@Composable
private fun SummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.surface
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Vertical divider for separating summary items.
 */
@Composable
private fun VerticalDivider() {
    Spacer(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
    )
}
