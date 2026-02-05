package com.workout.app.presentation.detail

import com.workout.app.data.repository.ExerciseRepository
import com.workout.app.data.repository.SetRepository
import com.workout.app.database.Exercise
import com.workout.app.domain.model.Result
import com.workout.app.domain.model.SetData
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
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExerciseDetailState())
    val state: StateFlow<ExerciseDetailState> = _state.asStateFlow()

    init {
        loadExercise()
        loadPerformanceStats()
        loadWorkoutHistory()
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

    private fun loadPerformanceStats() {
        viewModelScope.launch {
            val statsResult = setRepository.getExerciseStats(exerciseId)
            val prResult = setRepository.getPersonalRecord(exerciseId)

            val stats = (statsResult as? Result.Success)?.data
            val pr = (prResult as? Result.Success)?.data

            if (stats != null) {
                _state.update {
                    it.copy(
                        performanceStats = PerformanceStats(
                            oneRepMax = pr?.toFloat(),
                            totalVolume = stats.totalVolume.toLong(),
                            totalSets = stats.totalSets,
                            maxWeight = stats.maxWeight,
                            avgReps = stats.avgReps,
                            lastPerformed = stats.lastPerformed
                        )
                    )
                }
            }
        }
    }

    private fun loadWorkoutHistory() {
        viewModelScope.launch {
            when (val result = setRepository.getByExercise(exerciseId)) {
                is Result.Success -> {
                    val allSets = result.data
                    val grouped = allSets.groupBy { it.sessionId }

                    val historyItems = grouped.map { (sessionId, sets) ->
                        val sortedSets = sets.sortedBy { it.setNumber }
                        val workingSets = sortedSets.filter { !it.isWarmup }
                        val totalReps = workingSets.sumOf { it.reps }
                        val totalVolume = workingSets.sumOf { (it.weight * it.reps).toLong() }
                        val bestWeight = workingSets.maxOfOrNull { it.weight } ?: 0.0
                        val rpeValues = workingSets.mapNotNull { it.rpe }
                        val avgRpe = if (rpeValues.isNotEmpty()) {
                            rpeValues.average().toFloat()
                        } else null
                        val date = sets.maxOf { it.completedAt }

                        WorkoutHistoryItem(
                            id = sessionId,
                            workoutDate = date,
                            sets = workingSets.size,
                            totalReps = totalReps,
                            totalVolume = totalVolume,
                            bestWeight = bestWeight,
                            averageRPE = avgRpe,
                            setDetails = sortedSets
                        )
                    }.sortedByDescending { it.workoutDate }

                    // Compute averages for QuickStats
                    val avgSets = if (historyItems.isNotEmpty()) {
                        historyItems.map { it.sets }.average().toFloat()
                    } else 0f
                    val totalWorkingSets = historyItems.sumOf { it.sets }
                    val totalRepsAll = historyItems.sumOf { it.totalReps }
                    val avgReps = if (totalWorkingSets > 0) {
                        totalRepsAll.toFloat() / totalWorkingSets
                    } else 0f

                    _state.update {
                        it.copy(
                            workoutHistory = historyItems,
                            averageSetsPerSession = avgSets,
                            averageRepsPerSet = avgReps
                        )
                    }
                }
                is Result.Error -> {
                    // History loading failure is non-fatal; leave empty
                }
                is Result.Loading -> {}
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
    val maxWeight: Double = 0.0,
    val avgReps: Double = 0.0,
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
    val bestWeight: Double = 0.0,
    val averageRPE: Float?,
    val setDetails: List<SetData> = emptyList()
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
    val averageSetsPerSession: Float = 0f,
    val averageRepsPerSet: Float = 0f,
    val error: String? = null
) {
    val hasInstructions: Boolean
        get() = !exercise?.instructions.isNullOrBlank()

    val hasHistory: Boolean
        get() = workoutHistory.isNotEmpty()

    val isFavorite: Boolean
        get() = exercise?.isFavorite == 1L
}
