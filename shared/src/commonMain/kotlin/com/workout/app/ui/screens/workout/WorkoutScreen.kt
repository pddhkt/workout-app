package com.workout.app.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.workout.app.ui.components.exercise.ExerciseSetEditorBottomSheet
import com.workout.app.ui.components.exercise.ExerciseWorkoutCard
import com.workout.app.ui.components.exercise.LibraryExercise
import com.workout.app.ui.components.exercise.MuscleGroupFilters
import com.workout.app.ui.components.exercise.SetInfo
import com.workout.app.ui.components.exercise.getMockLibraryExercises
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.components.chips.Badge
import com.workout.app.ui.components.chips.BadgeVariant
import com.workout.app.ui.components.inputs.CompactRPESelector
import com.workout.app.ui.components.inputs.NotesInput
import com.workout.app.ui.components.inputs.SearchBar
import com.workout.app.ui.components.overlays.M3BottomSheet
import com.workout.app.ui.theme.AppTheme
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.material3.TextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.datetime.Clock
import com.workout.app.presentation.workout.WorkoutState
import com.workout.app.presentation.workout.WorkoutExercise

private enum class SheetType {
    OPTIONS, SET_EDITOR, ADD_EXERCISE, EXERCISE_OPTIONS, FINISH_CONFIRM, CREATE_EXERCISE
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
 * - Drag-to-reorder exercises with long-press detection (FT-028)
 * - Reorder mode overlay for easy exercise reordering
 *
 * @param state The workout state from ViewModel (single source of truth)
 * @param onCompleteSet Callback when user completes a set
 * @param onSkipSet Callback when user skips a set
 * @param onExerciseExpand Callback when an exercise card is expanded
 * @param onEndWorkout Callback when user ends the workout
 * @param onAddExercises Callback when adding exercises to the session
 * @param onRemoveExercise Callback when removing an exercise from the session
 * @param onReplaceExercise Callback when replacing an exercise
 * @param onAddSet Callback when adding a set to an exercise
 * @param onReorderExercise Callback when reordering exercises (fromIndex, toIndex)
 * @param onCreateExercise Callback when creating a custom exercise
 * @param onEnterReorderMode Callback when user wants to enter reorder mode
 * @param onExitReorderMode Callback when user wants to exit reorder mode
 * @param modifier Optional modifier for customization
 */
@Composable
fun WorkoutScreen(
    state: WorkoutState,
    onCompleteSet: (exerciseId: String, setNumber: Int, reps: Int, weight: Float, rpe: Int?) -> Unit = { _, _, _, _, _ -> },
    onSkipSet: (exerciseId: String) -> Unit = {},
    onExerciseExpand: (exerciseId: String) -> Unit = {},
    onEndWorkout: () -> Unit = {},
    onAddExercises: (List<String>) -> Unit = {},
    onRemoveExercise: (exerciseId: String) -> Unit = {},
    onReplaceExercise: (exerciseId: String, newExercise: LibraryExercise) -> Unit = { _, _ -> },
    onAddSet: (exerciseId: String) -> Unit = {},
    onReorderExercise: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onCreateExercise: (name: String, muscleGroup: String, equipment: String?, instructions: String?) -> Unit = { _, _, _, _ -> },
    onGetHistoricalWeights: suspend (String) -> List<String> = { emptyList() },
    onGetHistoricalReps: suspend (String) -> List<String> = { emptyList() },
    onSetActiveSet: (exerciseId: String, setIndex: Int) -> Unit = { _, _ -> },
    onEnterReorderMode: () -> Unit = {},
    onExitReorderMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Coroutine scope for async operations
    val scope = rememberCoroutineScope()

    // Use currentExerciseIndex from state (single source of truth)
    val currentExerciseIndex = state.currentExerciseIndex

    // Editor State
    var activeSheet by remember { mutableStateOf<SheetType?>(null) }
    var editingSetNumber by remember { mutableIntStateOf(1) }
    var selectedExerciseForOptions by remember { mutableStateOf<WorkoutExercise?>(null) }
    var selectedExerciseForEditor by remember { mutableStateOf<WorkoutExercise?>(null) }
    var replacingExerciseId by remember { mutableStateOf<String?>(null) }

    var currentReps by remember { mutableIntStateOf(0) }
    var currentWeight by remember { mutableFloatStateOf(0f) }
    var currentRPE by remember { mutableStateOf<Int?>(null) }
    var notes by remember { mutableStateOf("") }

    // History values for NumberPad quick selection
    var currentWeightHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentRepsHistory by remember { mutableStateOf<List<String>>(emptyList()) }

    // Previous set info for quick-select in set editor
    var previousSetNumber by remember { mutableStateOf<Int?>(null) }
    var previousSetWeight by remember { mutableStateOf<Float?>(null) }

    var showRestTimer by remember { mutableStateOf(false) }
    var restTimeRemaining by remember { mutableIntStateOf(90) }

    // Expanded state for legacy cards (though we might not need it for active card anymore)
    var expandedExerciseId by remember { mutableStateOf(state.exercises.firstOrNull()?.id) }

    // Drag reorder state
    var isDragging by remember { mutableStateOf(false) }
    var draggedItemIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var hoverIndex by remember { mutableIntStateOf(-1) }
    val itemHeights = remember { mutableStateMapOf<Int, Float>() }
    val density = LocalDensity.current

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

    val currentExercise = state.exercises.getOrNull(currentExerciseIndex)

    // Update local state when opening editor or changing set
    fun openSetEditor(exercise: WorkoutExercise, setIndex: Int) {
        selectedExerciseForEditor = exercise
        editingSetNumber = setIndex + 1

        // Call ViewModel to sync active set state
        onSetActiveSet(exercise.id, setIndex)

        // Fetch history values for NumberPad
        scope.launch {
            currentWeightHistory = onGetHistoricalWeights(exercise.exerciseId)
            currentRepsHistory = onGetHistoricalReps(exercise.exerciseId)
        }

        // Compute previous set info (for sets 2+)
        // setIndex is 0-indexed, so for set 2 (setIndex=1), we look for setNumber=1
        val prevSetNum = setIndex  // setIndex 1 means set 2, so prev is setNumber 1
        if (prevSetNum > 0) {
            val prevRecord = exercise.setRecords.find { it.setNumber == prevSetNum }
            previousSetNumber = if (prevRecord?.weight != null) prevSetNum else null
            previousSetWeight = prevRecord?.weight
        } else {
            previousSetNumber = null
            previousSetWeight = null
        }

        // Get set info from exercise's setRecords
        val setNum = setIndex + 1
        val record = exercise.setRecords.find { it.setNumber == setNum }
        currentReps = record?.reps?.takeIf { it > 0 } ?: 12 // Default target
        currentWeight = record?.weight?.takeIf { it > 0f } ?: 100f // Default target
        // Reset RPE and Notes for new set if not stored
        currentRPE = null
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
                workoutName = state.sessionName,
                elapsedTime = state.elapsedSeconds,
                completedExercises = state.exercises.count { it.completedSets == it.targetSets },
                totalExercises = state.exercises.size,
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
                itemsIndexed(state.exercises) { index, exercise ->
                    val isCompleted = exercise.completedSets == exercise.targetSets
                    val isActive = index == state.currentExerciseIndex && !isCompleted
                    val isBeingDragged = isDragging && draggedItemIndex == index

                    // Calculate shift for non-dragged items
                    val targetOffsetY = if (isDragging && index != draggedItemIndex && hoverIndex != -1) {
                        // Use measured height of dragged item, fallback to estimate
                        val draggedItemHeight = itemHeights[draggedItemIndex] ?: 200f
                        val spacingPx = with(density) { 12.dp.toPx() } // AppTheme.spacing.md
                        val totalItemHeight = draggedItemHeight + spacingPx
                        when {
                            // Dragged from above, item needs to shift up
                            draggedItemIndex < index && hoverIndex >= index -> -totalItemHeight
                            // Dragged from below, item needs to shift down
                            draggedItemIndex > index && hoverIndex <= index -> totalItemHeight
                            else -> 0f
                        }
                    } else 0f

                    val animatedOffsetY by animateFloatAsState(
                        targetValue = targetOffsetY,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                        label = "itemShift"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                val heightPx = coordinates.size.height.toFloat()
                                if (itemHeights[index] != heightPx) {
                                    itemHeights[index] = heightPx
                                }
                            }
                            .zIndex(if (isBeingDragged) 1f else 0f)
                            .graphicsLayer {
                                // Apply visual offset and elevation during drag
                                translationY = if (isBeingDragged) dragOffset else animatedOffsetY
                                shadowElevation = if (isBeingDragged) 8f else 0f
                                // Scale effect on drag
                                scaleX = if (isBeingDragged) 1.02f else 1f
                                scaleY = if (isBeingDragged) 1.02f else 1f
                            }
                            .pointerInput(index, state.exercises.size) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        isDragging = true
                                        draggedItemIndex = index
                                        dragOffset = 0f
                                        hoverIndex = index
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y
                                        // Calculate hover index using measured heights
                                        val draggedItemHeight = itemHeights[draggedItemIndex] ?: 200f
                                        val spacingPx = with(density) { 12.dp.toPx() }
                                        val totalItemHeight = draggedItemHeight + spacingPx
                                        val draggedPositions = (dragOffset / totalItemHeight).toInt()
                                        hoverIndex = (draggedItemIndex + draggedPositions)
                                            .coerceIn(0, state.exercises.size - 1)
                                    },
                                    onDragEnd = {
                                        // Calculate target index using measured heights
                                        val draggedItemHeight = itemHeights[draggedItemIndex] ?: 200f
                                        val spacingPx = with(density) { 12.dp.toPx() }
                                        val totalItemHeight = draggedItemHeight + spacingPx
                                        val draggedPositions = (dragOffset / totalItemHeight).toInt()
                                        val targetIndex = (draggedItemIndex + draggedPositions)
                                            .coerceIn(0, state.exercises.size - 1)

                                        if (targetIndex != draggedItemIndex) {
                                            onReorderExercise(draggedItemIndex, targetIndex)
                                        }

                                        // Reset drag state
                                        isDragging = false
                                        draggedItemIndex = -1
                                        dragOffset = 0f
                                        hoverIndex = -1
                                    },
                                    onDragCancel = {
                                        // Reset drag state on cancel
                                        isDragging = false
                                        draggedItemIndex = -1
                                        dragOffset = 0f
                                        hoverIndex = -1
                                    }
                                )
                            }
                    ) {
                        // Compute sets inline from WorkoutExercise
                        val sets = List(exercise.targetSets) { setIndex ->
                            val setNum = setIndex + 1
                            val record = exercise.setRecords.find { it.setNumber == setNum }
                            val completedSetNumbers = exercise.setRecords.map { it.setNumber }.toSet()
                            val firstPendingSetNum = (1..exercise.targetSets).firstOrNull { it !in completedSetNumbers }
                            SetInfo(
                                setNumber = setNum,
                                reps = record?.reps ?: 0,
                                weight = record?.weight ?: 0f,
                                state = when {
                                    record != null -> SetState.COMPLETED
                                    setNum == firstPendingSetNum -> SetState.ACTIVE
                                    else -> SetState.PENDING
                                }
                            )
                        }

                        ExerciseWorkoutCard(
                            exerciseName = exercise.name,
                            muscleGroup = exercise.muscleGroup,
                            targetSummary = "${exercise.targetSets} Sets • 8-12 Reps",
                            sets = sets,
                            isActive = isActive,
                            activeSetIndex = when {
                                // User explicitly selected a set on this exercise
                                exercise.userSelectedSetIndex != null -> exercise.userSelectedSetIndex!!
                                // This is the current exercise - show next pending set as auto-active
                                isActive -> exercise.completedSets
                                // Non-current exercise with no user selection - no active set
                                else -> -1
                            },
                            onSetClick = { setIndex ->
                                openSetEditor(exercise, setIndex)
                            },
                            onAddSet = { onAddSet(exercise.id) },
                            onOptionsClick = {
                                selectedExerciseForOptions = exercise
                                activeSheet = SheetType.EXERCISE_OPTIONS
                            },
                            onLongPressTitle = onEnterReorderMode
                        )
                    }
                }

                // Action Buttons (Add Exercise + Complete Workout)
                item {
                    WorkoutActionButtons(
                        onAddExerciseClick = { activeSheet = SheetType.ADD_EXERCISE },
                        onCompleteWorkoutClick = { activeSheet = SheetType.FINISH_CONFIRM }
                    )
                }

                // Bottom spacing for comfortable scrolling
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
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

                        SecondaryButton(
                            text = "Save & Exit",
                            onClick = {
                                activeSheet = null
                                onEndWorkout()
                            },
                            fullWidth = true
                        )

                        SecondaryButton(
                            text = "Cancel",
                            onClick = {
                                activeSheet = null
                            },
                            fullWidth = true
                        )
                    }
                }
                SheetType.SET_EDITOR -> {
                    val editingExercise = selectedExerciseForEditor
                    if (editingExercise != null) {
                        ExerciseSetEditorBottomSheet(
                            exerciseName = editingExercise.name,
                            setNumber = editingSetNumber,
                            previousPerformance = editingExercise.previousPerformance ?: "First time",
                            currentWeight = currentWeight,
                            currentReps = currentReps,
                            currentRpe = currentRPE,
                            restTimerSeconds = 120,
                            notes = notes,
                            onWeightChange = { currentWeight = it },
                            onRepsChange = { currentReps = it },
                            onRpeChange = { currentRPE = it },
                            onRestTimerChange = { /* Handle timer change */ },
                            onNotesChange = { notes = it },
                            onDeleteSet = {
                                previousSetNumber = null
                                previousSetWeight = null
                                activeSheet = null
                            },
                            onCompleteSet = {
                                onCompleteSet(editingExercise.id, editingSetNumber, currentReps, currentWeight, currentRPE)
                                currentWeightHistory = emptyList()
                                currentRepsHistory = emptyList()
                                previousSetNumber = null
                                previousSetWeight = null
                                activeSheet = null
                            },
                            weightHistoryValues = currentWeightHistory,
                            repsHistoryValues = currentRepsHistory,
                            previousSetNumber = previousSetNumber,
                            previousSetWeight = previousSetWeight,
                            onApplyPreviousWeight = {
                                previousSetWeight?.let { currentWeight = it }
                            }
                        )
                    }
                }
                SheetType.ADD_EXERCISE -> {
                    var selectedExercises by remember { mutableStateOf(setOf<String>()) }

                    // Check if we're replacing - use single-select mode
                    val isReplacing = replacingExerciseId != null

                    if (isReplacing) {
                        // Single-select mode for replace
                        SimpleExercisePickerContent(
                            onExerciseSelected = { exercise ->
                                val exerciseToReplace = replacingExerciseId
                                if (exerciseToReplace != null) {
                                    onReplaceExercise(exerciseToReplace, exercise)
                                    replacingExerciseId = null
                                }
                                activeSheet = null
                            }
                        )
                    } else {
                        // Multi-select mode for adding exercises
                        Column {
                            MultiSelectExercisePickerForWorkout(
                                selectedExerciseIds = selectedExercises,
                                onExerciseToggle = { exercise ->
                                    selectedExercises = if (selectedExercises.contains(exercise.id)) {
                                        selectedExercises - exercise.id
                                    } else {
                                        selectedExercises + exercise.id
                                    }
                                },
                                onCreateExerciseClick = {
                                    activeSheet = SheetType.CREATE_EXERCISE
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Add button at bottom
                            PrimaryButton(
                                text = if (selectedExercises.isEmpty()) "Select Exercises" else "Add ${selectedExercises.size} Exercise(s)",
                                onClick = {
                                    onAddExercises(selectedExercises.toList())
                                    selectedExercises = emptySet()
                                    activeSheet = null
                                },
                                enabled = selectedExercises.isNotEmpty(),
                                fullWidth = true,
                                modifier = Modifier.padding(AppTheme.spacing.md)
                            )
                        }
                    }
                }
                SheetType.EXERCISE_OPTIONS -> {
                    val exercise = selectedExerciseForOptions
                    if (exercise != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                        ) {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
                            )

                            SecondaryButton(
                                text = "Replace Exercise",
                                onClick = {
                                    replacingExerciseId = exercise.id
                                    activeSheet = SheetType.ADD_EXERCISE
                                },
                                fullWidth = true
                            )

                            SecondaryButton(
                                text = "Remove Exercise",
                                onClick = {
                                    onRemoveExercise(exercise.id)
                                    selectedExerciseForOptions = null
                                    activeSheet = null
                                },
                                fullWidth = true,
                                destructive = true
                            )

                            SecondaryButton(
                                text = "Cancel",
                                onClick = {
                                    selectedExerciseForOptions = null
                                    activeSheet = null
                                },
                                fullWidth = true
                            )
                        }
                    }
                }
                SheetType.FINISH_CONFIRM -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                    ) {
                        Text(
                            text = "Finish Workout?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Are you sure you want to finish this workout? Your progress will be saved.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                        PrimaryButton(
                            text = "Finish Workout",
                            onClick = {
                                activeSheet = null
                                onEndWorkout()
                            },
                            fullWidth = true
                        )
                        SecondaryButton(
                            text = "Cancel",
                            onClick = { activeSheet = null },
                            fullWidth = true
                        )
                    }
                }
                SheetType.CREATE_EXERCISE -> {
                    var exerciseName by remember { mutableStateOf("") }
                    var muscleGroup by remember { mutableStateOf("") }
                    var equipment by remember { mutableStateOf("") }
                    var instructions by remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                    ) {
                        Text(
                            text = "Create Custom Exercise",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        TextField(
                            value = exerciseName,
                            onValueChange = { exerciseName = it },
                            label = { Text("Exercise Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        TextField(
                            value = muscleGroup,
                            onValueChange = { muscleGroup = it },
                            label = { Text("Muscle Group") },
                            placeholder = { Text("e.g., Chest, Back, Legs") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        TextField(
                            value = equipment,
                            onValueChange = { equipment = it },
                            label = { Text("Equipment (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        TextField(
                            value = instructions,
                            onValueChange = { instructions = it },
                            label = { Text("Instructions (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                        ) {
                            SecondaryButton(
                                text = "Cancel",
                                onClick = { activeSheet = SheetType.ADD_EXERCISE },
                                modifier = Modifier.weight(1f)
                            )
                            PrimaryButton(
                                text = "Create & Add",
                                onClick = {
                                    if (exerciseName.isNotBlank() && muscleGroup.isNotBlank()) {
                                        onCreateExercise(
                                            exerciseName.trim(),
                                            muscleGroup.trim(),
                                            equipment.trim().takeIf { it.isNotBlank() },
                                            instructions.trim().takeIf { it.isNotBlank() }
                                        )
                                        activeSheet = null
                                    }
                                },
                                enabled = exerciseName.isNotBlank() && muscleGroup.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                null -> {}
            }
        }

        // Reorder Mode Overlay
        if (state.isReorderMode) {
            WorkoutReorderOverlay(
                exercises = state.exercises,
                onReorder = { fromIndex, toIndex ->
                    onReorderExercise(fromIndex, toIndex)
                },
                onDismiss = onExitReorderMode
            )
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
            .windowInsetsPadding(WindowInsets.statusBars)
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
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
 * Workout action buttons (Add Exercise + Complete Workout)
 */
@Composable
private fun WorkoutActionButtons(
    onAddExerciseClick: () -> Unit,
    onCompleteWorkoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        // Add Exercise button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable(onClick = onAddExerciseClick)
                .padding(AppTheme.spacing.md),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Add Exercise",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Complete Workout button
        PrimaryButton(
            text = "Complete Workout",
            onClick = onCompleteWorkoutClick,
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

/**
 * Simple single-select exercise picker for replace functionality
 */
@Composable
private fun SimpleExercisePickerContent(
    exercises: List<LibraryExercise> = getMockLibraryExercises(),
    onExerciseSelected: (LibraryExercise) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf("All") }

    val filteredExercises = exercises.filter { exercise ->
        val matchesSearch = exercise.name.contains(searchQuery, ignoreCase = true) ||
                exercise.muscleGroup.contains(searchQuery, ignoreCase = true)
        val matchesMuscleGroup = selectedMuscleGroup == "All" ||
                exercise.muscleGroup == selectedMuscleGroup
        matchesSearch && matchesMuscleGroup
    }

    val groupedExercises = filteredExercises.groupBy { it.category }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Replace Exercise",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { },
            placeholder = "Search exercises...",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        MuscleGroupFilters(
            selectedMuscleGroup = selectedMuscleGroup,
            onMuscleGroupSelected = { selectedMuscleGroup = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            groupedExercises.forEach { (_, categoryExercises) ->
                items(categoryExercises, key = { it.id }) { exercise ->
                    BaseCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onExerciseSelected(exercise) },
                        contentPadding = AppTheme.spacing.md
                    ) {
                        Column {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = exercise.muscleGroup,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Multi-select exercise picker for adding exercises to workout
 */
@Composable
private fun MultiSelectExercisePickerForWorkout(
    exercises: List<LibraryExercise> = getMockLibraryExercises(),
    selectedExerciseIds: Set<String>,
    onExerciseToggle: (LibraryExercise) -> Unit,
    onCreateExerciseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf("All") }

    val filteredExercises = exercises.filter { exercise ->
        val matchesSearch = exercise.name.contains(searchQuery, ignoreCase = true) ||
                exercise.muscleGroup.contains(searchQuery, ignoreCase = true)
        val matchesMuscleGroup = selectedMuscleGroup == "All" ||
                exercise.muscleGroup == selectedMuscleGroup
        matchesSearch && matchesMuscleGroup
    }

    val groupedExercises = filteredExercises.groupBy { it.category }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Add Exercises",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        SecondaryButton(
            text = "Create New Exercise",
            onClick = onCreateExerciseClick,
            fullWidth = true,
            modifier = Modifier.padding(bottom = AppTheme.spacing.md)
        )

        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { },
            placeholder = "Search exercises...",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        MuscleGroupFilters(
            selectedMuscleGroup = selectedMuscleGroup,
            onMuscleGroupSelected = { selectedMuscleGroup = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            groupedExercises.forEach { (_, categoryExercises) ->
                items(categoryExercises, key = { it.id }) { exercise ->
                    val isSelected = selectedExerciseIds.contains(exercise.id)

                    BaseCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onExerciseToggle(exercise) },
                        contentPadding = AppTheme.spacing.md,
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else null
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = exercise.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (exercise.isCustom) {
                                        Badge(text = "Custom", variant = BadgeVariant.INFO)
                                    }
                                }
                                Text(
                                    text = exercise.muscleGroup,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Mock data for previews
internal fun createMockWorkoutState(): WorkoutState {
    return WorkoutState(
        sessionName = "Upper Body Strength",
        elapsedSeconds = 300, // 5 minutes
        exercises = listOf(
            WorkoutExercise(
                id = "1",
                exerciseId = "ex_1",
                name = "Bench Press",
                muscleGroup = "Chest",
                targetSets = 4,
                completedSets = 2,
                setRecords = listOf(
                    com.workout.app.presentation.workout.CompletedSetRecord(setNumber = 1, weight = 80f, reps = 10, rpe = null),
                    com.workout.app.presentation.workout.CompletedSetRecord(setNumber = 2, weight = 80f, reps = 10, rpe = null)
                )
            ),
            WorkoutExercise(
                id = "2",
                exerciseId = "ex_2",
                name = "Overhead Press",
                muscleGroup = "Shoulders",
                targetSets = 3,
                completedSets = 0
            ),
            WorkoutExercise(
                id = "3",
                exerciseId = "ex_3",
                name = "Barbell Row",
                muscleGroup = "Back",
                targetSets = 4,
                completedSets = 0
            ),
            WorkoutExercise(
                id = "4",
                exerciseId = "ex_4",
                name = "Bicep Curl",
                muscleGroup = "Arms",
                targetSets = 3,
                completedSets = 0
            )
        )
    )
}
