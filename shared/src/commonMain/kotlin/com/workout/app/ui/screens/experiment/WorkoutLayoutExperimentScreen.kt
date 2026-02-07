package com.workout.app.ui.screens.experiment

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
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.workout.app.ui.components.buttons.AppIconButton
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.components.exercise.ExercisePickerContent
import com.workout.app.ui.components.overlays.M3BottomSheet
import com.workout.app.ui.theme.AppTheme

private data class MockExercise(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val targetSets: Int,
    val reps: String,
    val previousSessionSets: List<CompletedSet> = emptyList()
)

private data class CompletedSet(
    val setNumber: Int,
    val weight: String,
    val reps: String
)

private val mockExercises = listOf(
    MockExercise("1", "Bench Press", "Chest", 4, "8-12", listOf(
        CompletedSet(1, "80", "10"), CompletedSet(2, "80", "8"),
        CompletedSet(3, "75", "8"), CompletedSet(4, "75", "7")
    )),
    MockExercise("2", "Incline Dumbbell Press", "Chest", 3, "10-12", listOf(
        CompletedSet(1, "30", "12"), CompletedSet(2, "30", "10"),
        CompletedSet(3, "28", "10")
    )),
    MockExercise("3", "Cable Fly", "Chest", 3, "12-15", listOf(
        CompletedSet(1, "15", "15"), CompletedSet(2, "15", "12"),
        CompletedSet(3, "12.5", "12")
    )),
    MockExercise("4", "Overhead Press", "Shoulders", 4, "8-10", listOf(
        CompletedSet(1, "50", "10"), CompletedSet(2, "50", "8"),
        CompletedSet(3, "45", "8"), CompletedSet(4, "45", "7")
    )),
    MockExercise("5", "Lateral Raise", "Shoulders", 3, "12-15", listOf(
        CompletedSet(1, "10", "15"), CompletedSet(2, "10", "12"),
        CompletedSet(3, "10", "12")
    )),
    MockExercise("6", "Tricep Pushdown", "Arms", 3, "10-12", listOf(
        CompletedSet(1, "25", "12"), CompletedSet(2, "25", "10"),
        CompletedSet(3, "22.5", "10")
    )),
    MockExercise("7", "Skull Crushers", "Arms", 3, "10-12", listOf(
        CompletedSet(1, "20", "12"), CompletedSet(2, "20", "10"),
        CompletedSet(3, "17.5", "10")
    ))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLayoutExperimentScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Mutable exercise list for reorder/delete
    val exercises = remember { mutableStateListOf(*mockExercises.toTypedArray()) }

    // Track completed sets per exercise
    val completedSets = remember {
        mutableStateListOf<Pair<String, CompletedSet>>()
    }

    // Track current set count per exercise (can grow with "Add Set")
    val exerciseSetCounts = remember {
        mutableStateListOf<Pair<String, Int>>().apply {
            mockExercises.forEach { add(it.id to it.targetSets) }
        }
    }

    fun getSetCount(exerciseId: String): Int =
        exerciseSetCounts.firstOrNull { it.first == exerciseId }?.second
            ?: exercises.find { it.id == exerciseId }?.targetSets ?: 3

    fun getCompletedSets(exerciseId: String): List<CompletedSet> =
        completedSets.filter { it.first == exerciseId }.map { it.second }

    // Current exercise and page tracking
    var selectedExerciseIndex by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    val initialSet = exercises.first().previousSessionSets.find { it.setNumber == 1 }
    var weightInput by remember { mutableStateOf(initialSet?.weight ?: "0") }
    var repsInput by remember { mutableStateOf(initialSet?.reps ?: "10") }

    // Track swipe direction for animation
    var swipeDirection by remember { mutableIntStateOf(1) } // 1 = forward, -1 = backward

    // Clamp selectedExerciseIndex if exercises were removed
    if (exercises.isNotEmpty() && selectedExerciseIndex >= exercises.size) {
        selectedExerciseIndex = exercises.size - 1
    }

    val selectedExercise = exercises[selectedExerciseIndex]

    // Build pages for the current exercise — ALL sets (completed + pending) + end page
    data class SetPage(
        val setNumber: Int,
        val isEndPage: Boolean = false,
        val isCompleted: Boolean = false
    )

    val pages = remember(
        selectedExerciseIndex,
        completedSets.toList(),
        exerciseSetCounts.toList()
    ) {
        val exercise = exercises[selectedExerciseIndex]
        val completedNums = getCompletedSets(exercise.id).map { it.setNumber }.toSet()
        val total = getSetCount(exercise.id)
        val setPages = (1..total).map { setNum ->
            SetPage(setNum, isCompleted = setNum in completedNums)
        }
        setPages + SetPage(total + 1, isEndPage = true)
    }

    // Clamp page index when pages change
    if (currentPageIndex >= pages.size) {
        currentPageIndex = (pages.size - 1).coerceAtLeast(0)
    }

    // Load weight/reps for the current page using 4-tier priority:
    // 1. Current session record for this set
    // 2. Previous session record for same set number
    // 3. Carry forward from previous set in current session (set N-1)
    // 4. Defaults: first previous session set, or "0"/"10"
    fun loadPageInputs(page: SetPage) {
        if (page.isEndPage) return
        val exercise = exercises[selectedExerciseIndex]

        // Priority 1: Current session record
        val currentRecord = getCompletedSets(exercise.id).find { it.setNumber == page.setNumber }
        if (currentRecord != null) {
            weightInput = currentRecord.weight
            repsInput = currentRecord.reps
            return
        }

        // Priority 2: Previous session record for same set number
        val prevSessionRecord = exercise.previousSessionSets.find { it.setNumber == page.setNumber }
        if (prevSessionRecord != null) {
            weightInput = prevSessionRecord.weight
            repsInput = prevSessionRecord.reps
            return
        }

        // Priority 3: Carry forward from previous set in current session
        if (page.setNumber > 1) {
            val prevSetRecord = getCompletedSets(exercise.id).find { it.setNumber == page.setNumber - 1 }
            if (prevSetRecord != null) {
                weightInput = prevSetRecord.weight
                repsInput = prevSetRecord.reps
                return
            }
        }

        // Priority 4: Defaults — last previous session set, or fallback
        val lastPrevSet = exercise.previousSessionSets.lastOrNull()
        weightInput = lastPrevSet?.weight ?: "0"
        repsInput = lastPrevSet?.reps ?: "10"
    }

    fun navigatePage(delta: Int) {
        val newIndex = currentPageIndex + delta
        if (newIndex < 0 && selectedExerciseIndex > 0) {
            // Swipe right past first set → go to previous exercise's last page (end page)
            swipeDirection = delta
            selectedExerciseIndex--
            val prevExercise = exercises[selectedExerciseIndex]
            val total = getSetCount(prevExercise.id)
            currentPageIndex = total // end page index
        } else {
            val clamped = newIndex.coerceIn(0, pages.size - 1)
            if (clamped != currentPageIndex) {
                swipeDirection = delta
                currentPageIndex = clamped
                val page = pages[clamped]
                loadPageInputs(page)
            }
        }
    }

    fun selectExercise(index: Int) {
        selectedExerciseIndex = index
        // Jump to the first pending set, or page 0 if all done
        val exercise = exercises[index]
        val completedNums = getCompletedSets(exercise.id).map { it.setNumber }.toSet()
        val total = getSetCount(exercise.id)
        val firstPending = (1..total).firstOrNull { it !in completedNums }
        currentPageIndex = if (firstPending != null) firstPending - 1 else 0
        // Load inputs using priority chain
        val newPages = (1..total).map { setNum ->
            SetPage(setNum, isCompleted = setNum in completedNums)
        } + SetPage(total + 1, isEndPage = true)
        newPages.getOrNull(currentPageIndex)?.let { loadPageInputs(it) }
    }

    fun saveCurrentSet() {
        val page = pages.getOrNull(currentPageIndex) ?: return
        if (page.isEndPage) return
        // Remove existing record for this set if updating
        val existingIndex = completedSets.indexOfFirst {
            it.first == selectedExercise.id && it.second.setNumber == page.setNumber
        }
        if (existingIndex >= 0) {
            completedSets.removeAt(existingIndex)
        }
        completedSets.add(
            selectedExercise.id to CompletedSet(
                setNumber = page.setNumber,
                weight = weightInput,
                reps = repsInput
            )
        )
        // Auto-advance to next set if available
        if (currentPageIndex < pages.size - 2) { // -2 because last is end page
            navigatePage(1)
        }
    }

    fun addSetToExercise() {
        val idx = exerciseSetCounts.indexOfFirst { it.first == selectedExercise.id }
        if (idx >= 0) {
            val current = exerciseSetCounts[idx].second
            exerciseSetCounts[idx] = selectedExercise.id to (current + 1)
        }
    }

    fun goToNextExercise() {
        if (selectedExerciseIndex < exercises.size - 1) {
            selectExercise(selectedExerciseIndex + 1)
        }
    }

    fun deleteExercise(index: Int) {
        if (exercises.size <= 1) return // Don't delete the last exercise
        val exercise = exercises[index]
        exercises.removeAt(index)
        // Clean up related state
        completedSets.removeAll { it.first == exercise.id }
        exerciseSetCounts.removeAll { it.first == exercise.id }
        // Adjust selectedExerciseIndex
        if (selectedExerciseIndex >= exercises.size) {
            selectedExerciseIndex = exercises.size - 1
        } else if (index < selectedExerciseIndex) {
            selectedExerciseIndex--
        } else if (index == selectedExerciseIndex) {
            // Re-select same index (now the next exercise) or clamp
            selectedExerciseIndex = selectedExerciseIndex.coerceAtMost(exercises.size - 1)
        }
        selectExercise(selectedExerciseIndex)
    }

    // Bottom sheet state
    var showExercisePicker by remember { mutableStateOf(false) }

    // Swipe threshold for bottom section page navigation
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    // Drag-to-reorder state
    val density = LocalDensity.current
    val itemHeightDp = 72.dp // estimated card height
    val itemSpacingDp = AppTheme.spacing.md
    val itemHeightPx = with(density) { itemHeightDp.toPx() }
    val itemSpacingPx = with(density) { itemSpacingDp.toPx() }
    val totalItemHeightPx = itemHeightPx + itemSpacingPx

    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var hoverIndex by remember { mutableIntStateOf(-1) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Layout Experiment",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    AppIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBackClick
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top section: scrollable exercise list
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                exercises.forEachIndexed { index, exercise ->
                    val isSelected = index == selectedExerciseIndex
                    val completed = getCompletedSets(exercise.id)
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
                                deleteExercise(index)
                                false // Return false since we handle removal ourselves
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
                                scaleX = if (isBeingDragged) 1.02f else 1f
                                scaleY = if (isBeingDragged) 1.02f else 1f
                            },
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = exercises.size > 1,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(2.dp))
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
                                                .coerceIn(0, exercises.size - 1)
                                        },
                                        onDragEnd = {
                                            val targetIndex = hoverIndex
                                            if (targetIndex != draggedIndex && targetIndex >= 0) {
                                                val item = exercises.removeAt(draggedIndex)
                                                exercises.add(targetIndex, item)
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
                                    Text(
                                        text = "${completed.size}/${getSetCount(exercise.id)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (completed.size == getSetCount(exercise.id))
                                            AppTheme.colors.primaryText
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Compact completed sets
                                if (completed.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                                    ) {
                                        completed.sortedBy { it.setNumber }.forEach { set ->
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.background)
                                                    .padding(
                                                        horizontal = AppTheme.spacing.sm,
                                                        vertical = 4.dp
                                                    )
                                            ) {
                                                Text(
                                                    text = "S${set.setNumber} ${set.weight}kg x${set.reps}",
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

                Text(
                    text = "Add Exercise",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showExercisePicker = true }
                        .padding(vertical = AppTheme.spacing.md),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = { /* TODO: finish workout */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Finish Workout",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            }

            // Bottom section: swipeable input area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                    .padding(top = AppTheme.spacing.lg)
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
                                    navigatePage(1)  // swipe left → next
                                } else if (accumulatedDrag > threshold) {
                                    navigatePage(-1) // swipe right → prev
                                }
                                accumulatedDrag = 0f
                            },
                            onDragCancel = { accumulatedDrag = 0f }
                        )
                    }
            ) {
                val currentPage = pages.getOrNull(currentPageIndex)

                // Static header: name + dots + set info (does not animate on swipe)
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
                            text = "Set ${currentPage.setNumber}/${getSetCount(selectedExercise.id)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppTheme.spacing.md))

                // Animated content: only inputs/buttons slide on swipe
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppTheme.spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                        ) {
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
                                    onClick = { addSetToExercise() },
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

                                Button(
                                    onClick = { goToNextExercise() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Black,
                                        contentColor = Color.White
                                    ),
                                    enabled = selectedExerciseIndex < exercises.size - 1,
                                    contentPadding = PaddingValues(horizontal = AppTheme.spacing.md)
                                ) {
                                    Text(
                                        text = "Next Exercise",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Spacer(modifier = Modifier.width(AppTheme.spacing.xs))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else if (currentPage != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppTheme.spacing.lg)
                                .onSizeChanged { inputPageHeightPx = it.height },
                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = weightInput,
                                    onValueChange = { weightInput = it },
                                    label = { Text("Weight (kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                                OutlinedTextField(
                                    value = repsInput,
                                    onValueChange = { repsInput = it },
                                    label = { Text("Reps") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                            Button(
                                onClick = { saveCurrentSet() },
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

                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
            }
        }
    }

    M3BottomSheet(
        visible = showExercisePicker,
        onDismiss = { showExercisePicker = false },
        skipPartiallyExpanded = true,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        ExercisePickerContent(
            onExerciseSelected = { showExercisePicker = false }
        )
    }
}
