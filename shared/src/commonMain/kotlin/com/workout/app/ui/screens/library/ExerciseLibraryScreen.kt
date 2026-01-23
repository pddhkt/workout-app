package com.workout.app.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.AppIconButton
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.components.chips.Badge
import com.workout.app.ui.components.chips.BadgeVariant
import com.workout.app.ui.components.chips.FilterChip
import com.workout.app.ui.components.headers.SectionHeader
import com.workout.app.ui.components.inputs.SearchBar
import com.workout.app.ui.components.navigation.BottomNavBar
import com.workout.app.ui.theme.AppTheme

/**
 * Exercise data model for library
 */
data class LibraryExercise(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val category: String,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
)

/**
 * Exercise Library screen showing all available exercises.
 * Based on mockup AN-12 and elements EL-24 through EL-35.
 *
 * Features:
 * - Library header with title (EL-24)
 * - Search bar for filtering exercises (EL-25)
 * - Muscle group filter chips (EL-26/27)
 * - Exercise list grouped by category (EL-28)
 * - Exercise items with name, muscle group, favorite star (EL-29)
 * - Custom badge for user-created exercises (EL-31)
 * - More options button (EL-34)
 * - Add to workout FAB (EL-35)
 * - Bottom navigation bar (EL-04)
 *
 * @param onExerciseClick Callback when an exercise is clicked
 * @param onFavoriteToggle Callback when favorite star is clicked
 * @param onMoreOptionsClick Callback when more options button is clicked
 * @param onAddToWorkoutClick Callback when add to workout FAB is clicked
 * @param onNavigate Callback for bottom navigation
 * @param modifier Optional modifier for customization
 */
@Composable
fun ExerciseLibraryScreen(
    onExerciseClick: (String) -> Unit = {},
    onFavoriteToggle: (String) -> Unit = {},
    onMoreOptionsClick: (String) -> Unit = {},
    onAddToWorkoutClick: () -> Unit = {},
    onNavigate: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State management
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf("All") }
    var selectedNavIndex by remember { mutableIntStateOf(1) } // Library tab selected
    var favoriteExercises by remember { mutableStateOf(setOf<String>()) }

    // Mock exercise data
    val exercises = remember { getMockLibraryExercises() }

    // Filter exercises by search query and muscle group
    val filteredExercises = exercises.filter { exercise ->
        val matchesSearch = exercise.name.contains(searchQuery, ignoreCase = true) ||
                exercise.muscleGroup.contains(searchQuery, ignoreCase = true) ||
                exercise.category.contains(searchQuery, ignoreCase = true)

        val matchesMuscleGroup = selectedMuscleGroup == "All" ||
                exercise.muscleGroup == selectedMuscleGroup

        matchesSearch && matchesMuscleGroup
    }

    // Group exercises by category
    val groupedExercises = filteredExercises.groupBy { it.category }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedNavIndex,
                onItemSelected = { index ->
                    selectedNavIndex = index
                    onNavigate(index)
                },
                onAddClick = onAddToWorkoutClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Library Header (EL-24)
            LibraryHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg)
                    .padding(top = AppTheme.spacing.xl, bottom = AppTheme.spacing.md)
            )

            // Search Bar (EL-25)
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { /* Handle search action */ },
                placeholder = "Search exercises...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg)
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

            // Filter Chips (EL-26/27)
            MuscleGroupFilters(
                selectedMuscleGroup = selectedMuscleGroup,
                onMuscleGroupSelected = { selectedMuscleGroup = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

            // Exercise List grouped by category
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = AppTheme.spacing.lg,
                    end = AppTheme.spacing.lg,
                    bottom = AppTheme.spacing.xl
                )
            ) {
                groupedExercises.forEach { (category, categoryExercises) ->
                    item(key = "header_$category") {
                        // Section Title (EL-28)
                        SectionHeader(
                            title = category,
                            modifier = Modifier.padding(
                                top = AppTheme.spacing.md,
                                bottom = AppTheme.spacing.md
                            )
                        )
                    }

                    items(
                        items = categoryExercises,
                        key = { it.id }
                    ) { exercise ->
                        ExerciseLibraryItem(
                            exercise = exercise,
                            isFavorite = favoriteExercises.contains(exercise.id) || exercise.isFavorite,
                            onExerciseClick = { onExerciseClick(exercise.id) },
                            onFavoriteToggle = {
                                favoriteExercises = if (favoriteExercises.contains(exercise.id)) {
                                    favoriteExercises - exercise.id
                                } else {
                                    favoriteExercises + exercise.id
                                }
                                onFavoriteToggle(exercise.id)
                            },
                            onMoreOptionsClick = { onMoreOptionsClick(exercise.id) },
                            modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Library header with title.
 * Based on mockup element EL-24.
 */
@Composable
private fun LibraryHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Text(
            text = "Exercise Library",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Browse and manage your exercises",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Muscle group filter chips row.
 * Based on mockup elements EL-26/27.
 */
@Composable
private fun MuscleGroupFilters(
    selectedMuscleGroup: String,
    onMuscleGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val muscleGroups = listOf(
        "All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio"
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        contentPadding = PaddingValues(horizontal = AppTheme.spacing.lg)
    ) {
        items(muscleGroups) { muscleGroup ->
            FilterChip(
                text = muscleGroup,
                isActive = selectedMuscleGroup == muscleGroup,
                onClick = { onMuscleGroupSelected(muscleGroup) }
            )
        }
    }
}

/**
 * Exercise library item component.
 * Based on mockup element EL-29.
 *
 * Features:
 * - Exercise name and muscle group
 * - Favorite star icon (EL-32/33)
 * - Custom badge for user-created exercises (EL-31)
 * - More options button (EL-34)
 */
@Composable
private fun ExerciseLibraryItem(
    exercise: LibraryExercise,
    isFavorite: Boolean,
    onExerciseClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onExerciseClick,
        contentPadding = AppTheme.spacing.md
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Exercise info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
            ) {
                // Exercise name with optional custom badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Custom badge (EL-31)
                    if (exercise.isCustom) {
                        Badge(
                            text = "Custom",
                            variant = BadgeVariant.INFO
                        )
                    }
                }

                // Muscle group
                Text(
                    text = exercise.muscleGroup,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right side: Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorite star icon (EL-32/33)
                AppIconButton(
                    icon = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    onClick = onFavoriteToggle,
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(40.dp)
                )

                // More options button (EL-34)
                AppIconButton(
                    icon = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    onClick = onMoreOptionsClick,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

/**
 * Mock data: Library exercises
 * Comprehensive list of exercises across all muscle groups
 */
private fun getMockLibraryExercises(): List<LibraryExercise> = listOf(
    // Chest exercises
    LibraryExercise(
        id = "1",
        name = "Barbell Bench Press",
        muscleGroup = "Chest",
        category = "Chest - Compound",
        isCustom = false,
        isFavorite = true
    ),
    LibraryExercise(
        id = "2",
        name = "Dumbbell Fly",
        muscleGroup = "Chest",
        category = "Chest - Isolation",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "3",
        name = "Incline Dumbbell Press",
        muscleGroup = "Chest",
        category = "Chest - Compound",
        isCustom = false,
        isFavorite = true
    ),
    LibraryExercise(
        id = "4",
        name = "Cable Crossover",
        muscleGroup = "Chest",
        category = "Chest - Isolation",
        isCustom = false,
        isFavorite = false
    ),

    // Back exercises
    LibraryExercise(
        id = "5",
        name = "Barbell Row",
        muscleGroup = "Back",
        category = "Back - Compound",
        isCustom = false,
        isFavorite = true
    ),
    LibraryExercise(
        id = "6",
        name = "Pull-ups",
        muscleGroup = "Back",
        category = "Back - Compound",
        isCustom = false,
        isFavorite = true
    ),
    LibraryExercise(
        id = "7",
        name = "Lat Pulldown",
        muscleGroup = "Back",
        category = "Back - Compound",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "8",
        name = "Seated Cable Row",
        muscleGroup = "Back",
        category = "Back - Compound",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "9",
        name = "Face Pulls",
        muscleGroup = "Back",
        category = "Back - Isolation",
        isCustom = false,
        isFavorite = false
    ),

    // Legs exercises
    LibraryExercise(
        id = "10",
        name = "Barbell Squat",
        muscleGroup = "Legs",
        category = "Legs - Compound",
        isCustom = false,
        isFavorite = true
    ),
    LibraryExercise(
        id = "11",
        name = "Romanian Deadlift",
        muscleGroup = "Legs",
        category = "Legs - Compound",
        isCustom = false,
        isFavorite = true
    ),
    LibraryExercise(
        id = "12",
        name = "Leg Press",
        muscleGroup = "Legs",
        category = "Legs - Compound",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "13",
        name = "Leg Curl",
        muscleGroup = "Legs",
        category = "Legs - Isolation",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "14",
        name = "Leg Extension",
        muscleGroup = "Legs",
        category = "Legs - Isolation",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "15",
        name = "Bulgarian Split Squat",
        muscleGroup = "Legs",
        category = "Legs - Compound",
        isCustom = true,
        isFavorite = true
    ),

    // Shoulders exercises
    LibraryExercise(
        id = "16",
        name = "Overhead Press",
        muscleGroup = "Shoulders",
        category = "Shoulders - Compound",
        isCustom = false,
        isFavorite = true
    ),
    LibraryExercise(
        id = "17",
        name = "Lateral Raise",
        muscleGroup = "Shoulders",
        category = "Shoulders - Isolation",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "18",
        name = "Front Raise",
        muscleGroup = "Shoulders",
        category = "Shoulders - Isolation",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "19",
        name = "Arnold Press",
        muscleGroup = "Shoulders",
        category = "Shoulders - Compound",
        isCustom = false,
        isFavorite = false
    ),

    // Arms exercises
    LibraryExercise(
        id = "20",
        name = "Barbell Curl",
        muscleGroup = "Arms",
        category = "Arms - Isolation",
        isCustom = false,
        isFavorite = true
    ),
    LibraryExercise(
        id = "21",
        name = "Tricep Pushdown",
        muscleGroup = "Arms",
        category = "Arms - Isolation",
        isCustom = false,
        isFavorite = true
    ),
    LibraryExercise(
        id = "22",
        name = "Hammer Curl",
        muscleGroup = "Arms",
        category = "Arms - Isolation",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "23",
        name = "Overhead Tricep Extension",
        muscleGroup = "Arms",
        category = "Arms - Isolation",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "24",
        name = "Concentration Curl",
        muscleGroup = "Arms",
        category = "Arms - Isolation",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "25",
        name = "Close Grip Bench Press",
        muscleGroup = "Arms",
        category = "Arms - Compound",
        isCustom = false,
        isFavorite = false
    ),

    // Core exercises
    LibraryExercise(
        id = "26",
        name = "Plank",
        muscleGroup = "Core",
        category = "Core - Stability",
        isCustom = false,
        isFavorite = true
    ),
    LibraryExercise(
        id = "27",
        name = "Cable Crunch",
        muscleGroup = "Core",
        category = "Core - Isolation",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "28",
        name = "Russian Twist",
        muscleGroup = "Core",
        category = "Core - Rotation",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "29",
        name = "Hanging Leg Raise",
        muscleGroup = "Core",
        category = "Core - Isolation",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "30",
        name = "Ab Wheel Rollout",
        muscleGroup = "Core",
        category = "Core - Stability",
        isCustom = true,
        isFavorite = true
    ),

    // Cardio exercises
    LibraryExercise(
        id = "31",
        name = "Treadmill Running",
        muscleGroup = "Cardio",
        category = "Cardio - Steady State",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "32",
        name = "Cycling",
        muscleGroup = "Cardio",
        category = "Cardio - Steady State",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "33",
        name = "Jump Rope",
        muscleGroup = "Cardio",
        category = "Cardio - HIIT",
        isCustom = false,
        isFavorite = false
    ),
    LibraryExercise(
        id = "34",
        name = "Rowing Machine",
        muscleGroup = "Cardio",
        category = "Cardio - Full Body",
        isCustom = false,
        isFavorite = false
    )
)
