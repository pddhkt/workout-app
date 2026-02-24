package com.workout.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.workout.app.database.Goal
import com.workout.app.database.GoalProgressQueries
import com.workout.app.database.GoalQueries
import com.workout.app.domain.model.GoalFrequency
import com.workout.app.domain.model.GoalMetric
import com.workout.app.domain.model.GoalPeriodEntry
import com.workout.app.domain.model.GoalWithProgress
import com.workout.app.domain.model.Result
import com.workout.app.domain.model.calculatePeriodBounds
import com.workout.app.domain.model.deriveGoalStatus
import com.workout.app.domain.model.exerciseIdsToJson
import com.workout.app.domain.model.parseExerciseIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * SQLDelight implementation of GoalRepository.
 */
class GoalRepositoryImpl(
    private val goalQueries: GoalQueries,
    private val goalProgressQueries: GoalProgressQueries
) : GoalRepository {

    override fun observeActiveGoals(): Flow<List<GoalWithProgress>> {
        val now = Clock.System.now().toEpochMilliseconds()
        return goalQueries.selectActiveWithProgress(now)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows ->
                rows.map { row ->
                    val progressValue = row.progressValue ?: 0.0
                    val progressCompleted = (row.progressCompleted ?: 0L) == 1L
                    val exerciseIds = parseExerciseIds(row.exerciseIds)
                    val frequency = GoalFrequency.fromString(row.frequency)
                    val metric = GoalMetric.fromString(row.metric)
                    val streak = calculateStreakSync(row.id)

                    GoalWithProgress(
                        id = row.id,
                        name = row.name,
                        exerciseIds = exerciseIds,
                        metric = metric,
                        targetValue = row.targetValue,
                        targetUnit = row.targetUnit,
                        frequency = frequency,
                        startDate = row.startDate,
                        endDate = row.endDate,
                        isActive = row.isActive == 1L,
                        autoTrack = row.autoTrack == 1L,
                        currentPeriodValue = progressValue,
                        currentPeriodCompleted = progressCompleted,
                        status = deriveGoalStatus(
                            isActive = row.isActive == 1L,
                            endDate = row.endDate,
                            currentPeriodCompleted = progressCompleted
                        ),
                        streakCount = streak,
                        createdAt = row.createdAt
                    )
                }
            }
    }

    override fun observeAllGoals(): Flow<List<GoalWithProgress>> {
        val now = Clock.System.now().toEpochMilliseconds()
        return goalQueries.selectAllWithProgress(now)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows ->
                rows.map { row ->
                    val progressValue = row.progressValue ?: 0.0
                    val progressCompleted = (row.progressCompleted ?: 0L) == 1L
                    val exerciseIds = parseExerciseIds(row.exerciseIds)
                    val frequency = GoalFrequency.fromString(row.frequency)
                    val metric = GoalMetric.fromString(row.metric)
                    val streak = calculateStreakSync(row.id)

                    GoalWithProgress(
                        id = row.id,
                        name = row.name,
                        exerciseIds = exerciseIds,
                        metric = metric,
                        targetValue = row.targetValue,
                        targetUnit = row.targetUnit,
                        frequency = frequency,
                        startDate = row.startDate,
                        endDate = row.endDate,
                        isActive = row.isActive == 1L,
                        autoTrack = row.autoTrack == 1L,
                        currentPeriodValue = progressValue,
                        currentPeriodCompleted = progressCompleted,
                        status = deriveGoalStatus(
                            isActive = row.isActive == 1L,
                            endDate = row.endDate,
                            currentPeriodCompleted = progressCompleted
                        ),
                        streakCount = streak,
                        createdAt = row.createdAt
                    )
                }
            }
    }

    override suspend fun getById(id: String): Result<GoalWithProgress?> = withContext(Dispatchers.Default) {
        try {
            val goal = goalQueries.selectById(id).executeAsOneOrNull()
                ?: return@withContext Result.Success(null)

            val now = Clock.System.now().toEpochMilliseconds()
            val frequency = GoalFrequency.fromString(goal.frequency)
            val metric = GoalMetric.fromString(goal.metric)
            val exerciseIds = parseExerciseIds(goal.exerciseIds)

            // Get current period progress
            val (periodStart, periodEnd) = calculatePeriodBounds(frequency, now)
            val progress = goalProgressQueries.selectByGoalAndPeriod(id, periodStart)
                .executeAsOneOrNull()
            val progressValue = progress?.currentValue ?: 0.0
            val progressCompleted = (progress?.isCompleted ?: 0L) == 1L

            val streak = calculateStreakSync(id)

            Result.Success(
                GoalWithProgress(
                    id = goal.id,
                    name = goal.name,
                    exerciseIds = exerciseIds,
                    metric = metric,
                    targetValue = goal.targetValue,
                    targetUnit = goal.targetUnit,
                    frequency = frequency,
                    startDate = goal.startDate,
                    endDate = goal.endDate,
                    isActive = goal.isActive == 1L,
                    autoTrack = goal.autoTrack == 1L,
                    currentPeriodValue = progressValue,
                    currentPeriodCompleted = progressCompleted,
                    status = deriveGoalStatus(
                        isActive = goal.isActive == 1L,
                        endDate = goal.endDate,
                        currentPeriodCompleted = progressCompleted
                    ),
                    streakCount = streak,
                    createdAt = goal.createdAt
                )
            )
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun create(
        name: String,
        exerciseIds: List<String>,
        metric: String,
        targetValue: Double,
        targetUnit: String,
        frequency: String,
        startDate: Long,
        endDate: Long?,
        autoTrack: Boolean
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val id = generateId()
            val now = Clock.System.now().toEpochMilliseconds()

            goalQueries.insert(
                id = id,
                name = name,
                exerciseIds = exerciseIdsToJson(exerciseIds),
                metric = metric,
                targetValue = targetValue,
                targetUnit = targetUnit,
                frequency = frequency,
                startDate = startDate,
                endDate = endDate,
                isActive = 1L,
                autoTrack = if (autoTrack) 1L else 0L,
                createdAt = now,
                updatedAt = now
            )

            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun update(
        id: String,
        name: String,
        exerciseIds: List<String>,
        metric: String,
        targetValue: Double,
        targetUnit: String,
        frequency: String,
        startDate: Long,
        endDate: Long?,
        autoTrack: Boolean
    ): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            goalQueries.update(
                name = name,
                exerciseIds = exerciseIdsToJson(exerciseIds),
                metric = metric,
                targetValue = targetValue,
                targetUnit = targetUnit,
                frequency = frequency,
                startDate = startDate,
                endDate = endDate,
                autoTrack = if (autoTrack) 1L else 0L,
                updatedAt = now,
                id = id
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun toggleActive(id: String, isActive: Boolean): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            goalQueries.updateActive(
                isActive = if (isActive) 1L else 0L,
                updatedAt = now,
                id = id
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            goalProgressQueries.deleteByGoal(id)
            goalQueries.delete(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun clone(sourceGoalId: String): Result<String> = withContext(Dispatchers.Default) {
        try {
            val source = goalQueries.selectById(sourceGoalId).executeAsOneOrNull()
                ?: return@withContext Result.Error(Exception("Goal not found"))

            val newId = generateId()
            val now = Clock.System.now().toEpochMilliseconds()

            goalQueries.insert(
                id = newId,
                name = source.name,
                exerciseIds = source.exerciseIds,
                metric = source.metric,
                targetValue = source.targetValue,
                targetUnit = source.targetUnit,
                frequency = source.frequency,
                startDate = now,
                endDate = null, // Reset end date — user can set it
                isActive = 1L,
                autoTrack = source.autoTrack,
                createdAt = now,
                updatedAt = now
            )

            Result.Success(newId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun addProgress(
        goalId: String,
        value: Double,
        timestamp: Long
    ): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val goal = goalQueries.selectById(goalId).executeAsOneOrNull()
                ?: return@withContext Result.Error(Exception("Goal not found"))

            val frequency = GoalFrequency.fromString(goal.frequency)
            val (periodStart, periodEnd) = calculatePeriodBounds(frequency, timestamp)
            val now = Clock.System.now().toEpochMilliseconds()

            val existing = goalProgressQueries.selectByGoalAndPeriod(goalId, periodStart)
                .executeAsOneOrNull()

            if (existing != null) {
                val newValue = existing.currentValue + value
                val isCompleted = if (newValue >= goal.targetValue) 1L else 0L
                goalProgressQueries.updateProgress(
                    currentValue = newValue,
                    isCompleted = isCompleted,
                    updatedAt = now,
                    id = existing.id
                )
            } else {
                val progressId = "gp_${now}_${(0..9999).random()}"
                val isCompleted = if (value >= goal.targetValue) 1L else 0L
                goalProgressQueries.insert(
                    id = progressId,
                    goalId = goalId,
                    periodStart = periodStart,
                    periodEnd = periodEnd,
                    currentValue = value,
                    isCompleted = isCompleted,
                    updatedAt = now
                )
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun processWorkoutCompletion(
        exerciseIds: List<String>,
        setData: Map<String, Map<String, Double>>
    ): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val activeGoals = goalQueries.selectActive().executeAsList()
            val now = Clock.System.now().toEpochMilliseconds()

            for (goal in activeGoals) {
                if (goal.autoTrack != 1L) continue

                val goalExerciseIds = parseExerciseIds(goal.exerciseIds)
                val matchingExerciseIds = exerciseIds.filter { it in goalExerciseIds }
                if (matchingExerciseIds.isEmpty()) continue

                // Sum the relevant metric across matching exercises
                val metricValue = when (goal.metric) {
                    "distance" -> matchingExerciseIds.sumOf { id ->
                        setData[id]?.get("distance") ?: 0.0
                    }
                    "duration" -> matchingExerciseIds.sumOf { id ->
                        setData[id]?.get("duration") ?: 0.0
                    }
                    "reps" -> matchingExerciseIds.sumOf { id ->
                        setData[id]?.get("reps") ?: 0.0
                    }
                    "sets" -> matchingExerciseIds.sumOf { id ->
                        setData[id]?.get("sets") ?: 0.0
                    }
                    "volume" -> matchingExerciseIds.sumOf { id ->
                        setData[id]?.get("volume") ?: 0.0
                    }
                    "sessions" -> 1.0 // One session counts as 1
                    else -> 0.0
                }

                if (metricValue <= 0.0) continue

                // Find or create progress for current period
                val frequency = GoalFrequency.fromString(goal.frequency)
                val (periodStart, periodEnd) = calculatePeriodBounds(frequency, now)

                val existing = goalProgressQueries.selectByGoalAndPeriod(goal.id, periodStart)
                    .executeAsOneOrNull()

                if (existing != null) {
                    val newValue = existing.currentValue + metricValue
                    val isCompleted = if (newValue >= goal.targetValue) 1L else 0L
                    goalProgressQueries.updateProgress(
                        currentValue = newValue,
                        isCompleted = isCompleted,
                        updatedAt = now,
                        id = existing.id
                    )
                } else {
                    val progressId = "gp_${now}_${(0..9999).random()}"
                    val isCompleted = if (metricValue >= goal.targetValue) 1L else 0L
                    goalProgressQueries.insert(
                        id = progressId,
                        goalId = goal.id,
                        periodStart = periodStart,
                        periodEnd = periodEnd,
                        currentValue = metricValue,
                        isCompleted = isCompleted,
                        updatedAt = now
                    )
                }
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getPeriodHistory(
        goalId: String,
        limit: Int
    ): Result<List<GoalPeriodEntry>> = withContext(Dispatchers.Default) {
        try {
            val goal = goalQueries.selectById(goalId).executeAsOneOrNull()
                ?: return@withContext Result.Error(Exception("Goal not found"))

            val history = goalProgressQueries.selectRecentByGoal(goalId, limit.toLong())
                .executeAsList()
                .map { progress ->
                    GoalPeriodEntry(
                        periodStart = progress.periodStart,
                        periodEnd = progress.periodEnd,
                        value = progress.currentValue,
                        targetValue = goal.targetValue,
                        isCompleted = progress.isCompleted == 1L
                    )
                }

            Result.Success(history)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getStreak(goalId: String): Result<Int> = withContext(Dispatchers.Default) {
        try {
            Result.Success(calculateStreakSync(goalId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Calculate consecutive completed periods (streak).
     * Counts backwards from most recent completed period.
     */
    private fun calculateStreakSync(goalId: String): Int {
        val completedPeriods = goalProgressQueries.selectCompletedByGoal(goalId)
            .executeAsList()

        if (completedPeriods.isEmpty()) return 0

        // Periods are ordered by periodStart DESC
        // Count consecutive completed ones (no gaps)
        var streak = 0
        var previousPeriodStart: Long? = null

        for (period in completedPeriods) {
            if (previousPeriodStart == null) {
                // First (most recent) completed period
                streak = 1
                previousPeriodStart = period.periodStart
            } else {
                // Check if this period is consecutive with the previous one
                // The previous period's start should equal this period's end + 1 (approximately)
                val gap = previousPeriodStart - period.periodEnd
                if (gap <= 1) {
                    streak++
                    previousPeriodStart = period.periodStart
                } else {
                    break
                }
            }
        }

        return streak
    }

    private fun generateId(): String {
        return "goal_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }
}
