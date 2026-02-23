package com.workout.app.data.repository

import com.workout.app.database.SessionExerciseQueries
import com.workout.app.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Implementation of SessionExerciseRepository using SQLDelight.
 */
class SessionExerciseRepositoryImpl(
    private val sessionExerciseQueries: SessionExerciseQueries
) : SessionExerciseRepository {

    override suspend fun addExercisesToSession(
        sessionId: String,
        exercises: List<AddedExerciseInput>
    ): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()

            exercises.forEach { exercise ->
                val id = generateId()
                sessionExerciseQueries.insert(
                    id = id,
                    sessionId = sessionId,
                    exerciseId = exercise.exerciseId,
                    orderIndex = exercise.orderIndex.toLong(),
                    targetSets = exercise.targetSets.toLong(),
                    completedSets = 0L,
                    notes = null,
                    targetValues = exercise.targetValues,
                    createdAt = now,
                    updatedAt = now
                )
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getBySession(sessionId: String): Result<List<SessionExerciseWithDetails>> =
        withContext(Dispatchers.Default) {
            try {
                val sessionExercises = sessionExerciseQueries.selectBySession(sessionId).executeAsList()

                val result = sessionExercises.map { se ->
                    SessionExerciseWithDetails(
                        id = se.id,
                        sessionId = se.sessionId,
                        exerciseId = se.exerciseId,
                        exerciseName = se.exerciseName ?: "Unknown",
                        muscleGroup = se.exerciseMuscleGroup ?: "Other",
                        category = se.exerciseCategory,
                        orderIndex = se.orderIndex.toInt(),
                        targetSets = se.targetSets.toInt(),
                        completedSets = se.completedSets.toInt(),
                        targetValues = se.targetValues,
                        recordingFields = se.exerciseRecordingFields
                    )
                }

                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun incrementCompletedSets(id: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            try {
                val now = Clock.System.now().toEpochMilliseconds()
                sessionExerciseQueries.incrementCompletedSets(updatedAt = now, id = id)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun updateCompletedSets(id: String, completedSets: Int): Result<Unit> =
        withContext(Dispatchers.Default) {
            try {
                val now = Clock.System.now().toEpochMilliseconds()
                sessionExerciseQueries.updateCompletedSets(
                    completedSets = completedSets.toLong(),
                    updatedAt = now,
                    id = id
                )
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun updateTargetSets(id: String, targetSets: Int): Result<Unit> =
        withContext(Dispatchers.Default) {
            try {
                val now = Clock.System.now().toEpochMilliseconds()
                sessionExerciseQueries.updateTargetSets(
                    targetSets = targetSets.toLong(),
                    updatedAt = now,
                    id = id
                )
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun deleteBySession(sessionId: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            try {
                sessionExerciseQueries.deleteBySession(sessionId)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun deleteById(id: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            try {
                sessionExerciseQueries.deleteById(id)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun updateOrderIndex(id: String, newOrderIndex: Int): Result<Unit> =
        withContext(Dispatchers.Default) {
            try {
                val now = Clock.System.now().toEpochMilliseconds()
                sessionExerciseQueries.updateOrderIndex(
                    orderIndex = newOrderIndex.toLong(),
                    updatedAt = now,
                    id = id
                )
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    /**
     * Generate a unique ID for a new session exercise.
     */
    private fun generateId(): String {
        return "se_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }
}
