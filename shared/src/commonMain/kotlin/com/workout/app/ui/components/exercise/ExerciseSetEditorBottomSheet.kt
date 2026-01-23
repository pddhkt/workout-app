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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
    onHistoryClick: () -> Unit,
    onDeleteSet: () -> Unit,
    onCompleteSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
    ) {
        // Drag Handle (Already in BottomSheet, but visual space here)

        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
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

            FilledTonalButton(
                onClick = onHistoryClick,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("History", style = MaterialTheme.typography.labelLarge)
            }
        }

        // Input Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            // Weight Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppTheme.spacing.md))
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Using SurfaceVariant for input background
                    .padding(AppTheme.spacing.md)
            ) {
                DecimalNumberStepper(
                    value = currentWeight,
                    onValueChange = onWeightChange,
                    label = "WEIGHT (KG)",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Reps Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppTheme.spacing.md))
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Using SurfaceVariant for input background
                    .padding(AppTheme.spacing.md)
            ) {
                NumberStepper(
                    value = currentReps,
                    onValueChange = onRepsChange,
                    label = "REPS",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // RPE & Timer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            // RPE Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppTheme.spacing.md))
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Using SurfaceVariant for input background
                    .padding(AppTheme.spacing.md)
            ) {
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
                    .clip(RoundedCornerShape(AppTheme.spacing.md))
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Using SurfaceVariant for input background
                    .clickable { /* Open timer picker */ }
                    .padding(AppTheme.spacing.md)
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
            label = null, // Hidden label for cleaner look
            placeholder = "Add notes for this set...",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

        // Footer Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            // Delete Button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(AppTheme.spacing.md))
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Using SurfaceVariant for button background
                    .clickable(onClick = onDeleteSet),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Set",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Complete Button
            Button(
                onClick = onCompleteSet,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(AppTheme.spacing.md),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.success,
                    contentColor = Color.Black
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
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}
