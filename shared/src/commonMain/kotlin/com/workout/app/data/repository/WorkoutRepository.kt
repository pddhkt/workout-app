package com.workout.app.data.repository

import com.workout.app.database.GetStats
import com.workout.app.database.SelectForHeatmap
import com.workout.app.database.Workout
import com.workout.app.domain.model.MonthGroup
import com.workout.app.domain.model.Result
import com.workout.app.domain.model.WorkoutHistoryFilters
import kotlinx.coroutines.flow.Flow

/**
 * Repository for workout history data access.
 * Manages completed workout records.
 */
interface WorkoutRepository {

    /**
     * Observe all workouts ordered by date (most recent first).
     * @return Flow of all workouts
     */
    fun observeAll(): Flow<List<Workout>>

    /**
     * Observe recent workouts with a limit.
     * @param limit Maximum number of workouts to return
     * @return Flow of recent workouts
     */
    fun observeRecent(limit: Long): Flow<List<Workout>>

    /**
     * Get a single workout by ID.
     * @param id Workout ID
     * @return Result containing the workout or null if not found
     */
    suspend fun getById(id: String): Result<Workout?>

    /**
     * Get all workouts (non-reactive).
     * @return Result containing list of all workouts
     */
    suspend fun getAll(): Result<List<Workout>>

    /**
     * Get workouts within a date range.
     * @param startDate Start date (epoch millis)
     * @param endDate End date (epoch millis)
     * @return Result containing workouts in the date range
     */
    suspend fun getByDateRange(startDate: Long, endDate: Long): Result<List<Workout>>

    /**
     * Create a new workout record (typically from a completed session).
     * @param name Workout name
     * @param duration Duration in seconds
     * @param notes Optional notes
     * @param isPartnerWorkout Whether this was a partner workout
     * @param totalVolume Total weight lifted in kg
     * @param totalSets Total number of sets completed
     * @param exerciseCount Number of exercises performed
     * @param exerciseNames Comma-separated list of exercise names for display
     * @return Result containing the created workout ID
     */
    suspend fun create(
        name: String,
        duration: Long,
        notes: String? = null,
        isPartnerWorkout: Boolean = false,
        totalVolume: Long = 0,
        totalSets: Long = 0,
        exerciseCount: Long = 0,
        exerciseNames: String? = null
    ): Result<String>

    /**
     * Update an existing workout.
     * @param workout Workout to update
     * @return Result indicating success or failure
     */
    suspend fun update(workout: Workout): Result<Unit>

    /**
     * Delete a workout.
     * @param id Workout ID
     * @return Result indicating success or failure
     */
    suspend fun delete(id: String): Result<Unit>

    /**
     * Get workout statistics (total workouts, duration, volume, sets).
     * @return Result containing workout stats
     */
    suspend fun getStats(): Result<GetStats?>

    /**
     * Get workout data for consistency heatmap.
     * @param sinceDate Date to start from (epoch millis)
     * @return Result containing heatmap data (date to workout count)
     */
    suspend fun getHeatmapData(sinceDate: Long): Result<List<SelectForHeatmap>>

    /**
     * Get workouts grouped by month.
     * @return Result containing map of month string to workouts
     */
    suspend fun getGroupedByMonth(): Result<Map<String, List<Workout>>>

    /**
     * Get workouts for a specific month.
     * @param yearMonth Month in YYYY-MM format
     * @return Result containing workouts for that month
     */
    suspend fun getByMonth(yearMonth: String): Result<List<Workout>>

    /**
     * Get distinct months that have workouts.
     * @return Result containing list of month groups
     */
    suspend fun getDistinctMonths(): Result<List<MonthGroup>>

    /**
     * Search workouts by name.
     * @param query Search query
     * @return Result containing matching workouts
     */
    suspend fun search(query: String): Result<List<Workout>>

    /**
     * Get filtered workouts.
     * @param filters Filter criteria
     * @return Result containing filtered workouts
     */
    suspend fun getFiltered(filters: WorkoutHistoryFilters): Result<List<Workout>>

    /**
     * Observe all workouts grouped by month.
     * @return Flow of workouts grouped by month
     */
    fun observeGroupedByMonth(): Flow<Map<String, List<Workout>>>
}
