package com.workout.app.ui.components.recovery

import com.workout.app.domain.model.DEFAULT_LANDMARKS
import com.workout.app.domain.model.MuscleRecovery
import com.workout.app.domain.model.RECOVERY_THRESHOLDS
import com.workout.app.domain.model.RecoveryStatus
import com.workout.app.domain.model.VOLUME_LANDMARKS
import com.workout.app.domain.model.VolumeLandmarks
import kotlin.math.ceil
import kotlin.math.max

/**
 * Generate a human-readable recommendation based on recovery status and weekly volume.
 */
fun generateRecoveryRecommendation(
    recovery: MuscleRecovery,
    landmarks: VolumeLandmarks
): String {
    val sets = recovery.weeklySetCount
    val days = recovery.daysSinceLastTrained

    return when (recovery.status) {
        RecoveryStatus.REST -> {
            val restDays = estimateRestDays(recovery, landmarks)
            if (sets > landmarks.mavHigh) {
                "Near max volume. Rest this muscle for $restDays more day(s)."
            } else {
                "Wait $restDays more day(s) before training."
            }
        }
        RecoveryStatus.RECOVERING -> {
            val recDays = estimateRecoveringDays(recovery, landmarks)
            "Almost recovered. Wait $recDays more day(s)."
        }
        RecoveryStatus.READY -> {
            if (sets < landmarks.mev) {
                val lo = max(1, landmarks.mev - sets)
                val hi = max(lo, landmarks.mavLow - sets)
                "Recovered and under target. Add $lo–$hi sets."
            } else {
                val room = max(0, landmarks.mrv - sets)
                if (room > 0) "On track. Can add $room more sets if needed."
                else "On track. Volume near limit — maintain current load."
            }
        }
        RecoveryStatus.TRAIN -> {
            val lo = max(1, landmarks.mev - sets)
            val hi = max(lo, landmarks.mavLow - sets)
            "Fully recovered. Add $lo–$hi sets to maintain progress."
        }
        RecoveryStatus.NEW -> {
            "No history. Start with ${landmarks.mev}–${landmarks.mavLow} sets and assess."
        }
    }
}

/**
 * Estimate how many more days of rest are needed from current position.
 */
private fun estimateRestDays(
    recovery: MuscleRecovery,
    landmarks: VolumeLandmarks
): Int {
    val thresholds = RECOVERY_THRESHOLDS[landmarks.category] ?: return 1
    val multiplier = if (recovery.weeklySetCount > landmarks.mavHigh) 1.3f else 1f
    val restEnd = thresholds.restEnd * multiplier
    val daysSince = recovery.daysSinceLastTrained?.toFloat() ?: 0f
    return max(1, ceil(restEnd - daysSince).toInt())
}

/**
 * Estimate how many more days until fully recovered (past RECOVERING phase).
 */
private fun estimateRecoveringDays(
    recovery: MuscleRecovery,
    landmarks: VolumeLandmarks
): Int {
    val thresholds = RECOVERY_THRESHOLDS[landmarks.category] ?: return 1
    val multiplier = if (recovery.weeklySetCount > landmarks.mavHigh) 1.3f else 1f
    val recoveringEnd = thresholds.recoveringEnd * multiplier
    val daysSince = recovery.daysSinceLastTrained?.toFloat() ?: 0f
    return max(1, ceil(recoveringEnd - daysSince).toInt())
}
