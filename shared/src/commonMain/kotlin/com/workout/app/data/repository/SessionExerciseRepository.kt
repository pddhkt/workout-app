package com.workout.app.data.repository

import com.workout.app.domain.model.Result

/**
 * Input data for adding an exercise to a session.
 */
data class AddedExerciseInput(
    val exerciseId: String,
    val targetSets: Int,
    val orderIndex: Int
)

/**
 * Session exercise with details from joined exercise table.
 */
data class SessionExerciseWithDetails(
    val id: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String,
    val muscleGroup: String,
    val category: String?,
    val orderIndex: Int,
    val targetSets: Int,
    val completedSets: Int
)

/**
 * Repository for managing session exercises.
 * Handles the relationship between sessions and exercises.
 */
interface SessionExerciseRepository {

    /**
     * Add multiple exercises to a session in a single batch operation.
     * @param sessionId Session ID to add exercises to
     * @param exercises List of exercises with their target sets and order
     * @return Result indicating success or failure
     */
    suspend fun addExercisesToSession(
        sessionId: String,
        exercises: List<AddedExerciseInput>
    ): Result<Unit>

    /**
     * Get all exercises for a session with exercise details.
     * @param sessionId Session ID
     * @return Result containing list of session exercises with details
     */
    suspend fun getBySession(sessionId: String): Result<List<SessionExerciseWithDetails>>

    /**
     * Increment the completed sets count for a session exercise.
     * @param id Session exercise ID
     * @return Result indicating success or failure
     */
    suspend fun incrementCompletedSets(id: String): Result<Unit>

    /**
     * Update the completed sets count for a session exercise.
     * @param id Session exercise ID
     * @param completedSets New completed sets count
     * @return Result indicating success or failure
     */
    suspend fun updateCompletedSets(id: String, completedSets: Int): Result<Unit>

    /**
     * Delete all exercises for a session.
     * @param sessionId Session ID
     * @return Result indicating success or failure
     */
    suspend fun deleteBySession(sessionId: String): Result<Unit>
}
