package com.workout.app.presentation.home

import com.workout.app.data.repository.GoalRepository
import com.workout.app.data.repository.SessionRepository
import com.workout.app.data.repository.TemplateRepository
import com.workout.app.data.repository.WorkoutRepository
import com.workout.app.database.Session
import com.workout.app.database.Template
import com.workout.app.database.Workout
import com.workout.app.domain.model.GoalWithProgress
import com.workout.app.domain.model.Result
import com.workout.app.presentation.base.ViewModel
import com.workout.app.ui.components.dataviz.HeatmapDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel for Home screen.
 * Manages recent sessions, templates, and workout consistency data.
 */
class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val templateRepository: TemplateRepository,
    private val sessionRepository: SessionRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Combine reactive flows for templates, workouts, and goals
            combine(
                templateRepository.observeRecentlyUsed(limit = 4),
                workoutRepository.observeRecent(limit = 3),
                goalRepository.observeActiveGoals()
            ) { templates, workouts, goals ->
                Triple(templates, workouts, goals)
            }.catch { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load home data"
                    )
                }
            }.collect { (templates, workouts, goals) ->
                // Load heatmap data separately
                loadHeatmapData()

                _state.update {
                    it.copy(
                        isLoading = false,
                        templates = templates,
                        recentSessions = workouts,
                        activeGoals = goals,
                        error = null
                    )
                }
            }
        }
    }

    private suspend fun loadHeatmapData() {
        // Calculate date range for last 28 days
        val now = Clock.System.now()
        val startDate = now.minus(28, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toEpochMilliseconds()

        when (val result = workoutRepository.getHeatmapData(startDate)) {
            is Result.Success -> {
                // Convert workout counts to HeatmapDay format
                val heatmapData = result.data.mapIndexed { index, item ->
                    HeatmapDay(day = index + 1, count = item.workoutCount.toInt())
                }
                _state.update { it.copy(heatmapData = heatmapData) }
            }
            is Result.Error -> {
                // Heatmap is not critical, log but don't show error
                println("Failed to load heatmap: ${result.exception.message}")
            }
            is Result.Loading -> {
                // Should not happen for this call
            }
        }
    }

    fun refresh() {
        loadHomeData()
    }
}

/**
 * UI state for Home screen.
 */
data class HomeState(
    val isLoading: Boolean = false,
    val templates: List<Template> = emptyList(),
    val recentSessions: List<Workout> = emptyList(),
    val activeGoals: List<GoalWithProgress> = emptyList(),
    val heatmapData: List<HeatmapDay> = emptyList(),
    val error: String? = null
)
