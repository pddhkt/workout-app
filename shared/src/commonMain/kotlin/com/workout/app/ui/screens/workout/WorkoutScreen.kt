package com.workout.app.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.components.chips.SetState
import com.workout.app.ui.components.dataviz.CompactCircularTimer
import com.workout.app.ui.components.exercise.ExerciseCard
import com.workout.app.ui.components.exercise.ExerciseCardState
import com.workout.app.ui.components.exercise.SetInfo
import com.workout.app.ui.components.inputs.CompactRPESelector
import com.workout.app.ui.components.inputs.NotesInput
import com.workout.app.ui.components.overlays.BottomSheet
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.SurfaceVariant
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

/**
 * Data class representing a workout session
 */
data class WorkoutSession(
    val workoutName: String,
    val exercises: List<ExerciseData>,
    val startTime: Long = Clock.System.now().toEpochMilliseconds()
)

/**
 * Data class representing an exercise in a workout
 */
data class ExerciseData(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val targetSets: Int,
    val completedSets: Int = 0,
    val sets: List<SetInfo> = List(targetSets) { index ->
        SetInfo(
            setNumber = index + 1,
            reps = 0,
            weight = 0f,
            state = SetState.PENDING
        )
    }
)

/**
 * Workout Screen - Active workout session with exercise tracking
 *
 * Features:
 * - Session header with elapsed time and exercise count (EL-02)
 * - Exercise list with current exercise expanded (EL-09/10/79)
 * - Set chips showing progress (EL-17/18/19)
 * - Set input form with weight/reps steppers (EL-11)
 * - RPE selector after completing sets (EL-13)
 * - Rest timer inline trigger (EL-20)
 * - Notes input field (EL-12)
 * - Complete set and skip set buttons (EL-14)
 * - Bottom drawer for additional options (EL-23)
 *
 * @param session The workout session data
 * @param onCompleteSet Callback when user completes a set
 * @param onSkipSet Callback when user skips a set
 * @param onExerciseExpand Callback when an exercise card is expanded
 * @param onEndWorkout Callback when user ends the workout
 * @param modifier Optional modifier for customization
 */
@Composable
fun WorkoutScreen(
    session: WorkoutSession,
    onCompleteSet: (exerciseId: String, reps: Int, weight: Float, rpe: Int?) -> Unit = { _, _, _, _ -> },
    onSkipSet: (exerciseId: String) -> Unit = {},
    onExerciseExpand: (exerciseId: String) -> Unit = {},
    onEndWorkout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State management
    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var currentReps by remember { mutableIntStateOf(0) }
    var currentWeight by remember { mutableFloatStateOf(0f) }
    var currentRPE by remember { mutableStateOf<Int?>(null) }
    var notes by remember { mutableStateOf("") }
    var showRPESelector by remember { mutableStateOf(false) }
    var showRestTimer by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var restTimeRemaining by remember { mutableIntStateOf(90) }
    var elapsedTime by remember { mutableIntStateOf(0) }
    var expandedExerciseId by remember { mutableStateOf(session.exercises.firstOrNull()?.id) }

    // Timer for elapsed time
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedTime = ((Clock.System.now().toEpochMilliseconds() - session.startTime) / 1000).toInt()
        }
    }

    // Rest timer countdown
    LaunchedEffect(showRestTimer) {
        if (showRestTimer) {
            while (restTimeRemaining > 0) {
                delay(1000)
                restTimeRemaining--
            }
            if (restTimeRemaining == 0) {
                showRestTimer = false
                restTimeRemaining = 90
            }
        }
    }

    val currentExercise = session.exercises.getOrNull(currentExerciseIndex)
    val totalSetsCompleted = session.exercises.sumOf { it.completedSets }
    val totalSetsTarget = session.exercises.sumOf { it.targetSets }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Session Header
            SessionHeader(
                workoutName = session.workoutName,
                elapsedTime = elapsedTime,
                completedExercises = session.exercises.count { it.completedSets == it.targetSets },
                totalExercises = session.exercises.size,
                onMoreClick = { showBottomSheet = true }
            )

            // Exercise List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                itemsIndexed(session.exercises) { index, exercise ->
                    val cardState = when {
                        exercise.completedSets == exercise.targetSets -> ExerciseCardState.COMPLETED
                        index == currentExerciseIndex -> ExerciseCardState.ACTIVE
                        else -> ExerciseCardState.PENDING
                    }

                    ExerciseCard(
                        exerciseName = exercise.name,
                        muscleGroup = exercise.muscleGroup,
                        sets = exercise.sets,
                        state = cardState,
                        isExpanded = expandedExerciseId == exercise.id && cardState == ExerciseCardState.ACTIVE,
                        onExpandToggle = {
                            expandedExerciseId = if (expandedExerciseId == exercise.id) null else exercise.id
                            onExerciseExpand(exercise.id)
                        },
                        onSetClick = { setNumber ->
                            // Handle set chip click
                        },
                        onRepsChange = { currentReps = it },
                        onWeightChange = { currentWeight = it },
                        currentReps = currentReps,
                        currentWeight = currentWeight
                    )
                }

                // RPE Selector section (shows after completing set)
                if (showRPESelector && currentExercise != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppTheme.spacing.md)
                        ) {
                            Text(
                                text = "How hard was that set?",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
                            )
                            CompactRPESelector(
                                selectedRPE = currentRPE,
                                onRPESelected = { rpe ->
                                    currentRPE = rpe
                                },
                                label = "RPE (Rate of Perceived Exertion)"
                            )
                        }
                    }
                }

                // Rest Timer section
                if (showRestTimer) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppTheme.spacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Rest Timer",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
                            )
                            CompactCircularTimer(
                                remainingSeconds = restTimeRemaining,
                                totalSeconds = 90,
                                size = 120.dp
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                            SecondaryButton(
                                text = "Skip Rest",
                                onClick = {
                                    showRestTimer = false
                                    restTimeRemaining = 90
                                }
                            )
                        }
                    }
                }

                // Notes section
                item {
                    NotesInput(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Workout Notes",
                        placeholder = "Add notes about this set or exercise...",
                        minLines = 3,
                        maxLines = 6,
                        maxCharacters = 500,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppTheme.spacing.md)
                    )
                }

                // Action buttons section
                if (currentExercise != null && expandedExerciseId == currentExercise.id) {
                    item {
                        ActionButtons(
                            onCompleteSet = {
                                if (currentReps > 0 || currentWeight > 0f) {
                                    onCompleteSet(currentExercise.id, currentReps, currentWeight, currentRPE)
                                    showRPESelector = true
                                    showRestTimer = true
                                    // Reset inputs
                                    currentReps = 0
                                    currentWeight = 0f
                                    currentRPE = null

                                    // Move to next exercise if current is complete
                                    if (currentExercise.completedSets + 1 == currentExercise.targetSets) {
                                        showRPESelector = false
                                        if (currentExerciseIndex < session.exercises.size - 1) {
                                            currentExerciseIndex++
                                            expandedExerciseId = session.exercises[currentExerciseIndex].id
                                        }
                                    }
                                }
                            },
                            onSkipSet = {
                                onSkipSet(currentExercise.id)
                                currentReps = 0
                                currentWeight = 0f
                                currentRPE = null
                                showRPESelector = false
                            },
                            isCompleteEnabled = currentReps > 0 || currentWeight > 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppTheme.spacing.lg)
                        )
                    }
                }

                // Bottom spacing for comfortable scrolling
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Bottom Sheet for additional options
        BottomSheet(
            visible = showBottomSheet,
            onDismiss = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Text(
                    text = "Workout Options",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
                )

                PrimaryButton(
                    text = "Finish Workout",
                    onClick = {
                        showBottomSheet = false
                        onEndWorkout()
                    },
                    fullWidth = true
                )

                SecondaryButton(
                    text = "Save & Exit",
                    onClick = {
                        showBottomSheet = false
                        onEndWorkout()
                    },
                    fullWidth = true
                )

                SecondaryButton(
                    text = "Cancel Workout",
                    onClick = {
                        showBottomSheet = false
                    },
                    fullWidth = true
                )
            }
        }
    }
}

/**
 * Session Header component showing workout name, elapsed time, and progress
 */
@Composable
private fun SessionHeader(
    workoutName: String,
    elapsedTime: Int,
    completedExercises: Int,
    totalExercises: Int,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(AppTheme.spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workoutName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Elapsed time
                    Text(
                        text = "Time: ${formatTime(elapsedTime)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Exercise progress
                    Text(
                        text = "$completedExercises/$totalExercises exercises",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // More options button
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Action Buttons component for completing or skipping sets
 */
@Composable
private fun ActionButtons(
    onCompleteSet: () -> Unit,
    onSkipSet: () -> Unit,
    isCompleteEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        PrimaryButton(
            text = "Complete Set",
            onClick = onCompleteSet,
            enabled = isCompleteEnabled,
            fullWidth = true
        )

        SecondaryButton(
            text = "Skip Set",
            onClick = onSkipSet,
            fullWidth = true
        )
    }
}

/**
 * Format elapsed time in MM:SS format
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}

// Mock data for previews
internal fun createMockWorkoutSession(): WorkoutSession {
    return WorkoutSession(
        workoutName = "Upper Body Strength",
        exercises = listOf(
            ExerciseData(
                id = "1",
                name = "Bench Press",
                muscleGroup = "Chest",
                targetSets = 4,
                completedSets = 2,
                sets = listOf(
                    SetInfo(setNumber = 1, reps = 10, weight = 80f, state = SetState.COMPLETED),
                    SetInfo(setNumber = 2, reps = 10, weight = 80f, state = SetState.COMPLETED),
                    SetInfo(setNumber = 3, reps = 0, weight = 0f, state = SetState.ACTIVE),
                    SetInfo(setNumber = 4, reps = 0, weight = 0f, state = SetState.PENDING)
                )
            ),
            ExerciseData(
                id = "2",
                name = "Overhead Press",
                muscleGroup = "Shoulders",
                targetSets = 3,
                completedSets = 0,
                sets = listOf(
                    SetInfo(setNumber = 1, reps = 0, weight = 0f, state = SetState.PENDING),
                    SetInfo(setNumber = 2, reps = 0, weight = 0f, state = SetState.PENDING),
                    SetInfo(setNumber = 3, reps = 0, weight = 0f, state = SetState.PENDING)
                )
            ),
            ExerciseData(
                id = "3",
                name = "Barbell Row",
                muscleGroup = "Back",
                targetSets = 4,
                completedSets = 0,
                sets = listOf(
                    SetInfo(setNumber = 1, reps = 0, weight = 0f, state = SetState.PENDING),
                    SetInfo(setNumber = 2, reps = 0, weight = 0f, state = SetState.PENDING),
                    SetInfo(setNumber = 3, reps = 0, weight = 0f, state = SetState.PENDING),
                    SetInfo(setNumber = 4, reps = 0, weight = 0f, state = SetState.PENDING)
                )
            ),
            ExerciseData(
                id = "4",
                name = "Bicep Curl",
                muscleGroup = "Arms",
                targetSets = 3,
                completedSets = 0,
                sets = listOf(
                    SetInfo(setNumber = 1, reps = 0, weight = 0f, state = SetState.PENDING),
                    SetInfo(setNumber = 2, reps = 0, weight = 0f, state = SetState.PENDING),
                    SetInfo(setNumber = 3, reps = 0, weight = 0f, state = SetState.PENDING)
                )
            )
        ),
        startTime = Clock.System.now().toEpochMilliseconds() - 300000 // Started 5 minutes ago
    )
}
