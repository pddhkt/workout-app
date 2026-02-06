package com.workout.app.ui.components.exercise

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.inputs.CompactRPESelector
import com.workout.app.ui.components.inputs.DecimalNumberStepper
import com.workout.app.ui.components.inputs.NotesInput
import com.workout.app.ui.components.inputs.NumberStepper
import com.workout.app.ui.theme.AppTheme

@Composable
fun ExerciseSetEditorBottomSheet(
    exerciseName: String,
    setNumber: Int,
    previousPerformance: String?,
    currentWeight: Float,
    currentReps: Int,
    currentRpe: Int?,
    restTimerSeconds: Int,
    notes: String,
    onWeightChange: (Float) -> Unit,
    onRepsChange: (Int) -> Unit,
    onRpeChange: (Int) -> Unit,
    onRestTimerChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onDeleteSet: () -> Unit,
    onCompleteSet: () -> Unit,
    modifier: Modifier = Modifier,
    weightHistoryValues: List<String> = emptyList(),
    repsHistoryValues: List<String> = emptyList(),
    previousSetNumber: Int? = null,
    previousSetWeight: Float? = null,
    onApplyPreviousWeight: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = AppTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        // Header Section
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppTheme.colors.success.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SET $setNumber",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppTheme.colors.success
                    )
                }
                Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                Text(
                    text = previousPerformance ?: "First time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Previous Set Weight Quick-Apply (only shown for set 2+)
        if (previousSetNumber != null && previousSetWeight != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Set $previousSetNumber:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppTheme.colors.success.copy(alpha = 0.15f))
                        .clickable(onClick = onApplyPreviousWeight)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${formatWeight(previousSetWeight)} kg",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppTheme.colors.success
                    )
                }
            }
        }

        // Input Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            // Weight Input
            Box(modifier = Modifier.weight(1f)) {
                DecimalNumberStepper(
                    value = currentWeight,
                    onValueChange = onWeightChange,
                    label = "WEIGHT (KG)",
                    historyValues = weightHistoryValues,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Reps Input
            Box(modifier = Modifier.weight(1f)) {
                NumberStepper(
                    value = currentReps,
                    onValueChange = onRepsChange,
                    label = "REPS",
                    historyValues = repsHistoryValues,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // RPE & Timer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            // RPE Input
            Box(modifier = Modifier.weight(1f)) {
                CompactRPESelector(
                    selectedRPE = currentRpe,
                    onRPESelected = onRpeChange,
                    label = "RPE",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Rest Timer Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { /* Open timer picker */ }
                    .padding(AppTheme.spacing.sm)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "REST TIMER",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = AppTheme.colors.success,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatTime(restTimerSeconds),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AppTheme.colors.success,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Notes Input
        NotesInput(
            value = notes,
            onValueChange = onNotesChange,
            label = null,
            placeholder = "Add notes for this set...",
            modifier = Modifier.fillMaxWidth()
        )

        // Complete Button
        Button(
            onClick = onCompleteSet,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(2.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mark Complete",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}

private fun formatWeight(weight: Float): String {
    return if (weight == weight.toLong().toFloat()) {
        weight.toLong().toString()
    } else {
        "%.1f".format(weight)
    }
}
