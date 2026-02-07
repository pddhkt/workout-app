package com.workout.app.data.repository

import com.workout.app.domain.model.Result
import com.workout.app.domain.model.SetData

/**
 * Aggregate statistics for an exercise across all sessions.
 */
data class ExerciseStats(
    val totalSets: Long,
    val totalVolume: Double,
    val maxWeight: Double,
    val avgReps: Double,
    val lastPerformed: Long?
)

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
     * Get previous sets for an exercise from completed sessions.
     * @param exerciseId Exercise ID
     * @param excludeSessionId Session ID to exclude (current session)
     * @param limit Maximum number of sets to return
     * @return Result containing list of sets
     */
    suspend fun getPreviousByExercise(
        exerciseId: String,
        excludeSessionId: String,
        limit: Int = 10
    ): Result<List<SetData>>

    /**
     * Delete all sets for a session.
     * @param sessionId Session ID
     * @return Result indicating success or failure
     */
    suspend fun deleteBySession(sessionId: String): Result<Unit>

    /**
     * Get all sets for an exercise across all sessions.
     * @param exerciseId Exercise ID
     * @return Result containing list of sets ordered by completedAt DESC
     */
    suspend fun getByExercise(exerciseId: String): Result<List<SetData>>

    /**
     * Get aggregate statistics for an exercise.
     * @param exerciseId Exercise ID
     * @return Result containing exercise stats
     */
    suspend fun getExerciseStats(exerciseId: String): Result<ExerciseStats>

    /**
     * Get the personal record (max weight at 1 rep) for an exercise.
     * @param exerciseId Exercise ID
     * @return Result containing max weight or null if no 1RM exists
     */
    suspend fun getPersonalRecord(exerciseId: String): Result<Double?>

    /**
     * Get the last trained timestamp per muscle group from completed sessions.
     * @return Result containing a map of muscleGroup name to lastTrainedAt epoch millis
     */
    suspend fun getLastTrainedPerMuscleGroup(): Result<Map<String, Long>>

    /**
     * Get weekly set count per muscle group from completed sessions in the last 7 days.
     * @return Result containing a map of muscleGroup name to set count
     */
    suspend fun getWeeklySetCountPerMuscleGroup(): Result<Map<String, Long>>
}
