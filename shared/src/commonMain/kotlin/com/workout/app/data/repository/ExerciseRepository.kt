package com.workout.app.data.repository

import com.workout.app.database.Exercise
import com.workout.app.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository for exercise data access.
 * Provides CRUD operations, search, and favorites management.
 */
interface ExerciseRepository {

    /**
     * Observe all exercises.
     * @return Flow of all exercises ordered by name
     */
    fun observeAll(): Flow<List<Exercise>>

    /**
     * Observe exercises filtered by muscle group.
     * @param muscleGroup Muscle group to filter by
     * @return Flow of exercises for the muscle group
     */
    fun observeByMuscleGroup(muscleGroup: String): Flow<List<Exercise>>

    /**
     * Observe favorite exercises.
     * @return Flow of favorite exercises
     */
    fun observeFavorites(): Flow<List<Exercise>>

    /**
     * Observe custom exercises created by the user.
     * @return Flow of custom exercises
     */
    fun observeCustom(): Flow<List<Exercise>>

    /**
     * Get a single exercise by ID.
     * @param id Exercise ID
     * @return Result containing the exercise or null if not found
     */
    suspend fun getById(id: String): Result<Exercise?>

    /**
     * Get all exercises (non-reactive).
     * @return Result containing list of all exercises
     */
    suspend fun getAll(): Result<List<Exercise>>

    /**
     * Search exercises by name, muscle group, or category.
     * @param query Search query
     * @return Result containing matching exercises
     */
    suspend fun search(query: String): Result<List<Exercise>>

    /**
     * Create a new custom exercise.
     * @param name Exercise name
     * @param muscleGroup Target muscle group
     * @param category Exercise category (optional)
     * @param equipment Equipment needed (optional)
     * @param difficulty Difficulty level (optional)
     * @param instructions Exercise instructions (optional)
     * @param videoUrl Video URL (optional)
     * @return Result containing the created exercise ID
     */
    suspend fun create(
        name: String,
        muscleGroup: String,
        category: String? = null,
        equipment: String? = null,
        difficulty: String? = null,
        instructions: String? = null,
        videoUrl: String? = null
    ): Result<String>

    /**
     * Update an existing exercise.
     * @param exercise Exercise to update
     * @return Result indicating success or failure
     */
    suspend fun update(exercise: Exercise): Result<Unit>

    /**
     * Toggle favorite status of an exercise.
     * @param id Exercise ID
     * @return Result indicating success or failure
     */
    suspend fun toggleFavorite(id: String): Result<Unit>

    /**
     * Delete a custom exercise.
     * @param id Exercise ID (must be a custom exercise)
     * @return Result indicating success or failure
     */
    suspend fun delete(id: String): Result<Unit>

    /**
     * Get count of exercises grouped by muscle group.
     * @return Result containing map of muscle group to count
     */
    suspend fun getCountByMuscleGroup(): Result<Map<String, Long>>
}
