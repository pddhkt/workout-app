package com.workout.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.workout.app.database.Session
import com.workout.app.database.SessionQueries
import com.workout.app.database.SessionExerciseQueries
import com.workout.app.database.SetQueries
import com.workout.app.database.SelectWithExercises
import com.workout.app.domain.model.ExerciseWithSets
import com.workout.app.domain.model.SetData
import com.workout.app.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Implementation of SessionRepository using SQLDelight.
 */
class SessionRepositoryImpl(
    private val sessionQueries: SessionQueries,
    private val sessionExerciseQueries: SessionExerciseQueries,
    private val setQueries: SetQueries
) : SessionRepository {

    override fun observeAll(): Flow<List<Session>> {
        return sessionQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override fun observeActive(): Flow<List<Session>> {
        return sessionQueries.selectActive()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override fun observeByStatus(status: String): Flow<List<Session>> {
        return sessionQueries.selectByStatus(status)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override suspend fun getById(id: String): Result<Session?> = withContext(Dispatchers.Default) {
        try {
            val session = sessionQueries.selectById(id).executeAsOneOrNull()
            Result.Success(session)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getByWorkoutId(workoutId: String): Result<Session?> = withContext(Dispatchers.Default) {
        try {
            val session = sessionQueries.selectByWorkoutId(workoutId).executeAsOneOrNull()
            Result.Success(session)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getWithExercises(id: String): Result<List<SelectWithExercises>> =
        withContext(Dispatchers.Default) {
            try {
                val session = sessionQueries.selectWithExercises(id).executeAsList()
                Result.Success(session)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getAll(): Result<List<Session>> = withContext(Dispatchers.Default) {
        try {
            val sessions = sessionQueries.selectAll().executeAsList()
            Result.Success(sessions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun create(
        name: String,
        templateId: String?,
        isPartnerWorkout: Boolean,
        status: String
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val id = generateId()
            val now = Clock.System.now().toEpochMilliseconds()

            sessionQueries.insert(
                id = id,
                workoutId = null,
                templateId = templateId,
                name = name,
                startTime = now,
                endTime = null,
                status = status,
                notes = null,
                isPartnerWorkout = if (isPartnerWorkout) 1 else 0,
                currentExerciseIndex = 0,
                createdAt = now,
                updatedAt = now
            )

            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun update(session: Session): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()

            sessionQueries.update(
                name = session.name,
                endTime = session.endTime,
                status = session.status,
                notes = session.notes,
                isPartnerWorkout = session.isPartnerWorkout,
                currentExerciseIndex = session.currentExerciseIndex,
                updatedAt = now,
                id = session.id
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateStatus(id: String, status: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            try {
                val now = Clock.System.now().toEpochMilliseconds()
                sessionQueries.updateStatus(status = status, updatedAt = now, id = id)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun complete(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            sessionQueries.complete(endTime = now, updatedAt = now, id = id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            sessionQueries.delete(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCountByStatus(): Result<Map<String, Long>> = withContext(Dispatchers.Default) {
        try {
            val counts = sessionQueries.countByStatus().executeAsList()
            val map = counts.associate { it.status to it.sessionCount }
            Result.Success(map)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getExercisesWithSets(sessionId: String): Result<List<ExerciseWithSets>> =
        withContext(Dispatchers.Default) {
            try {
                // Get session exercises with joined exercise info
                val sessionExercises = sessionExerciseQueries.selectBySession(sessionId).executeAsList()

                // Build exercises with sets by querying sets for each exercise
                val exercises = sessionExercises.map { se ->
                    // Get sets for this session exercise using the simple query
                    val setsForExercise = setQueries.selectBySessionExercise(se.id).executeAsList()

                    val exerciseSets = setsForExercise.map { setRow ->
                        SetData(
                            id = setRow.id,
                            setNumber = setRow.setNumber.toInt(),
                            weight = setRow.weight,
                            reps = setRow.reps.toInt(),
                            rpe = setRow.rpe?.toInt(),
                            isWarmup = setRow.isWarmup == 1L,
                            completedAt = setRow.completedAt,
                            isPR = false
                        )
                    }.sortedBy { it.setNumber }

                    ExerciseWithSets(
                        exerciseId = se.exerciseId,
                        exerciseName = se.exerciseName ?: "Unknown",
                        muscleGroup = se.exerciseMuscleGroup ?: "Other",
                        category = se.exerciseCategory,
                        sets = exerciseSets
                    )
                }

                Result.Success(exercises)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    /**
     * Generate a unique ID for a new session.
     */
    private fun generateId(): String {
        return "session_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }
}
