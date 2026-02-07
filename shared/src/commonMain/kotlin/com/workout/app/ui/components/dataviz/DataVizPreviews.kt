package com.workout.app.ui.components.dataviz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview composable showcasing all data visualization components
 * Can be used as a reference or demo screen
 */
@Composable
fun DataVizShowcase() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xl)
        ) {
            Text(
                text = "Data Visualization Components",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            HorizontalDivider()

            // CircularTimer section
            SectionTitle("Circular Timer")

            var timerSeconds by remember { mutableStateOf(90) }
            val totalSeconds = 120

            CircularTimer(
                remainingSeconds = timerSeconds,
                totalSeconds = totalSeconds,
                onAddSeconds = { timerSeconds = (timerSeconds + 15).coerceAtMost(totalSeconds) },
                onSubtractSeconds = { timerSeconds = (timerSeconds - 15).coerceAtLeast(0) }
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            Text(
                text = "Compact Variants",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
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

            HorizontalDivider()

            // ConsistencyHeatmap section
            SectionTitle("Consistency Heatmap")

            // Sample data for 28 days (4 weeks)
            val sampleDays = remember {
                List(28) { index ->
                    HeatmapDay(
                        day = index + 1,
                        count = when {
                            index % 7 == 6 -> 0 // Sundays off
                            index < 7 -> (0..2).random() // First week ramping up
                            index < 14 -> (1..3).random() // Second week consistent
                            index < 21 -> (2..4).random() // Third week peak
                            else -> (0..3).random() // Fourth week varied
                        }
                    )
                }
            }

            ConsistencyHeatmap(
                days = sampleDays,
                title = "Last 4 Weeks",
                showDayLabels = true
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            Text(
                text = "Compact Variant",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CompactConsistencyHeatmap(
                days = sampleDays
            )

            // Different intensity examples
            HorizontalDivider()

            SectionTitle("Intensity Examples")

            Text(
                text = "High Consistency",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val highConsistency = List(28) { HeatmapDay(it + 1, (2..4).random()) }
            CompactConsistencyHeatmap(days = highConsistency)

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            Text(
                text = "Low Consistency",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val lowConsistency = List(28) { HeatmapDay(it + 1, if (it % 3 == 0) 1 else 0) }
            CompactConsistencyHeatmap(days = lowConsistency)

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            Text(
                text = "Mixed Consistency",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val mixedConsistency = List(28) { HeatmapDay(it + 1, (0..4).random()) }
            CompactConsistencyHeatmap(days = mixedConsistency)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = AppTheme.colors.primaryText
    )
}
