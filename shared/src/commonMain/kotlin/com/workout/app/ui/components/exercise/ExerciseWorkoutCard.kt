package com.workout.app.ui.components.exercise

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Unified Exercise Workout Card with adaptive layout.
 *
 * - Active exercises: Show carousel of large set cards
 * - Non-active exercises: Show compact set chips
 *
 * Provides consistent styling across all exercise states.
 *
 * @param exerciseName Name of the exercise
 * @param muscleGroup Muscle group targeted
 * @param targetSummary Target summary (e.g., "4 Sets - 8-12 Reps")
 * @param sets List of sets for this exercise
 * @param isActive Whether this is the currently active exercise
 * @param activeSetIndex Index of the currently active set (0-based)
 * @param onSetClick Callback when a set is clicked, receives set index
 * @param onOptionsClick Optional callback for options menu
 * @param onAddSet Optional callback for adding a new set
 * @param onLongPressTitle Optional callback when exercise title is long-pressed (for reorder mode)
 * @param modifier Modifier for customization
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseWorkoutCard(
    exerciseName: String,
    muscleGroup: String,
    targetSummary: String,
    sets: List<SetInfo>,
    isActive: Boolean,
    activeSetIndex: Int = 0,
    onSetClick: (Int) -> Unit,
    onOptionsClick: (() -> Unit)? = null,
    onAddSet: (() -> Unit)? = null,
    onLongPressTitle: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCompleted = sets.all { it.state == SetState.COMPLETED }

    BaseCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 0.dp
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .let { mod ->
                            if (onLongPressTitle != null) {
                                mod.combinedClickable(
                                    onClick = { },
                                    onLongClick = onLongPressTitle
                                )
                            } else {
                                mod
                            }
                        }
                ) {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                    Text(
                        text = if (isActive) "Target: $targetSummary" else muscleGroup,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = AppTheme.colors.success,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (onOptionsClick != null) {
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
        }

        // Sets Carousel - same layout for all exercises
        LazyRow(
            contentPadding = PaddingValues(horizontal = AppTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppTheme.spacing.lg)
        ) {
            items(sets) { set ->
                val setIndex = set.setNumber - 1
                val isSetActive = setIndex == activeSetIndex && set.state != SetState.COMPLETED
                val targetText = "Target: ${sets.firstOrNull()?.weight?.toInt() ?: 0}kg x 12"

                WorkoutSetCard(
                    setNumber = set.setNumber,
                    weight = set.weight,
                    reps = set.reps,
                    state = when {
                        set.state == SetState.COMPLETED -> WorkoutSetState.COMPLETED
                        isSetActive -> WorkoutSetState.ACTIVE
                        else -> WorkoutSetState.PENDING
                    },
                    targetText = targetText,
                    onClick = { onSetClick(setIndex) }
                )
            }

            if (onAddSet != null) {
                item {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onAddSet),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Set",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * State for workout set cards
 */
enum class WorkoutSetState {
    ACTIVE,
    COMPLETED,
    PENDING
}

/**
 * Reusable set card for the workout carousel.
 * Displays set number, weight/reps, and target with state-based styling.
 */
@Composable
fun WorkoutSetCard(
    setNumber: Int,
    weight: Float,
    reps: Int,
    state: WorkoutSetState,
    targetText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (state) {
        WorkoutSetState.ACTIVE -> MaterialTheme.colorScheme.primary
        WorkoutSetState.COMPLETED -> AppTheme.colors.success
        WorkoutSetState.PENDING -> MaterialTheme.colorScheme.background
    }

    val contentColor = when (state) {
        WorkoutSetState.ACTIVE -> MaterialTheme.colorScheme.onPrimary
        WorkoutSetState.COMPLETED -> MaterialTheme.colorScheme.onPrimary
        WorkoutSetState.PENDING -> MaterialTheme.colorScheme.onBackground
    }

    val cardWidth = if (state == WorkoutSetState.ACTIVE) 160.dp else 140.dp

    Box(
        modifier = modifier
            .width(cardWidth)
            .height(140.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row: Set Label + Badge
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
                    color = contentColor
                )

                when (state) {
                    WorkoutSetState.ACTIVE -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(contentColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = contentColor
                            )
                        }
                    }
                    WorkoutSetState.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    WorkoutSetState.PENDING -> { /* No badge */ }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Value
            val displayWeight = if (weight > 0) "${weight.toInt()}" else "--"
            val displayReps = if (reps > 0) "$reps" else "--"
            val showValue = state == WorkoutSetState.COMPLETED || state == WorkoutSetState.ACTIVE

            if (state == WorkoutSetState.ACTIVE) {
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
                    color = contentColor
                )
            } else {
                Text(
                    text = if (showValue && weight > 0) "$displayWeight x $displayReps" else "--",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = contentColor
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Target
            Text(
                text = targetText,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}
