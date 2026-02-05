package com.workout.app.data.repository

import com.workout.app.database.SetQueries
import com.workout.app.domain.model.Result
import com.workout.app.domain.model.SetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Implementation of SetRepository using SQLDelight.
 */
class SetRepositoryImpl(
    private val setQueries: SetQueries
) : SetRepository {

    override suspend fun createSet(
        sessionId: String,
        sessionExerciseId: String,
        exerciseId: String,
        setNumber: Int,
        weight: Double,
        reps: Int,
        rpe: Int?,
        isWarmup: Boolean,
        notes: String?
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val id = generateId()
            val now = Clock.System.now().toEpochMilliseconds()

            setQueries.insert(
                id = id,
                sessionId = sessionId,
                sessionExerciseId = sessionExerciseId,
                exerciseId = exerciseId,
                setNumber = setNumber.toLong(),
                weight = weight,
                reps = reps.toLong(),
                rpe = rpe?.toLong(),
                isWarmup = if (isWarmup) 1L else 0L,
                restTime = null,
                notes = notes,
                completedAt = now,
                createdAt = now
            )

            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getBySession(sessionId: String): Result<List<SetData>> =
        withContext(Dispatchers.Default) {
            try {
                val sets = setQueries.selectBySession(sessionId).executeAsList()

                val result = sets.map { set ->
                    SetData(
                        id = set.id,
                        setNumber = set.setNumber.toInt(),
                        weight = set.weight,
                        reps = set.reps.toInt(),
                        rpe = set.rpe?.toInt(),
                        isWarmup = set.isWarmup == 1L,
                        completedAt = set.completedAt,
                        isPR = false,
                        sessionId = set.sessionId
                    )
                }

                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getBySessionExercise(sessionExerciseId: String): Result<List<SetData>> =
        withContext(Dispatchers.Default) {
            try {
                val sets = setQueries.selectBySessionExercise(sessionExerciseId).executeAsList()

                val result = sets.map { set ->
                    SetData(
                        id = set.id,
                        setNumber = set.setNumber.toInt(),
                        weight = set.weight,
                        reps = set.reps.toInt(),
                        rpe = set.rpe?.toInt(),
                        isWarmup = set.isWarmup == 1L,
                        completedAt = set.completedAt,
                        isPR = false,
                        sessionId = set.sessionId
                    )
                }

                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun calculateSessionVolume(sessionId: String): Result<Double> =
        withContext(Dispatchers.Default) {
            try {
                val volume = setQueries.calculateSessionVolume(sessionId).executeAsOneOrNull()
                Result.Success(volume?.totalVolume ?: 0.0)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun countBySession(sessionId: String): Result<Long> =
        withContext(Dispatchers.Default) {
            try {
                val count = setQueries.countBySession(sessionId).executeAsOne()
                // The query returns COUNT(*) AS setCount, which is of type Long
                Result.Success(count)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getPreviousByExercise(
        exerciseId: String,
        excludeSessionId: String,
        limit: Int
    ): Result<List<SetData>> = withContext(Dispatchers.Default) {
        try {
            val sets = setQueries.selectPreviousByExercise(
                exerciseId,
                excludeSessionId,
                limit.toLong()
            ).executeAsList()

            val result = sets.map { set ->
                SetData(
                    id = set.id,
                    setNumber = set.setNumber.toInt(),
                    weight = set.weight,
                    reps = set.reps.toInt(),
                    rpe = set.rpe?.toInt(),
                    isWarmup = set.isWarmup == 1L,
                    completedAt = set.completedAt,
                    isPR = false,
                    sessionId = set.sessionId
                )
            }

            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteBySession(sessionId: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            try {
                setQueries.deleteBySession(sessionId)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getByExercise(exerciseId: String): Result<List<SetData>> =
        withContext(Dispatchers.Default) {
            try {
                val sets = setQueries.selectByExercise(exerciseId).executeAsList()

                val result = sets.map { set ->
                    SetData(
                        id = set.id,
                        setNumber = set.setNumber.toInt(),
                        weight = set.weight,
                        reps = set.reps.toInt(),
                        rpe = set.rpe?.toInt(),
                        isWarmup = set.isWarmup == 1L,
                        completedAt = set.completedAt,
                        isPR = false,
                        sessionId = set.sessionId
                    )
                }

                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getExerciseStats(exerciseId: String): Result<ExerciseStats> =
        withContext(Dispatchers.Default) {
            try {
                val stats = setQueries.getExerciseStats(exerciseId).executeAsOne()
                Result.Success(
                    ExerciseStats(
                        totalSets = stats.totalSets,
                        totalVolume = stats.totalVolume ?: 0.0,
                        maxWeight = stats.maxWeight ?: 0.0,
                        avgReps = stats.avgReps ?: 0.0,
                        lastPerformed = stats.lastPerformed
                    )
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getPersonalRecord(exerciseId: String): Result<Double?> =
        withContext(Dispatchers.Default) {
            try {
                val record = setQueries.getPersonalRecord(exerciseId).executeAsOneOrNull()
                Result.Success(record?.maxWeight)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getLastTrainedPerMuscleGroup(): Result<Map<String, Long>> =
        withContext(Dispatchers.Default) {
            try {
                val rows = setQueries.getLastTrainedPerMuscleGroup().executeAsList()
                val result = mutableMapOf<String, Long>()
                rows.forEach { row ->
                    val ts = row.lastTrainedAt
                    if (ts != null) {
                        result[row.muscleGroup] = ts
                    }
                }
                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    /**
     * Generate a unique ID for a new set.
     */
    private fun generateId(): String {
        return "set_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }
}
