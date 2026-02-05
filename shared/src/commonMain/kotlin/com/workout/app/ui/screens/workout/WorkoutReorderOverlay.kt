package com.workout.app.ui.screens.workout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.workout.app.presentation.workout.WorkoutExercise
import com.workout.app.ui.components.exercise.ExerciseReorderItem
import com.workout.app.ui.theme.AppTheme

/**
 * Full-screen overlay for reordering exercises.
 * Shows all exercises as compact items in a regular Column (not LazyColumn)
 * to ensure all items are visible and drag can complete.
 *
 * @param exercises List of exercises to reorder
 * @param onReorder Callback when exercises are reordered (fromIndex, toIndex)
 * @param onDismiss Callback to dismiss the overlay (cancel)
 */
@Composable
fun WorkoutReorderOverlay(
    exercises: List<WorkoutExercise>,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val itemHeightDp = 52.dp
    val itemSpacingDp = 8.dp
    val itemHeightPx = with(density) { itemHeightDp.toPx() }
    val itemSpacingPx = with(density) { itemSpacingDp.toPx() }
    val totalItemHeightPx = itemHeightPx + itemSpacingPx

    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var hoverIndex by remember { mutableIntStateOf(-1) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.lg)
                .padding(top = 48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(enabled = false) { } // Prevent click-through
                .padding(AppTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Reorder Exercises",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

            Text(
                text = "Drag exercises to reorder",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

            // Exercise list (regular Column, scrollable if needed)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(itemSpacingDp)
            ) {
                exercises.forEachIndexed { index, exercise ->
                    val isBeingDragged = draggedIndex == index

                    // Calculate shift for non-dragged items
                    val targetOffsetY = if (draggedIndex != -1 && !isBeingDragged && hoverIndex != -1) {
                        when {
                            // Dragged from above, item needs to shift up
                            draggedIndex < index && hoverIndex >= index -> -totalItemHeightPx
                            // Dragged from below, item needs to shift down
                            draggedIndex > index && hoverIndex <= index -> totalItemHeightPx
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
                            .zIndex(if (isBeingDragged) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (isBeingDragged) dragOffset else animatedOffsetY
                                shadowElevation = if (isBeingDragged) 8f else 0f
                                scaleX = if (isBeingDragged) 1.02f else 1f
                                scaleY = if (isBeingDragged) 1.02f else 1f
                            }
                            .pointerInput(index, exercises.size) {
                                detectDragGestures(
                                    onDragStart = {
                                        draggedIndex = index
                                        dragOffset = 0f
                                        hoverIndex = index
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y

                                        // Calculate hover index
                                        val draggedPositions = (dragOffset / totalItemHeightPx).toInt()
                                        hoverIndex = (draggedIndex + draggedPositions)
                                            .coerceIn(0, exercises.size - 1)
                                    },
                                    onDragEnd = {
                                        val targetIndex = hoverIndex
                                        if (targetIndex != draggedIndex && targetIndex >= 0) {
                                            onReorder(draggedIndex, targetIndex)
                                        }
                                        // Reset and auto-exit
                                        draggedIndex = -1
                                        dragOffset = 0f
                                        hoverIndex = -1
                                        onDismiss()
                                    },
                                    onDragCancel = {
                                        draggedIndex = -1
                                        dragOffset = 0f
                                        hoverIndex = -1
                                    }
                                )
                            }
                    ) {
                        ExerciseReorderItem(
                            exerciseName = exercise.name,
                            index = index + 1,
                            isBeingDragged = isBeingDragged
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

            // Cancel button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onDismiss)
                    .padding(AppTheme.spacing.md),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
