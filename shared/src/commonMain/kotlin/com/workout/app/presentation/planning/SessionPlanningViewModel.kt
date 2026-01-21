package com.workout.app.presentation.planning

import com.workout.app.data.repository.ExerciseRepository
import com.workout.app.data.repository.SessionRepository
import com.workout.app.database.Exercise
import com.workout.app.domain.model.Result
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Session Planning screen.
 * Manages exercise selection and session creation.
 */
class SessionPlanningViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SessionPlanningState())
    val state: StateFlow<SessionPlanningState> = _state.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            exerciseRepository.observeAll()
                .catch { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load exercises"
                        )
                    }
                }
                .collect { exercises ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            allExercises = exercises,
                            error = null
                        )
                    }
                }
        }
    }

    fun selectMuscleGroup(muscleGroup: String?) {
        _state.update { it.copy(selectedMuscleGroup = muscleGroup) }
    }

    fun addExercise(exerciseId: String, sets: Int = 3) {
        val currentAdded = _state.value.addedExercises.toMutableMap()
        currentAdded[exerciseId] = AddedExerciseData(exerciseId = exerciseId, setCount = sets)
        _state.update { it.copy(addedExercises = currentAdded) }
    }

    fun removeExercise(exerciseId: String) {
        val currentAdded = _state.value.addedExercises.toMutableMap()
        currentAdded.remove(exerciseId)
        _state.update { it.copy(addedExercises = currentAdded) }
    }

    fun updateExerciseSets(exerciseId: String, sets: Int) {
        val currentAdded = _state.value.addedExercises.toMutableMap()
        val existing = currentAdded[exerciseId] ?: return
        currentAdded[exerciseId] = existing.copy(setCount = sets)
        _state.update { it.copy(addedExercises = currentAdded) }
    }

    fun toggleExercise(exerciseId: String) {
        if (_state.value.addedExercises.containsKey(exerciseId)) {
            removeExercise(exerciseId)
        } else {
            addExercise(exerciseId)
        }
    }

    suspend fun createSession(sessionName: String): Result<String> {
        val addedExercises = _state.value.addedExercises

        if (addedExercises.isEmpty()) {
            return Result.Error(Exception("Please add at least one exercise"))
        }

        _state.update { it.copy(isCreatingSession = true) }

        return try {
            // Create session
            val sessionResult = sessionRepository.create(
                name = sessionName,
                templateId = null,
                status = "active",
                isPartnerWorkout = false
            )

            when (sessionResult) {
                is Result.Success -> {
                    val sessionId = sessionResult.data
                    // TODO: Add exercises to session via SessionExercise repository
                    _state.update { it.copy(isCreatingSession = false) }
                    Result.Success(sessionId)
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isCreatingSession = false,
                            error = sessionResult.exception.message
                        )
                    }
                    sessionResult
                }
                is Result.Loading -> {
                    Result.Loading
                }
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isCreatingSession = false,
                    error = e.message ?: "Failed to create session"
                )
            }
            Result.Error(e)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

/**
 * Data class for added exercise with set count.
 */
data class AddedExerciseData(
    val exerciseId: String,
    val setCount: Int
)

/**
 * UI state for Session Planning screen.
 */
data class SessionPlanningState(
    val isLoading: Boolean = false,
    val isCreatingSession: Boolean = false,
    val allExercises: List<Exercise> = emptyList(),
    val addedExercises: Map<String, AddedExerciseData> = emptyMap(),
    val selectedMuscleGroup: String? = null,
    val error: String? = null
) {
    /**
     * Get filtered exercises based on selected muscle group.
     */
    val filteredExercises: List<Exercise>
        get() = if (selectedMuscleGroup == null) {
            allExercises
        } else {
            allExercises.filter { it.muscleGroup == selectedMuscleGroup }
        }

    /**
     * Total sets across all added exercises.
     */
    val totalSets: Int
        get() = addedExercises.values.sumOf { it.setCount }

    /**
     * Whether session can be started (has at least one exercise).
     */
    val canStartSession: Boolean
        get() = addedExercises.isNotEmpty() && !isCreatingSession
}
