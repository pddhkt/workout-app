package com.workout.app.data.repository

import com.workout.app.database.Session
import com.workout.app.database.SelectWithExercises
import com.workout.app.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository for active workout session management.
 * Handles in-progress and draft workout sessions.
 */
interface SessionRepository {

    /**
     * Observe all sessions ordered by start time.
     * @return Flow of all sessions
     */
    fun observeAll(): Flow<List<Session>>

    /**
     * Observe active sessions.
     * @return Flow of active sessions
     */
    fun observeActive(): Flow<List<Session>>

    /**
     * Observe sessions by status.
     * @param status Session status ('active', 'paused', 'completed', 'draft')
     * @return Flow of sessions with the given status
     */
    fun observeByStatus(status: String): Flow<List<Session>>

    /**
     * Get a single session by ID.
     * @param id Session ID
     * @return Result containing the session or null if not found
     */
    suspend fun getById(id: String): Result<Session?>

    /**
     * Get a session by workout ID.
     * @param workoutId Workout ID
     * @return Result containing the session or null if not found
     */
    suspend fun getByWorkoutId(workoutId: String): Result<Session?>

    /**
     * Get session with all its exercises (join query).
     * @param id Session ID
     * @return Result containing session with exercises
     */
    suspend fun getWithExercises(id: String): Result<List<SelectWithExercises>>

    /**
     * Get exercises with sets for a session.
     * @param sessionId Session ID
     * @return Result containing list of exercises with their sets
     */
    suspend fun getExercisesWithSets(sessionId: String): Result<List<com.workout.app.domain.model.ExerciseWithSets>>

    /**
     * Get all sessions (non-reactive).
     * @return Result containing list of all sessions
     */
    suspend fun getAll(): Result<List<Session>>

    /**
     * Create a new workout session.
     * @param name Session name
     * @param templateId Optional template ID if created from template
     * @param isPartnerWorkout Whether this is a partner workout
     * @param status Initial status (default 'draft')
     * @return Result containing the created session ID
     */
    suspend fun create(
        name: String,
        templateId: String? = null,
        isPartnerWorkout: Boolean = false,
        status: String = "draft"
    ): Result<String>

    /**
     * Update an existing session.
     * @param session Session to update
     * @return Result indicating success or failure
     */
    suspend fun update(session: Session): Result<Unit>

    /**
     * Update session name.
     * @param id Session ID
     * @param name New name
     * @return Result indicating success or failure
     */
    suspend fun updateName(id: String, name: String): Result<Unit>

    /**
     * Update session status.
     * @param id Session ID
     * @param status New status
     * @return Result indicating success or failure
     */
    suspend fun updateStatus(id: String, status: String): Result<Unit>

    /**
     * Complete a session.
     * @param id Session ID
     * @return Result indicating success or failure
     */
    suspend fun complete(id: String): Result<Unit>

    /**
     * Update session notes.
     * @param id Session ID
     * @param notes New notes text
     * @return Result indicating success or failure
     */
    suspend fun updateNotes(id: String, notes: String?): Result<Unit>

    /**
     * Update the workout ID for a session, linking it to a saved workout.
     * @param id Session ID
     * @param workoutId Workout ID to link
     * @return Result indicating success or failure
     */
    suspend fun updateWorkoutId(id: String, workoutId: String): Result<Unit>

    /**
     * Delete a session.
     * @param id Session ID
     * @return Result indicating success or failure
     */
    suspend fun delete(id: String): Result<Unit>

    /**
     * Get count of sessions by status.
     * @return Result containing map of status to count
     */
    suspend fun getCountByStatus(): Result<Map<String, Long>>
}
