package com.workout.app.presentation.workout

import com.workout.app.data.repository.SessionExerciseRepository
import com.workout.app.data.repository.SessionRepository
import com.workout.app.data.repository.SetRepository
import com.workout.app.domain.model.Result
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for active workout session.
 * Manages session state machine, set completion, rest timers, and navigation.
 */
class WorkoutViewModel(
    private val sessionId: String,
    private val sessionRepository: SessionRepository,
    private val sessionExerciseRepository: SessionExerciseRepository,
    private val setRepository: SetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutState())
    val state: StateFlow<WorkoutState> = _state.asStateFlow()

    private var sessionStartTime: Long = 0L
    private var restTimerJob: kotlinx.coroutines.Job? = null

    init {
        loadSession()
        startElapsedTimer()
    }

    private fun loadSession() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Get session info
            when (val sessionResult = sessionRepository.getById(sessionId)) {
                is Result.Success -> {
                    val session = sessionResult.data
                    if (session != null) {
                        sessionStartTime = session.startTime

                        // Get session exercises
                        when (val exercisesResult = sessionExerciseRepository.getBySession(sessionId)) {
                            is Result.Success -> {
                                val exercises = exercisesResult.data.map { se ->
                                    WorkoutExercise(
                                        id = se.id,
                                        exerciseId = se.exerciseId,
                                        name = se.exerciseName,
                                        muscleGroup = se.muscleGroup,
                                        targetSets = se.targetSets,
                                        completedSets = se.completedSets
                                    )
                                }

                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        sessionName = session.name,
                                        exercises = exercises,
                                        currentExerciseIndex = session.currentExerciseIndex.toInt(),
                                        error = null
                                    )
                                }
                            }
                            is Result.Error -> {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        error = exercisesResult.exception.message ?: "Failed to load exercises"
                                    )
                                }
                            }
                            is Result.Loading -> { }
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Session not found"
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = sessionResult.exception.message ?: "Failed to load session"
                        )
                    }
                }
                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    private fun startElapsedTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update {
                    it.copy(elapsedSeconds = it.elapsedSeconds + 1)
                }
            }
        }
    }

    fun updateReps(reps: Int) {
        _state.update { it.copy(currentReps = reps) }
    }

    fun updateWeight(weight: Float) {
        _state.update { it.copy(currentWeight = weight) }
    }

    fun updateRPE(rpe: Int?) {
        _state.update { it.copy(currentRPE = rpe) }
    }

    fun updateNotes(notes: String) {
        _state.update { it.copy(currentNotes = notes) }
    }

    fun completeSet() {
        viewModelScope.launch {
            val currentState = _state.value
            val currentExercise = currentState.exercises.getOrNull(currentState.currentExerciseIndex)
                ?: return@launch

            // Save set to database
            val setResult = setRepository.createSet(
                sessionId = sessionId,
                sessionExerciseId = currentExercise.id,
                exerciseId = currentExercise.exerciseId,
                setNumber = currentExercise.completedSets + 1,
                weight = currentState.currentWeight.toDouble(),
                reps = currentState.currentReps,
                rpe = currentState.currentRPE,
                isWarmup = false,
                notes = currentState.currentNotes.takeIf { it.isNotBlank() }
            )

            if (setResult is Result.Error) {
                _state.update {
                    it.copy(error = setResult.exception.message ?: "Failed to save set")
                }
                return@launch
            }

            // Increment completed sets in database
            sessionExerciseRepository.incrementCompletedSets(currentExercise.id)

            // Update UI state
            val updatedExercises = currentState.exercises.toMutableList()
            val updated = currentExercise.copy(
                completedSets = currentExercise.completedSets + 1
            )
            updatedExercises[currentState.currentExerciseIndex] = updated

            _state.update {
                it.copy(
                    exercises = updatedExercises,
                    currentReps = 0,
                    currentWeight = 0f,
                    currentRPE = null,
                    currentNotes = ""
                )
            }

            // Check if exercise is complete
            if (updated.completedSets >= updated.targetSets) {
                // Move to next exercise if available
                if (currentState.currentExerciseIndex < currentState.exercises.size - 1) {
                    _state.update {
                        it.copy(currentExerciseIndex = it.currentExerciseIndex + 1)
                    }
                }
            }

            // Start rest timer
            startRestTimer()
        }
    }

    fun skipSet() {
        val currentState = _state.value
        val currentExercise = currentState.exercises.getOrNull(currentState.currentExerciseIndex)
            ?: return

        // Mark as skipped and move to next exercise if this was the last set
        val updatedExercises = currentState.exercises.toMutableList()
        val updated = currentExercise.copy(
            completedSets = currentExercise.completedSets + 1
        )
        updatedExercises[currentState.currentExerciseIndex] = updated

        _state.update {
            it.copy(
                exercises = updatedExercises,
                currentReps = 0,
                currentWeight = 0f,
                currentRPE = null,
                currentNotes = ""
            )
        }

        if (updated.completedSets >= updated.targetSets &&
            currentState.currentExerciseIndex < currentState.exercises.size - 1) {
            _state.update {
                it.copy(currentExerciseIndex = it.currentExerciseIndex + 1)
            }
        }
    }

    private fun startRestTimer(durationSeconds: Int = 90) {
        restTimerJob?.cancel()
        _state.update {
            it.copy(
                restTimerRemaining = durationSeconds,
                isRestTimerActive = true
            )
        }

        restTimerJob = viewModelScope.launch {
            var remaining = durationSeconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _state.update { it.copy(restTimerRemaining = remaining) }
            }
            _state.update { it.copy(isRestTimerActive = false, restTimerRemaining = 0) }
        }
    }

    fun skipRest() {
        restTimerJob?.cancel()
        _state.update {
            it.copy(
                isRestTimerActive = false,
                restTimerRemaining = 0
            )
        }
    }

    fun addRestTime(seconds: Int) {
        val currentRemaining = _state.value.restTimerRemaining
        _state.update {
            it.copy(restTimerRemaining = currentRemaining + seconds)
        }
    }

    suspend fun finishWorkout(): Result<Unit> {
        _state.update { it.copy(isFinishing = true) }

        return try {
            when (val result = sessionRepository.complete(sessionId)) {
                is Result.Success -> {
                    _state.update { it.copy(isFinishing = false, isFinished = true) }
                    Result.Success(Unit)
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isFinishing = false,
                            error = result.exception.message ?: "Failed to finish workout"
                        )
                    }
                    result
                }
                is Result.Loading -> {
                    Result.Loading
                }
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isFinishing = false,
                    error = e.message ?: "Failed to finish workout"
                )
            }
            Result.Error(e)
        }
    }

    suspend fun saveAndExit(): Result<Unit> {
        _state.update { it.copy(isSaving = true) }

        return try {
            // Save current progress but keep status as 'paused'
            when (val result = sessionRepository.updateStatus(sessionId, "paused")) {
                is Result.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    Result.Success(Unit)
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = result.exception.message ?: "Failed to save session"
                        )
                    }
                    result
                }
                is Result.Loading -> {
                    Result.Loading
                }
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save session"
                )
            }
            Result.Error(e)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        restTimerJob?.cancel()
    }
}

/**
 * Exercise data for active workout.
 */
data class WorkoutExercise(
    val id: String,           // SessionExercise ID
    val exerciseId: String,   // Exercise ID (for saving sets)
    val name: String,
    val muscleGroup: String,
    val targetSets: Int,
    val completedSets: Int = 0
)

/**
 * UI state for active workout session.
 */
data class WorkoutState(
    val isLoading: Boolean = false,
    val isFinishing: Boolean = false,
    val isSaving: Boolean = false,
    val isFinished: Boolean = false,
    val sessionName: String = "",
    val exercises: List<WorkoutExercise> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val elapsedSeconds: Int = 0,
    val currentReps: Int = 0,
    val currentWeight: Float = 0f,
    val currentRPE: Int? = null,
    val currentNotes: String = "",
    val isRestTimerActive: Boolean = false,
    val restTimerRemaining: Int = 0,
    val error: String? = null
) {
    /**
     * Current exercise being performed.
     */
    val currentExercise: WorkoutExercise?
        get() = exercises.getOrNull(currentExerciseIndex)

    /**
     * Next exercise to be performed.
     */
    val nextExercise: WorkoutExercise?
        get() = exercises.getOrNull(currentExerciseIndex + 1)

    /**
     * Whether all exercises are complete.
     */
    val isWorkoutComplete: Boolean
        get() = exercises.isNotEmpty() && exercises.all { it.completedSets >= it.targetSets }

    /**
     * Whether set can be completed (has reps entered).
     */
    val canCompleteSet: Boolean
        get() = currentReps > 0 && !isFinishing && !isSaving

    /**
     * Total sets completed across all exercises.
     */
    val totalSetsCompleted: Int
        get() = exercises.sumOf { it.completedSets }

    /**
     * Total target sets across all exercises.
     */
    val totalTargetSets: Int
        get() = exercises.sumOf { it.targetSets }
}
