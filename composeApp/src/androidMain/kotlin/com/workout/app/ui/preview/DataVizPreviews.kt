package com.workout.app.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.dataviz.CircularTimer
import com.workout.app.ui.components.dataviz.CompactCircularTimer
import com.workout.app.ui.components.dataviz.CompactConsistencyHeatmap
import com.workout.app.ui.components.dataviz.ConsistencyHeatmap
import com.workout.app.ui.components.dataviz.HeatmapDay
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Android Studio preview for CircularTimer variants.
 */
@Preview(name = "Circular Timer", showBackground = true)
@Composable
private fun CircularTimerPreview() {
    WorkoutAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Standard Timer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                var timerSeconds by remember { mutableStateOf(90) }
                CircularTimer(
                    remainingSeconds = timerSeconds,
                    totalSeconds = 120,
                    onAddSeconds = { timerSeconds = (timerSeconds + 15).coerceAtMost(120) },
                    onSubtractSeconds = { timerSeconds = (timerSeconds - 15).coerceAtLeast(0) }
                )
            }
        }
    }
}

/**
 * Android Studio preview for CompactCircularTimer variants.
 */
@Preview(name = "Compact Circular Timers", showBackground = true)
@Composable
private fun CompactCircularTimerPreview() {
    WorkoutAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Compact Timers",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompactCircularTimer(
                        remainingSeconds = 45,
                        totalSeconds = 60
                    )

                    CompactCircularTimer(
                        remainingSeconds = 180,
                        totalSeconds = 300
                    )

                    CompactCircularTimer(
                        remainingSeconds = 5,
                        totalSeconds = 120
                    )
                }
            }
        }
    }
}

/**
 * Android Studio preview for ConsistencyHeatmap.
 */
@Preview(name = "Consistency Heatmap", showBackground = true)
@Composable
private fun ConsistencyHeatmapPreview() {
    WorkoutAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sample data for 28 days (4 weeks)
                val sampleDays = remember {
                    List(28) { index ->
                        HeatmapDay(
                            day = index + 1,
                            count = when {
                                index % 7 == 6 -> 0 // Sundays off
                                index < 7 -> (0..2).random()
                                index < 14 -> (1..3).random()
                                index < 21 -> (2..4).random()
                                else -> (0..3).random()
                            }
                        )
                    }
                }

                ConsistencyHeatmap(
                    days = sampleDays,
                    title = "Last 4 Weeks",
                    showDayLabels = true
                )
            }
        }
    }
}

/**
 * Android Studio preview for CompactConsistencyHeatmap.
 */
@Preview(name = "Compact Consistency Heatmap", showBackground = true)
@Composable
private fun CompactConsistencyHeatmapPreview() {
    WorkoutAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Compact Heatmap",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                val sampleDays = remember {
                    List(28) { index ->
                        HeatmapDay(day = index + 1, count = (0..4).random())
                    }
                }

                CompactConsistencyHeatmap(days = sampleDays)
            }
        }
    }
}

/**
 * Android Studio preview showing all dataviz components together.
 */
@Preview(name = "All Data Viz Components", showBackground = true, heightDp = 1200)
@Composable
private fun AllDataVizPreview() {
    WorkoutAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xl)
            ) {
                Text(
                    text = "Data Visualization Components",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                // CircularTimer
                Text(
                    text = "Circular Timer",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                var timerSeconds by remember { mutableStateOf(75) }
                CircularTimer(
                    remainingSeconds = timerSeconds,
                    totalSeconds = 120,
                    onAddSeconds = { timerSeconds = (timerSeconds + 15).coerceAtMost(120) },
                    onSubtractSeconds = { timerSeconds = (timerSeconds - 15).coerceAtLeast(0) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompactCircularTimer(remainingSeconds = 45, totalSeconds = 60)
                    CompactCircularTimer(remainingSeconds = 180, totalSeconds = 300)
                    CompactCircularTimer(remainingSeconds = 5, totalSeconds = 120)
                }

                // ConsistencyHeatmap
                Text(
                    text = "Consistency Heatmap",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                val sampleDays = remember {
                    List(28) { index ->
                        HeatmapDay(
                            day = index + 1,
                            count = when {
                                index % 7 == 6 -> 0
                                index < 7 -> (0..2).random()
                                index < 14 -> (1..3).random()
                                index < 21 -> (2..4).random()
                                else -> (0..3).random()
                            }
                        )
                    }
                }

                ConsistencyHeatmap(
                    days = sampleDays,
                    title = "Last 4 Weeks",
                    showDayLabels = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Compact Variant",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CompactConsistencyHeatmap(days = sampleDays)
            }
        }
    }
}
