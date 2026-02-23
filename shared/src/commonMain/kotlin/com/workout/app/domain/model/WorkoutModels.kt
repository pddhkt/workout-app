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
    val isPR: Boolean = false,
    val sessionId: String = "",
    val fieldValues: Map<String, String>? = null
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
 * Session mode for workout planning.
 */
enum class SessionMode(val label: String) {
    SOLO("Solo"),
    COACHING("Coaching"),
    GROUP("Group")
}

/**
 * A participant in a multi-person workout session.
 */
data class SessionParticipant(
    val id: String,
    val name: String,
    val isOwner: Boolean = false
)

/**
 * Time range for muscle recovery display.
 */
enum class RecoveryTimeRange(val label: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    fun next(): RecoveryTimeRange = when (this) {
        WEEKLY -> MONTHLY
        MONTHLY -> WEEKLY
    }
}

/**
 * Recovery status for a muscle group.
 */
enum class RecoveryStatus(val label: String) {
    REST("Rest"),
    RECOVERING("Recov."),
    READY("Ready"),
    TRAIN("Train!"),
    NEW("New")
}

/**
 * Muscle recovery data for planning screen.
 */
data class MuscleRecovery(
    val muscleGroup: String,
    val daysSinceLastTrained: Int?,
    val hoursSinceLastTrained: Int? = null,
    val status: RecoveryStatus,
    val progress: Float, // 0f..1f for bar fill
    val weeklySetCount: Int = 0
) {
    /** Format elapsed time: "Xd" for days, "Xh" for sub-day, "--" for never. */
    val elapsedLabel: String
        get() = when {
            daysSinceLastTrained == null -> "--"
            daysSinceLastTrained > 0 -> "${daysSinceLastTrained}d"
            hoursSinceLastTrained != null -> "${hoursSinceLastTrained}h"
            else -> "0d"
        }
}

/**
 * Muscle size category affects recovery windows.
 * Based on sports science research (RP, Schoenfeld et al.).
 */
enum class MuscleSizeCategory {
    SMALL,  // Arms, Shoulders, Core — recover in 1-3 days
    MEDIUM, // Chest — recovers in 1.5-4 days
    LARGE   // Back, Legs — recover in 2-5 days
}

/**
 * Volume landmarks per muscle group for intermediate lifters.
 * MEV = Minimum Effective Volume, MAV = Maximum Adaptive Volume, MRV = Maximum Recoverable Volume.
 */
data class VolumeLandmarks(
    val category: MuscleSizeCategory,
    val mev: Int,
    val mavLow: Int,
    val mavHigh: Int,
    val mrv: Int
)

/**
 * Recovery day thresholds per muscle size category.
 * Each pair represents the upper bound (in days) for that status.
 */
internal data class RecoveryThresholds(
    val restEnd: Float,
    val recoveringEnd: Float,
    val readyEnd: Float,
    val trainEnd: Float
)

internal val RECOVERY_THRESHOLDS = mapOf(
    MuscleSizeCategory.SMALL to RecoveryThresholds(1f, 2f, 3f, 5f),
    MuscleSizeCategory.MEDIUM to RecoveryThresholds(1.5f, 2.5f, 4f, 6f),
    MuscleSizeCategory.LARGE to RecoveryThresholds(2f, 3f, 5f, 7f)
)

val VOLUME_LANDMARKS = mapOf(
    "Chest" to VolumeLandmarks(MuscleSizeCategory.MEDIUM, mev = 8, mavLow = 12, mavHigh = 16, mrv = 22),
    "Back" to VolumeLandmarks(MuscleSizeCategory.LARGE, mev = 8, mavLow = 12, mavHigh = 16, mrv = 22),
    "Legs" to VolumeLandmarks(MuscleSizeCategory.LARGE, mev = 6, mavLow = 12, mavHigh = 16, mrv = 20),
    "Shoulders" to VolumeLandmarks(MuscleSizeCategory.SMALL, mev = 8, mavLow = 14, mavHigh = 18, mrv = 24),
    "Arms" to VolumeLandmarks(MuscleSizeCategory.SMALL, mev = 6, mavLow = 10, mavHigh = 14, mrv = 20),
    "Core" to VolumeLandmarks(MuscleSizeCategory.SMALL, mev = 4, mavLow = 10, mavHigh = 14, mrv = 20)
)

internal val DEFAULT_LANDMARKS = VolumeLandmarks(MuscleSizeCategory.MEDIUM, mev = 6, mavLow = 10, mavHigh = 14, mrv = 20)

/**
 * Determine recovery status using both days since last trained AND weekly set volume.
 *
 * Logic based on Renaissance Periodization volume landmarks and
 * Schoenfeld et al. (2017) dose-response research.
 *
 * - Time-based: muscle size determines how many days each status lasts
 * - Volume override: exceeding MRV forces REST; exceeding MAV extends recovery by 1.3x
 * - Under-volume promotion: if weekly sets < MEV and time says READY, promote to TRAIN
 */
fun calculateRecoveryStatus(
    muscleGroup: String,
    daysSinceLastTrained: Int?,
    weeklySetCount: Int = 0
): RecoveryStatus {
    if (daysSinceLastTrained == null) return RecoveryStatus.NEW

    val landmarks = VOLUME_LANDMARKS[muscleGroup] ?: DEFAULT_LANDMARKS
    val baseThresholds = RECOVERY_THRESHOLDS[landmarks.category]
        ?: RECOVERY_THRESHOLDS[MuscleSizeCategory.MEDIUM]!!

    // Volume-based override: exceeded maximum recoverable volume → force rest
    if (weeklySetCount > landmarks.mrv) return RecoveryStatus.REST

    // If volume is above MAV, extend recovery thresholds by 1.3x (needs more rest)
    val multiplier = if (weeklySetCount > landmarks.mavHigh) 1.3f else 1f
    val restEnd = baseThresholds.restEnd * multiplier
    val recoveringEnd = baseThresholds.recoveringEnd * multiplier
    val readyEnd = baseThresholds.readyEnd * multiplier

    val days = daysSinceLastTrained.toFloat()

    return when {
        days < restEnd -> RecoveryStatus.REST
        days < recoveringEnd -> RecoveryStatus.RECOVERING
        days < readyEnd -> {
            // Under MEV and recovered enough → promote to TRAIN
            if (weeklySetCount < landmarks.mev) RecoveryStatus.TRAIN
            else RecoveryStatus.READY
        }
        else -> RecoveryStatus.TRAIN
    }
}

// Backward-compatible overload
fun calculateRecoveryStatus(
    daysSinceLastTrained: Int?,
    timeRange: RecoveryTimeRange = RecoveryTimeRange.WEEKLY
): RecoveryStatus = calculateRecoveryStatus("Chest", daysSinceLastTrained, 0)

/**
 * Calculate recovery bar progress (0f..1f) based on days and muscle size.
 * Maps linearly from REST (0) through to TRAIN (1).
 */
fun calculateRecoveryProgress(
    muscleGroup: String,
    daysSinceLastTrained: Int?,
    weeklySetCount: Int = 0
): Float {
    if (daysSinceLastTrained == null) return 1f

    val landmarks = VOLUME_LANDMARKS[muscleGroup] ?: DEFAULT_LANDMARKS
    val baseThresholds = RECOVERY_THRESHOLDS[landmarks.category]
        ?: RECOVERY_THRESHOLDS[MuscleSizeCategory.MEDIUM]!!

    // Volume exceeding MRV → minimal progress (still needs rest)
    if (weeklySetCount > landmarks.mrv) return 0.1f

    val multiplier = if (weeklySetCount > landmarks.mavHigh) 1.3f else 1f
    val trainEnd = baseThresholds.trainEnd * multiplier
    val days = daysSinceLastTrained.toFloat()

    return (days / trainEnd).coerceIn(0f, 1f)
}

// Backward-compatible overload
fun calculateRecoveryProgress(
    daysSinceLastTrained: Int?,
    timeRange: RecoveryTimeRange = RecoveryTimeRange.WEEKLY
): Float = calculateRecoveryProgress("Chest", daysSinceLastTrained, 0)

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
