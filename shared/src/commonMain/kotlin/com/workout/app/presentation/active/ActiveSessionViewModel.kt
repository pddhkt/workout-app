package com.workout.app.presentation.active

import com.workout.app.data.repository.SessionRepository
import com.workout.app.presentation.base.ViewModel
import com.workout.app.presentation.workout.WorkoutState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Singleton ViewModel that observes active workout sessions.
 * Used by BottomNavBar to show active session indicator and
 * coordinates workout overlay state for minimized view.
 */
class ActiveSessionViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ActiveSessionState())
    val state: StateFlow<ActiveSessionState> = _state.asStateFlow()

    // Hold workout state for overlay when minimized
    private val _workoutState = MutableStateFlow<WorkoutState?>(null)
    val workoutState: StateFlow<WorkoutState?> = _workoutState.asStateFlow()

    init {
        observeActiveSessions()
    }

    private fun observeActiveSessions() {
        viewModelScope.launch {
            sessionRepository.observeActive()
                .catch { /* silently ignore errors */ }
                .collect { sessions ->
                    val activeSession = sessions.firstOrNull()
                    _state.update { current ->
                        // Preserve minimized state and workout progress when session data updates
                        current.copy(
                            sessionId = activeSession?.id,
                            sessionName = activeSession?.name,
                            startTime = activeSession?.startTime,
                            // Reset minimized state when session ends
                            isMinimized = if (activeSession == null) false else current.isMinimized,
                            currentExerciseName = if (activeSession == null) null else current.currentExerciseName,
                            currentExerciseIndex = if (activeSession == null) 0 else current.currentExerciseIndex,
                            totalExercises = if (activeSession == null) 0 else current.totalExercises
                        )
                    }
                }
        }
    }

    /**
     * Called when user swipes back from workout screen.
     * Sets the session to minimized state so the bar appears.
     */
    fun minimize() {
        _state.update { it.copy(isMinimized = true) }
    }

    /**
     * Called when user taps the minimized bar to expand back to full workout.
     */
    fun expand() {
        _state.update { it.copy(isMinimized = false) }
    }

    /**
     * Called by WorkoutViewModel to sync current exercise info for the minimized bar display.
     */
    fun updateWorkoutProgress(
        currentExerciseName: String?,
        currentIndex: Int,
        totalExercises: Int
    ) {
        _state.update {
            it.copy(
                currentExerciseName = currentExerciseName,
                currentExerciseIndex = currentIndex,
                totalExercises = totalExercises
            )
        }
    }

    /**
     * Clears the minimized state when workout ends.
     */
    fun clearMinimizedState() {
        _state.update {
            it.copy(
                isMinimized = false,
                currentExerciseName = null,
                currentExerciseIndex = 0,
                totalExercises = 0
            )
        }
        _workoutState.value = null
    }

    /**
     * Updates the cached workout state for overlay display.
     * Called by WorkoutViewModel to sync state for minimized bar.
     */
    fun updateWorkoutState(state: WorkoutState) {
        _workoutState.value = state
    }

    /**
     * Clears the cached workout state.
     */
    fun clearWorkoutState() {
        _workoutState.value = null
    }
}

/**
 * State for active session indicator and minimized workout bar.
 */
data class ActiveSessionState(
    val sessionId: String? = null,
    val sessionName: String? = null,
    val startTime: Long? = null,
    // Minimized workout bar state
    val isMinimized: Boolean = false,
    val currentExerciseName: String? = null,
    val currentExerciseIndex: Int = 0,
    val totalExercises: Int = 0
) {
    val hasActiveSession: Boolean get() = sessionId != null
}
