package com.workout.app.data.repository

import com.workout.app.domain.model.Result
import com.workout.app.domain.model.SetData

/**
 * Repository for managing workout sets.
 * Handles individual set records within workout sessions.
 */
interface SetRepository {

    /**
     * Create a new set record.
     * @param sessionId Session ID
     * @param sessionExerciseId Session exercise ID
     * @param exerciseId Exercise ID
     * @param setNumber Set number within the exercise
     * @param weight Weight used (in kg)
     * @param reps Number of repetitions
     * @param rpe Rate of perceived exertion (1-10, nullable)
     * @param isWarmup Whether this is a warmup set
     * @param notes Optional notes
     * @return Result containing the created set ID
     */
    suspend fun createSet(
        sessionId: String,
        sessionExerciseId: String,
        exerciseId: String,
        setNumber: Int,
        weight: Double,
        reps: Int,
        rpe: Int?,
        isWarmup: Boolean = false,
        notes: String? = null
    ): Result<String>

    /**
     * Get all sets for a session.
     * @param sessionId Session ID
     * @return Result containing list of sets
     */
    suspend fun getBySession(sessionId: String): Result<List<SetData>>

    /**
     * Get all sets for a session exercise.
     * @param sessionExerciseId Session exercise ID
     * @return Result containing list of sets
     */
    suspend fun getBySessionExercise(sessionExerciseId: String): Result<List<SetData>>

    /**
     * Calculate total volume (weight * reps) for a session.
     * @param sessionId Session ID
     * @return Result containing total volume
     */
    suspend fun calculateSessionVolume(sessionId: String): Result<Double>

    /**
     * Count total sets in a session.
     * @param sessionId Session ID
     * @return Result containing set count
     */
    suspend fun countBySession(sessionId: String): Result<Long>

    /**
     * Delete all sets for a session.
     * @param sessionId Session ID
     * @return Result indicating success or failure
     */
    suspend fun deleteBySession(sessionId: String): Result<Unit>
}
