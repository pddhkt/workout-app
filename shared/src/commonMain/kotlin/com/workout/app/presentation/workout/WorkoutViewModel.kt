package com.workout.app.presentation.workout

import com.workout.app.data.repository.AddedExerciseInput
import com.workout.app.data.repository.ExerciseRepository
import com.workout.app.data.repository.SessionExerciseRepository
import com.workout.app.data.repository.SessionRepository
import com.workout.app.data.repository.SetRepository
import com.workout.app.ui.components.exercise.getMockLibraryExercises
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
    private val setRepository: SetRepository,
    private val exerciseRepository: ExerciseRepository
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
                                    val prevPerf = loadPreviousPerformance(se.exerciseId)
                                    WorkoutExercise(
                                        id = se.id,
                                        exerciseId = se.exerciseId,
                                        name = se.exerciseName,
                                        muscleGroup = se.muscleGroup,
                                        targetSets = se.targetSets,
                                        completedSets = se.completedSets,
                                        previousPerformance = prevPerf
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

    private suspend fun loadPreviousPerformance(exerciseId: String): String? {
        return when (val result = setRepository.getPreviousByExercise(exerciseId, sessionId, 5)) {
            is Result.Success -> {
                val sets = result.data
                if (sets.isEmpty()) return "First time"
                // Group by sessionId to find the most recent session's sets
                // Since results are ordered by completedAt DESC, the first set's session is most recent
                // Find the best set by weight from that group
                val bestSet = sets.maxByOrNull { it.weight }
                if (bestSet != null) {
                    val weightStr = if (bestSet.weight == bestSet.weight.toLong().toDouble()) {
                        bestSet.weight.toLong().toString()
                    } else {
                        bestSet.weight.toString()
                    }
                    "Previous: ${weightStr}kg x ${bestSet.reps}"
                } else {
                    "First time"
                }
            }
            else -> null
        }
    }

    /**
     * Get historical weight values for an exercise (for number pad quick selection).
     * Returns distinct values sorted by most recent.
     */
    suspend fun getHistoricalWeights(exerciseId: String): List<String> {
        return when (val result = setRepository.getPreviousByExercise(exerciseId, sessionId, 10)) {
            is Result.Success -> {
                result.data
                    .map { it.weight }
                    .distinct()
                    .take(5)
                    .map { weight ->
                        // Format: remove decimal if whole number
                        if (weight == weight.toLong().toDouble()) {
                            weight.toLong().toString()
                        } else {
                            weight.toString()
                        }
                    }
            }
            else -> emptyList()
        }
    }

    /**
     * Get historical reps values for an exercise (for number pad quick selection).
     * Returns distinct values sorted by most recent.
     */
    suspend fun getHistoricalReps(exerciseId: String): List<String> {
        return when (val result = setRepository.getPreviousByExercise(exerciseId, sessionId, 10)) {
            is Result.Success -> {
                result.data
                    .map { it.reps }
                    .distinct()
                    .take(5)
                    .map { it.toString() }
            }
            else -> emptyList()
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

    fun completeSet(exerciseId: String, setNumber: Int) {
        viewModelScope.launch {
            val currentState = _state.value
            val exerciseIndex = currentState.exercises.indexOfFirst { it.id == exerciseId }
            if (exerciseIndex == -1) return@launch
            val exercise = currentState.exercises[exerciseIndex]

            // Check if this set was already completed (editing an existing record)
            val existingRecordIndex = exercise.setRecords.indexOfFirst { it.setNumber == setNumber }
            val isEdit = existingRecordIndex != -1

            // Save set to database (only for new completions)
            if (!isEdit) {
                val setResult = setRepository.createSet(
                    sessionId = sessionId,
                    sessionExerciseId = exercise.id,
                    exerciseId = exercise.exerciseId,
                    setNumber = setNumber,
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
                sessionExerciseRepository.incrementCompletedSets(exercise.id)
            }

            // Create set record with current values
            val setRecord = CompletedSetRecord(
                setNumber = setNumber,
                weight = currentState.currentWeight,
                reps = currentState.currentReps,
                rpe = currentState.currentRPE
            )

            // Update UI state - replace existing record or append new one
            val updatedSetRecords = if (isEdit) {
                exercise.setRecords.toMutableList().apply {
                    this[existingRecordIndex] = setRecord
                }
            } else {
                exercise.setRecords + setRecord
            }

            val updatedExercises = currentState.exercises.toMutableList()
            val updated = exercise.copy(
                completedSets = if (isEdit) exercise.completedSets else exercise.completedSets + 1,
                setRecords = updatedSetRecords,
                userSelectedSetIndex = null  // Clear after completion
            )
            updatedExercises[exerciseIndex] = updated

            _state.update {
                it.copy(
                    exercises = updatedExercises,
                    currentReps = 0,
                    currentWeight = 0f,
                    currentRPE = null,
                    currentNotes = ""
                )
            }

            // Only advance currentExerciseIndex if the completed exercise IS the current one
            if (exerciseIndex == currentState.currentExerciseIndex &&
                updated.completedSets >= updated.targetSets &&
                currentState.currentExerciseIndex < currentState.exercises.size - 1) {
                _state.update {
                    it.copy(currentExerciseIndex = it.currentExerciseIndex + 1)
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

    fun addSetToExercise(exerciseId: String) {
        viewModelScope.launch {
            val currentState = _state.value
            val exerciseIndex = currentState.exercises.indexOfFirst { it.id == exerciseId }
            if (exerciseIndex == -1) return@launch

            val exercise = currentState.exercises[exerciseIndex]
            val newTargetSets = exercise.targetSets + 1

            // Persist to database
            val result = sessionExerciseRepository.updateTargetSets(exercise.id, newTargetSets)
            if (result is Result.Error) {
                _state.update { it.copy(error = result.exception.message ?: "Failed to add set") }
                return@launch
            }

            // Update UI state
            val updatedExercises = currentState.exercises.toMutableList()
            updatedExercises[exerciseIndex] = exercise.copy(targetSets = newTargetSets)
            _state.update { it.copy(exercises = updatedExercises) }
        }
    }

    fun addExercises(exerciseIds: List<String>) {
        viewModelScope.launch {
            val currentState = _state.value
            val currentMaxOrder = currentState.exercises.size

            // Create input list for repository
            val inputs = exerciseIds.mapIndexed { index, exerciseId ->
                AddedExerciseInput(
                    exerciseId = exerciseId,
                    targetSets = 3,
                    orderIndex = currentMaxOrder + index
                )
            }

            // Add to database
            sessionExerciseRepository.addExercisesToSession(sessionId, inputs)

            // Update UI state with new exercises
            val newExercises = exerciseIds.mapIndexed { index, exerciseId ->
                val exercise = getMockLibraryExercises().find { it.id == exerciseId }
                WorkoutExercise(
                    id = "${sessionId}_${currentMaxOrder + index}",
                    exerciseId = exerciseId,
                    name = exercise?.name ?: "Unknown",
                    muscleGroup = exercise?.muscleGroup ?: "Other",
                    targetSets = 3,
                    completedSets = 0,
                    setRecords = emptyList()
                )
            }

            _state.update {
                it.copy(exercises = it.exercises + newExercises)
            }
        }
    }

    /**
     * Delete an exercise from the session.
     * Removes from both database and UI, adjusting currentExerciseIndex as needed.
     */
    fun deleteExercise(sessionExerciseId: String) {
        viewModelScope.launch {
            val currentState = _state.value
            val exerciseIndex = currentState.exercises.indexOfFirst { it.id == sessionExerciseId }
            if (exerciseIndex == -1) return@launch

            // Delete from database
            val result = sessionExerciseRepository.deleteById(sessionExerciseId)

            if (result is Result.Error) {
                _state.update { it.copy(error = result.exception.message ?: "Failed to delete exercise") }
                return@launch
            }

            // Update UI state
            val updatedExercises = currentState.exercises.toMutableList()
            updatedExercises.removeAt(exerciseIndex)

            // Adjust currentExerciseIndex if needed
            val newCurrentIndex = when {
                updatedExercises.isEmpty() -> 0
                currentState.currentExerciseIndex >= updatedExercises.size -> updatedExercises.size - 1
                currentState.currentExerciseIndex > exerciseIndex -> currentState.currentExerciseIndex - 1
                else -> currentState.currentExerciseIndex
            }

            _state.update {
                it.copy(
                    exercises = updatedExercises,
                    currentExerciseIndex = newCurrentIndex
                )
            }
        }
    }

    /**
     * Replace an exercise with a different one.
     * Preserves targetSets but resets progress (completed sets, records, etc.).
     */
    fun replaceExercise(
        sessionExerciseId: String,
        newExerciseId: String,
        newExerciseName: String,
        newMuscleGroup: String
    ) {
        viewModelScope.launch {
            val currentState = _state.value
            val exerciseIndex = currentState.exercises.indexOfFirst { it.id == sessionExerciseId }
            if (exerciseIndex == -1) return@launch

            val currentExercise = currentState.exercises[exerciseIndex]

            // Update in database - we need to update the exerciseId field
            // Since we don't have a direct update method, delete and re-add
            val deleteResult = sessionExerciseRepository.deleteById(sessionExerciseId)
            if (deleteResult is Result.Error) {
                _state.update { it.copy(error = deleteResult.exception.message ?: "Failed to replace exercise") }
                return@launch
            }

            // Add the new exercise at the same position
            val addResult = sessionExerciseRepository.addExercisesToSession(
                sessionId,
                listOf(AddedExerciseInput(
                    exerciseId = newExerciseId,
                    targetSets = currentExercise.targetSets,
                    orderIndex = exerciseIndex
                ))
            )

            if (addResult is Result.Error) {
                _state.update { it.copy(error = "Failed to add replacement exercise") }
                return@launch
            }

            // Fetch all session exercises to get the newly created ID
            when (val exercisesResult = sessionExerciseRepository.getBySession(sessionId)) {
                is Result.Success -> {
                    // Find the exercise at the expected order index with the new exercise ID
                    val newSessionExercise = exercisesResult.data.find {
                        it.exerciseId == newExerciseId && it.orderIndex == exerciseIndex
                    }

                    if (newSessionExercise != null) {
                        // Load previous performance for the new exercise
                        val prevPerf = loadPreviousPerformance(newExerciseId)

                        // Update UI state - replace the exercise
                        val updatedExercises = currentState.exercises.toMutableList()
                        updatedExercises[exerciseIndex] = currentExercise.copy(
                            id = newSessionExercise.id,
                            exerciseId = newExerciseId,
                            name = newExerciseName,
                            muscleGroup = newMuscleGroup,
                            completedSets = 0,  // Reset progress
                            setRecords = emptyList(),  // Clear records
                            previousPerformance = prevPerf
                        )

                        _state.update { it.copy(exercises = updatedExercises) }
                    } else {
                        _state.update { it.copy(error = "Failed to find replacement exercise") }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(error = exercisesResult.exception.message ?: "Failed to reload exercises")
                    }
                }
                is Result.Loading -> { }
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

    fun setActiveSet(exerciseId: String, setIndex: Int) {
        _state.update { currentState ->
            val exerciseIndex = currentState.exercises.indexOfFirst { it.id == exerciseId }
            if (exerciseIndex == -1) return@update currentState

            val updatedExercises = currentState.exercises.toMutableList()
            // Clear userSelectedSetIndex from all other exercises
            updatedExercises.forEachIndexed { index, exercise ->
                if (index != exerciseIndex && exercise.userSelectedSetIndex != null) {
                    updatedExercises[index] = exercise.copy(userSelectedSetIndex = null)
                }
            }
            // Set the active set for the clicked exercise
            updatedExercises[exerciseIndex] = updatedExercises[exerciseIndex].copy(
                userSelectedSetIndex = setIndex
            )

            currentState.copy(exercises = updatedExercises)
        }
    }

    /**
     * Create a custom exercise and add it to the current session.
     * Returns the created exercise ID on success.
     */
    suspend fun createCustomExercise(
        name: String,
        muscleGroup: String,
        equipment: String? = null,
        instructions: String? = null,
        targetSets: Int = 3
    ): Result<String> {
        // Create the exercise in the database
        val exerciseResult = exerciseRepository.create(
            name = name,
            muscleGroup = muscleGroup,
            equipment = equipment,
            instructions = instructions
        )

        return when (exerciseResult) {
            is Result.Success -> {
                val exerciseId = exerciseResult.data

                // Add to current session
                val currentState = _state.value
                val currentMaxOrder = currentState.exercises.size

                val addResult = sessionExerciseRepository.addExercisesToSession(
                    sessionId,
                    listOf(
                        AddedExerciseInput(
                            exerciseId = exerciseId,
                            targetSets = targetSets,
                            orderIndex = currentMaxOrder
                        )
                    )
                )

                when (addResult) {
                    is Result.Success -> {
                        // Update UI state
                        val newExercise = WorkoutExercise(
                            id = "${sessionId}_${currentMaxOrder}",
                            exerciseId = exerciseId,
                            name = name,
                            muscleGroup = muscleGroup,
                            targetSets = targetSets,
                            completedSets = 0,
                            setRecords = emptyList()
                        )

                        _state.update {
                            it.copy(exercises = it.exercises + newExercise)
                        }

                        Result.Success(exerciseId)
                    }
                    is Result.Error -> addResult
                    is Result.Loading -> Result.Loading
                }
            }
            is Result.Error -> exerciseResult
            is Result.Loading -> Result.Loading
        }
    }

    /**
     * Reorder exercises by moving one from fromIndex to toIndex.
     * Updates both UI state and persists to database.
     */
    fun reorderExercises(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return

        viewModelScope.launch {
            val currentState = _state.value
            if (fromIndex < 0 || fromIndex >= currentState.exercises.size ||
                toIndex < 0 || toIndex >= currentState.exercises.size) {
                return@launch
            }

            // Create new list with reordered exercises
            val updatedExercises = currentState.exercises.toMutableList()
            val movedExercise = updatedExercises.removeAt(fromIndex)
            updatedExercises.add(toIndex, movedExercise)

            // Update UI state immediately for smooth UX
            _state.update { it.copy(exercises = updatedExercises) }

            // Persist new order to database
            updatedExercises.forEachIndexed { index, exercise ->
                val result = sessionExerciseRepository.updateOrderIndex(exercise.id, index)
                if (result is Result.Error) {
                    // Revert on error
                    _state.update {
                        it.copy(
                            exercises = currentState.exercises,
                            error = "Failed to save exercise order"
                        )
                    }
                    return@launch
                }
            }

            // Update currentExerciseIndex if the currently active exercise moved
            val newCurrentIndex = when {
                currentState.currentExerciseIndex == fromIndex -> toIndex
                fromIndex < currentState.currentExerciseIndex && toIndex >= currentState.currentExerciseIndex ->
                    currentState.currentExerciseIndex - 1
                fromIndex > currentState.currentExerciseIndex && toIndex <= currentState.currentExerciseIndex ->
                    currentState.currentExerciseIndex + 1
                else -> currentState.currentExerciseIndex
            }

            if (newCurrentIndex != currentState.currentExerciseIndex) {
                _state.update { it.copy(currentExerciseIndex = newCurrentIndex) }
            }
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
 * Record of a completed set with its actual values.
 */
data class CompletedSetRecord(
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val rpe: Int?
)

/**
 * Exercise data for active workout.
 */
data class WorkoutExercise(
    val id: String,           // SessionExercise ID
    val exerciseId: String,   // Exercise ID (for saving sets)
    val name: String,
    val muscleGroup: String,
    val targetSets: Int,
    val completedSets: Int = 0,
    val setRecords: List<CompletedSetRecord> = emptyList(),
    val previousPerformance: String? = null,
    val userSelectedSetIndex: Int? = null  // User-selected active set
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
