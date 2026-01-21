package com.workout.app.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.Border

/**
 * Navigation item data class.
 *
 * @param label Display label for the navigation item
 * @param selectedIcon Icon shown when item is selected
 * @param unselectedIcon Icon shown when item is not selected
 * @param contentDescription Accessibility description
 */
data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String
)

/**
 * Bottom navigation bar with 4 navigation items.
 * Based on mockup element EL-04.
 *
 * Provides navigation between main app sections: Home, Library, Workout, Profile.
 * Supports selected/unselected states with visual feedback.
 *
 * @param selectedIndex Currently selected item index (0-3)
 * @param onItemSelected Callback invoked when a navigation item is clicked
 * @param modifier Modifier to be applied to the navigation bar
 * @param items Custom navigation items (defaults to standard 4-item layout)
 */
@Composable
fun BottomNavBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavItem> = defaultNavItems()
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .border(
                    width = 1.dp,
                    color = Border,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
                .padding(
                    horizontal = AppTheme.spacing.lg,
                    vertical = AppTheme.spacing.sm
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                NavBarItem(
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    icon = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                    label = item.label,
                    contentDescription = item.contentDescription,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual navigation bar item.
 *
 * @param selected Whether the item is currently selected
 * @param onClick Callback invoked when item is clicked
 * @param icon Icon to display
 * @param label Text label below the icon
 * @param contentDescription Accessibility description
 * @param modifier Modifier to be applied to the item
 */
@Composable
private fun NavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null
            )
            .semantics {
                this.contentDescription = contentDescription
            }
            .padding(vertical = AppTheme.spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Content description on parent
                modifier = Modifier.size(24.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Default navigation items for the app.
 * Home, Library, Workout, Profile sections.
 */
private fun defaultNavItems(): List<NavItem> = listOf(
    NavItem(
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        contentDescription = "Navigate to Home"
    ),
    NavItem(
        label = "Library",
        selectedIcon = Icons.Filled.FavoriteBorder,
        unselectedIcon = Icons.Outlined.FavoriteBorder,
        contentDescription = "Navigate to Library"
    ),
    NavItem(
        label = "Workout",
        selectedIcon = Icons.Filled.FavoriteBorder, // Placeholder - replace with fitness icon
        unselectedIcon = Icons.Outlined.FavoriteBorder,
        contentDescription = "Navigate to Workout"
    ),
    NavItem(
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "Navigate to Profile"
    )
)
