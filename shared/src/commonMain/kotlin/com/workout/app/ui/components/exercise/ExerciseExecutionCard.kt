package com.workout.app.ui.components.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.components.chips.SetState
import com.workout.app.ui.theme.AppTheme

/**
 * Exercise Execution Card for the active workout screen.
 * Replaces the standard ExerciseCard for the currently active exercise.
 * Features a carousel of large set cards.
 *
 * @param exerciseName Name of the exercise
 * @param targetSummary Summary string (e.g. "Target: 4 Sets - 10-12 Reps")
 * @param sets List of sets for this exercise
 * @param activeSetIndex Index of the currently active set
 * @param onSetClick Callback when a set card is clicked
 * @param onOptionsClick Callback for the options menu
 */
@Composable
fun ExerciseExecutionCard(
    exerciseName: String,
    targetSummary: String,
    sets: List<SetInfo>,
    activeSetIndex: Int,
    onSetClick: (Int) -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 0.dp // Custom padding for this card
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.lg)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                    Text(
                        text = targetSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = onOptionsClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Sets Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = AppTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppTheme.spacing.lg)
        ) {
            items(sets) { set ->
                val isActive = set.setNumber - 1 == activeSetIndex
                // Assuming target is static for now, or could be passed in SetInfo if variable.
                val targetText = "Target: 180kg x 12" 

                if (isActive) {
                    ActiveSetCard(
                        setNumber = set.setNumber,
                        weight = set.weight,
                        reps = set.reps,
                        targetText = targetText,
                        onClick = { onSetClick(set.setNumber - 1) }
                    )
                } else {
                    PendingSetCard(
                        setNumber = set.setNumber,
                        weight = set.weight,
                        reps = set.reps,
                        targetText = targetText,
                        isCompleted = set.state == SetState.COMPLETED,
                        onClick = { onSetClick(set.setNumber - 1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveSetCard(
    setNumber: Int,
    weight: Float,
    reps: Int,
    targetText: String,
    onClick: () -> Unit
) {
    // Active Card using primary theme color
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier
            .width(160.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(primaryColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row: Set Label + Active Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SET $setNumber",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = onPrimaryColor
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(onPrimaryColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = onPrimaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Value
            // "200 kg x 12"
            val displayWeight = if (weight > 0) "${weight.toInt()}" else "--"
            val displayReps = if (reps > 0) "$reps" else "--"

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) {
                        append(displayWeight)
                    }
                    withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)) {
                        append("kg")
                    }
                    withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) {
                        append(" x ")
                        append(displayReps)
                    }
                },
                color = onPrimaryColor
            )

            Spacer(modifier = Modifier.weight(1f))

            // Target
            Text(
                text = targetText,
                style = MaterialTheme.typography.bodySmall,
                color = onPrimaryColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PendingSetCard(
    setNumber: Int,
    weight: Float,
    reps: Int,
    targetText: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    // Dark/Surface Card using MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .width(140.dp) // Slightly smaller width for non-active
            .height(140.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant) // Using theme surface variant
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "SET $setNumber",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant // Ensuring readable text on surface variant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val displayWeight = if (weight > 0) "${weight.toInt()}" else "--"
            val displayReps = if (reps > 0) "$reps" else "--"
            
            Text(
                text = if (isCompleted) "$displayWeight x $displayReps" else "--",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = targetText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
