package com.workout.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.workout.app.database.GetStats
import com.workout.app.database.SelectForHeatmap
import com.workout.app.database.Workout
import com.workout.app.database.WorkoutQueries
import com.workout.app.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Implementation of WorkoutRepository using SQLDelight.
 */
class WorkoutRepositoryImpl(
    private val workoutQueries: WorkoutQueries
) : WorkoutRepository {

    override fun observeAll(): Flow<List<Workout>> {
        return workoutQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override fun observeRecent(limit: Long): Flow<List<Workout>> {
        return workoutQueries.selectRecent(limit)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override suspend fun getById(id: String): Result<Workout?> = withContext(Dispatchers.Default) {
        try {
            val workout = workoutQueries.selectById(id).executeAsOneOrNull()
            Result.Success(workout)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getAll(): Result<List<Workout>> = withContext(Dispatchers.Default) {
        try {
            val workouts = workoutQueries.selectAll().executeAsList()
            Result.Success(workouts)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getByDateRange(startDate: Long, endDate: Long): Result<List<Workout>> =
        withContext(Dispatchers.Default) {
            try {
                val workouts = workoutQueries.selectByDateRange(startDate, endDate).executeAsList()
                Result.Success(workouts)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun create(
        name: String,
        duration: Long,
        notes: String?,
        isPartnerWorkout: Boolean,
        totalVolume: Long,
        totalSets: Long,
        exerciseCount: Long
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val id = generateId()
            val now = Clock.System.now().toEpochMilliseconds()

            workoutQueries.insert(
                id = id,
                name = name,
                createdAt = now,
                duration = duration,
                notes = notes,
                isPartnerWorkout = if (isPartnerWorkout) 1 else 0,
                totalVolume = totalVolume,
                totalSets = totalSets,
                exerciseCount = exerciseCount
            )

            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun update(workout: Workout): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            workoutQueries.update(
                name = workout.name,
                duration = workout.duration,
                notes = workout.notes,
                isPartnerWorkout = workout.isPartnerWorkout,
                totalVolume = workout.totalVolume,
                totalSets = workout.totalSets,
                exerciseCount = workout.exerciseCount,
                id = workout.id
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            workoutQueries.delete(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getStats(): Result<GetStats?> = withContext(Dispatchers.Default) {
        try {
            val stats = workoutQueries.getStats().executeAsOneOrNull()
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getHeatmapData(sinceDate: Long): Result<List<SelectForHeatmap>> =
        withContext(Dispatchers.Default) {
            try {
                val data = workoutQueries.selectForHeatmap(sinceDate).executeAsList()
                Result.Success(data)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    /**
     * Generate a unique ID for a new workout.
     */
    private fun generateId(): String {
        return "workout_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }
}
