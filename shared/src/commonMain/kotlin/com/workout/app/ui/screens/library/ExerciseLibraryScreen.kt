package com.workout.app.ui.screens.library

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.workout.app.ui.components.exercise.AddCustomExerciseBottomSheet
import com.workout.app.ui.components.exercise.CustomExerciseFormState
import com.workout.app.ui.components.exercise.ExerciseLibraryItem
import com.workout.app.ui.components.exercise.LibraryExercise
import com.workout.app.ui.components.exercise.MuscleGroupFilters
import com.workout.app.ui.components.headers.SectionHeader
import com.workout.app.ui.components.inputs.SearchBar
import com.workout.app.ui.components.navigation.BottomNavBar
import com.workout.app.ui.components.overlays.M3BottomSheet
import com.workout.app.ui.theme.AppTheme

/**
 * Exercise Library screen showing all available exercises.
 * Based on mockup AN-12 and elements EL-24 through EL-35.
 *
 * Features:
 * - Library header with title and add button (EL-24)
 * - Search bar for filtering exercises (EL-25)
 * - Muscle group filter chips (EL-26/27)
 * - Exercise list grouped by category (EL-28)
 * - Exercise items with name, muscle group, favorite star (EL-29)
 * - Custom badge for user-created exercises (EL-31)
 * - More options button (EL-34)
 * - Add custom exercise bottom sheet
 * - Bottom navigation bar (EL-04)
 *
 * @param onExerciseClick Callback when an exercise is clicked
 * @param onFavoriteToggle Callback when favorite star is clicked
 * @param onMoreOptionsClick Callback when more options button is clicked
 * @param onAddToWorkoutClick Callback when add to workout FAB is clicked
 * @param onCreateExercise Callback when a custom exercise is created
 * @param onNavigate Callback for bottom navigation
 * @param showAddExerciseSheet Whether to show the add exercise bottom sheet
 * @param onShowAddExerciseSheet Callback to show the add exercise sheet
 * @param onHideAddExerciseSheet Callback to hide the add exercise sheet
 * @param isCreatingExercise Whether an exercise is being created
 * @param modifier Optional modifier for customization
 */
@Composable
fun ExerciseLibraryScreen(
    onExerciseClick: (String) -> Unit = {},
    onFavoriteToggle: (String) -> Unit = {},
    onMoreOptionsClick: (String) -> Unit = {},
    onAddToWorkoutClick: () -> Unit = {},
    onCreateExercise: (CustomExerciseFormState) -> Unit = {},
    onNavigate: (Int) -> Unit = {},
    showAddExerciseSheet: Boolean = false,
    onShowAddExerciseSheet: () -> Unit = {},
    onHideAddExerciseSheet: () -> Unit = {},
    exercises: List<LibraryExercise> = emptyList(),
    isCreatingExercise: Boolean = false,
    activeSessionId: String? = null,
    activeSessionStartTime: Long? = null,
    isSessionMinimized: Boolean = false,
    onResumeSession: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State management
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf("All") }
    var selectedNavIndex by remember { mutableIntStateOf(1) } // Library tab selected
    var favoriteExercises by remember { mutableStateOf(setOf<String>()) }

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
                onAddClick = onAddToWorkoutClick,
                activeSessionId = activeSessionId,
                activeSessionStartTime = activeSessionStartTime,
                isSessionMinimized = isSessionMinimized,
                onResumeSession = onResumeSession
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Library Header (EL-24) with Add button
            LibraryHeader(
                onAddClick = onShowAddExerciseSheet,
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

    // Add Custom Exercise Bottom Sheet
    M3BottomSheet(
        visible = showAddExerciseSheet,
        onDismiss = onHideAddExerciseSheet,
        skipPartiallyExpanded = true
    ) {
        AddCustomExerciseBottomSheet(
            onSave = onCreateExercise,
            onCancel = onHideAddExerciseSheet,
            isLoading = isCreatingExercise
        )
    }
}

/**
 * Library header with title and add button.
 * Based on mockup element EL-24.
 *
 * @param onAddClick Callback when the add button is clicked
 * @param modifier Modifier for the header
 */
@Composable
private fun LibraryHeader(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
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

        // Add button
        IconButton(
            onClick = onAddClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add custom exercise",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
