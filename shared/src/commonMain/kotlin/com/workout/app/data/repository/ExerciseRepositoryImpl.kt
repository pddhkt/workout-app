package com.workout.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.workout.app.database.Exercise
import com.workout.app.database.ExerciseQueries
import com.workout.app.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Implementation of ExerciseRepository using SQLDelight.
 */
class ExerciseRepositoryImpl(
    private val exerciseQueries: ExerciseQueries
) : ExerciseRepository {

    override fun observeAll(): Flow<List<Exercise>> {
        return exerciseQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override fun observeByMuscleGroup(muscleGroup: String): Flow<List<Exercise>> {
        return exerciseQueries.selectByMuscleGroup(muscleGroup)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override fun observeFavorites(): Flow<List<Exercise>> {
        return exerciseQueries.selectFavorites()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override fun observeCustom(): Flow<List<Exercise>> {
        return exerciseQueries.selectCustom()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override suspend fun getById(id: String): Result<Exercise?> = withContext(Dispatchers.Default) {
        try {
            val exercise = exerciseQueries.selectById(id).executeAsOneOrNull()
            Result.Success(exercise)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getAll(): Result<List<Exercise>> = withContext(Dispatchers.Default) {
        try {
            val exercises = exerciseQueries.selectAll().executeAsList()
            Result.Success(exercises)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun search(query: String): Result<List<Exercise>> = withContext(Dispatchers.Default) {
        try {
            val exercises = exerciseQueries.search(query, query, query).executeAsList()
            Result.Success(exercises)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun create(
        name: String,
        muscleGroup: String,
        category: String?,
        equipment: String?,
        difficulty: String?,
        instructions: String?,
        videoUrl: String?
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val id = generateId()
            val now = Clock.System.now().toEpochMilliseconds()

            exerciseQueries.insert(
                id = id,
                name = name,
                muscleGroup = muscleGroup,
                category = category,
                equipment = equipment,
                difficulty = difficulty,
                instructions = instructions,
                videoUrl = videoUrl,
                isCustom = 1,
                isFavorite = 0,
                createdAt = now,
                updatedAt = now
            )

            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun update(exercise: Exercise): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()

            exerciseQueries.update(
                name = exercise.name,
                muscleGroup = exercise.muscleGroup,
                category = exercise.category,
                equipment = exercise.equipment,
                difficulty = exercise.difficulty,
                instructions = exercise.instructions,
                videoUrl = exercise.videoUrl,
                isFavorite = exercise.isFavorite,
                updatedAt = now,
                id = exercise.id
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun toggleFavorite(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            exerciseQueries.toggleFavorite(updatedAt = now, id = id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            exerciseQueries.delete(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCountByMuscleGroup(): Result<Map<String, Long>> = withContext(Dispatchers.Default) {
        try {
            val counts = exerciseQueries.countByMuscleGroup().executeAsList()
            val map = counts.associate { it.muscleGroup to it.exerciseCount }
            Result.Success(map)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Generate a unique ID for a new exercise.
     * In production, this could use UUID or another unique ID generation strategy.
     */
    private fun generateId(): String {
        return "exercise_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }
}
