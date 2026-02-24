package com.workout.app.presentation.goals

import com.workout.app.data.repository.ExerciseRepository
import com.workout.app.data.repository.GoalRepository
import com.workout.app.domain.model.GoalFrequency
import com.workout.app.domain.model.GoalMetric
import com.workout.app.domain.model.RecordingField
import com.workout.app.domain.model.Result
import com.workout.app.domain.model.parseExerciseIds
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ViewModel for Goal Create/Edit screen.
 */
class GoalCreateEditViewModel(
    private val goalId: String?,
    private val goalRepository: GoalRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GoalFormState())
    val state: StateFlow<GoalFormState> = _state.asStateFlow()

    init {
        loadExercises()
        if (goalId != null) {
            loadGoal(goalId)
        }
    }

    private fun loadExercises() {
        viewModelScope.launch {
            exerciseRepository.observeAll()
                .catch { /* ignore errors for exercise list */ }
                .collect { exercises ->
                    val selections = exercises.map { exercise ->
                        val fields = if (!exercise.recordingFields.isNullOrBlank()) {
                            RecordingField.fromJsonArray(exercise.recordingFields!!) ?: RecordingField.DEFAULT_FIELDS
                        } else {
                            RecordingField.DEFAULT_FIELDS
                        }
                        ExerciseSelection(
                            id = exercise.id,
                            name = exercise.name,
                            muscleGroup = exercise.muscleGroup,
                            recordingFields = fields
                        )
                    }
                    _state.update { it.copy(availableExercises = selections) }
                }
        }
    }

    private fun loadGoal(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = goalRepository.getById(id)) {
                is Result.Success -> {
                    val goal = result.data
                    if (goal != null) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isEditing = true,
                                name = goal.name,
                                selectedExerciseIds = goal.exerciseIds.toSet(),
                                metric = goal.metric,
                                targetValue = goal.targetValue.toBigDecimal().stripTrailingZeros().toPlainString(),
                                targetUnit = goal.targetUnit,
                                frequency = goal.frequency,
                                isOngoing = goal.endDate == null,
                                endDate = goal.endDate,
                                autoTrack = goal.autoTrack
                            )
                        }
                        updateAvailableMetrics()
                    } else {
                        _state.update {
                            it.copy(isLoading = false, error = "Goal not found")
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to load goal"
                        )
                    }
                }
                is Result.Loading -> { }
            }
        }
    }

    fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun addExercise(exerciseId: String) {
        _state.update {
            it.copy(selectedExerciseIds = it.selectedExerciseIds + exerciseId)
        }
        updateAvailableMetrics()
    }

    fun removeExercise(exerciseId: String) {
        _state.update {
            it.copy(selectedExerciseIds = it.selectedExerciseIds - exerciseId)
        }
        updateAvailableMetrics()
    }

    fun toggleExercise(exerciseId: String) {
        val current = _state.value.selectedExerciseIds
        if (exerciseId in current) {
            removeExercise(exerciseId)
        } else {
            addExercise(exerciseId)
        }
    }

    fun setMetric(metric: GoalMetric) {
        _state.update {
            it.copy(metric = metric, targetUnit = metric.defaultUnit)
        }
    }

    fun updateTargetValue(value: String) {
        _state.update { it.copy(targetValue = value) }
    }

    fun setFrequency(frequency: GoalFrequency) {
        _state.update { it.copy(frequency = frequency) }
    }

    fun setOngoing(ongoing: Boolean) {
        _state.update {
            it.copy(
                isOngoing = ongoing,
                endDate = if (ongoing) null else it.endDate
            )
        }
    }

    fun setEndDate(dateMillis: Long?) {
        _state.update { it.copy(endDate = dateMillis) }
    }

    fun setAutoTrack(autoTrack: Boolean) {
        _state.update { it.copy(autoTrack = autoTrack) }
    }

    fun toggleShowExercisePicker() {
        _state.update { it.copy(showExercisePicker = !it.showExercisePicker) }
    }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (!current.isValid) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            val targetValue = current.targetValue.toDoubleOrNull() ?: 0.0
            val now = Clock.System.now().toEpochMilliseconds()

            val result = if (current.isEditing && goalId != null) {
                goalRepository.update(
                    id = goalId,
                    name = current.name,
                    exerciseIds = current.selectedExerciseIds.toList(),
                    metric = current.metric.value,
                    targetValue = targetValue,
                    targetUnit = current.targetUnit,
                    frequency = current.frequency.value,
                    startDate = now,
                    endDate = current.endDate,
                    autoTrack = current.autoTrack
                )
            } else {
                goalRepository.create(
                    name = current.name,
                    exerciseIds = current.selectedExerciseIds.toList(),
                    metric = current.metric.value,
                    targetValue = targetValue,
                    targetUnit = current.targetUnit,
                    frequency = current.frequency.value,
                    startDate = now,
                    endDate = current.endDate,
                    autoTrack = current.autoTrack
                )
            }

            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    onSaved()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = result.exception.message ?: "Failed to save goal"
                        )
                    }
                }
                is Result.Loading -> { }
            }
        }
    }

    private fun updateAvailableMetrics() {
        val selectedIds = _state.value.selectedExerciseIds
        val exercises = _state.value.availableExercises.filter { it.id in selectedIds }

        val allFieldKeys = exercises.flatMap { ex ->
            ex.recordingFields.map { it.key }
        }.toSet()

        val metrics = GoalMetric.availableForFields(allFieldKeys)
        _state.update {
            val currentMetric = if (it.metric in metrics) it.metric else metrics.first()
            it.copy(
                availableMetrics = metrics,
                metric = currentMetric,
                targetUnit = currentMetric.defaultUnit
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

/**
 * Exercise selection model for the goal form.
 */
data class ExerciseSelection(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val recordingFields: List<RecordingField>
)

/**
 * UI state for Goal Create/Edit form.
 */
data class GoalFormState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val name: String = "",
    val selectedExerciseIds: Set<String> = emptySet(),
    val availableExercises: List<ExerciseSelection> = emptyList(),
    val availableMetrics: List<GoalMetric> = listOf(GoalMetric.SESSIONS),
    val metric: GoalMetric = GoalMetric.SESSIONS,
    val targetValue: String = "",
    val targetUnit: String = "sessions",
    val frequency: GoalFrequency = GoalFrequency.WEEKLY,
    val isOngoing: Boolean = true,
    val endDate: Long? = null,
    val autoTrack: Boolean = true,
    val showExercisePicker: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                selectedExerciseIds.isNotEmpty() &&
                (targetValue.toDoubleOrNull() ?: 0.0) > 0

    val selectedExercises: List<ExerciseSelection>
        get() = availableExercises.filter { it.id in selectedExerciseIds }

    val nameError: String?
        get() = if (name.isBlank()) "Goal name is required" else null

    val exerciseError: String?
        get() = if (selectedExerciseIds.isEmpty()) "Select at least one exercise" else null

    val targetError: String?
        get() = when {
            targetValue.isBlank() -> "Target value is required"
            (targetValue.toDoubleOrNull() ?: 0.0) <= 0 -> "Target must be greater than 0"
            else -> null
        }
}
