package com.workout.app.presentation.planning

import com.workout.app.data.repository.AddedExerciseInput
import com.workout.app.data.repository.ExerciseRepository
import com.workout.app.data.repository.SessionExerciseRepository
import com.workout.app.data.repository.SessionRepository
import com.workout.app.data.repository.SettingsRepository
import com.workout.app.data.repository.SetRepository
import com.workout.app.database.Exercise
import com.workout.app.domain.model.MuscleRecovery
import com.workout.app.domain.model.RecoveryTimeRange
import com.workout.app.domain.model.Result
import com.workout.app.domain.model.SessionMode
import com.workout.app.domain.model.SessionParticipant
import com.workout.app.domain.model.calculateRecoveryProgress
import com.workout.app.domain.model.calculateRecoveryStatus
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ViewModel for Session Planning screen.
 * Manages exercise selection and session creation.
 */
class SessionPlanningViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val sessionRepository: SessionRepository,
    private val sessionExerciseRepository: SessionExerciseRepository,
    private val setRepository: SetRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SessionPlanningState())
    val state: StateFlow<SessionPlanningState> = _state.asStateFlow()

    init {
        loadExercises()
        loadMuscleRecovery()
        loadRecentPartners()
    }

    companion object {
        private const val RECENT_PARTNERS_KEY = "recent_partners"
        private const val MAX_RECENT_PARTNERS = 10
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

    // Cached data for recalculation
    private var muscleGroupDays: Map<String, Int?> = emptyMap()
    private var muscleGroupHours: Map<String, Int?> = emptyMap()
    private var muscleGroupWeeklySets: Map<String, Int> = emptyMap()

    private fun loadMuscleRecovery() {
        viewModelScope.launch {
            val muscleGroups = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core")

            // Load last trained dates
            when (val result = setRepository.getLastTrainedPerMuscleGroup()) {
                is Result.Success -> {
                    val nowMillis = Clock.System.now().toEpochMilliseconds()
                    muscleGroupDays = muscleGroups.associateWith { group ->
                        val lastTrained = result.data[group]
                        if (lastTrained != null) {
                            ((nowMillis - lastTrained) / (1000L * 60 * 60 * 24)).toInt()
                        } else {
                            null
                        }
                    }
                    muscleGroupHours = muscleGroups.associateWith { group ->
                        val lastTrained = result.data[group]
                        if (lastTrained != null) {
                            ((nowMillis - lastTrained) / (1000L * 60 * 60)).toInt()
                        } else {
                            null
                        }
                    }
                }
                is Result.Error -> { /* silently ignore */ }
                is Result.Loading -> {}
            }

            // Load weekly set counts
            when (val result = setRepository.getWeeklySetCountPerMuscleGroup()) {
                is Result.Success -> {
                    muscleGroupWeeklySets = result.data.mapValues { it.value.toInt() }
                }
                is Result.Error -> { /* silently ignore */ }
                is Result.Loading -> {}
            }

            rebuildRecoveryList()
        }
    }

    private fun rebuildRecoveryList() {
        val recoveryList = muscleGroupDays.map { (group, daysSince) ->
            val weeklySets = muscleGroupWeeklySets[group] ?: 0
            MuscleRecovery(
                muscleGroup = group,
                daysSinceLastTrained = daysSince,
                hoursSinceLastTrained = muscleGroupHours[group],
                status = calculateRecoveryStatus(group, daysSince, weeklySets),
                progress = calculateRecoveryProgress(group, daysSince, weeklySets),
                weeklySetCount = weeklySets
            )
        }
        _state.update { it.copy(muscleRecovery = recoveryList) }
    }

    fun toggleTimeRange() {
        _state.update { it.copy(recoveryTimeRange = it.recoveryTimeRange.next()) }
        rebuildRecoveryList()
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

    fun updateExerciseRecording(
        exerciseId: String,
        recordingFields: List<com.workout.app.domain.model.RecordingField>?,
        targetValues: Map<String, String>?
    ) {
        val currentAdded = _state.value.addedExercises.toMutableMap()
        val existing = currentAdded[exerciseId] ?: return
        currentAdded[exerciseId] = existing.copy(
            recordingFields = recordingFields,
            targetValues = targetValues
        )
        _state.update { it.copy(addedExercises = currentAdded) }
    }

    // --- Session Mode & Participants ---

    private fun loadRecentPartners() {
        viewModelScope.launch {
            val stored = settingsRepository.getString(RECENT_PARTNERS_KEY)
            if (stored != null) {
                val names = stored.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                _state.update { it.copy(recentPartners = names) }
            }
        }
    }

    private suspend fun saveRecentPartners(names: List<String>) {
        val trimmed = names.distinct().take(MAX_RECENT_PARTNERS)
        settingsRepository.setString(RECENT_PARTNERS_KEY, trimmed.joinToString(","))
        _state.update { it.copy(recentPartners = trimmed) }
    }

    fun setSessionMode(mode: SessionMode) {
        _state.update { current ->
            val newParticipants = when (mode) {
                SessionMode.SOLO -> emptyList()
                SessionMode.GROUP -> {
                    if (current.participants.none { it.isOwner }) {
                        listOf(
                            SessionParticipant(id = "owner", name = "You", isOwner = true)
                        ) + current.participants.filter { !it.isOwner }
                    } else {
                        current.participants
                    }
                }
                SessionMode.COACHING -> {
                    current.participants.filter { !it.isOwner }
                }
            }
            current.copy(sessionMode = mode, participants = newParticipants)
        }
    }

    fun addParticipant(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return

        val id = "participant_${Clock.System.now().toEpochMilliseconds()}"

        _state.update { current ->
            if (current.participants.any { it.name.equals(trimmedName, ignoreCase = true) && !it.isOwner }) {
                return@update current
            }
            current.copy(
                participants = current.participants + SessionParticipant(id = id, name = trimmedName)
            )
        }

        viewModelScope.launch {
            val current = _state.value.recentPartners.toMutableList()
            current.remove(trimmedName)
            current.add(0, trimmedName)
            saveRecentPartners(current)
        }
    }

    fun removeParticipant(participantId: String) {
        _state.update { current ->
            current.copy(participants = current.participants.filter { it.id != participantId })
        }
    }

    fun showAddParticipantSheet() {
        _state.update { it.copy(showAddParticipantSheet = true) }
    }

    fun hideAddParticipantSheet() {
        _state.update { it.copy(showAddParticipantSheet = false) }
    }

    suspend fun createSession(sessionName: String): Result<String> {
        val currentState = _state.value
        val addedExercises = currentState.addedExercises

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
                isPartnerWorkout = currentState.sessionMode != SessionMode.SOLO
            )

            when (sessionResult) {
                is Result.Success -> {
                    val sessionId = sessionResult.data

                    // Add exercises to session
                    val exerciseInputs = addedExercises.entries.mapIndexed { index, entry ->
                        AddedExerciseInput(
                            exerciseId = entry.key,
                            targetSets = entry.value.setCount,
                            orderIndex = index,
                            targetValues = com.workout.app.domain.model.fieldValuesToJson(entry.value.targetValues)
                        )
                    }

                    val addResult = sessionExerciseRepository.addExercisesToSession(
                        sessionId = sessionId,
                        exercises = exerciseInputs
                    )

                    when (addResult) {
                        is Result.Success -> {
                            // Persist participant data for the workout screen
                            if (currentState.sessionMode != SessionMode.SOLO && currentState.participants.isNotEmpty()) {
                                val participantsJson = currentState.participants.joinToString(";") {
                                    "${it.id},${it.name},${it.isOwner}"
                                }
                                settingsRepository.setString("session_${sessionId}_participants", participantsJson)
                                settingsRepository.setString("session_${sessionId}_mode", currentState.sessionMode.name)
                            }
                            _state.update { it.copy(isCreatingSession = false) }
                            Result.Success(sessionId)
                        }
                        is Result.Error -> {
                            // Attempt to clean up the created session on failure
                            sessionRepository.delete(sessionId)
                            _state.update {
                                it.copy(
                                    isCreatingSession = false,
                                    error = addResult.exception.message ?: "Failed to add exercises"
                                )
                            }
                            addResult
                        }
                        is Result.Loading -> Result.Loading
                    }
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

    fun expandExercise(exerciseId: String) {
        _state.update {
            it.copy(
                expandedExerciseId = exerciseId,
                expandedExerciseLastSummary = null,
                isLoadingLastWorkout = true
            )
        }
        viewModelScope.launch {
            val summary = when (val result = setRepository.getByExercise(exerciseId)) {
                is Result.Success -> {
                    val sets = result.data
                    if (sets.isNotEmpty()) {
                        val first = sets.first()
                        val fieldValues = first.fieldValues
                        if (fieldValues != null && fieldValues.isNotEmpty()) {
                            fieldValues.entries.joinToString(", ") { "${it.key}: ${it.value}" }
                        } else {
                            "${first.weight}kg x ${first.reps} reps"
                        }
                    } else {
                        null
                    }
                }
                else -> null
            }
            // Only update if this exercise is still the expanded one
            if (_state.value.expandedExerciseId == exerciseId) {
                _state.update {
                    it.copy(
                        expandedExerciseLastSummary = summary,
                        isLoadingLastWorkout = false
                    )
                }
            }
        }
    }

    fun collapseExercise() {
        _state.update {
            it.copy(
                expandedExerciseId = null,
                expandedExerciseLastSummary = null,
                isLoadingLastWorkout = false
            )
        }
    }

    /**
     * Dismiss the currently expanded exercise: collapse and remove it if it was added.
     * Used when the user taps outside the expanded card (cancels the selection).
     */
    fun dismissExpandedExercise() {
        val expandedId = _state.value.expandedExerciseId ?: return
        val currentAdded = _state.value.addedExercises.toMutableMap()
        currentAdded.remove(expandedId)
        _state.update {
            it.copy(
                addedExercises = currentAdded,
                expandedExerciseId = null,
                expandedExerciseLastSummary = null,
                isLoadingLastWorkout = false
            )
        }
    }

    fun addExerciseWithPreset(exerciseId: String, preset: ExercisePreset) {
        viewModelScope.launch {
            val targetValues: Map<String, String>? = when (preset) {
                ExercisePreset.RECOMMENDED -> mapOf("weight" to "0", "reps" to "10")
                ExercisePreset.LAST_WORKOUT -> {
                    when (val result = setRepository.getByExercise(exerciseId)) {
                        is Result.Success -> result.data.firstOrNull()?.fieldValues
                        else -> null
                    }
                }
                ExercisePreset.EMPTY -> null
            }
            val currentAdded = _state.value.addedExercises.toMutableMap()
            currentAdded[exerciseId] = AddedExerciseData(
                exerciseId = exerciseId,
                setCount = 3,
                targetValues = targetValues
            )
            _state.update {
                it.copy(
                    addedExercises = currentAdded
                )
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

/**
 * Data class for added exercise with set count and optional recording configuration.
 */
data class AddedExerciseData(
    val exerciseId: String,
    val setCount: Int,
    val recordingFields: List<com.workout.app.domain.model.RecordingField>? = null,
    val targetValues: Map<String, String>? = null
)

/**
 * Preset options for how exercise input fields should be pre-filled.
 */
enum class ExercisePreset { RECOMMENDED, LAST_WORKOUT, EMPTY }

/**
 * UI state for Session Planning screen.
 */
data class SessionPlanningState(
    val isLoading: Boolean = false,
    val isCreatingSession: Boolean = false,
    val allExercises: List<Exercise> = emptyList(),
    val addedExercises: Map<String, AddedExerciseData> = emptyMap(),
    val selectedMuscleGroup: String? = null,
    val muscleRecovery: List<MuscleRecovery> = emptyList(),
    val recoveryTimeRange: RecoveryTimeRange = RecoveryTimeRange.WEEKLY,
    val error: String? = null,
    val sessionMode: SessionMode = SessionMode.SOLO,
    val participants: List<SessionParticipant> = emptyList(),
    val recentPartners: List<String> = emptyList(),
    val showAddParticipantSheet: Boolean = false,
    val expandedExerciseId: String? = null,
    val expandedExerciseLastSummary: String? = null,
    val isLoadingLastWorkout: Boolean = false
) {
    val filteredExercises: List<Exercise>
        get() = if (selectedMuscleGroup == null) {
            allExercises
        } else {
            allExercises.filter { it.muscleGroup == selectedMuscleGroup }
        }

    val totalSets: Int
        get() = addedExercises.values.sumOf { it.setCount }

    val canStartSession: Boolean
        get() {
            if (addedExercises.isEmpty() || isCreatingSession) return false
            return when (sessionMode) {
                SessionMode.SOLO -> true
                SessionMode.COACHING -> participants.isNotEmpty()
                SessionMode.GROUP -> participants.size > 1
            }
        }

    val participantHelperText: String?
        get() = when (sessionMode) {
            SessionMode.SOLO -> null
            SessionMode.COACHING -> {
                val count = participants.size
                if (count == 0) "Add clients to record for"
                else "Recording for $count client(s)"
            }
            SessionMode.GROUP -> {
                if (participants.size <= 1) "Add friends to work out with"
                else "Working out together"
            }
        }
}
