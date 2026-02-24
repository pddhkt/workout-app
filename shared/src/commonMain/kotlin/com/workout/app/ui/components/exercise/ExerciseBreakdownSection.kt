package com.workout.app.ui.components.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.ExerciseWithSets
import com.workout.app.domain.model.RecordingField
import com.workout.app.domain.model.SetData
import com.workout.app.ui.components.chips.Badge
import com.workout.app.ui.components.chips.BadgeVariant
import com.workout.app.ui.components.gps.GpsPathCanvas
import com.workout.app.ui.components.gps.parseGpsPath
import com.workout.app.ui.theme.AppTheme

@Composable
fun ExerciseBreakdownSection(
    exercises: List<ExerciseWithSets>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Exercises (${exercises.size})",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = AppTheme.spacing.sm)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            exercises.forEach { exercise ->
                ExerciseBreakdownCard(
                    exercise = exercise
                )
            }
        }
    }
}

@Composable
private fun ExerciseBreakdownCard(
    exercise: ExerciseWithSets,
    modifier: Modifier = Modifier
) {
    var showSets by remember { mutableStateOf(false) }

    val fields = remember(exercise.recordingFields) {
        RecordingField.fromJsonArray(exercise.recordingFields) ?: RecordingField.DEFAULT_FIELDS
    }
    val hasDistanceField = fields.any { it.key == "distance" }
    val isDefaultFields = fields.size == 2 && fields.any { it.key == "weight" } && fields.any { it.key == "reps" }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { showSets = !showSets }
            .padding(AppTheme.spacing.md)
    ) {
        // Exercise header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val bestSetText = if (isDefaultFields) {
                    exercise.bestSet?.let { " | Best: ${formatNumber(it.weight)}kg x ${it.reps}" } ?: ""
                } else {
                    exercise.bestSet?.let { best ->
                        val fv = best.fieldValues?.filterKeys { !it.startsWith("_") }
                        if (fv != null && fv.isNotEmpty()) {
                            " | Best: " + fields.mapNotNull { field ->
                                val v = fv[field.key] ?: return@mapNotNull null
                                if (v.isBlank()) return@mapNotNull null
                                val unit = if (field.unit.isNotEmpty()) field.unit else ""
                                "${formatNumber(v.toDoubleOrNull() ?: return@mapNotNull null)}$unit"
                            }.joinToString(" x ")
                        } else null
                    } ?: ""
                }

                Text(
                    text = "${exercise.sets.size} sets$bestSetText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (exercise.hasPR) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "PR",
                        modifier = Modifier.size(16.dp),
                        tint = AppTheme.colors.primaryText
                    )
                    Badge(
                        text = "PR",
                        variant = BadgeVariant.WARNING
                    )
                }
            }
        }

        // Expanded sets view
        AnimatedVisibility(
            visible = showSets,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = AppTheme.spacing.md)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                // Per-set GPS paths (vertical list)
                if (hasDistanceField) {
                    exercise.sets.forEach { set ->
                        val gpsPathStr = set.fieldValues?.get("_gpsPath")
                        if (gpsPathStr != null && gpsPathStr.isNotBlank()) {
                            val path = parseGpsPath(gpsPathStr)
                            val distance = set.fieldValues?.get("distance")?.toDoubleOrNull()
                            if (path.size >= 2) {
                                Text(
                                    text = "Set ${set.setNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                                GpsPathCanvas(
                                    points = path,
                                    distanceKm = distance,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                            }
                        }
                    }
                }

                if (isDefaultFields) {
                    // Default weight/reps table
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Set", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("Weight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("Reps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("RPE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

                    exercise.sets.forEach { set ->
                        DefaultSetRow(set = set)
                    }
                } else {
                    // Dynamic field columns
                    val displayFields = fields.filter { it.key != "duration" || fields.size == 1 }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Set", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        displayFields.forEach { field ->
                            Text(
                                text = field.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

                    exercise.sets.forEach { set ->
                        DynamicSetRow(set = set, fields = displayFields)
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultSetRow(
    set: SetData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) {
            Text(
                text = if (set.isWarmup) "W" else set.setNumber.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (set.isWarmup) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            if (set.isPR) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "PR",
                    modifier = Modifier.size(12.dp),
                    tint = AppTheme.colors.primaryText
                )
            }
        }
        Text(
            text = "${formatNumber(set.weight)} kg",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${set.reps}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = set.rpe?.toString() ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DynamicSetRow(
    set: SetData,
    fields: List<RecordingField>,
    modifier: Modifier = Modifier
) {
    val fv = set.fieldValues?.filterKeys { !it.startsWith("_") } ?: emptyMap()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) {
            Text(
                text = if (set.isWarmup) "W" else set.setNumber.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (set.isWarmup) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            if (set.isPR) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "PR",
                    modifier = Modifier.size(12.dp),
                    tint = AppTheme.colors.primaryText
                )
            }
        }
        fields.forEach { field ->
            val value = fv[field.key]
            val displayValue = if (value != null && value.isNotBlank()) {
                val unit = if (field.unit.isNotEmpty()) " ${field.unit}" else ""
                if (field.type == "duration") {
                    val secs = value.toIntOrNull() ?: 0
                    val mins = secs / 60
                    val remainSecs = secs % 60
                    "%d:%02d".format(mins, remainSecs)
                } else {
                    "${formatNumber(value.toDoubleOrNull() ?: 0.0)}$unit"
                }
            } else {
                "-"
            }
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun formatNumber(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}

private fun formatNumber(value: String): String {
    val d = value.toDoubleOrNull() ?: return value
    return formatNumber(d)
}
