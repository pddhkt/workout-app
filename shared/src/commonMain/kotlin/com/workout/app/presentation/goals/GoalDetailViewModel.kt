package com.workout.app.presentation.goals

import com.workout.app.data.repository.GoalRepository
import com.workout.app.domain.model.GoalPeriodEntry
import com.workout.app.domain.model.GoalWithProgress
import com.workout.app.domain.model.Result
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Goal Detail screen.
 */
class GoalDetailViewModel(
    private val goalId: String,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GoalDetailState())
    val state: StateFlow<GoalDetailState> = _state.asStateFlow()

    init {
        loadGoalData()
    }

    private fun loadGoalData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Load goal with progress
            when (val goalResult = goalRepository.getById(goalId)) {
                is Result.Success -> {
                    val goal = goalResult.data
                    if (goal != null) {
                        // Load period history
                        val historyResult = goalRepository.getPeriodHistory(goalId, 20)
                        val history = when (historyResult) {
                            is Result.Success -> historyResult.data
                            else -> emptyList()
                        }

                        // Load streak
                        val streakResult = goalRepository.getStreak(goalId)
                        val streak = when (streakResult) {
                            is Result.Success -> streakResult.data
                            else -> 0
                        }

                        _state.update {
                            it.copy(
                                isLoading = false,
                                goal = goal,
                                periodHistory = history,
                                streakCount = streak,
                                error = null
                            )
                        }
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
                            error = goalResult.exception.message ?: "Failed to load goal"
                        )
                    }
                }
                is Result.Loading -> { }
            }
        }
    }

    fun toggleActive() {
        val goal = _state.value.goal ?: return
        viewModelScope.launch {
            when (goalRepository.toggleActive(goalId, !goal.isActive)) {
                is Result.Success -> loadGoalData()
                is Result.Error -> {
                    _state.update {
                        it.copy(error = "Failed to update goal")
                    }
                }
                else -> { }
            }
        }
    }

    fun deleteGoal(onDeleted: () -> Unit) {
        viewModelScope.launch {
            when (goalRepository.delete(goalId)) {
                is Result.Success -> onDeleted()
                is Result.Error -> {
                    _state.update {
                        it.copy(error = "Failed to delete goal")
                    }
                }
                else -> { }
            }
        }
    }

    fun refresh() {
        loadGoalData()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

/**
 * UI state for Goal Detail screen.
 */
data class GoalDetailState(
    val isLoading: Boolean = false,
    val goal: GoalWithProgress? = null,
    val periodHistory: List<GoalPeriodEntry> = emptyList(),
    val streakCount: Int = 0,
    val error: String? = null
)
