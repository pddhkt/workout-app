package com.workout.app.ui.components.navigation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

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
 * Layout: Home | Library | [Plan FAB / Active Session Pill] | Template | Profile
 *
 * When an active session exists and is NOT minimized, the center FAB transforms into a pill
 * showing a pulsing green dot, elapsed time, and "Resume" text.
 *
 * When the session IS minimized, the MinimizedWorkoutBar appears above this nav bar instead,
 * so the pill is hidden and the FAB is shown.
 *
 * @param selectedIndex Currently selected item index (0-3)
 * @param onItemSelected Callback invoked when a navigation item is clicked
 * @param onAddClick Callback invoked when the center planning button is clicked
 * @param activeSessionId Active session ID if a workout is in progress, null otherwise
 * @param activeSessionStartTime Start time of the active session in epoch millis
 * @param isSessionMinimized Whether the workout session is in minimized state (bar shown above)
 * @param onResumeSession Callback invoked when the active session pill is tapped
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
    val islandShape = RoundedCornerShape(32.dp)
    val fabSize = 56.dp
    val fabOffset = (-20).dp
    // Show pill only when active session exists AND is NOT minimized
    val hasActiveSession = activeSessionId != null && !isSessionMinimized

    // Pill width when active session is shown
    val pillShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
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

            // Center spacer for FAB/Pill
            val centerWidth = if (hasActiveSession) 140.dp else fabSize + AppTheme.spacing.md
            Spacer(modifier = Modifier.width(centerWidth))

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

        if (hasActiveSession && activeSessionStartTime != null) {
            // Active Session Pill
            ActiveSessionPill(
                startTime = activeSessionStartTime,
                onClick = onResumeSession,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = fabOffset)
            )
        } else {
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
}

/**
 * Active session pill showing pulsing dot, elapsed time, and "Resume" text.
 */
@Composable
private fun ActiveSessionPill(
    startTime: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(28.dp)

    // Elapsed time counter
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(startTime) {
        while (true) {
            elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - startTime) / 1000).toInt()
            delay(1000)
        }
    }

    // Pulsing animation for the dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val timeText = formatElapsedTime(elapsedSeconds)

    Box(
        modifier = modifier
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = pillShape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(pillShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = AppTheme.spacing.lg)
            .semantics {
                contentDescription = "Resume active workout, $timeText elapsed"
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            // Pulsing green dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { alpha = pulseAlpha }
                    .clip(CircleShape)
                    .background(AppTheme.colors.success)
            )

            // Elapsed time
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )

            // Resume text
            Text(
                text = "Resume",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Format elapsed seconds to MM:SS display.
 */
private fun formatElapsedTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
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
