package com.workout.app.data.repository

import com.workout.app.domain.model.GoalPeriodEntry
import com.workout.app.domain.model.GoalWithProgress
import com.workout.app.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository for goal management and progress tracking.
 */
interface GoalRepository {

    /**
     * Observe all active goals with current period progress.
     * Used on the home screen.
     */
    fun observeActiveGoals(): Flow<List<GoalWithProgress>>

    /**
     * Observe all goals with current period progress.
     * Used on the goals management screen.
     */
    fun observeAllGoals(): Flow<List<GoalWithProgress>>

    /**
     * Get a single goal with progress by ID.
     */
    suspend fun getById(id: String): Result<GoalWithProgress?>

    /**
     * Create a new goal. Returns the goal ID.
     */
    suspend fun create(
        name: String,
        exerciseIds: List<String>,
        metric: String,
        targetValue: Double,
        targetUnit: String,
        frequency: String,
        startDate: Long,
        endDate: Long?,
        autoTrack: Boolean = true
    ): Result<String>

    /**
     * Update an existing goal.
     */
    suspend fun update(
        id: String,
        name: String,
        exerciseIds: List<String>,
        metric: String,
        targetValue: Double,
        targetUnit: String,
        frequency: String,
        startDate: Long,
        endDate: Long?,
        autoTrack: Boolean
    ): Result<Unit>

    /**
     * Toggle goal active/paused state.
     */
    suspend fun toggleActive(id: String, isActive: Boolean): Result<Unit>

    /**
     * Delete a goal and all its progress.
     */
    suspend fun delete(id: String): Result<Unit>

    /**
     * Clone an existing goal with fresh dates and reset progress.
     * Returns the new goal ID.
     */
    suspend fun clone(sourceGoalId: String): Result<String>

    /**
     * Add progress value to a goal for the period containing the given timestamp.
     */
    suspend fun addProgress(goalId: String, value: Double, timestamp: Long): Result<Unit>

    /**
     * Process workout completion: update all matching active auto-track goals.
     * @param exerciseIds exercises used in the completed workout
     * @param setData map of exerciseId -> (metricKey -> summed value)
     */
    suspend fun processWorkoutCompletion(
        exerciseIds: List<String>,
        setData: Map<String, Map<String, Double>>
    ): Result<Unit>

    /**
     * Get period history for a goal.
     */
    suspend fun getPeriodHistory(goalId: String, limit: Int = 20): Result<List<GoalPeriodEntry>>

    /**
     * Calculate current streak (consecutive completed periods).
     */
    suspend fun getStreak(goalId: String): Result<Int>
}
