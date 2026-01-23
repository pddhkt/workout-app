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
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.components.chips.SetState
import com.workout.app.ui.components.dataviz.CompactCircularTimer
import com.workout.app.ui.components.exercise.ExerciseCard
import com.workout.app.ui.components.exercise.ExerciseCardState
import com.workout.app.ui.components.exercise.ExerciseExecutionCard
import com.workout.app.ui.components.exercise.ExerciseSetEditorBottomSheet
import com.workout.app.ui.components.exercise.SetInfo
import com.workout.app.ui.components.inputs.CompactRPESelector
import com.workout.app.ui.components.inputs.NotesInput
import com.workout.app.ui.components.overlays.M3BottomSheet
import com.workout.app.ui.theme.AppTheme
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

private enum class SheetType {
    OPTIONS, SET_EDITOR
}

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
    
    // Editor State
    var activeSheet by remember { mutableStateOf<SheetType?>(null) }
    var editingSetNumber by remember { mutableIntStateOf(1) }
    
    var currentReps by remember { mutableIntStateOf(0) }
    var currentWeight by remember { mutableFloatStateOf(0f) }
    var currentRPE by remember { mutableStateOf<Int?>(null) }
    var notes by remember { mutableStateOf("") }
    
    var showRestTimer by remember { mutableStateOf(false) }
    var restTimeRemaining by remember { mutableIntStateOf(90) }
    var elapsedTime by remember { mutableIntStateOf(0) }
    
    // Expanded state for legacy cards (though we might not need it for active card anymore)
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
    
    // Update local state when opening editor or changing set
    fun openSetEditor(exercise: ExerciseData, setIndex: Int) {
        editingSetNumber = setIndex + 1
        val set = exercise.sets.getOrNull(setIndex)
        if (set != null) {
            currentReps = if (set.reps > 0) set.reps else 12 // Default target
            currentWeight = if (set.weight > 0f) set.weight else 100f // Default target
            // Reset RPE and Notes for new set if not stored (mock assumption)
            currentRPE = null 
            // notes = ...
        }
        activeSheet = SheetType.SET_EDITOR
    }

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
                onMoreClick = { activeSheet = SheetType.OPTIONS }
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
                    val isCompleted = exercise.completedSets == exercise.targetSets
                    val isActive = index == currentExerciseIndex
                    
                    if (isActive && !isCompleted) {
                        // Use new ExerciseExecutionCard for the active exercise
                        ExerciseExecutionCard(
                            exerciseName = exercise.name,
                            targetSummary = "Target: ${exercise.targetSets} Sets • 8-12 Reps", // Mock target
                            sets = exercise.sets,
                            activeSetIndex = exercise.completedSets, // Next pending set is active
                            onSetClick = { setIndex ->
                                openSetEditor(exercise, setIndex)
                            },
                            onOptionsClick = { activeSheet = SheetType.OPTIONS }
                        )
                    } else {
                        // Use standard ExerciseCard for others
                        ExerciseCard(
                            exerciseName = exercise.name,
                            muscleGroup = exercise.muscleGroup,
                            sets = exercise.sets,
                            state = if (isCompleted) ExerciseCardState.COMPLETED else ExerciseCardState.PENDING,
                            isExpanded = false,
                            onExpandToggle = { /* No expansion for non-active in this mode */ }
                        )
                    }
                }

                // Bottom spacing for comfortable scrolling
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Bottom Sheet
        M3BottomSheet(
            visible = activeSheet != null,
            onDismiss = { activeSheet = null }
        ) {
            when (activeSheet) {
                SheetType.OPTIONS -> {
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
                                activeSheet = null
                                onEndWorkout()
                            },
                            fullWidth = true
                        )

                        SecondaryButton(
                            text = "Save & Exit",
                            onClick = {
                                activeSheet = null
                                onEndWorkout()
                            },
                            fullWidth = true
                        )

                        SecondaryButton(
                            text = "Cancel Workout",
                            onClick = {
                                activeSheet = null
                            },
                            fullWidth = true
                        )
                    }
                }
                SheetType.SET_EDITOR -> {
                    if (currentExercise != null) {
                        ExerciseSetEditorBottomSheet(
                            exerciseName = currentExercise.name,
                            setNumber = editingSetNumber,
                            previousPerformance = "Previous: 100kg x 10", // Mock
                            currentWeight = currentWeight,
                            currentReps = currentReps,
                            currentRpe = currentRPE,
                            restTimerSeconds = 120, // Mock default
                            notes = notes,
                            onWeightChange = { currentWeight = it },
                            onRepsChange = { currentReps = it },
                            onRpeChange = { currentRPE = it },
                            onRestTimerChange = { /* Handle timer change */ },
                            onNotesChange = { notes = it },
                            onHistoryClick = { /* Show history */ },
                            onDeleteSet = { 
                                // Handle delete
                                activeSheet = null 
                            },
                            onCompleteSet = {
                                onCompleteSet(currentExercise.id, currentReps, currentWeight, currentRPE)
                                activeSheet = null
                                
                                // Logic to advance set/exercise is in parent/viewmodel usually, 
                                // but here we update UI state locally for mock
                                if (currentExercise.completedSets + 1 == currentExercise.targetSets) {
                                    if (currentExerciseIndex < session.exercises.size - 1) {
                                        currentExerciseIndex++
                                    }
                                }
                            }
                        )
                    }
                }
                null -> {}
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
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
