package com.workout.app.presentation.goals

import com.workout.app.data.repository.GoalRepository
import com.workout.app.domain.model.GoalStatus
import com.workout.app.domain.model.GoalWithProgress
import com.workout.app.domain.model.Result
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Goals management screen.
 */
class GoalsViewModel(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GoalsState())
    val state: StateFlow<GoalsState> = _state.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            goalRepository.observeAllGoals()
                .catch { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load goals"
                        )
                    }
                }
                .collect { goals ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            activeGoals = goals.filter { g -> g.status == GoalStatus.ACTIVE },
                            pausedGoals = goals.filter { g -> g.status == GoalStatus.PAUSED },
                            completedGoals = goals.filter { g ->
                                g.status == GoalStatus.COMPLETED || g.status == GoalStatus.EXPIRED
                            },
                            error = null
                        )
                    }
                }
        }
    }

    fun toggleGoalActive(goalId: String, isActive: Boolean) {
        viewModelScope.launch {
            when (val result = goalRepository.toggleActive(goalId, isActive)) {
                is Result.Error -> {
                    _state.update {
                        it.copy(error = result.exception.message ?: "Failed to update goal")
                    }
                }
                else -> { }
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            when (val result = goalRepository.delete(goalId)) {
                is Result.Error -> {
                    _state.update {
                        it.copy(error = result.exception.message ?: "Failed to delete goal")
                    }
                }
                else -> { }
            }
        }
    }

    fun cloneGoal(goalId: String) {
        viewModelScope.launch {
            when (val result = goalRepository.clone(goalId)) {
                is Result.Error -> {
                    _state.update {
                        it.copy(error = result.exception.message ?: "Failed to clone goal")
                    }
                }
                else -> { }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

/**
 * UI state for Goals management screen.
 */
data class GoalsState(
    val isLoading: Boolean = false,
    val activeGoals: List<GoalWithProgress> = emptyList(),
    val pausedGoals: List<GoalWithProgress> = emptyList(),
    val completedGoals: List<GoalWithProgress> = emptyList(),
    val error: String? = null
)
