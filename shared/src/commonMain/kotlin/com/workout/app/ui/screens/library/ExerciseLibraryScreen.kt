package com.workout.app.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.workout.app.ui.components.exercise.ExerciseLibraryItem
import com.workout.app.ui.components.exercise.LibraryExercise
import com.workout.app.ui.components.exercise.MuscleGroupFilters
import com.workout.app.ui.components.exercise.getMockLibraryExercises
import com.workout.app.ui.components.headers.SectionHeader
import com.workout.app.ui.components.inputs.SearchBar
import com.workout.app.ui.components.navigation.BottomNavBar
import com.workout.app.ui.theme.AppTheme

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
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = AppTheme.spacing.lg)
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
