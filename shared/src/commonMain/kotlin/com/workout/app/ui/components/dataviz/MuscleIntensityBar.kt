package com.workout.app.ui.components.dataviz

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.IntensityLevel
import com.workout.app.domain.model.MuscleGroupIntensity
import com.workout.app.ui.theme.AppTheme

/**
 * Displays muscle group intensity with a progress bar visualization.
 *
 * @param muscleIntensity The muscle group intensity data
 * @param modifier Optional modifier
 */
@Composable
fun MuscleIntensityBar(
    muscleIntensity: MuscleGroupIntensity,
    modifier: Modifier = Modifier
) {
    val progress = when (muscleIntensity.intensity) {
        IntensityLevel.LOW -> 0.25f
        IntensityLevel.MEDIUM -> 0.5f
        IntensityLevel.HIGH -> 0.75f
        IntensityLevel.VERY_HIGH -> 1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    val intensityColor = when (muscleIntensity.intensity) {
        IntensityLevel.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        IntensityLevel.MEDIUM -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        IntensityLevel.HIGH -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        IntensityLevel.VERY_HIGH -> MaterialTheme.colorScheme.primary
    }

    val intensityLabel = when (muscleIntensity.intensity) {
        IntensityLevel.LOW -> "Low"
        IntensityLevel.MEDIUM -> "Medium"
        IntensityLevel.HIGH -> "High"
        IntensityLevel.VERY_HIGH -> "Very High"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Muscle group name
        Text(
            text = muscleIntensity.muscleGroup,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.3f)
        )

        // Progress bar
        Box(
            modifier = Modifier
                .weight(0.5f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(intensityColor)
            )
        }

        // Intensity label
        Text(
            text = intensityLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(0.2f)
                .padding(start = AppTheme.spacing.sm)
        )
    }
}

/**
 * Section showing all muscles targeted with intensity bars.
 * Content is wrapped in a Card with surface background.
 *
 * @param muscleGroups List of muscle group intensities
 * @param modifier Optional modifier
 */
@Composable
fun MusclesTargetedSection(
    muscleGroups: List<MuscleGroupIntensity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        Text(
            text = "Muscles Targeted",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AppTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                muscleGroups.forEach { muscleIntensity ->
                    MuscleIntensityBar(
                        muscleIntensity = muscleIntensity
                    )
                }
            }
        }
    }
}

/**
 * Metrics grid showing duration, volume, sets, and PRs.
 *
 * @param duration Duration in seconds
 * @param volume Total volume in kg
 * @param sets Total sets
 * @param prCount Number of personal records
 * @param modifier Optional modifier
 */
@Composable
fun MetricsGrid(
    duration: Long,
    volume: Long,
    sets: Long,
    prCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        MetricCard(
            value = formatDuration(duration),
            label = "Duration",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            value = formatVolume(volume),
            label = "Volume",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            value = sets.toString(),
            label = "Sets",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            value = prCount.toString(),
            label = if (prCount == 1) "New PR" else "New PRs",
            showTrophy = prCount > 0,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    value: String,
    label: String,
    showTrophy: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(AppTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (showTrophy) AppTheme.colors.warning else MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60

    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}

private fun formatVolume(volume: Long): String {
    return if (volume >= 1000) {
        "${volume / 1000},${(volume % 1000 / 100)}k"
    } else {
        "$volume"
    }
}
