package com.workout.app.presentation.detail

import com.workout.app.data.repository.ExerciseRepository
import com.workout.app.database.Exercise
import com.workout.app.domain.model.Result
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Exercise Detail screen.
 * Manages exercise details, performance stats, and workout history.
 */
class ExerciseDetailViewModel(
    private val exerciseId: String,
    private val exerciseRepository: ExerciseRepository
    // TODO: Add WorkoutSetRepository for history and stats
) : ViewModel() {

    private val _state = MutableStateFlow(ExerciseDetailState())
    val state: StateFlow<ExerciseDetailState> = _state.asStateFlow()

    init {
        loadExercise()
        // TODO: Load performance stats and history
    }

    private fun loadExercise() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = exerciseRepository.getById(exerciseId)) {
                is Result.Success -> {
                    val exercise = result.data
                    if (exercise != null) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                exercise = exercise,
                                error = null
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Exercise not found"
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to load exercise"
                        )
                    }
                }
                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    fun toggleInstructionsExpanded() {
        _state.update { it.copy(isInstructionsExpanded = !it.isInstructionsExpanded) }
    }

    fun toggleHistoryItemExpanded(historyId: String) {
        val currentExpanded = _state.value.expandedHistoryItems.toMutableSet()
        if (currentExpanded.contains(historyId)) {
            currentExpanded.remove(historyId)
        } else {
            currentExpanded.add(historyId)
        }
        _state.update { it.copy(expandedHistoryItems = currentExpanded) }
    }

    fun selectView(view: ExerciseView) {
        _state.update { it.copy(selectedView = view) }
        // TODO: Load partner stats if switching to Partner view
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            when (val result = exerciseRepository.toggleFavorite(exerciseId)) {
                is Result.Success -> {
                    // Update local state optimistically
                    _state.update {
                        val currentIsFavorite = it.exercise?.isFavorite == 1L
                        it.copy(
                            exercise = it.exercise?.copy(
                                isFavorite = if (currentIsFavorite) 0L else 1L
                            )
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(error = result.exception.message ?: "Failed to update favorite")
                    }
                }
                is Result.Loading -> {
                    // Not applicable
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

/**
 * View mode for exercise details (Me vs Partner).
 */
enum class ExerciseView {
    ME,
    PARTNER
}

/**
 * Performance statistics for an exercise.
 */
data class PerformanceStats(
    val oneRepMax: Float? = null,
    val totalVolume: Long = 0,
    val totalSets: Long = 0,
    val lastPerformed: Long? = null
)

/**
 * Workout history item.
 */
data class WorkoutHistoryItem(
    val id: String,
    val workoutDate: Long,
    val sets: Int,
    val totalReps: Int,
    val totalVolume: Long,
    val averageRPE: Float?
)

/**
 * UI state for Exercise Detail screen.
 */
data class ExerciseDetailState(
    val isLoading: Boolean = false,
    val exercise: Exercise? = null,
    val selectedView: ExerciseView = ExerciseView.ME,
    val isInstructionsExpanded: Boolean = false,
    val expandedHistoryItems: Set<String> = emptySet(),
    val performanceStats: PerformanceStats = PerformanceStats(),
    val workoutHistory: List<WorkoutHistoryItem> = emptyList(),
    val error: String? = null
) {
    /**
     * Whether instructions section can be expanded.
     */
    val hasInstructions: Boolean
        get() = !exercise?.instructions.isNullOrBlank()

    /**
     * Whether exercise has workout history.
     */
    val hasHistory: Boolean
        get() = workoutHistory.isNotEmpty()

    /**
     * Whether exercise is favorited.
     */
    val isFavorite: Boolean
        get() = exercise?.isFavorite == 1L
}
