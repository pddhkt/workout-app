package com.workout.app.presentation.complete

import com.workout.app.data.repository.SessionRepository
import com.workout.app.data.repository.SetRepository
import com.workout.app.data.repository.TemplateRepository
import com.workout.app.data.repository.WorkoutRepository
import com.workout.app.domain.model.ExerciseWithSets
import com.workout.app.domain.model.IntensityLevel
import com.workout.app.domain.model.MuscleGroupIntensity
import com.workout.app.domain.model.Participant
import com.workout.app.domain.model.Result
import com.workout.app.domain.model.WorkoutWithSets
import com.workout.app.domain.model.calculateIntensity
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel for Workout Complete screen.
 * Manages workout completion display, RPE input, and save operations.
 */
class WorkoutCompleteViewModel(
    private val sessionId: String,
    private val sessionRepository: SessionRepository,
    private val setRepository: SetRepository,
    private val workoutRepository: WorkoutRepository,
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutCompleteState())
    val state: StateFlow<WorkoutCompleteState> = _state.asStateFlow()

    init {
        loadWorkoutData()
    }

    private fun loadWorkoutData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Load session data
            when (val sessionResult = sessionRepository.getById(sessionId)) {
                is Result.Success -> {
                    val session = sessionResult.data
                    if (session != null) {
                        // Get exercises with sets
                        val exercisesResult = sessionRepository.getExercisesWithSets(sessionId)
                        val exercises = when (exercisesResult) {
                            is Result.Success -> exercisesResult.data
                            else -> emptyList()
                        }

                        // Calculate volume
                        val volumeResult = setRepository.calculateSessionVolume(sessionId)
                        val totalVolume = when (volumeResult) {
                            is Result.Success -> volumeResult.data.toLong()
                            else -> 0L
                        }

                        // Count sets
                        val setCountResult = setRepository.countBySession(sessionId)
                        val totalSets = when (setCountResult) {
                            is Result.Success -> setCountResult.data
                            else -> 0L
                        }

                        // Calculate duration
                        val duration = if (session.endTime != null) {
                            (session.endTime - session.startTime) / 1000
                        } else {
                            0L
                        }

                        // Calculate muscle group intensities
                        val muscleGroups = calculateMuscleGroupIntensities(exercises)

                        val workoutWithSets = WorkoutWithSets(
                            id = session.id,
                            name = session.name,
                            createdAt = session.startTime,
                            endTime = session.endTime,
                            duration = duration,
                            totalVolume = totalVolume,
                            totalSets = totalSets,
                            exerciseCount = exercises.size.toLong(),
                            isPartnerWorkout = session.isPartnerWorkout == 1L,
                            notes = session.notes,
                            rpe = null,
                            exercises = exercises,
                            muscleGroups = muscleGroups,
                            prCount = 0 // TODO: Calculate from set records
                        )

                        _state.update {
                            it.copy(
                                isLoading = false,
                                workout = workoutWithSets,
                                error = null
                            )
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
                is Result.Loading -> { }
            }
        }
    }

    private fun calculateMuscleGroupIntensities(exercises: List<ExerciseWithSets>): List<MuscleGroupIntensity> {
        return exercises
            .groupBy { it.muscleGroup }
            .map { (muscleGroup, exercisesInGroup) ->
                val setCount = exercisesInGroup.sumOf { it.sets.size }
                val volume = exercisesInGroup.sumOf { exercise ->
                    exercise.sets.sumOf { set -> (set.weight * set.reps).toLong() }
                }
                MuscleGroupIntensity(
                    muscleGroup = muscleGroup,
                    intensity = calculateIntensity(setCount),
                    setCount = setCount,
                    volume = volume
                )
            }
            .sortedByDescending { it.setCount }
    }

    fun selectParticipant(participant: Participant) {
        _state.update { it.copy(selectedParticipant = participant) }
    }

    fun updateNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun updateRpe(rpe: Int) {
        _state.update { it.copy(rpe = rpe) }
    }

    fun saveAsTemplate() {
        val workout = _state.value.workout ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSavingTemplate = true) }

            when (val result = templateRepository.create(
                name = workout.name,
                exercises = "[]", // TODO: Serialize exercise data
                estimatedDuration = workout.duration / 60
            )) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isSavingTemplate = false,
                            templateSaved = true
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isSavingTemplate = false,
                            error = result.exception.message ?: "Failed to save template"
                        )
                    }
                }
                is Result.Loading -> { }
            }
        }
    }

    fun saveWorkout(onComplete: () -> Unit) {
        val currentState = _state.value
        val workout = currentState.workout ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            // Build exercise names from loaded exercises
            val exerciseNames = workout.exercises
                .map { it.exerciseName }
                .joinToString(", ")

            // Update workout with notes, RPE, and exercise names
            val updatedWorkout = com.workout.app.database.Workout(
                id = workout.id,
                name = workout.name,
                createdAt = workout.createdAt,
                duration = workout.duration,
                notes = currentState.notes.takeIf { it.isNotBlank() },
                isPartnerWorkout = if (workout.isPartnerWorkout) 1L else 0L,
                totalVolume = workout.totalVolume,
                totalSets = workout.totalSets,
                exerciseCount = workout.exerciseCount,
                exerciseNames = exerciseNames.takeIf { it.isNotBlank() }
            )

            when (val result = workoutRepository.update(updatedWorkout)) {
                is Result.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    onComplete()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = result.exception.message ?: "Failed to save workout"
                        )
                    }
                }
                is Result.Loading -> { }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun dismissTemplateSaved() {
        _state.update { it.copy(templateSaved = false) }
    }
}

/**
 * UI state for Workout Complete screen.
 */
data class WorkoutCompleteState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSavingTemplate: Boolean = false,
    val workout: WorkoutWithSets? = null,
    val selectedParticipant: Participant = Participant.ME,
    val notes: String = "",
    val rpe: Int = 7,
    val templateSaved: Boolean = false,
    val error: String? = null
) {
    val isPartnerWorkout: Boolean
        get() = workout?.isPartnerWorkout == true

    val formattedStartTime: String
        get() {
            val workout = workout ?: return ""
            val instant = Instant.fromEpochMilliseconds(workout.createdAt)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            val month = when (localDateTime.monthNumber) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> ""
            }

            val hour = localDateTime.hour
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour

            return "$month ${localDateTime.dayOfMonth}, $displayHour:$minute $amPm"
        }

    val formattedEndTime: String
        get() {
            val workout = workout ?: return ""
            val endTime = workout.endTime ?: (workout.createdAt + (workout.duration * 1000))
            val instant = Instant.fromEpochMilliseconds(endTime)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            val hour = localDateTime.hour
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour

            return "$displayHour:$minute $amPm"
        }
}
