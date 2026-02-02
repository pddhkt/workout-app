package com.workout.app.domain.model

/**
 * Domain models for workout history and session details.
 */

/**
 * Represents a workout history item for list display.
 * Contains summary information for quick display in history cards.
 */
data class WorkoutHistoryItem(
    val id: String,
    val name: String,
    val createdAt: Long,
    val duration: Long,
    val totalVolume: Long,
    val totalSets: Long,
    val exerciseCount: Long,
    val isPartnerWorkout: Boolean,
    val notes: String?,
    val muscleGroups: List<String> = emptyList(),
    val prCount: Int = 0,
    val rpe: Int? = null
)

/**
 * Represents a complete workout with all exercise and set details.
 * Used for the session detail screen.
 */
data class WorkoutWithSets(
    val id: String,
    val name: String,
    val createdAt: Long,
    val endTime: Long?,
    val duration: Long,
    val totalVolume: Long,
    val totalSets: Long,
    val exerciseCount: Long,
    val isPartnerWorkout: Boolean,
    val notes: String?,
    val rpe: Int?,
    val exercises: List<ExerciseWithSets>,
    val muscleGroups: List<MuscleGroupIntensity>,
    val prCount: Int = 0
)

/**
 * Represents an exercise within a workout with its performed sets.
 */
data class ExerciseWithSets(
    val exerciseId: String,
    val exerciseName: String,
    val muscleGroup: String,
    val category: String?,
    val sets: List<SetData>,
    val hasPR: Boolean = false,
    val bestSet: SetData? = null
)

/**
 * Represents a single set's data.
 */
data class SetData(
    val id: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val rpe: Int?,
    val isWarmup: Boolean,
    val completedAt: Long,
    val isPR: Boolean = false
)

/**
 * Represents muscle group intensity for visualization.
 */
data class MuscleGroupIntensity(
    val muscleGroup: String,
    val intensity: IntensityLevel,
    val setCount: Int,
    val volume: Long
)

/**
 * Intensity level for muscle group visualization.
 */
enum class IntensityLevel {
    LOW,      // 1-2 sets
    MEDIUM,   // 3-4 sets
    HIGH,     // 5-6 sets
    VERY_HIGH // 7+ sets
}

/**
 * Month group header for history list.
 */
data class MonthGroup(
    val yearMonth: String,   // "YYYY-MM"
    val displayName: String, // "January 2026"
    val sessionCount: Int,
    val totalVolume: Long,
    val totalDuration: Long
)

/**
 * Filter options for workout history.
 */
data class WorkoutHistoryFilters(
    val dateRange: DateRange? = null,
    val muscleGroups: List<String> = emptyList(),
    val workoutType: WorkoutType? = null,
    val searchQuery: String = ""
)

/**
 * Date range for filtering.
 */
data class DateRange(
    val startDate: Long,
    val endDate: Long
)

/**
 * Workout type for filtering.
 */
enum class WorkoutType {
    ALL,
    SOLO,
    PARTNER
}

/**
 * Participant type for partner workout display.
 */
enum class Participant {
    ME,
    PARTNER
}

/**
 * Utility to calculate intensity level from set count.
 */
fun calculateIntensity(setCount: Int): IntensityLevel = when {
    setCount <= 2 -> IntensityLevel.LOW
    setCount <= 4 -> IntensityLevel.MEDIUM
    setCount <= 6 -> IntensityLevel.HIGH
    else -> IntensityLevel.VERY_HIGH
}

/**
 * Format month string to display name.
 */
fun formatMonthDisplay(yearMonth: String): String {
    val parts = yearMonth.split("-")
    if (parts.size != 2) return yearMonth

    val year = parts[0]
    val month = when (parts[1]) {
        "01" -> "January"
        "02" -> "February"
        "03" -> "March"
        "04" -> "April"
        "05" -> "May"
        "06" -> "June"
        "07" -> "July"
        "08" -> "August"
        "09" -> "September"
        "10" -> "October"
        "11" -> "November"
        "12" -> "December"
        else -> parts[1]
    }

    return "$month $year"
}
