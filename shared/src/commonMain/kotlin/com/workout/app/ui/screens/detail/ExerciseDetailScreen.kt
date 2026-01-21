package com.workout.app.ui.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.AppIconButton
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.ToggleButton
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.components.cards.ElevatedCard
import com.workout.app.ui.components.chips.Badge
import com.workout.app.ui.components.chips.BadgeVariant
import com.workout.app.ui.components.headers.SectionHeader
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.Border

/**
 * Exercise data model for detail screen
 */
data class ExerciseDetail(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val category: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String,
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null
)

/**
 * Quick stats data for exercise overview
 */
data class QuickStats(
    val targetSets: String,
    val targetReps: String,
    val primaryMuscle: String
)

/**
 * Performance stats data
 */
data class PerformanceStats(
    val oneRepMax: String?,
    val totalVolume: String,
    val totalSets: Int,
    val lastPerformed: String?
)

/**
 * Muscle target data
 */
data class MuscleTarget(
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>
)

/**
 * History item data
 */
data class HistoryItem(
    val id: String,
    val date: String,
    val sets: Int,
    val reps: String,
    val weight: String,
    val volume: String,
    val rpe: Int?
)

/**
 * Exercise Detail screen showing comprehensive exercise information.
 * Based on mockup AN-13 and elements EL-03, EL-86 through EL-94.
 *
 * Features:
 * - Top app bar with back navigation (EL-03)
 * - Hero image with play button (EL-86)
 * - Quick stats tiles (EL-87)
 * - Collapsible instructions card (EL-88)
 * - Performance stats card (EL-89)
 * - Muscle target card (EL-90)
 * - History items with expandable details (EL-91/92)
 * - Me/Partner toggle for stats view (EL-94)
 * - Sticky footer button to add to workout (EL-93)
 *
 * @param exerciseId ID of the exercise to display
 * @param onBackClick Callback when back button is clicked
 * @param onAddToWorkout Callback when add to workout button is clicked
 * @param onPlayVideo Callback when video play button is clicked
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onBackClick: () -> Unit = {},
    onAddToWorkout: () -> Unit = {},
    onPlayVideo: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State management
    var showMeStats by remember { mutableStateOf(true) }
    var isInstructionsExpanded by remember { mutableStateOf(false) }

    // Mock data
    val exercise = remember { getMockExerciseDetail(exerciseId) }
    val quickStats = remember { getMockQuickStats() }
    val performanceStats = remember { getMockPerformanceStats(showMeStats) }
    val muscleTarget = remember { getMockMuscleTarget() }
    val historyItems = remember { getMockHistoryItems(showMeStats) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    AppIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBackClick
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            // Sticky footer button (EL-93)
            StickyFooterButton(
                onAddToWorkout = onAddToWorkout
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = AppTheme.spacing.xl)
        ) {
            // Hero Image with Play Button (EL-86)
            item {
                HeroImage(
                    thumbnailUrl = exercise.thumbnailUrl,
                    onPlayClick = { exercise.videoUrl?.let(onPlayVideo) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Quick Stats Section (EL-87)
            item {
                QuickStatsSection(
                    stats = quickStats,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg)
                        .padding(top = AppTheme.spacing.lg)
                )
            }

            // Exercise Info
            item {
                ExerciseInfo(
                    exercise = exercise,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg)
                        .padding(top = AppTheme.spacing.lg)
                )
            }

            // Instructions Card (EL-88)
            item {
                InstructionsCard(
                    instructions = exercise.instructions,
                    isExpanded = isInstructionsExpanded,
                    onToggleExpand = { isInstructionsExpanded = !isInstructionsExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg)
                        .padding(top = AppTheme.spacing.lg)
                )
            }

            // Me/Partner Toggle (EL-94)
            item {
                MePartnerToggle(
                    showMeStats = showMeStats,
                    onToggle = { showMeStats = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg)
                        .padding(top = AppTheme.spacing.xl)
                )
            }

            // Performance Stats Card (EL-89)
            item {
                PerformanceStatsCard(
                    stats = performanceStats,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg)
                        .padding(top = AppTheme.spacing.lg)
                )
            }

            // Muscle Target Card (EL-90)
            item {
                MuscleTargetCard(
                    target = muscleTarget,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg)
                        .padding(top = AppTheme.spacing.lg)
                )
            }

            // History Section (EL-91/92)
            item {
                SectionHeader(
                    title = "History",
                    modifier = Modifier
                        .padding(horizontal = AppTheme.spacing.lg)
                        .padding(top = AppTheme.spacing.xl, bottom = AppTheme.spacing.md)
                )
            }

            items(historyItems, key = { it.id }) { item ->
                HistoryItemCard(
                    item = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg)
                        .padding(bottom = AppTheme.spacing.sm)
                )
            }
        }
    }
}

/**
 * Hero image component with play button overlay.
 * Based on mockup element EL-86.
 */
@Composable
private fun HeroImage(
    thumbnailUrl: String?,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(240.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder for image (would load from thumbnailUrl)
        Text(
            text = "Exercise Video",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Play button overlay
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onPlayClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play video",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Quick stats section with three stat tiles.
 * Based on mockup element EL-87.
 */
@Composable
private fun QuickStatsSection(
    stats: QuickStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        QuickStatTile(
            label = "Sets",
            value = stats.targetSets,
            modifier = Modifier.weight(1f)
        )
        QuickStatTile(
            label = "Reps",
            value = stats.targetReps,
            modifier = Modifier.weight(1f)
        )
        QuickStatTile(
            label = "Muscle",
            value = stats.primaryMuscle,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual quick stat tile.
 * Based on mockup element EL-87.
 */
@Composable
private fun QuickStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    BaseCard(
        modifier = modifier,
        contentPadding = AppTheme.spacing.md
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Exercise info section with badges.
 */
@Composable
private fun ExerciseInfo(
    exercise: ExerciseDetail,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            Badge(text = exercise.category, variant = BadgeVariant.INFO)
            Badge(text = exercise.equipment, variant = BadgeVariant.NEUTRAL)
            Badge(text = exercise.difficulty, variant = BadgeVariant.WARNING)
        }
    }
}

/**
 * Collapsible instructions card.
 * Based on mockup element EL-88.
 */
@Composable
private fun InstructionsCard(
    instructions: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "Arrow rotation"
    )

    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.md)
        ) {
            // Header with toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Instructions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            // Expandable content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = AppTheme.spacing.md)
                ) {
                    Text(
                        text = instructions,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Me/Partner toggle for stats view.
 * Based on mockup element EL-94.
 */
@Composable
private fun MePartnerToggle(
    showMeStats: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        ToggleButton(
            text = "Me",
            selected = showMeStats,
            onClick = { onToggle(true) },
            modifier = Modifier.weight(1f)
        )
        ToggleButton(
            text = "Partner",
            selected = !showMeStats,
            onClick = { onToggle(false) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Performance stats card showing 1RM, volume, etc.
 * Based on mockup element EL-89.
 */
@Composable
private fun PerformanceStatsCard(
    stats: PerformanceStats,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Text(
                text = "Performance Stats",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Stats grid
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "1 Rep Max",
                        value = stats.oneRepMax ?: "Not set"
                    )
                    StatItem(
                        label = "Total Volume",
                        value = stats.totalVolume
                    )
                }

                HorizontalDivider(color = Border)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Total Sets",
                        value = stats.totalSets.toString()
                    )
                    StatItem(
                        label = "Last Performed",
                        value = stats.lastPerformed ?: "Never"
                    )
                }
            }
        }
    }
}

/**
 * Individual stat item component.
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Muscle target card showing primary and secondary muscles.
 * Based on mockup element EL-90.
 */
@Composable
private fun MuscleTargetCard(
    target: MuscleTarget,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Text(
                text = "Muscle Groups Targeted",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Muscle diagram placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(AppTheme.spacing.sm))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Muscle Diagram",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Muscle lists
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                MuscleList(
                    label = "Primary",
                    muscles = target.primaryMuscles
                )
                MuscleList(
                    label = "Secondary",
                    muscles = target.secondaryMuscles
                )
            }
        }
    }
}

/**
 * Muscle list component.
 */
@Composable
private fun MuscleList(
    label: String,
    muscles: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = muscles.joinToString(", "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * History item card with expandable details.
 * Based on mockup elements EL-91/92.
 */
@Composable
private fun HistoryItemCard(
    item: HistoryItem,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "Arrow rotation"
    )

    BaseCard(
        modifier = modifier,
        onClick = { isExpanded = !isExpanded },
        contentPadding = AppTheme.spacing.md
    ) {
        Column {
            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
                ) {
                    Text(
                        text = item.date,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.sets} sets • ${item.volume} volume",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            // Expandable details
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    HorizontalDivider(color = Border)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailItem(label = "Reps", value = item.reps)
                        DetailItem(label = "Weight", value = item.weight)
                        DetailItem(
                            label = "RPE",
                            value = item.rpe?.toString() ?: "N/A"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Detail item for history expanded view.
 */
@Composable
private fun DetailItem(
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
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Sticky footer button for adding to workout.
 * Based on mockup element EL-93.
 */
@Composable
private fun StickyFooterButton(
    onAddToWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                start = AppTheme.spacing.lg,
                end = AppTheme.spacing.lg,
                top = AppTheme.spacing.md,
                bottom = AppTheme.spacing.md
            )
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        HorizontalDivider(
            color = Border,
            modifier = Modifier.padding(bottom = AppTheme.spacing.md)
        )

        PrimaryButton(
            text = "Add to Workout",
            onClick = onAddToWorkout,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Mock data: Exercise detail
 */
private fun getMockExerciseDetail(exerciseId: String): ExerciseDetail {
    return ExerciseDetail(
        id = exerciseId,
        name = "Barbell Bench Press",
        muscleGroup = "Chest",
        category = "Compound",
        equipment = "Barbell",
        difficulty = "Intermediate",
        instructions = """
            1. Lie flat on a bench with your feet planted firmly on the ground.
            2. Grip the barbell slightly wider than shoulder-width apart.
            3. Unrack the bar and lower it slowly to your chest.
            4. Press the bar back up explosively until your arms are fully extended.
            5. Keep your shoulder blades retracted throughout the movement.
            6. Maintain a slight arch in your lower back.
        """.trimIndent(),
        videoUrl = "https://example.com/video.mp4",
        thumbnailUrl = "https://example.com/thumbnail.jpg"
    )
}

/**
 * Mock data: Quick stats
 */
private fun getMockQuickStats(): QuickStats {
    return QuickStats(
        targetSets = "3-4",
        targetReps = "8-12",
        primaryMuscle = "Chest"
    )
}

/**
 * Mock data: Performance stats
 */
private fun getMockPerformanceStats(isMe: Boolean): PerformanceStats {
    return if (isMe) {
        PerformanceStats(
            oneRepMax = "225 lbs",
            totalVolume = "24,500 lbs",
            totalSets = 156,
            lastPerformed = "2 days ago"
        )
    } else {
        PerformanceStats(
            oneRepMax = "185 lbs",
            totalVolume = "18,200 lbs",
            totalSets = 124,
            lastPerformed = "3 days ago"
        )
    }
}

/**
 * Mock data: Muscle target
 */
private fun getMockMuscleTarget(): MuscleTarget {
    return MuscleTarget(
        primaryMuscles = listOf("Pectoralis Major", "Anterior Deltoid"),
        secondaryMuscles = listOf("Triceps", "Serratus Anterior")
    )
}

/**
 * Mock data: History items
 */
private fun getMockHistoryItems(isMe: Boolean): List<HistoryItem> {
    return if (isMe) {
        listOf(
            HistoryItem(
                id = "1",
                date = "Jan 19, 2026",
                sets = 4,
                reps = "10, 10, 8, 8",
                weight = "185 lbs",
                volume = "6,660 lbs",
                rpe = 8
            ),
            HistoryItem(
                id = "2",
                date = "Jan 16, 2026",
                sets = 4,
                reps = "12, 10, 10, 8",
                weight = "175 lbs",
                volume = "7,000 lbs",
                rpe = 7
            ),
            HistoryItem(
                id = "3",
                date = "Jan 12, 2026",
                sets = 3,
                reps = "12, 12, 10",
                weight = "165 lbs",
                volume = "5,610 lbs",
                rpe = 7
            ),
            HistoryItem(
                id = "4",
                date = "Jan 9, 2026",
                sets = 4,
                reps = "10, 10, 10, 8",
                weight = "175 lbs",
                volume = "6,650 lbs",
                rpe = 8
            ),
            HistoryItem(
                id = "5",
                date = "Jan 5, 2026",
                sets = 3,
                reps = "12, 10, 10",
                weight = "165 lbs",
                volume = "5,280 lbs",
                rpe = 6
            )
        )
    } else {
        listOf(
            HistoryItem(
                id = "6",
                date = "Jan 18, 2026",
                sets = 3,
                reps = "10, 10, 8",
                weight = "155 lbs",
                volume = "4,340 lbs",
                rpe = 7
            ),
            HistoryItem(
                id = "7",
                date = "Jan 14, 2026",
                sets = 4,
                reps = "12, 10, 10, 8",
                weight = "145 lbs",
                volume = "5,800 lbs",
                rpe = 8
            ),
            HistoryItem(
                id = "8",
                date = "Jan 11, 2026",
                sets = 3,
                reps = "10, 10, 10",
                weight = "155 lbs",
                volume = "4,650 lbs",
                rpe = 7
            )
        )
    }
}
