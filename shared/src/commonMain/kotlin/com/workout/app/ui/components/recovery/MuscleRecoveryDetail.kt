package com.workout.app.ui.components.recovery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.DEFAULT_LANDMARKS
import com.workout.app.domain.model.MuscleRecovery
import com.workout.app.domain.model.MuscleSizeCategory
import com.workout.app.domain.model.RECOVERY_THRESHOLDS
import com.workout.app.domain.model.RecoveryStatus
import com.workout.app.domain.model.VOLUME_LANDMARKS
import com.workout.app.domain.model.VolumeLandmarks
import com.workout.app.ui.theme.AppTheme
import kotlin.math.max

@Composable
fun MuscleRecoveryDetailView(
    recovery: MuscleRecovery,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val landmarks = VOLUME_LANDMARKS[recovery.muscleGroup] ?: DEFAULT_LANDMARKS

    Column(modifier = modifier) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .clickable(onClick = onBackClick)
                    .padding(2.dp)
            )
            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
            Text(
                text = recovery.muscleGroup,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        RecoverySection(recovery, landmarks)

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        WeeklyVolumeSection(recovery, landmarks)

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        RecommendationSection(recovery, landmarks)
    }
}

@Composable
private fun RecoverySection(recovery: MuscleRecovery, landmarks: VolumeLandmarks) {
    val statusColor = statusColor(recovery.status)

    Text(
        text = "Recovery",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
    )

    DetailInfoRow(
        label = "Last trained",
        value = when {
            recovery.daysSinceLastTrained == null -> "Never"
            recovery.daysSinceLastTrained > 0 -> "${recovery.daysSinceLastTrained} day(s) ago"
            recovery.hoursSinceLastTrained != null -> "${recovery.hoursSinceLastTrained} hour(s) ago"
            else -> "Today"
        }
    )
    DetailInfoRow(
        label = "Status",
        value = recovery.status.label,
        valueColor = statusColor
    )

    // Show "Ready in" for muscles that aren't ready yet
    val readyInLabel = estimateReadyIn(recovery, landmarks)
    if (readyInLabel != null) {
        DetailInfoRow(
            label = "Ready in",
            value = readyInLabel
        )
    }

    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

    // Full-width recovery bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = recovery.progress)
                .height(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(statusColor)
        )
    }

    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

    // Status markers below bar
    RecoveryStatusMarkers(recovery.status)
}

@Composable
private fun RecoveryStatusMarkers(currentStatus: RecoveryStatus) {
    val statuses = listOf(
        RecoveryStatus.REST,
        RecoveryStatus.RECOVERING,
        RecoveryStatus.READY,
        RecoveryStatus.TRAIN
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        statuses.forEach { status ->
            val isCurrent = status == currentStatus
            val color = statusColor(status)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isCurrent) {
                    // Triangle indicator
                    Canvas(modifier = Modifier.size(6.dp)) {
                        val path = Path().apply {
                            moveTo(size.width / 2, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path, color)
                    }
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun WeeklyVolumeSection(
    recovery: MuscleRecovery,
    landmarks: VolumeLandmarks
) {
    Text(
        text = "Weekly Volume",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
    )

    DetailInfoRow("Sets this week", "${recovery.weeklySetCount}")
    DetailInfoRow("Sweet spot", "${landmarks.mavLow}–${landmarks.mavHigh} sets")
    DetailInfoRow("Max recoverable", "${landmarks.mrv} sets")

    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

    VolumeGauge(
        currentSets = recovery.weeklySetCount,
        landmarks = landmarks,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    )

    // Delta text
    val delta = volumeDeltaText(recovery.weeklySetCount, landmarks)
    if (delta != null) {
        Text(
            text = delta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppTheme.spacing.xs)
        )
    }
}

@Composable
private fun VolumeGauge(
    currentSets: Int,
    landmarks: VolumeLandmarks,
    modifier: Modifier = Modifier
) {
    val barTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val mevColor = AppTheme.colors.warning
    val mavColor = AppTheme.colors.success
    val mrvColor = AppTheme.colors.error
    val pointerColor = MaterialTheme.colorScheme.onSurface
    val tickLabelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val barHeight = 10.dp.toPx()
        val barY = size.height / 2 - barHeight / 2
        val maxVal = (landmarks.mrv + 4).toFloat()
        val barLeft = 0f
        val barRight = size.width

        fun xFor(value: Int): Float =
            barLeft + (value.toFloat() / maxVal) * (barRight - barLeft)

        // Background track
        drawRoundRect(
            color = barTrackColor,
            topLeft = Offset(barLeft, barY),
            size = androidx.compose.ui.geometry.Size(barRight - barLeft, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )

        // MEV to MAV low segment (yellow)
        val mevX = xFor(landmarks.mev)
        val mavLowX = xFor(landmarks.mavLow)
        drawRect(
            color = mevColor.copy(alpha = 0.3f),
            topLeft = Offset(mevX, barY),
            size = androidx.compose.ui.geometry.Size(mavLowX - mevX, barHeight)
        )

        // MAV low to MAV high segment (green)
        val mavHighX = xFor(landmarks.mavHigh)
        drawRect(
            color = mavColor.copy(alpha = 0.4f),
            topLeft = Offset(mavLowX, barY),
            size = androidx.compose.ui.geometry.Size(mavHighX - mavLowX, barHeight)
        )

        // MAV high to MRV segment (red)
        val mrvX = xFor(landmarks.mrv)
        drawRect(
            color = mrvColor.copy(alpha = 0.3f),
            topLeft = Offset(mavHighX, barY),
            size = androidx.compose.ui.geometry.Size(mrvX - mavHighX, barHeight)
        )

        // Tick marks at MEV, MAV low, MAV high, MRV
        val tickHeight = 4.dp.toPx()
        val tickWidth = 1.dp.toPx()
        listOf(
            landmarks.mev to "MEV",
            landmarks.mavLow to "MAV",
            landmarks.mrv to "MRV"
        ).forEach { (value, _) ->
            val x = xFor(value)
            drawRect(
                color = tickLabelColor.copy(alpha = 0.6f),
                topLeft = Offset(x - tickWidth / 2, barY - tickHeight),
                size = androidx.compose.ui.geometry.Size(tickWidth, tickHeight)
            )
            drawRect(
                color = tickLabelColor.copy(alpha = 0.6f),
                topLeft = Offset(x - tickWidth / 2, barY + barHeight),
                size = androidx.compose.ui.geometry.Size(tickWidth, tickHeight)
            )
        }

        // Current position triangle pointer (below bar)
        val currentX = xFor(currentSets.coerceAtMost(landmarks.mrv + 4))
        val triSize = 6.dp.toPx()
        val triTop = barY - tickHeight - triSize - 1.dp.toPx()
        val path = Path().apply {
            moveTo(currentX, triTop + triSize)
            lineTo(currentX - triSize / 2, triTop)
            lineTo(currentX + triSize / 2, triTop)
            close()
        }
        drawPath(path, pointerColor)
    }
}

@Composable
private fun RecommendationSection(
    recovery: MuscleRecovery,
    landmarks: VolumeLandmarks
) {
    val text = generateRecoveryRecommendation(recovery, landmarks)

    Text(
        text = "Recommendation",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(AppTheme.spacing.md)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun statusColor(status: RecoveryStatus): Color = when (status) {
    RecoveryStatus.REST -> AppTheme.colors.error
    RecoveryStatus.RECOVERING -> AppTheme.colors.warning
    RecoveryStatus.READY -> AppTheme.colors.success
    RecoveryStatus.TRAIN -> AppTheme.colors.info
    RecoveryStatus.NEW -> AppTheme.colors.info
}

/**
 * Estimate remaining time until READY status. Returns null if already ready/train/new.
 * Shows hours when remaining < 1 day.
 */
private fun estimateReadyIn(recovery: MuscleRecovery, landmarks: VolumeLandmarks): String? {
    if (recovery.status !in listOf(RecoveryStatus.REST, RecoveryStatus.RECOVERING)) return null

    val thresholds = RECOVERY_THRESHOLDS[landmarks.category]
        ?: RECOVERY_THRESHOLDS[MuscleSizeCategory.MEDIUM] ?: return null
    val multiplier = if (recovery.weeklySetCount > landmarks.mavHigh) 1.3f else 1f
    val readyEnd = thresholds.recoveringEnd * multiplier

    // Use hours for more precision
    val hoursSince = recovery.hoursSinceLastTrained?.toFloat() ?: 0f
    val readyAtHours = readyEnd * 24f
    val remainingHours = (readyAtHours - hoursSince).coerceAtLeast(0f)
    val remainingDays = (remainingHours / 24f).toInt()
    val leftoverHours = (remainingHours % 24f).toInt()

    return when {
        remainingDays > 0 && leftoverHours > 0 -> "~${remainingDays}d ${leftoverHours}h"
        remainingDays > 0 -> "~${remainingDays}d"
        remainingHours > 0 -> "~${remainingHours.toInt()}h"
        else -> "Soon"
    }
}

private fun volumeDeltaText(currentSets: Int, landmarks: VolumeLandmarks): String? {
    return when {
        currentSets < landmarks.mev -> {
            val lo = landmarks.mev - currentSets
            val hi = landmarks.mavLow - currentSets
            "+ $lo to $hi more sets to reach target"
        }
        currentSets < landmarks.mavLow -> {
            val more = landmarks.mavLow - currentSets
            "+ $more more sets to sweet spot"
        }
        currentSets in landmarks.mavLow..landmarks.mavHigh -> {
            "In sweet spot range"
        }
        currentSets in (landmarks.mavHigh + 1)..landmarks.mrv -> {
            val over = currentSets - landmarks.mavHigh
            "$over sets above sweet spot"
        }
        else -> {
            val over = currentSets - landmarks.mrv
            "$over sets over max recoverable"
        }
    }
}
