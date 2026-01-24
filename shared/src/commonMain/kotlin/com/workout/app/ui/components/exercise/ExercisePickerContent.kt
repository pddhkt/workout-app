package com.workout.app.ui.components.exercise

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.workout.app.ui.theme.AppTheme

/**
 * Exercise data model for library and picker
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
 * Exercise picker content for use in bottom sheets.
 * Displays a searchable, filterable list of exercises.
 *
 * @param exercises List of exercises to display
 * @param onExerciseSelected Callback when an exercise is selected
 * @param modifier Optional modifier for customization
 */
@Composable
fun ExercisePickerContent(
    exercises: List<LibraryExercise> = getMockLibraryExercises(),
    onExerciseSelected: (LibraryExercise) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf("All") }

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

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Add Exercise",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { },
            placeholder = "Search exercises...",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Filter Chips
        MuscleGroupFilters(
            selectedMuscleGroup = selectedMuscleGroup,
            onMuscleGroupSelected = { selectedMuscleGroup = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Exercise List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            groupedExercises.forEach { (category, categoryExercises) ->
                item(key = "header_$category") {
                    SectionHeader(
                        title = category,
                        modifier = Modifier.padding(
                            top = AppTheme.spacing.sm,
                            bottom = AppTheme.spacing.xs
                        )
                    )
                }

                items(
                    items = categoryExercises,
                    key = { it.id }
                ) { exercise ->
                    ExercisePickerItem(
                        exercise = exercise,
                        onClick = { onExerciseSelected(exercise) }
                    )
                }
            }
        }
    }
}

/**
 * Muscle group filter chips row.
 */
@Composable
fun MuscleGroupFilters(
    selectedMuscleGroup: String,
    onMuscleGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 0.dp)
) {
    val muscleGroups = listOf(
        "All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio"
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        contentPadding = contentPadding
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
 * Exercise library item component for the library screen.
 * Shows exercise name, muscle group, favorite star, and more options.
 */
@Composable
fun ExerciseLibraryItem(
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

                    // Custom badge
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
                // Favorite star icon
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

                // More options button
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
 * Simplified exercise item for the picker.
 * Shows exercise name and muscle group with click to select.
 */
@Composable
private fun ExercisePickerItem(
    exercise: LibraryExercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = AppTheme.spacing.md
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
            ) {
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

                    if (exercise.isCustom) {
                        Badge(
                            text = "Custom",
                            variant = BadgeVariant.INFO
                        )
                    }
                }

                Text(
                    text = exercise.muscleGroup,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (exercise.isFavorite) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Mock data: Library exercises
 * Comprehensive list of exercises across all muscle groups
 */
fun getMockLibraryExercises(): List<LibraryExercise> = listOf(
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
