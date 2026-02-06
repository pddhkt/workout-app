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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Bottom navigation bar with 4 navigation items and a center workout button.
 * Layout: Home | Library | [Workout +] | Template | Profile
 *
 * Flat design with icons and labels below each item.
 * The center Workout button has a highlighted blue circle with + icon.
 *
 * @param selectedIndex Currently selected item index (0-3, excluding center)
 * @param onItemSelected Callback invoked when a navigation item is clicked
 * @param onAddClick Callback invoked when the center workout button is clicked
 * @param activeSessionId Active session ID if a workout is in progress, null otherwise
 * @param activeSessionStartTime Start time of the active session in epoch millis
 * @param isSessionMinimized Whether the workout session is in minimized state (bar shown above)
 * @param onResumeSession Callback invoked when the active session is resumed
 * @param modifier Modifier to be applied to the navigation bar
 * @param items Custom navigation items (defaults to standard 4-item layout)
 */
@Composable
fun BottomNavBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    activeSessionId: String? = null,
    activeSessionStartTime: Long? = null,
    isSessionMinimized: Boolean = false,
    onResumeSession: () -> Unit = {},
    modifier: Modifier = Modifier,
    items: List<NavItem> = defaultNavItems()
) {
    val fabSize = 48.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Left side: First 2 items (Home, Library)
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

        // Center: Workout button with + icon
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(fabSize)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onAddClick
                        )
                        .semantics {
                            contentDescription = "Start workout"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Workout",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }

        // Right side: Last 2 items (Template, Profile)
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
}

/**
 * Individual navigation bar item with icon and label.
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
    val color = if (selected) {
        Color.White
    } else {
        Color.White.copy(alpha = 0.6f)
    }

    androidx.compose.foundation.layout.Column(
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
            .padding(vertical = AppTheme.spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
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
