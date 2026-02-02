package com.workout.app.presentation.history

import com.workout.app.data.repository.SessionRepository
import com.workout.app.data.repository.WorkoutRepository
import com.workout.app.domain.model.ExerciseWithSets
import com.workout.app.domain.model.IntensityLevel
import com.workout.app.domain.model.MuscleGroupIntensity
import com.workout.app.domain.model.Participant
import com.workout.app.domain.model.Result
import com.workout.app.domain.model.WorkoutWithSets
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
 * ViewModel for Session Detail screen.
 * Manages workout detail display with exercise and set data.
 */
class SessionDetailViewModel(
    private val workoutId: String,
    private val workoutRepository: WorkoutRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SessionDetailState())
    val state: StateFlow<SessionDetailState> = _state.asStateFlow()

    init {
        loadWorkoutDetails()
    }

    private fun loadWorkoutDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Load workout summary
            when (val workoutResult = workoutRepository.getById(workoutId)) {
                is Result.Success -> {
                    val workout = workoutResult.data
                    if (workout != null) {
                        // Try to load session with exercises
                        val exercises = loadSessionExercises()
                        val muscleGroups = calculateMuscleGroups(exercises)

                        val workoutWithSets = WorkoutWithSets(
                            id = workout.id,
                            name = workout.name,
                            createdAt = workout.createdAt,
                            endTime = null,
                            duration = workout.duration,
                            totalVolume = workout.totalVolume,
                            totalSets = workout.totalSets,
                            exerciseCount = workout.exerciseCount,
                            isPartnerWorkout = workout.isPartnerWorkout == 1L,
                            notes = workout.notes,
                            rpe = null,
                            exercises = exercises,
                            muscleGroups = muscleGroups,
                            prCount = 0
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
                                error = "Workout not found"
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = workoutResult.exception.message ?: "Failed to load workout"
                        )
                    }
                }
                is Result.Loading -> { }
            }
        }
    }

    /**
     * Load exercises and sets from the session data.
     */
    private suspend fun loadSessionExercises(): List<ExerciseWithSets> {
        // Find session by workout ID
        val sessionResult = sessionRepository.getByWorkoutId(workoutId)
        if (sessionResult !is Result.Success || sessionResult.data == null) {
            return emptyList()
        }

        val session = sessionResult.data

        // Get exercises with sets using the repository method
        return when (val result = sessionRepository.getExercisesWithSets(session.id)) {
            is Result.Success -> result.data
            else -> emptyList()
        }
    }

    /**
     * Calculate muscle group intensities from exercise data.
     */
    private fun calculateMuscleGroups(exercises: List<ExerciseWithSets>): List<MuscleGroupIntensity> {
        if (exercises.isEmpty()) {
            return emptyList()
        }

        // Group by muscle group and calculate stats
        val muscleGroupStats = exercises.groupBy { it.muscleGroup }
            .map { (muscleGroup, exerciseList) ->
                val totalSets = exerciseList.sumOf { it.sets.size }
                val totalVolume = exerciseList.sumOf { exercise ->
                    exercise.sets.sumOf { (it.weight * it.reps).toLong() }
                }

                val intensity = when {
                    totalSets >= 12 -> IntensityLevel.VERY_HIGH
                    totalSets >= 9 -> IntensityLevel.HIGH
                    totalSets >= 6 -> IntensityLevel.MEDIUM
                    else -> IntensityLevel.LOW
                }

                MuscleGroupIntensity(
                    muscleGroup = muscleGroup,
                    intensity = intensity,
                    setCount = totalSets,
                    volume = totalVolume
                )
            }
            .sortedByDescending { it.setCount }

        return muscleGroupStats
    }

    fun selectParticipant(participant: Participant) {
        _state.update { it.copy(selectedParticipant = participant) }
    }

    fun toggleExercisesExpanded() {
        _state.update { it.copy(exercisesExpanded = !it.exercisesExpanded) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun refresh() {
        loadWorkoutDetails()
    }
}

/**
 * UI state for Session Detail screen.
 */
data class SessionDetailState(
    val isLoading: Boolean = false,
    val workout: WorkoutWithSets? = null,
    val selectedParticipant: Participant = Participant.ME,
    val exercisesExpanded: Boolean = true,
    val error: String? = null
) {
    /**
     * Whether this is a partner workout.
     */
    val isPartnerWorkout: Boolean
        get() = workout?.isPartnerWorkout == true

    /**
     * Formatted start time for display.
     */
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

    /**
     * Formatted end time for display.
     */
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
