package com.workout.app.ui.screens.history

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.WorkoutType
import com.workout.app.domain.model.formatMonthDisplay
import com.workout.app.presentation.history.SessionHistoryState
import com.workout.app.presentation.history.SessionHistoryViewModel
import com.workout.app.ui.components.chips.FilterChip
import com.workout.app.ui.components.history.HistoryCard
import com.workout.app.ui.components.inputs.SearchBar
import com.workout.app.ui.theme.AppTheme
import org.koin.compose.koinInject

/**
 * Session History Screen - displays all past workouts with filtering capabilities.
 *
 * Features:
 * - Workouts grouped by month with section headers
 * - Search by workout name
 * - Filter by workout type (Solo/Partner)
 * - Filter by month
 * - Filter by muscle group
 *
 * @param onBackClick Callback when back button is pressed
 * @param onSessionClick Callback when a session card is tapped (navigates to detail)
 * @param viewModel ViewModel for managing screen state
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionHistoryScreen(
    onBackClick: () -> Unit,
    onSessionClick: (String) -> Unit,
    viewModel: SessionHistoryViewModel = koinInject(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        SearchBar(
                            query = state.searchQuery,
                            onQueryChange = { query ->
                                viewModel.updateSearchQuery(query)
                                if (query.isEmpty()) {
                                    showSearch = false
                                }
                            },
                            placeholder = "Search workouts...",
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Session History",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!showSearch) {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter section
            FilterSection(
                state = state,
                onWorkoutTypeSelect = viewModel::selectWorkoutType,
                onMuscleGroupSelect = viewModel::selectMuscleGroup,
                onMonthSelect = viewModel::selectMonth,
                onClearFilters = viewModel::clearFilters,
                modifier = Modifier.fillMaxWidth()
            )

            // Content
            when {
                state.isLoading && state.displayedWorkouts.isEmpty() -> {
                    LoadingState(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }
                state.displayedWorkouts.isEmpty() -> {
                    EmptyState(
                        hasFilters = state.hasActiveFilters,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }
                else -> {
                    SessionList(
                        workoutsByMonth = state.displayedWorkouts,
                        onSessionClick = onSessionClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    state: SessionHistoryState,
    onWorkoutTypeSelect: (WorkoutType?) -> Unit,
    onMuscleGroupSelect: (String?) -> Unit,
    onMonthSelect: (String?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = AppTheme.spacing.sm)
    ) {
        // Workout type filters
        LazyRow(
            contentPadding = PaddingValues(horizontal = AppTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            // All types
            item {
                FilterChip(
                    text = "All Types",
                    isActive = state.selectedWorkoutType == WorkoutType.ALL,
                    onClick = { onWorkoutTypeSelect(WorkoutType.ALL) }
                )
            }

            // Solo
            item {
                FilterChip(
                    text = "Solo",
                    isActive = state.selectedWorkoutType == WorkoutType.SOLO,
                    onClick = { onWorkoutTypeSelect(WorkoutType.SOLO) }
                )
            }

            // Partner
            item {
                FilterChip(
                    text = "Partner",
                    isActive = state.selectedWorkoutType == WorkoutType.PARTNER,
                    onClick = { onWorkoutTypeSelect(WorkoutType.PARTNER) }
                )
            }

            // Month filters
            if (state.monthGroups.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = AppTheme.spacing.sm)
                            .size(width = 1.dp, height = 24.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                items(state.monthGroups.take(6)) { monthGroup ->
                    FilterChip(
                        text = formatMonthDisplay(monthGroup.yearMonth).split(" ").first(),
                        isActive = state.selectedMonth == monthGroup.yearMonth,
                        onClick = {
                            if (state.selectedMonth == monthGroup.yearMonth) {
                                onMonthSelect(null)
                            } else {
                                onMonthSelect(monthGroup.yearMonth)
                            }
                        }
                    )
                }
            }
        }

        // Muscle group filters (second row if available)
        if (state.availableMuscleGroups.isNotEmpty()) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

            LazyRow(
                contentPadding = PaddingValues(horizontal = AppTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                item {
                    FilterChip(
                        text = "All Muscles",
                        isActive = state.selectedMuscleGroup == null,
                        onClick = { onMuscleGroupSelect(null) }
                    )
                }

                items(state.availableMuscleGroups) { muscleGroup ->
                    FilterChip(
                        text = muscleGroup,
                        isActive = state.selectedMuscleGroup == muscleGroup,
                        onClick = {
                            if (state.selectedMuscleGroup == muscleGroup) {
                                onMuscleGroupSelect(null)
                            } else {
                                onMuscleGroupSelect(muscleGroup)
                            }
                        }
                    )
                }
            }
        }

        // Clear filters button if filters are active
        if (state.hasActiveFilters) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.totalSessions} sessions found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FilterChip(
                    text = "Clear Filters",
                    isActive = false,
                    onClick = onClearFilters
                )
            }
        }
    }
}

@Composable
private fun SessionList(
    workoutsByMonth: Map<String, List<com.workout.app.domain.model.WorkoutHistoryItem>>,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = AppTheme.spacing.lg,
            vertical = AppTheme.spacing.md
        ),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        workoutsByMonth.forEach { (month, workouts) ->
            // Month header
            item(key = "header_$month") {
                MonthHeader(
                    month = month,
                    sessionCount = workouts.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppTheme.spacing.sm)
                )
            }

            // Workout cards for this month
            items(
                items = workouts,
                key = { it.id }
            ) { workout ->
                HistoryCard(
                    workout = workout,
                    onClick = { onSessionClick(workout.id) }
                )
            }
        }
    }
}

@Composable
private fun MonthHeader(
    month: String,
    sessionCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatMonthDisplay(month),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "$sessionCount sessions",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(
    hasFilters: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Text(
                text = if (hasFilters) "No matching sessions" else "No workout history",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (hasFilters) {
                    "Try adjusting your filters"
                } else {
                    "Complete your first workout to see it here"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
