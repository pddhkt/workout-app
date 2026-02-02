package com.workout.app.presentation.history

import com.workout.app.data.repository.WorkoutRepository
import com.workout.app.database.Workout
import com.workout.app.domain.model.DateRange
import com.workout.app.domain.model.MonthGroup
import com.workout.app.domain.model.Result
import com.workout.app.domain.model.WorkoutHistoryFilters
import com.workout.app.domain.model.WorkoutHistoryItem
import com.workout.app.domain.model.WorkoutType
import com.workout.app.domain.model.formatMonthDisplay
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel for Session History screen.
 * Manages workout history list, filtering, and search.
 */
class SessionHistoryViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SessionHistoryState())
    val state: StateFlow<SessionHistoryState> = _state.asStateFlow()

    init {
        loadWorkouts()
        loadMonthGroups()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            workoutRepository.observeGroupedByMonth()
                .catch { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load workouts"
                        )
                    }
                }
                .collect { groupedWorkouts ->
                    val historyItems = groupedWorkouts.mapValues { (_, workouts) ->
                        workouts.map { it.toHistoryItem() }
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            workoutsByMonth = historyItems,
                            error = null
                        )
                    }
                }
        }
    }

    private fun loadMonthGroups() {
        viewModelScope.launch {
            when (val result = workoutRepository.getDistinctMonths()) {
                is Result.Success -> {
                    _state.update { it.copy(monthGroups = result.data) }
                }
                is Result.Error -> {
                    // Non-critical, don't show error
                }
                is Result.Loading -> { }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun selectWorkoutType(type: WorkoutType?) {
        _state.update { it.copy(selectedWorkoutType = type ?: WorkoutType.ALL) }
        applyFilters()
    }

    fun selectMuscleGroup(muscleGroup: String?) {
        _state.update { it.copy(selectedMuscleGroup = muscleGroup) }
        applyFilters()
    }

    fun setDateRange(dateRange: DateRange?) {
        _state.update { it.copy(selectedDateRange = dateRange) }
        applyFilters()
    }

    fun selectMonth(yearMonth: String?) {
        _state.update { it.copy(selectedMonth = yearMonth) }
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _state.value

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val filters = WorkoutHistoryFilters(
                dateRange = currentState.selectedDateRange,
                muscleGroups = currentState.selectedMuscleGroup?.let { listOf(it) } ?: emptyList(),
                workoutType = currentState.selectedWorkoutType,
                searchQuery = currentState.searchQuery
            )

            when (val result = workoutRepository.getFiltered(filters)) {
                is Result.Success -> {
                    val filtered = result.data
                        .filter { workout ->
                            // Apply month filter if set
                            if (currentState.selectedMonth != null) {
                                val workoutMonth = getWorkoutMonth(workout.createdAt)
                                workoutMonth == currentState.selectedMonth
                            } else true
                        }
                        .map { it.toHistoryItem() }
                        .groupBy { getWorkoutMonth(it.createdAt) }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            filteredWorkouts = filtered.takeIf {
                                currentState.hasActiveFilters
                            },
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                }
                is Result.Loading -> { }
            }
        }
    }

    fun clearFilters() {
        _state.update {
            it.copy(
                searchQuery = "",
                selectedWorkoutType = WorkoutType.ALL,
                selectedMuscleGroup = null,
                selectedDateRange = null,
                selectedMonth = null,
                filteredWorkouts = null
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun refresh() {
        loadWorkouts()
        loadMonthGroups()
    }

    private fun getWorkoutMonth(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}"
    }
}

/**
 * UI state for Session History screen.
 */
data class SessionHistoryState(
    val isLoading: Boolean = false,
    val workoutsByMonth: Map<String, List<WorkoutHistoryItem>> = emptyMap(),
    val filteredWorkouts: Map<String, List<WorkoutHistoryItem>>? = null,
    val monthGroups: List<MonthGroup> = emptyList(),
    val searchQuery: String = "",
    val selectedWorkoutType: WorkoutType = WorkoutType.ALL,
    val selectedMuscleGroup: String? = null,
    val selectedDateRange: DateRange? = null,
    val selectedMonth: String? = null,
    val error: String? = null
) {
    /**
     * Get displayed workouts (filtered or all).
     */
    val displayedWorkouts: Map<String, List<WorkoutHistoryItem>>
        get() = filteredWorkouts ?: workoutsByMonth

    /**
     * Whether any filters are active.
     */
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() ||
                selectedWorkoutType != WorkoutType.ALL ||
                selectedMuscleGroup != null ||
                selectedDateRange != null ||
                selectedMonth != null

    /**
     * Total number of displayed sessions.
     */
    val totalSessions: Int
        get() = displayedWorkouts.values.sumOf { it.size }

    /**
     * Available muscle groups from all workouts.
     */
    val availableMuscleGroups: List<String>
        get() = workoutsByMonth.values
            .flatten()
            .flatMap { it.muscleGroups }
            .distinct()
            .sorted()
}

/**
 * Extension to convert database Workout to WorkoutHistoryItem domain model.
 */
private fun Workout.toHistoryItem(): WorkoutHistoryItem {
    return WorkoutHistoryItem(
        id = id,
        name = name,
        createdAt = createdAt,
        duration = duration,
        totalVolume = totalVolume,
        totalSets = totalSets,
        exerciseCount = exerciseCount,
        isPartnerWorkout = isPartnerWorkout == 1L,
        notes = notes,
        muscleGroups = emptyList(), // TODO: Parse from related data
        prCount = 0, // TODO: Calculate from set records
        rpe = null // TODO: Parse from notes or separate field
    )
}
