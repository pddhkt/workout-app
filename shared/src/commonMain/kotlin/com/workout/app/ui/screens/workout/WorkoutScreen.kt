package com.workout.app.ui.screens.workout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.components.chips.Badge
import com.workout.app.ui.components.chips.BadgeVariant
import com.workout.app.ui.components.exercise.LibraryExercise
import com.workout.app.ui.components.exercise.MuscleGroupFilters
import com.workout.app.ui.components.exercise.getMockLibraryExercises
import com.workout.app.ui.components.inputs.SearchBar
import com.workout.app.ui.components.overlays.M3BottomSheet
import com.workout.app.ui.components.timer.RestTimerSection
import com.workout.app.ui.components.timer.StopwatchInput
import com.workout.app.ui.components.gps.GpsPathCanvas
import com.workout.app.ui.components.gps.RequestLocationPermission
import com.workout.app.ui.theme.AppTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import com.workout.app.domain.location.LocationTracker
import com.workout.app.domain.model.GpsPoint
import com.workout.app.domain.model.SessionMode
import com.workout.app.presentation.workout.WorkoutState
import com.workout.app.presentation.workout.WorkoutExercise
import org.koin.compose.koinInject

private enum class SheetType {
    OPTIONS, ADD_EXERCISE, EXERCISE_OPTIONS, FINISH_CONFIRM, CREATE_EXERCISE
}

/**
 * Format a completed set record for display, using the exercise's recording fields.
 * Produces: "80kg x10" for weight+reps, "10 slow blinks" for reps-only, "30sec" for duration, etc.
 */
private fun formatSetRecord(
    set: com.workout.app.presentation.workout.CompletedSetRecord,
    exercise: com.workout.app.presentation.workout.WorkoutExercise?
): String {
    val fields = exercise?.recordingFields ?: com.workout.app.domain.model.RecordingField.DEFAULT_FIELDS
    val fv = set.fieldValues.filterKeys { !it.startsWith("_") }

    // If we have dynamic field values, format from those
    if (fv.isNotEmpty()) {
        val isDefault = fields.size == 2 && fields.any { it.key == "weight" } && fields.any { it.key == "reps" }
        if (isDefault) {
            val w = fv["weight"] ?: set.weight.toString()
            val r = fv["reps"] ?: set.reps.toString()
            return "${formatNumber(w)}kg x$r"
        }
        return fields.mapNotNull { field ->
            val v = fv[field.key] ?: return@mapNotNull null
            if (v.isBlank()) return@mapNotNull null
            val unitSuffix = if (field.unit.isNotEmpty()) field.unit else ""
            if (fields.size == 1) {
                // Single field: "10 Slow Blinks" or "30sec"
                if (unitSuffix.isNotEmpty()) "${formatNumber(v)}$unitSuffix" else "${formatNumber(v)} ${field.label}"
            } else {
                "${formatNumber(v)}$unitSuffix"
            }
        }.joinToString(" x ")
    }

    // Legacy fallback
    val w = if (set.weight == set.weight.toLong().toFloat()) set.weight.toLong().toString() else set.weight.toString()
    return "${w}kg x${set.reps}"
}

private fun formatNumber(s: String): String {
    val d = s.toDoubleOrNull() ?: return s
    return if (d == d.toLong().toDouble()) d.toLong().toString() else s
}

private data class SetPage(
    val setNumber: Int,
    val isEndPage: Boolean = false,
    val isCompleted: Boolean = false
)

/**
 * Workout Screen - Active workout session with two-pane layout
 *
 * Top pane: scrollable exercise cards (compact) with swipe-to-delete and drag-to-reorder
 * Bottom pane: inline set input with swipeable set pages
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkoutScreen(
    state: WorkoutState,
    onCompleteSet: (exerciseId: String, setNumber: Int, fieldValues: Map<String, String>) -> Unit = { _, _, _ -> },
    onExerciseExpand: (exerciseId: String) -> Unit = {},
    onEndWorkout: () -> Unit = {},
    onCancelWorkout: () -> Unit = {},
    onRenameWorkout: (String) -> Unit = {},
    onAddExercises: (List<String>) -> Unit = {},
    onRemoveExercise: (exerciseId: String) -> Unit = {},
    onReplaceExercise: (exerciseId: String, newExercise: LibraryExercise) -> Unit = { _, _ -> },
    onAddSet: (exerciseId: String) -> Unit = {},
    onReorderExercise: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onCreateExercise: (name: String, muscleGroup: String, equipment: String?, instructions: String?) -> Unit = { _, _, _, _ -> },
    onRestTimerStart: () -> Unit = {},
    onRestTimerStop: () -> Unit = {},
    onRestTimerReset: () -> Unit = {},
    onRestTimerDurationChange: (Int) -> Unit = {},
    onRestTimerAdjust: (Int) -> Unit = {},
    onSwitchParticipant: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Sheet state
    var activeSheet by remember { mutableStateOf<SheetType?>(null) }
    var selectedExerciseForOptions by remember { mutableStateOf<WorkoutExercise?>(null) }
    var replacingExerciseId by remember { mutableStateOf<String?>(null) }
    var exerciseToRemove by remember { mutableStateOf<WorkoutExercise?>(null) }

    // Selected exercise and set page tracking
    var selectedExerciseIndex by remember { mutableIntStateOf(state.currentExerciseIndex) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var fieldInputs by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var swipeDirection by remember { mutableIntStateOf(1) }

    // Stopwatch state for duration-type fields (keyed by field key)
    var stopwatchRunningKey by remember { mutableStateOf<String?>(null) }
    var stopwatchSeconds by remember { mutableIntStateOf(0) }

    // GPS tracking for distance exercises
    val locationTracker: LocationTracker = koinInject()
    val gpsPoints by locationTracker.points.collectAsState()
    val gpsDistanceMeters by locationTracker.distanceMeters.collectAsState()
    val isGpsTracking by locationTracker.isTracking.collectAsState()
    val hasLocationPermission by locationTracker.hasPermission.collectAsState()
    var showPermissionRequest by remember { mutableStateOf(false) }

    // Preserve last completed GPS path so it stays visible after set completion
    var lastCompletedGpsPath by remember { mutableStateOf<List<GpsPoint>>(emptyList()) }
    var lastCompletedDistanceKm by remember { mutableStateOf(0.0) }
    var lastCompletedExerciseIndex by remember { mutableIntStateOf(-1) }

    // Tick the stopwatch every second when running
    LaunchedEffect(stopwatchRunningKey) {
        val key = stopwatchRunningKey
        if (key != null) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                stopwatchSeconds++
                fieldInputs = fieldInputs + (key to stopwatchSeconds.toString())
            }
        }
    }
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    // Per-participant draft inputs: saves uncommitted field values when switching participants
    val participantDrafts = remember { mutableMapOf<String, Map<String, String>>() }
    var previousParticipantId by remember { mutableStateOf(state.activeParticipantId) }

    // Drag-to-reorder state
    val density = LocalDensity.current
    val itemHeightDp = 72.dp
    val itemSpacingDp = AppTheme.spacing.md
    val itemHeightPx = with(density) { itemHeightDp.toPx() }
    val itemSpacingPx = with(density) { itemSpacingDp.toPx() }
    val totalItemHeightPx = itemHeightPx + itemSpacingPx

    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var hoverIndex by remember { mutableIntStateOf(-1) }

    // Clamp selectedExerciseIndex if exercises were removed
    if (state.exercises.isNotEmpty() && selectedExerciseIndex >= state.exercises.size) {
        selectedExerciseIndex = state.exercises.size - 1
    }

    // Sync with ViewModel auto-advance
    LaunchedEffect(state.currentExerciseIndex) {
        selectedExerciseIndex = state.currentExerciseIndex
        currentPageIndex = 0
    }

    val selectedExercise = state.exercises.getOrNull(selectedExerciseIndex)
    val hasDistanceField = selectedExercise?.recordingFields?.any { it.key == "distance" } == true

    // Auto-fill distance from GPS
    LaunchedEffect(gpsDistanceMeters) {
        if (hasDistanceField && gpsDistanceMeters > 0) {
            val distanceKm = gpsDistanceMeters / 1000.0
            val formatted = "%.2f".format(distanceKm)
            fieldInputs = fieldInputs + ("distance" to formatted)
        }
    }

    // Build pages for the current exercise (filtered by active participant)
    val isMultiParticipant = state.participants.size > 1
    val pages = remember(selectedExerciseIndex, state.exercises, state.activeParticipantId) {
        val exercise = state.exercises.getOrNull(selectedExerciseIndex) ?: return@remember listOf(SetPage(1, isEndPage = true))
        val participantRecords = exercise.setRecords.filter { it.participantId == state.activeParticipantId }
        val completedNums = participantRecords.map { it.setNumber }.toSet()
        val setPages = (1..exercise.targetSets).map { setNum ->
            SetPage(setNum, isCompleted = setNum in completedNums)
        }
        setPages + SetPage(exercise.targetSets + 1, isEndPage = true)
    }

    // Clamp page index when pages change
    if (currentPageIndex >= pages.size) {
        currentPageIndex = (pages.size - 1).coerceAtLeast(0)
    }

    // Load field values for a set page (filtered by active participant)
    fun loadPageInputs(page: SetPage) {
        if (page.isEndPage) return
        val exercise = state.exercises.getOrNull(selectedExerciseIndex) ?: return
        val participantRecords = exercise.setRecords.filter { it.participantId == state.activeParticipantId }

        // Priority 1: Current session record for this set
        val currentRecord = participantRecords.find { it.setNumber == page.setNumber }
        if (currentRecord != null && currentRecord.fieldValues.isNotEmpty()) {
            fieldInputs = currentRecord.fieldValues
            return
        } else if (currentRecord != null) {
            // Backward compat: reconstruct from legacy fields
            fieldInputs = mapOf("weight" to currentRecord.weight.toString(), "reps" to currentRecord.reps.toString())
            return
        }

        // Priority 2: Carry forward from previous set in current session
        if (page.setNumber > 1) {
            val prevRecord = participantRecords.find { it.setNumber == page.setNumber - 1 }
            if (prevRecord != null && prevRecord.fieldValues.isNotEmpty()) {
                fieldInputs = prevRecord.fieldValues
                return
            } else if (prevRecord != null) {
                fieldInputs = mapOf("weight" to prevRecord.weight.toString(), "reps" to prevRecord.reps.toString())
                return
            }
        }

        // Priority 3: Target values from template (filter out _prefixed meta-keys)
        val targets = exercise.targetValues?.filterKeys { !it.startsWith("_") }
        if (targets != null && targets.isNotEmpty()) {
            fieldInputs = targets
            return
        }

        // Priority 4: Defaults based on recording fields
        fieldInputs = exercise.recordingFields.associate { field ->
            field.key to when (field.key) {
                "weight" -> "0"
                "reps" -> "10"
                else -> ""
            }
        }
    }

    fun navigatePage(delta: Int) {
        val newIndex = currentPageIndex + delta
        if (newIndex < 0 && selectedExerciseIndex > 0) {
            // Swipe right past first set → go to previous exercise's end page
            swipeDirection = delta
            selectedExerciseIndex--
            val prevExercise = state.exercises[selectedExerciseIndex]
            currentPageIndex = prevExercise.targetSets // end page index
        } else {
            val clamped = newIndex.coerceIn(0, pages.size - 1)
            if (clamped != currentPageIndex) {
                swipeDirection = delta
                currentPageIndex = clamped
                // Don't call loadPageInputs here — let the LaunchedEffect handle it
                // to avoid reading stale state after onCompleteSet
            }
        }
    }

    fun selectExercise(index: Int) {
        selectedExerciseIndex = index
        val exercise = state.exercises[index]
        val participantRecords = exercise.setRecords.filter { it.participantId == state.activeParticipantId }
        val completedNums = participantRecords.map { it.setNumber }.toSet()
        val firstPending = (1..exercise.targetSets).firstOrNull { it !in completedNums }
        currentPageIndex = if (firstPending != null) firstPending - 1 else 0
        // loadPageInputs will be triggered by the LaunchedEffect reacting to index changes
    }

    // Initialize inputs on first composition
    LaunchedEffect(Unit) {
        if (state.exercises.isNotEmpty()) {
            selectExercise(selectedExerciseIndex.coerceIn(0, state.exercises.size - 1))
        }
    }

    // Save/restore drafts when switching participants
    LaunchedEffect(state.activeParticipantId) {
        if (state.activeParticipantId != previousParticipantId) {
            // Save outgoing participant's current inputs as draft
            participantDrafts[previousParticipantId] = fieldInputs
            previousParticipantId = state.activeParticipantId

            // Restore incoming participant's draft if available, otherwise auto-fill
            val draft = participantDrafts[state.activeParticipantId]
            if (draft != null) {
                fieldInputs = draft
            } else {
                pages.getOrNull(currentPageIndex)?.let { loadPageInputs(it) }
            }
        }
    }

    // Reload inputs when page/exercise changes or setRecords update
    LaunchedEffect(
        selectedExerciseIndex,
        currentPageIndex,
        state.exercises.getOrNull(selectedExerciseIndex)?.setRecords
    ) {
        // Clear drafts when exercise or set page changes (drafts are per-context)
        participantDrafts.clear()
        // Reset stopwatch on page/exercise change
        stopwatchRunningKey = null
        stopwatchSeconds = 0
        // Reset live GPS tracking on page/exercise change
        locationTracker.reset()
        // Clear last completed path only when exercise changes
        if (selectedExerciseIndex != lastCompletedExerciseIndex) {
            lastCompletedGpsPath = emptyList()
            lastCompletedDistanceKm = 0.0
        }
        pages.getOrNull(currentPageIndex)?.let { loadPageInputs(it) }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val navBarBottomDp = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val imeBottomDp = with(density) { WindowInsets.ime.getBottom(this).toDp() }
    val gpsCanvasExtra = if (hasDistanceField) 132.dp else 0.dp
    val sheetPeekHeight = if (selectedExercise != null) {
        val baseHeight = if (isMultiParticipant) 280.dp else 230.dp
        val imeAdjustment = (imeBottomDp - navBarBottomDp).coerceAtLeast(0.dp)
        baseHeight + gpsCanvasExtra + navBarBottomDp + imeAdjustment
    } else 0.dp

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetContainerColor = MaterialTheme.colorScheme.primary,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetDragHandle = if (selectedExercise != null) {
            {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )
            }
        } else null,
        sheetContent = {
            if (selectedExercise != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .pointerInput(pages.size) {
                            detectHorizontalDragGestures(
                                onDragStart = { accumulatedDrag = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    accumulatedDrag += dragAmount
                                },
                                onDragEnd = {
                                    val threshold = 100f
                                    if (accumulatedDrag < -threshold) {
                                        navigatePage(1)
                                    } else if (accumulatedDrag > threshold) {
                                        navigatePage(-1)
                                    }
                                    accumulatedDrag = 0f
                                },
                                onDragCancel = { accumulatedDrag = 0f }
                            )
                        }
                ) {
                    val currentPage = pages.getOrNull(currentPageIndex)

                    // Static header: exercise name + dots + set info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppTheme.spacing.lg),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedExercise.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                pages.forEachIndexed { dotIndex, page ->
                                    val isCurrent = dotIndex == currentPageIndex
                                    val dotColor = when {
                                        isCurrent -> MaterialTheme.colorScheme.onPrimary
                                        page.isCompleted -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                        else -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(if (isCurrent) 8.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                }
                            }
                        }
                        if (currentPage != null && !currentPage.isEndPage) {
                            Text(
                                text = "Set ${currentPage.setNumber}/${selectedExercise.targetSets}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Participant chips (only shown in multi-participant mode)
                    if (isMultiParticipant) {
                        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppTheme.spacing.lg),
                            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                        ) {
                            state.participants.forEach { participant ->
                                val isActive = participant.id == state.activeParticipantId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isActive) Color.Black
                                            else Color.Black.copy(alpha = 0.15f)
                                        )
                                        .clickable { onSwitchParticipant(participant.id) }
                                        .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.xs)
                                ) {
                                    Text(
                                        text = participant.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isActive) Color.White
                                        else MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))

                    // Animated content: inputs/buttons slide on swipe
                    var inputPageHeightPx by remember { mutableIntStateOf(0) }

                    val pageKey = if (currentPage?.isEndPage == true) {
                        "end-$selectedExerciseIndex"
                    } else {
                        "set-$selectedExerciseIndex-${currentPage?.setNumber}"
                    }

                    AnimatedContent(
                        targetState = pageKey,
                        modifier = Modifier.then(
                            if (inputPageHeightPx > 0)
                                Modifier.heightIn(min = with(density) { inputPageHeightPx.toDp() })
                            else Modifier
                        ),
                        transitionSpec = {
                            val slide = if (swipeDirection > 0) {
                                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                            } else {
                                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                            }
                            slide using SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> snap() })
                        },
                        label = "setPageTransition"
                    ) { _ ->
                        if (currentPage?.isEndPage == true) {
                            // End page: all sets done
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppTheme.spacing.lg),
                                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                            ) {
                                // Show last completed GPS path on end page
                                if (hasDistanceField && lastCompletedGpsPath.isNotEmpty()) {
                                    GpsPathCanvas(
                                        points = lastCompletedGpsPath,
                                        distanceKm = lastCompletedDistanceKm,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "All sets done",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                                ) {
                                    Button(
                                        onClick = { onAddSet(selectedExercise.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(2.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Black.copy(alpha = 0.15f),
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        contentPadding = PaddingValues(horizontal = AppTheme.spacing.md)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(AppTheme.spacing.xs))
                                        Text(
                                            text = "Add Set",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }

                                    val isLastExercise = selectedExerciseIndex >= state.exercises.size - 1

                                    Button(
                                        onClick = {
                                            if (isLastExercise) {
                                                activeSheet = SheetType.FINISH_CONFIRM
                                            } else {
                                                selectExercise(selectedExerciseIndex + 1)
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(2.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Black,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = AppTheme.spacing.md)
                                    ) {
                                        Text(
                                            text = if (isLastExercise) "Finish Workout" else "Next Exercise",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        if (!isLastExercise) {
                                            Spacer(modifier = Modifier.width(AppTheme.spacing.xs))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (currentPage != null) {
                            // Normal set page: dynamic input fields + complete button
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppTheme.spacing.lg)
                                    .onSizeChanged { inputPageHeightPx = it.height },
                                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                            ) {
                                // Check if any field is a duration (stopwatch) type
                                val hasDurationField = selectedExercise.recordingFields.any { it.type == "duration" }
                                val nonDurationFields = selectedExercise.recordingFields.filter { it.type != "duration" }
                                val durationField = selectedExercise.recordingFields.find { it.type == "duration" }

                                // GPS Path Canvas (only for distance exercises)
                                if (hasDistanceField) {
                                    // Show live GPS points if tracking, otherwise show last completed path
                                    val displayPoints = if (gpsPoints.isNotEmpty()) gpsPoints else lastCompletedGpsPath
                                    val displayDistance = if (gpsPoints.isNotEmpty()) gpsDistanceMeters / 1000.0 else lastCompletedDistanceKm
                                    GpsPathCanvas(
                                        points = displayPoints,
                                        distanceKm = displayDistance,
                                        isTracking = isGpsTracking,
                                        placeholderText = "Start set to track route",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Stopwatch for duration fields (shown above the other inputs)
                                if (durationField != null) {
                                    StopwatchInput(
                                        elapsedSeconds = stopwatchSeconds,
                                        isRunning = stopwatchRunningKey == durationField.key,
                                        onToggle = {
                                            if (stopwatchRunningKey == durationField.key) {
                                                // Stop: write final value
                                                fieldInputs = fieldInputs + (durationField.key to stopwatchSeconds.toString())
                                                stopwatchRunningKey = null
                                                // Stop GPS tracking
                                                if (hasDistanceField) {
                                                    locationTracker.stopTracking()
                                                }
                                            } else {
                                                // Start
                                                stopwatchRunningKey = durationField.key
                                                // Start GPS tracking
                                                if (hasDistanceField) {
                                                    locationTracker.checkPermission()
                                                    if (hasLocationPermission) {
                                                        locationTracker.startTracking()
                                                    } else {
                                                        showPermissionRequest = true
                                                    }
                                                }
                                            }
                                        },
                                        onReset = {
                                            stopwatchRunningKey = null
                                            stopwatchSeconds = 0
                                            fieldInputs = fieldInputs + (durationField.key to "")
                                            // Reset GPS tracking
                                            if (hasDistanceField) {
                                                locationTracker.reset()
                                            }
                                        },
                                        label = if (durationField.unit.isNotEmpty()) {
                                            "${durationField.label} (${durationField.unit})"
                                        } else {
                                            durationField.label
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Non-duration fields in a row
                                if (nonDurationFields.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        nonDurationFields.forEach { field ->
                                            val label = if (field.unit.isNotEmpty()) {
                                                "${field.label} (${field.unit})"
                                            } else {
                                                field.label
                                            }
                                            val keyboardType = when (field.type) {
                                                "decimal" -> KeyboardType.Decimal
                                                else -> KeyboardType.Number
                                            }
                                            OutlinedTextField(
                                                value = fieldInputs[field.key] ?: "",
                                                onValueChange = { value ->
                                                    fieldInputs = fieldInputs + (field.key to value)
                                                },
                                                label = { Text(label) },
                                                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                                    focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                                    focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                    unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                                    cursorColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }
                                }

                                val canComplete = selectedExercise.recordingFields.all { field ->
                                    !field.required || fieldInputs[field.key]?.isNotBlank() == true
                                }

                                Button(
                                    enabled = canComplete,
                                    onClick = {
                                        // Auto-stop stopwatch if running
                                        if (stopwatchRunningKey != null) {
                                            fieldInputs = fieldInputs + (stopwatchRunningKey!! to stopwatchSeconds.toString())
                                            stopwatchRunningKey = null
                                        }

                                        // Stop GPS tracking and store path data
                                        if (hasDistanceField) {
                                            locationTracker.stopTracking()
                                            if (gpsPoints.isNotEmpty()) {
                                                val pathJson = gpsPoints.joinToString(";") { "${it.latitude},${it.longitude}" }
                                                fieldInputs = fieldInputs + ("_gpsPath" to pathJson)
                                                // Preserve path for display after set completion
                                                lastCompletedGpsPath = gpsPoints.toList()
                                                lastCompletedDistanceKm = gpsDistanceMeters / 1000.0
                                                lastCompletedExerciseIndex = selectedExerciseIndex
                                            }
                                        }

                                        onCompleteSet(selectedExercise.id, currentPage.setNumber, fieldInputs)

                                        // Clear draft for completing participant (values are now in records)
                                        participantDrafts.remove(state.activeParticipantId)

                                        if (isMultiParticipant) {
                                            // In multi-participant mode: cycle through participants for this set,
                                            // only advance to next set when all participants have completed it
                                            val setNum = currentPage.setNumber
                                            // After this completion, this participant will have done this set.
                                            // Build the set of participant IDs who have already completed this set
                                            // (including the one we just completed).
                                            val alreadyCompleted = selectedExercise.setRecords
                                                .filter { it.setNumber == setNum }
                                                .map { it.participantId }
                                                .toSet() + state.activeParticipantId
                                            val pendingParticipants = state.participants
                                                .filter { it.id !in alreadyCompleted }

                                            if (pendingParticipants.isNotEmpty()) {
                                                // Switch to the next participant who hasn't done this set
                                                onSwitchParticipant(pendingParticipants.first().id)
                                            } else {
                                                // All participants done for this set — advance to next set
                                                // and switch back to the first participant
                                                onSwitchParticipant(state.participants.first().id)
                                                if (currentPageIndex < pages.size - 1) {
                                                    navigatePage(1)
                                                }
                                            }
                                        } else {
                                            // Solo mode: auto-advance to next page
                                            if (currentPageIndex < pages.size - 1) {
                                                navigatePage(1)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Black,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(
                                        horizontal = AppTheme.spacing.lg,
                                        vertical = AppTheme.spacing.md
                                    )
                                ) {
                                    Text(
                                        text = if (currentPage.isCompleted) "Update Set" else "Complete Set",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Rest timer section: revealed when sheet is swiped up
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))

                    RestTimerSection(
                        isTimerActive = state.isRestTimerActive,
                        remainingSeconds = state.restTimerRemaining,
                        totalDurationSeconds = state.restTimerDuration,
                        onDurationChange = onRestTimerDurationChange,
                        onStart = onRestTimerStart,
                        onStop = onRestTimerStop,
                        onReset = onRestTimerReset,
                        onAdjustRunning = onRestTimerAdjust
                    )

                    Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Session Header
            SessionHeader(
                workoutName = state.sessionName,
                onNameChange = onRenameWorkout,
                elapsedTime = state.elapsedSeconds,
                completedExercises = state.exercises.count { it.completedSets == it.targetSets },
                totalExercises = state.exercises.size,
                onMoreClick = { activeSheet = SheetType.OPTIONS }
            )

            // Top pane: scrollable exercise list
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                state.exercises.forEachIndexed { index, exercise ->
                    key(exercise.id) {
                    val isSelected = index == selectedExerciseIndex
                    val completedCount = exercise.completedSets
                    val isBeingDragged = index == draggedIndex

                    // Shift calculation for non-dragged items
                    val targetOffsetY = if (draggedIndex != -1 && !isBeingDragged && hoverIndex != -1) {
                        when {
                            draggedIndex < index && hoverIndex >= index -> -totalItemHeightPx
                            draggedIndex > index && hoverIndex <= index -> totalItemHeightPx
                            else -> 0f
                        }
                    } else 0f

                    val animatedOffsetY by animateFloatAsState(
                        targetValue = targetOffsetY,
                        animationSpec = if (draggedIndex != -1) spring(dampingRatio = 0.8f, stiffness = 300f) else snap(),
                        label = "itemShift"
                    )

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                exerciseToRemove = exercise
                                false
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isBeingDragged) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (isBeingDragged) dragOffset else animatedOffsetY
                                shadowElevation = if (isBeingDragged) 8f else 0f
                                shape = RoundedCornerShape(12.dp)
                                clip = false
                                scaleX = if (isBeingDragged) 1.02f else 1f
                                scaleY = if (isBeingDragged) 1.02f else 1f
                            },
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = state.exercises.size > 1,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.error),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.padding(end = AppTheme.spacing.lg)
                                )
                            }
                        }
                    ) {
                        BaseCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(index) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedIndex = index
                                            dragOffset = 0f
                                            hoverIndex = index
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount.y
                                            val draggedPositions = (dragOffset / totalItemHeightPx).toInt()
                                            hoverIndex = (draggedIndex + draggedPositions)
                                                .coerceIn(0, state.exercises.size - 1)
                                        },
                                        onDragEnd = {
                                            val targetIndex = hoverIndex
                                            if (targetIndex != draggedIndex && targetIndex >= 0) {
                                                onReorderExercise(draggedIndex, targetIndex)
                                                // Adjust selectedExerciseIndex to follow the selected exercise
                                                if (selectedExerciseIndex == draggedIndex) {
                                                    selectedExerciseIndex = targetIndex
                                                } else if (draggedIndex < selectedExerciseIndex && targetIndex >= selectedExerciseIndex) {
                                                    selectedExerciseIndex--
                                                } else if (draggedIndex > selectedExerciseIndex && targetIndex <= selectedExerciseIndex) {
                                                    selectedExerciseIndex++
                                                }
                                            }
                                            draggedIndex = -1
                                            dragOffset = 0f
                                            hoverIndex = -1
                                        },
                                        onDragCancel = {
                                            draggedIndex = -1
                                            dragOffset = 0f
                                            hoverIndex = -1
                                        }
                                    )
                                },
                            onClick = { selectExercise(index) },
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else null
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exercise.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = exercise.muscleGroup,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (!isMultiParticipant) {
                                        Text(
                                            text = "$completedCount/${exercise.targetSets}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (completedCount == exercise.targetSets)
                                                AppTheme.colors.primaryText
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Compact completed sets summary
                                if (exercise.setRecords.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                                    if (isMultiParticipant) {
                                        // Per-participant progress rows
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                                        ) {
                                            state.participants.forEach { participant ->
                                                val participantRecords = exercise.setRecords
                                                    .filter { it.participantId == participant.id }
                                                    .sortedBy { it.setNumber }
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
                                                ) {
                                                    Text(
                                                        text = "${participant.name} · ${participantRecords.size}/${exercise.targetSets}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (participantRecords.isNotEmpty()) {
                                                        FlowRow(
                                                            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                                                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
                                                        ) {
                                                            participantRecords.forEach { set ->
                                                                Box(
                                                                    modifier = Modifier
                                                                        .background(MaterialTheme.colorScheme.background)
                                                                        .padding(
                                                                            horizontal = AppTheme.spacing.xs,
                                                                            vertical = 2.dp
                                                                        )
                                                                ) {
                                                                    Text(
                                                                        text = formatSetRecord(set, state.exercises.getOrNull(selectedExerciseIndex)),
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
                                        ) {
                                            exercise.setRecords.sortedBy { it.setNumber }.forEach { set ->
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.background)
                                                        .padding(
                                                            horizontal = AppTheme.spacing.sm,
                                                            vertical = 4.dp
                                                        )
                                                ) {
                                                    Text(
                                                        text = "S${set.setNumber} ${formatSetRecord(set, exercise)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    } // key
                }

                // Add Exercise text button
                Text(
                    text = "Add Exercise",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeSheet = SheetType.ADD_EXERCISE }
                        .padding(vertical = AppTheme.spacing.md),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
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
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
                    )

                    SecondaryButton(
                        text = "Cancel Workout",
                        onClick = {
                            activeSheet = null
                            onCancelWorkout()
                        },
                        fullWidth = true,
                        destructive = true
                    )

                    SecondaryButton(
                        text = "Dismiss",
                        onClick = { activeSheet = null },
                        fullWidth = true
                    )
                }
            }
            SheetType.ADD_EXERCISE -> {
                var selectedExercises by remember { mutableStateOf(setOf<String>()) }
                val isReplacing = replacingExerciseId != null

                val pickerExercises = state.availableExercises.ifEmpty { getMockLibraryExercises() }

                if (isReplacing) {
                    SimpleExercisePickerContent(
                        exercises = pickerExercises,
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
                    Column {
                        MultiSelectExercisePickerForWorkout(
                            exercises = pickerExercises,
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
                            fontWeight = FontWeight.Bold,
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
                                exerciseToRemove = exercise
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

    // Location permission request
    if (showPermissionRequest) {
        RequestLocationPermission { granted ->
            showPermissionRequest = false
            locationTracker.checkPermission()
            if (granted) {
                locationTracker.startTracking()
            }
        }
    }

    // Remove exercise confirmation dialog
    exerciseToRemove?.let { exercise ->
        AlertDialog(
            onDismissRequest = { exerciseToRemove = null },
            title = { Text("Remove Exercise") },
            text = {
                Text("Remove ${exercise.name} from this workout? Any recorded sets will be lost.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveExercise(exercise.id)
                        exerciseToRemove = null
                    }
                ) {
                    Text(
                        "Remove",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Session Header component showing editable workout name, elapsed time, and progress
 */
@Composable
private fun SessionHeader(
    workoutName: String,
    onNameChange: (String) -> Unit,
    elapsedTime: Int,
    completedExercises: Int,
    totalExercises: Int,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingName by remember { mutableStateOf(workoutName) }

    // Sync when external state changes
    LaunchedEffect(workoutName) {
        editingName = workoutName
    }

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
                BasicTextField(
                    value = editingName,
                    onValueChange = {
                        editingName = it
                        onNameChange(it.trim())
                    },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && editingName != workoutName) {
                                val trimmed = editingName.trim()
                                if (trimmed.isNotEmpty()) {
                                    onNameChange(trimmed)
                                } else {
                                    editingName = workoutName
                                }
                            }
                        }
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time: ${formatTime(elapsedTime)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$completedExercises/$totalExercises exercises",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
        elapsedSeconds = 300,
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
