package com.workout.app.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Goal frequency/cadence.
 */
enum class GoalFrequency(val label: String, val value: String) {
    DAILY("Daily", "daily"),
    WEEKLY("Weekly", "weekly"),
    MONTHLY("Monthly", "monthly");

    companion object {
        fun fromString(value: String): GoalFrequency {
            return entries.firstOrNull { it.value == value } ?: WEEKLY
        }
    }
}

/**
 * What metric the goal tracks.
 */
enum class GoalMetric(val label: String, val defaultUnit: String, val value: String) {
    DISTANCE("Distance", "km", "distance"),
    DURATION("Duration", "min", "duration"),
    REPS("Reps", "reps", "reps"),
    SETS("Sets", "sets", "sets"),
    SESSIONS("Sessions", "sessions", "sessions"),
    VOLUME("Volume", "kg", "volume");

    companion object {
        fun fromString(value: String): GoalMetric {
            return entries.firstOrNull { it.value == value } ?: SESSIONS
        }

        /**
         * Returns available metrics based on exercise recording field keys.
         */
        fun availableForFields(fieldKeys: Set<String>): List<GoalMetric> {
            val metrics = mutableListOf<GoalMetric>()
            if ("distance" in fieldKeys) metrics.add(DISTANCE)
            if ("duration" in fieldKeys) metrics.add(DURATION)
            if ("reps" in fieldKeys || "weight" in fieldKeys) {
                metrics.add(REPS)
                metrics.add(SETS)
                metrics.add(VOLUME)
            }
            metrics.add(SESSIONS)
            return metrics.ifEmpty { listOf(SESSIONS) }
        }
    }
}

/**
 * Goal status derived from isActive, endDate, and progress.
 */
enum class GoalStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    EXPIRED
}

/**
 * UI model for a goal with its current period progress.
 */
data class GoalWithProgress(
    val id: String,
    val name: String,
    val exerciseIds: List<String>,
    val metric: GoalMetric,
    val targetValue: Double,
    val targetUnit: String,
    val frequency: GoalFrequency,
    val startDate: Long,
    val endDate: Long?,
    val isActive: Boolean,
    val autoTrack: Boolean,
    val currentPeriodValue: Double,
    val currentPeriodCompleted: Boolean,
    val status: GoalStatus,
    val streakCount: Int = 0,
    val createdAt: Long = 0L
) {
    val progressFraction: Float
        get() = if (targetValue > 0) {
            (currentPeriodValue / targetValue).coerceIn(0.0, 1.0).toFloat()
        } else 0f

    val isOngoing: Boolean
        get() = endDate == null

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}

/**
 * Period history entry for goal detail screen.
 */
data class GoalPeriodEntry(
    val periodStart: Long,
    val periodEnd: Long,
    val value: Double,
    val targetValue: Double,
    val isCompleted: Boolean
) {
    val progressFraction: Float
        get() = if (targetValue > 0) {
            (value / targetValue).coerceIn(0.0, 1.0).toFloat()
        } else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}

/**
 * Parse a JSON array of exercise IDs.
 * Format: ["id1","id2","id3"]
 */
fun parseExerciseIds(json: String): List<String> {
    if (json.isBlank() || json == "[]") return emptyList()
    return json
        .removeSurrounding("[", "]")
        .split(",")
        .map { it.trim().removeSurrounding("\"") }
        .filter { it.isNotBlank() }
}

/**
 * Serialize a list of exercise IDs to JSON array.
 */
fun exerciseIdsToJson(ids: List<String>): String {
    if (ids.isEmpty()) return "[]"
    return ids.joinToString(
        prefix = "[",
        postfix = "]",
        separator = ","
    ) { "\"$it\"" }
}

/**
 * Calculate period start and end timestamps for a given frequency and reference timestamp.
 * Returns (periodStart, periodEnd) in epoch milliseconds.
 *
 * - DAILY: start of day to end of day
 * - WEEKLY: Monday 00:00 to Sunday 23:59:59.999
 * - MONTHLY: 1st of month 00:00 to last day of month 23:59:59.999
 */
fun calculatePeriodBounds(frequency: GoalFrequency, timestampMillis: Long): Pair<Long, Long> {
    val tz = TimeZone.currentSystemDefault()
    val instant = Instant.fromEpochMilliseconds(timestampMillis)
    val localDate = instant.toLocalDateTime(tz).date

    return when (frequency) {
        GoalFrequency.DAILY -> {
            val start = localDate.atStartOfDayIn(tz).toEpochMilliseconds()
            val end = localDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz).toEpochMilliseconds() - 1
            start to end
        }
        GoalFrequency.WEEKLY -> {
            // Week starts on Monday
            val daysSinceMonday = (localDate.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
            val monday = LocalDate.fromEpochDays(localDate.toEpochDays() - daysSinceMonday)
            val start = monday.atStartOfDayIn(tz).toEpochMilliseconds()
            val end = monday.plus(7, DateTimeUnit.DAY).atStartOfDayIn(tz).toEpochMilliseconds() - 1
            start to end
        }
        GoalFrequency.MONTHLY -> {
            val firstOfMonth = LocalDate(localDate.year, localDate.month, 1)
            val firstOfNextMonth = firstOfMonth.plus(1, DateTimeUnit.MONTH)
            val start = firstOfMonth.atStartOfDayIn(tz).toEpochMilliseconds()
            val end = firstOfNextMonth.atStartOfDayIn(tz).toEpochMilliseconds() - 1
            start to end
        }
    }
}

/**
 * Derive goal status from its properties.
 */
fun deriveGoalStatus(
    isActive: Boolean,
    endDate: Long?,
    currentPeriodCompleted: Boolean
): GoalStatus {
    if (!isActive) return GoalStatus.PAUSED

    val now = Clock.System.now().toEpochMilliseconds()
    if (endDate != null && endDate < now) {
        return if (currentPeriodCompleted) GoalStatus.COMPLETED else GoalStatus.EXPIRED
    }

    return GoalStatus.ACTIVE
}
