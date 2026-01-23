package com.workout.app.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme

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
 * Bottom navigation bar with 4 navigation items and a center floating action button.
 * Layout: Home | Library | [Plan FAB] | Template | Profile
 *
 * @param selectedIndex Currently selected item index (0-3)
 * @param onItemSelected Callback invoked when a navigation item is clicked
 * @param onAddClick Callback invoked when the center planning button is clicked to start a planning session
 * @param modifier Modifier to be applied to the navigation bar
 * @param items Custom navigation items (defaults to standard 4-item layout)
 */
@Composable
fun BottomNavBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavItem> = defaultNavItems()
) {
    val islandShape = RoundedCornerShape(32.dp)
    val fabSize = 56.dp
    val fabOffset = (-20).dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = AppTheme.spacing.lg, end = AppTheme.spacing.lg, bottom = AppTheme.spacing.md)
    ) {
        // Navigation bar island
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = islandShape,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(islandShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(
                    horizontal = AppTheme.spacing.lg,
                    vertical = AppTheme.spacing.sm
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Home, Library
            items.take(2).forEachIndexed { index, item ->
                NavBarItem(
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    icon = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                    label = item.label,
                    contentDescription = item.contentDescription,
                    modifier = Modifier.weight(1f)
                )
            }

            // Center spacer for FAB
            Spacer(modifier = Modifier.width(fabSize + AppTheme.spacing.md))

            // Right side: Template, Profile
            items.drop(2).forEachIndexed { index, item ->
                val actualIndex = index + 2
                NavBarItem(
                    selected = selectedIndex == actualIndex,
                    onClick = { onItemSelected(actualIndex) },
                    icon = if (selectedIndex == actualIndex) item.selectedIcon else item.unselectedIcon,
                    label = item.label,
                    contentDescription = item.contentDescription,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Floating Add Button (centered, raised)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = fabOffset)
                .size(fabSize)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAddClick
                )
                .semantics {
                    contentDescription = "Start planning session"
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
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
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * Default navigation items for the app.
 * Home, Library, Template, Profile sections.
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
        selectedIcon = Icons.AutoMirrored.Filled.List,
        unselectedIcon = Icons.AutoMirrored.Outlined.List,
        contentDescription = "Navigate to Library"
    ),
    NavItem(
        label = "Template",
        selectedIcon = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description,
        contentDescription = "Navigate to Template"
    ),
    NavItem(
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "Navigate to Profile"
    )
)
