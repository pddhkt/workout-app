package com.workout.app.ui.components.headers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.chips.CountBadge
import com.workout.app.ui.components.chips.BadgeVariant
import com.workout.app.ui.theme.AppTheme

/**
 * Section header component with multiple variants.
 * Used to organize content sections with optional badges and actions.
 * Based on mockup elements EL-28, EL-44, EL-50, EL-78.
 *
 * Variants:
 * - Simple: Title only
 * - With Count Badge: Title with count indicator
 * - With Action: Title with "View All" or custom trailing action
 *
 * @param title The main heading text for the section
 * @param modifier Optional modifier for customization
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

/**
 * Section header with a count badge.
 * Displays a numeric count indicator next to the title.
 *
 * @param title The main heading text for the section
 * @param count The numeric count to display in the badge
 * @param badgeVariant Visual style of the count badge
 * @param modifier Optional modifier for customization
 */
@Composable
fun SectionHeaderWithCount(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    badgeVariant: BadgeVariant = BadgeVariant.NEUTRAL
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        CountBadge(
            count = count,
            variant = badgeVariant
        )
    }
}

/**
 * Section header with a trailing action button.
 * Displays "View All" or custom action text that can be clicked.
 *
 * @param title The main heading text for the section
 * @param actionText Text for the trailing action button (default: "View All")
 * @param onActionClick Callback invoked when action button is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun SectionHeaderWithAction(
    title: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String = "View All"
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Button(
            onClick = onActionClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(2.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Section header with both count badge and action button.
 * Combines count indicator and trailing action in one component.
 *
 * @param title The main heading text for the section
 * @param count The numeric count to display in the badge
 * @param onActionClick Callback invoked when action button is clicked
 * @param modifier Optional modifier for customization
 * @param actionText Text for the trailing action button (default: "View All")
 * @param badgeVariant Visual style of the count badge
 */
@Composable
fun SectionHeaderWithCountAndAction(
    title: String,
    count: Int,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String = "View All",
    badgeVariant: BadgeVariant = BadgeVariant.NEUTRAL
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            CountBadge(
                count = count,
                variant = badgeVariant
            )
        }

        Button(
            onClick = onActionClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(2.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
