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
import androidx.compose.material3.TextField
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
    },
    val previousPerformance: String? = null,
    val userSelectedSetIndex: Int? = null  // User-selected active set
)

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
 *
 * @param session The workout session data
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
 * @param modifier Optional modifier for customization
 */
@Composable
fun WorkoutScreen(
    session: WorkoutSession,
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
    modifier: Modifier = Modifier
) {
    // State management
    var currentExerciseIndex by remember { mutableIntStateOf(0) }

    // State for user-selected active set per exercise
    var userSelectedSets by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // Editor State
    var activeSheet by remember { mutableStateOf<SheetType?>(null) }
    var editingSetNumber by remember { mutableIntStateOf(1) }
    var selectedExerciseForOptions by remember { mutableStateOf<ExerciseData?>(null) }
    var selectedExerciseForEditor by remember { mutableStateOf<ExerciseData?>(null) }
    var replacingExerciseId by remember { mutableStateOf<String?>(null) }
    
    var currentReps by remember { mutableIntStateOf(0) }
    var currentWeight by remember { mutableFloatStateOf(0f) }
    var currentRPE by remember { mutableStateOf<Int?>(null) }
    var notes by remember { mutableStateOf("") }
    
    var showRestTimer by remember { mutableStateOf(false) }
    var restTimeRemaining by remember { mutableIntStateOf(90) }
    var elapsedTime by remember { mutableIntStateOf(0) }
    
    // Expanded state for legacy cards (though we might not need it for active card anymore)
    var expandedExerciseId by remember { mutableStateOf(session.exercises.firstOrNull()?.id) }

    // Drag reorder state
    var isDragging by remember { mutableStateOf(false) }
    var draggedItemIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

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
        selectedExerciseForEditor = exercise
        editingSetNumber = setIndex + 1
        // Track user selection
        userSelectedSets = userSelectedSets + (exercise.id to setIndex)
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
                    val isActive = index == currentExerciseIndex && !isCompleted
                    val isBeingDragged = isDragging && draggedItemIndex == index

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                // Apply visual offset and elevation during drag
                                translationY = if (isBeingDragged) dragOffset else 0f
                                shadowElevation = if (isBeingDragged) 8f else 0f
                                // Scale effect on drag
                                scaleX = if (isBeingDragged) 1.02f else 1f
                                scaleY = if (isBeingDragged) 1.02f else 1f
                            }
                            .pointerInput(index, session.exercises.size) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        isDragging = true
                                        draggedItemIndex = index
                                        dragOffset = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y
                                    },
                                    onDragEnd = {
                                        // Calculate target index based on drag offset
                                        // Approximate card height including spacing: 180dp card + 8dp spacing
                                        val itemHeight = 188f
                                        val draggedPositions = (dragOffset / itemHeight).toInt()
                                        val targetIndex = (draggedItemIndex + draggedPositions)
                                            .coerceIn(0, session.exercises.size - 1)

                                        if (targetIndex != draggedItemIndex) {
                                            onReorderExercise(draggedItemIndex, targetIndex)
                                        }

                                        // Reset drag state
                                        isDragging = false
                                        draggedItemIndex = -1
                                        dragOffset = 0f
                                    },
                                    onDragCancel = {
                                        // Reset drag state on cancel
                                        isDragging = false
                                        draggedItemIndex = -1
                                        dragOffset = 0f
                                    }
                                )
                            }
                    ) {
                        ExerciseWorkoutCard(
                            exerciseName = exercise.name,
                            muscleGroup = exercise.muscleGroup,
                            targetSummary = "${exercise.targetSets} Sets • 8-12 Reps",
                            sets = exercise.sets,
                            isActive = isActive,
                            activeSetIndex = userSelectedSets[exercise.id] ?: exercise.completedSets,
                            onSetClick = { setIndex ->
                                openSetEditor(exercise, setIndex)
                            },
                            onAddSet = { onAddSet(exercise.id) },
                            onOptionsClick = {
                                selectedExerciseForOptions = exercise
                                activeSheet = SheetType.EXERCISE_OPTIONS
                            }
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
                            restTimerSeconds = 120, // Mock default
                            notes = notes,
                            onWeightChange = { currentWeight = it },
                            onRepsChange = { currentReps = it },
                            onRpeChange = { currentRPE = it },
                            onRestTimerChange = { /* Handle timer change */ },
                            onNotesChange = { notes = it },
                            onDeleteSet = {
                                // Handle delete
                                activeSheet = null
                            },
                            onCompleteSet = {
                                onCompleteSet(editingExercise.id, editingSetNumber, currentReps, currentWeight, currentRPE)
                                // Clear user selection after completing a set
                                userSelectedSets = userSelectedSets - editingExercise.id
                                activeSheet = null
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
