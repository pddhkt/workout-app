package com.workout.app.ui.components.timer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workout.app.ui.components.dataviz.CompactCircularTimer
import com.workout.app.ui.theme.AppTheme

private enum class TimerViewState {
    IDLE, SETUP, RUNNING
}

/**
 * Inline rest timer section that lives inside the workout bottom sheet.
 * Manages its own view state (idle pill, setup, running) internally.
 */
@Composable
fun RestTimerSection(
    isTimerActive: Boolean,
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    onDurationChange: (Int) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    onAdjustRunning: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewState by remember { mutableStateOf(TimerViewState.IDLE) }

    // When timer finishes (was active, now inactive and remaining is 0), go to idle
    LaunchedEffect(isTimerActive) {
        if (!isTimerActive && viewState == TimerViewState.RUNNING) {
            viewState = TimerViewState.IDLE
        }
    }

    // When timer starts externally (e.g., auto-start after completing a set),
    // show running state if we're currently idle
    LaunchedEffect(isTimerActive) {
        if (isTimerActive && viewState == TimerViewState.IDLE) {
            viewState = TimerViewState.RUNNING
        }
    }

    AnimatedContent(
        targetState = viewState,
        modifier = modifier.fillMaxWidth(),
        transitionSpec = {
            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
        },
        label = "timerViewState"
    ) { state ->
        when (state) {
            TimerViewState.IDLE -> {
                TimerIdlePill(
                    isRunning = isTimerActive,
                    remainingSeconds = remainingSeconds,
                    totalDurationSeconds = totalDurationSeconds,
                    onClick = {
                        viewState = if (isTimerActive) {
                            TimerViewState.RUNNING
                        } else {
                            TimerViewState.SETUP
                        }
                    }
                )
            }

            TimerViewState.SETUP -> {
                TimerSetupView(
                    durationSeconds = totalDurationSeconds,
                    onDurationChange = onDurationChange,
                    onStart = {
                        onStart()
                        viewState = TimerViewState.RUNNING
                    },
                    onBack = { viewState = TimerViewState.IDLE }
                )
            }

            TimerViewState.RUNNING -> {
                TimerRunningView(
                    remainingSeconds = remainingSeconds,
                    totalSeconds = totalDurationSeconds,
                    onAdjust = onAdjustRunning,
                    onStop = {
                        onStop()
                        viewState = TimerViewState.IDLE
                    },
                    onReset = {
                        onReset()
                        viewState = TimerViewState.SETUP
                    },
                    onMinimize = { viewState = TimerViewState.IDLE }
                )
            }
        }
    }
}

/**
 * State 1 & 1b: Compact pill showing timer status.
 * When idle: shows default duration. When running (minimized): shows live countdown with pulsing border.
 */
@Composable
private fun TimerIdlePill(
    isRunning: Boolean,
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    onClick: () -> Unit
) {
    val displaySeconds = if (isRunning) remainingSeconds else totalDurationSeconds

    val pillShape = RoundedCornerShape(24.dp)

    val borderModifier = if (isRunning) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulsingBorder")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "borderAlpha"
        )
        Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = alpha),
            shape = pillShape
        )
    } else {
        Modifier
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(pillShape)
                .then(borderModifier)
                .background(Color.Black, pillShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
            Text(
                text = "Rest \u00B7 ${formatTimerTime(displaySeconds)}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * State 2: Timer setup view with ring preview, duration adjustment, and start button.
 */
@Composable
private fun TimerSetupView(
    durationSeconds: Int,
    onDurationChange: (Int) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with back arrow
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
            Text(
                text = "Rest Timer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Ring with time display — show full ring (static preview)
        Box(contentAlignment = Alignment.Center) {
            CompactCircularTimer(
                remainingSeconds = durationSeconds,
                totalSeconds = durationSeconds,
                size = 160.dp,
                strokeWidth = 10.dp,
                backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                progressColor = MaterialTheme.colorScheme.onPrimary,
                showTime = false
            )
            Text(
                text = formatTimerTime(durationSeconds),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Duration adjust buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Button(
                onClick = { onDurationChange(-30) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = durationSeconds > 30
            ) {
                Text(
                    text = "\u221230s",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Button(
                onClick = { onDurationChange(30) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = durationSeconds < 600
            ) {
                Text(
                    text = "+30s",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        // Start button
        Button(
            onClick = onStart,
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
                text = "Start",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
    }
}

/**
 * State 3: Running timer view with animated ring, fine-tune buttons, and stop/reset controls.
 */
@Composable
private fun TimerRunningView(
    remainingSeconds: Int,
    totalSeconds: Int,
    onAdjust: (Int) -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    onMinimize: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with minimize chevron
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMinimize,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Minimize",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
            Text(
                text = "Rest Timer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Animated ring with countdown
        Box(contentAlignment = Alignment.Center) {
            CompactCircularTimer(
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds,
                size = 160.dp,
                strokeWidth = 10.dp,
                backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                progressColor = MaterialTheme.colorScheme.onPrimary,
                showTime = false
            )
            Text(
                text = formatTimerTime(remainingSeconds),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Fine-tune adjust buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Button(
                onClick = { onAdjust(-10) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = remainingSeconds > 0
            ) {
                Text(
                    text = "\u221210s",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Button(
                onClick = { onAdjust(10) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "+10s",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        // Reset + Stop buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Button(
                onClick = onReset,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Reset",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Button(
                onClick = onStop,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Stop",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
    }
}

private fun formatTimerTime(seconds: Int): String {
    val absSeconds = seconds.coerceAtLeast(0)
    val minutes = absSeconds / 60
    val secs = absSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}
