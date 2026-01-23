package com.workout.app.ui.screens.landing

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.navigation.BottomNavBar
import com.workout.app.ui.theme.AccentGreen
import com.workout.app.ui.theme.AccentOrange
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.CardCardio
import com.workout.app.ui.theme.CardCardioDark
import com.workout.app.ui.theme.CardYoga
import com.workout.app.ui.theme.CardYogaDark
import com.workout.app.ui.theme.Charcoal
import com.workout.app.ui.theme.Primary
import com.workout.app.ui.theme.Secondary
import com.workout.app.ui.theme.Tertiary

/**
 * Data class representing a workout category/type.
 */
data class WorkoutCategory(
    val id: String,
    val name: String,
    val description: String,
    val duration: String,
    val calories: String,
    val difficulty: WorkoutDifficulty,
    val isFeatured: Boolean = false
)

enum class WorkoutDifficulty(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    HIGH_INTENSITY("High Intensity")
}

/**
 * Landing Screen - Workout Selection
 *
 * Displays available workout categories with a featured card and compact cards.
 * Based on the "Choose your flavor" mockup design.
 *
 * Design tokens used:
 * - Colors: Primary (yellow), Secondary/Tertiary (gradients), AccentGreen, AccentOrange
 * - Typography: headlineLarge, headlineMedium, bodyMedium, labelSmall
 * - Spacing: lg, xl, xxl
 * - Shadows: warm shadow with Primary color tint
 *
 * @param onWorkoutSelect Callback when a workout is selected
 * @param onQuickStart Callback for quick start FAB
 * @param onNavigate Callback for bottom navigation
 * @param onAddClick Callback for the center FAB in bottom nav
 * @param modifier Optional modifier
 */
@Composable
fun LandingScreen(
    onWorkoutSelect: (String) -> Unit = {},
    onQuickStart: () -> Unit = {},
    onNavigate: (Int) -> Unit = {},
    onAddClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    val workouts = remember {
        listOf(
            WorkoutCategory(
                id = "strength",
                name = "Strength Training",
                description = "Build muscle and power",
                duration = "20 mins",
                calories = "150 kcal",
                difficulty = WorkoutDifficulty.INTERMEDIATE,
                isFeatured = true
            ),
            WorkoutCategory(
                id = "yoga",
                name = "Morning Yoga",
                description = "Start your day with balance and flexibility.",
                duration = "15 min",
                calories = "80 kcal",
                difficulty = WorkoutDifficulty.BEGINNER
            ),
            WorkoutCategory(
                id = "cardio",
                name = "Citrus Cardio",
                description = "Get that heart rate up and burn energy!",
                duration = "30 min",
                calories = "300 kcal",
                difficulty = WorkoutDifficulty.HIGH_INTENSITY
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            QuickStartFab(
                onClick = onQuickStart,
                modifier = Modifier.padding(bottom = 80.dp) // Above bottom nav
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedNavIndex,
                onItemSelected = { index ->
                    selectedNavIndex = index
                    onNavigate(index)
                },
                onAddClick = onAddClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            LandingHeader(
                onAvatarClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg)
                    .padding(top = AppTheme.spacing.lg)
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

            // Section Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Choose your flavor",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "View all",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { /* View all */ }
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

            // Workout Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
            ) {
                workouts.forEach { workout ->
                    if (workout.isFeatured) {
                        FeaturedWorkoutCard(
                            workout = workout,
                            onClick = { onWorkoutSelect(workout.id) }
                        )
                    } else {
                        CompactWorkoutCard(
                            workout = workout,
                            onClick = { onWorkoutSelect(workout.id) }
                        )
                    }
                }
            }

            // Bottom spacer for FAB and nav
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

/**
 * Landing screen header with greeting and profile avatar.
 */
@Composable
private fun LandingHeader(
    onAvatarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good Morning, User!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
            Text(
                text = "Ready to squeeze the day?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Profile Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = Primary.copy(alpha = 0.15f),
                    spotColor = Primary.copy(alpha = 0.1f)
                )
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "U",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Featured workout card with gradient background.
 *
 * Design tokens:
 * - Gradient: Secondary → Tertiary
 * - Border radius: 3xl (40dp)
 * - Shadow: warm shadow
 * - Press effect: scale(0.98)
 */
@Composable
private fun FeaturedWorkoutCard(
    workout: WorkoutCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "cardScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(40.dp),
                ambientColor = Primary.copy(alpha = 0.2f),
                spotColor = Primary.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(40.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Secondary, Tertiary)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: Badge and icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Duration badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = workout.duration,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Charcoal
                    )
                }

                // Category icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💪",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Bottom content
            Column {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Charcoal
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Charcoal)
                    )
                    Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                    Text(
                        text = workout.difficulty.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Charcoal.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(AppTheme.spacing.md))

                // Start button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Charcoal)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start Workout",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact workout card for secondary workout options.
 *
 * Design tokens:
 * - Background: surfaceVariant
 * - Border: 1dp outline
 * - Border radius: 3xl (40dp)
 * - Press effect: scale(0.98)
 */
@Composable
private fun CompactWorkoutCard(
    workout: WorkoutCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "cardScale"
    )

    // Determine colors based on workout type
    val (cardBackground, accentColor) = when (workout.id) {
        "yoga" -> CardYoga to AccentGreen
        "cardio" -> CardCardio to AccentOrange
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(136.dp)
            .scale(scale)
            .clip(RoundedCornerShape(40.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(40.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppTheme.spacing.xs)
        ) {
            // Image section
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
                    .background(cardBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (workout.id == "yoga") "🧘" else "🏃",
                    style = MaterialTheme.typography.displaySmall
                )
            }

            // Content section
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxSize()
                    .padding(AppTheme.spacing.md),
                verticalArrangement = Arrangement.Center
            ) {
                // Difficulty badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = workout.difficulty.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = workout.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
                ) {
                    MetadataItem(icon = "⏱", text = workout.duration)
                    MetadataItem(icon = "🔥", text = workout.calories)
                }
            }

            // Play button
            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.2f))
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start ${workout.name}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Small metadata display with icon and text.
 */
@Composable
private fun MetadataItem(
    icon: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Quick Start Floating Action Button.
 *
 * Design tokens:
 * - Background: Charcoal (light) / Primary (dark)
 * - Border radius: 2xl (32dp)
 * - Shadow: elevated
 */
@Composable
private fun QuickStartFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        containerColor = Charcoal,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚡",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Quick Start",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Bottom navigation bar for landing screen.
 *
 * Design tokens:
 * - Background: surface with 90% opacity + blur effect
 * - Border radius: 2xl (32dp)
 * - Shadow: elevated
 */
@Composable
private fun LandingBottomNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.lg)
            .padding(bottom = AppTheme.spacing.lg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(32.dp)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppTheme.spacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = if (selectedIndex == 0) Icons.Filled.Home else Icons.Outlined.Home,
                    label = "Home",
                    isSelected = selectedIndex == 0,
                    onClick = { onItemSelected(0) }
                )
                NavItem(
                    icon = Icons.Outlined.Settings, // Using settings as placeholder
                    label = "Stats",
                    isSelected = selectedIndex == 1,
                    onClick = { onItemSelected(1) }
                )
                NavItem(
                    icon = if (selectedIndex == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                    label = "Settings",
                    isSelected = selectedIndex == 2,
                    onClick = { onItemSelected(2) }
                )
            }
        }
    }
}

/**
 * Individual navigation item.
 */
@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(AppTheme.spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            ),
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
