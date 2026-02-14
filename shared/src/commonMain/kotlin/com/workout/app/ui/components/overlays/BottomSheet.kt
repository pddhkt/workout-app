package com.workout.app.ui.components.overlays

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Bottom sheet overlay component with drag-to-dismiss and scrim background.
 * Based on mockup element EL-23.
 *
 * Features:
 * - Smooth slide-up animation on show
 * - Drag-to-dismiss gesture handling
 * - Semi-transparent scrim that dismisses on tap
 * - Customizable content slot
 *
 * @param visible Whether the bottom sheet is visible
 * @param onDismiss Callback invoked when sheet is dismissed (via drag, scrim tap, or programmatic)
 * @param modifier Modifier to be applied to the container
 * @param scrimColor Semi-transparent overlay color. Defaults to OnSurface with 60% opacity
 * @param sheetColor Background color of the bottom sheet. Defaults to Material3 surface color
 * @param dragDismissThreshold Percentage of sheet height that triggers dismiss when dragged. Range 0.0-1.0
 * @param content Bottom sheet content composable
 */
@Composable
fun BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    scrimColor: Color = MaterialTheme.colorScheme.scrim,
    sheetColor: Color = MaterialTheme.colorScheme.surface,
    dragDismissThreshold: Float = 0.3f,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Track drag offset
    var dragOffset by remember { mutableStateOf(0f) }
    var sheetHeight by remember { mutableStateOf(0) }

    // Scrim alpha animation
    val scrimAlpha = remember { Animatable(0f) }

    // Sheet offset animation
    val sheetOffsetY = remember { Animatable(0f) }

    // Animate in on appear
    LaunchedEffect(visible) {
        launch {
            scrimAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300)
            )
        }
        launch {
            sheetOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // Handle dismiss animation
    val dismiss = {
        scope.launch {
            launch {
                scrimAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200)
                )
            }
            launch {
                sheetOffsetY.animateTo(
                    targetValue = if (sheetHeight > 0) sheetHeight.toFloat() else 1000f,
                    animationSpec = tween(durationMillis = 200)
                )
            }.invokeOnCompletion {
                onDismiss()
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Scrim background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor.copy(alpha = scrimAlpha.value))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            // Tap detected (no significant drag)
                            if (dragOffset < 10f) {
                                dismiss()
                            }
                        },
                        onDragCancel = { },
                        onVerticalDrag = { change, dragAmount ->
                            dragOffset += dragAmount
                        }
                    )
                }
        )

        // Bottom sheet content
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = 0,
                        y = (sheetOffsetY.value + dragOffset.coerceAtLeast(0f)).roundToInt()
                    )
                }
                .clip(
                    RoundedCornerShape(
                        topStart = AppTheme.spacing.lg,
                        topEnd = AppTheme.spacing.lg
                    )
                )
                .background(sheetColor)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            dragOffset = 0f
                        },
                        onDragEnd = {
                            // Check if drag exceeds dismiss threshold
                            val dismissThreshold = sheetHeight * dragDismissThreshold
                            if (dragOffset > dismissThreshold) {
                                dismiss()
                            } else {
                                // Snap back to original position
                                scope.launch {
                                    // Animate dragOffset back to 0
                                    val animatable = Animatable(dragOffset)
                                    animatable.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                    dragOffset = 0f
                                }
                            }
                        },
                        onDragCancel = {
                            dragOffset = 0f
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            // Only allow dragging down
                            if (dragAmount > 0 || dragOffset > 0) {
                                dragOffset = (dragOffset + dragAmount).coerceAtLeast(0f)
                            }
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacing.lg)
                    .let {
                        // Capture height for drag calculations
                        it.then(
                            Modifier.pointerInput(Unit) {
                                // Update sheet height when layout completes
                                sheetHeight = size.height
                            }
                        )
                    }
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = AppTheme.spacing.md)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        .fillMaxWidth(0.15f)
                        .padding(vertical = 2.dp)
                )

                content()
            }
        }
    }
}
