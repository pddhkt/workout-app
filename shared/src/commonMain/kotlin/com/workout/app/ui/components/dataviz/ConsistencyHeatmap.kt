package com.workout.app.ui.components.dataviz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme

/**
 * Data class representing a day's workout data for the heatmap
 *
 * @param day Day number or identifier
 * @param count Number of workouts completed on this day
 */
data class HeatmapDay(
    val day: Int,
    val count: Int
)

/**
 * ConsistencyHeatmap component showing workout frequency over time
 * Based on mockup element EL-21
 *
 * Displays a GitHub-style heatmap grid where days are rows and weeks are columns.
 * Color intensity represents workout frequency. Time flows left-to-right with
 * the most recent day at the bottom-right corner.
 *
 * The grid automatically fills the available width, showing empty cells for weeks
 * without data. Data is right-aligned so the most recent entries appear on the right.
 *
 * @param days List of HeatmapDay data in chronological order (oldest to newest)
 * @param modifier Optional modifier for customization
 * @param cellSpacing Spacing between cells
 * @param lowIntensityColor Color for low workout count
 * @param mediumIntensityColor Color for medium workout count
 * @param highIntensityColor Color for high workout count
 * @param emptyColor Color for days with no workouts
 * @param showDayLabels Whether to show day labels (M, T, W, etc.)
 * @param title Optional title text above the heatmap
 */
@Composable
fun ConsistencyHeatmap(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier,
    cellSpacing: Dp = 2.dp,
    lowIntensityColor: Color? = null,
    mediumIntensityColor: Color? = null,
    highIntensityColor: Color? = null,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    legendTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    dayLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showDayLabels: Boolean = true,
    title: String? = null
) {
    val colors = AppTheme.colors
    val resolvedLowIntensityColor = lowIntensityColor ?: colors.success.copy(alpha = 0.3f)
    val resolvedMediumIntensityColor = mediumIntensityColor ?: colors.success.copy(alpha = 0.6f)
    val resolvedHighIntensityColor = highIntensityColor ?: colors.success
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        // Optional title
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        val dayLabelWidth = 20.dp // Approximate width for day labels

        // Use BoxWithConstraints to calculate how many weeks fit
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val availableWidth = maxWidth

            // Calculate cell size and weeks count to fill width
            // Formula: availableWidth = labelWidth + (weeksCount * cellSize) + ((weeksCount - 1) * spacing)
            // Rearranged: weeksCount = (availableWidth - labelWidth + spacing) / (cellSize + spacing)
            val labelSpace = if (showDayLabels) dayLabelWidth + cellSpacing else 0.dp
            val gridWidth = availableWidth - labelSpace

            // Target around 16dp cells, calculate how many weeks fit
            val targetCellSize = 16.dp
            val weeksCount = ((gridWidth + cellSpacing) / (targetCellSize + cellSpacing)).toInt().coerceAtLeast(1)

            // Calculate actual cell size for alignment
            val cellSize = (gridWidth - (cellSpacing * (weeksCount - 1))) / weeksCount

            // Data weeks (ceiling division)
            val dataWeeksCount = (days.size + 6) / 7

            // Calculate offset to right-align data (empty weeks on left, data on right)
            val emptyWeeksCount = (weeksCount - dataWeeksCount).coerceAtLeast(0)

            Row(
                horizontalArrangement = Arrangement.spacedBy(cellSpacing)
            ) {
                // Day labels column (left side) - fixed width, no weight
                if (showDayLabels) {
                    Column(
                        modifier = Modifier.width(dayLabelWidth),
                        verticalArrangement = Arrangement.spacedBy(cellSpacing),
                        horizontalAlignment = Alignment.End
                    ) {
                        dayLabels.forEach { label ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cellSize),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = dayLabelColor
                                )
                            }
                        }
                    }
                }

                // Heatmap grid - each week is a column with equal weight
                repeat(weeksCount) { weekIndex ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(cellSpacing)
                    ) {
                        repeat(7) { dayIndex ->
                            // Calculate data index with right-alignment offset
                            val dataWeekIndex = weekIndex - emptyWeeksCount
                            val dataIndex = dataWeekIndex * 7 + dayIndex

                            if (dataWeekIndex >= 0 && dataIndex >= 0 && dataIndex < days.size) {
                                val day = days[dataIndex]
                                HeatmapCell(
                                    count = day.count,
                                    lowIntensityColor = resolvedLowIntensityColor,
                                    mediumIntensityColor = resolvedMediumIntensityColor,
                                    highIntensityColor = resolvedHighIntensityColor,
                                    emptyColor = emptyColor
                                )
                            } else {
                                // Empty cell (no data for this position)
                                HeatmapCell(
                                    count = 0,
                                    lowIntensityColor = resolvedLowIntensityColor,
                                    mediumIntensityColor = resolvedMediumIntensityColor,
                                    highIntensityColor = resolvedHighIntensityColor,
                                    emptyColor = emptyColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Legend
        HeatmapLegend(
            emptyColor = emptyColor,
            lowIntensityColor = resolvedLowIntensityColor,
            mediumIntensityColor = resolvedMediumIntensityColor,
            highIntensityColor = resolvedHighIntensityColor,
            textColor = legendTextColor
        )
    }
}

/**
 * Individual cell in the heatmap grid
 * Color intensity is determined by workout count
 *
 * @param count Number of workouts
 * @param lowIntensityColor Color for 1 workout
 * @param mediumIntensityColor Color for 2-3 workouts
 * @param highIntensityColor Color for 4+ workouts
 * @param emptyColor Color for 0 workouts
 */
@Composable
private fun HeatmapCell(
    count: Int,
    lowIntensityColor: Color,
    mediumIntensityColor: Color,
    highIntensityColor: Color,
    emptyColor: Color
) {
    val cellColor = when {
        count == 0 -> emptyColor
        count == 1 -> lowIntensityColor
        count in 2..3 -> mediumIntensityColor
        else -> highIntensityColor
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(2.dp))
            .background(cellColor)
    )
}

/**
 * Legend explaining the color scale
 *
 * @param emptyColor Color for no workouts
 * @param lowIntensityColor Color for low count
 * @param mediumIntensityColor Color for medium count
 * @param highIntensityColor Color for high count
 */
@Composable
private fun HeatmapLegend(
    emptyColor: Color,
    lowIntensityColor: Color,
    mediumIntensityColor: Color,
    highIntensityColor: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Less",
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )

        Spacer(modifier = Modifier.width(AppTheme.spacing.xs))

        // Color scale boxes
        val colors = listOf(
            emptyColor,
            lowIntensityColor,
            mediumIntensityColor,
            highIntensityColor
        )

        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.width(AppTheme.spacing.xs))

        Text(
            text = "More",
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * Compact version of ConsistencyHeatmap without labels and legend
 * Useful for dashboard cards or preview displays
 * Uses GitHub-style layout (days as rows, weeks as columns)
 * Automatically fills available width with empty cells for weeks without data.
 *
 * @param days List of HeatmapDay data in chronological order
 * @param modifier Optional modifier for customization
 * @param cellSpacing Spacing between cells
 */
@Composable
fun CompactConsistencyHeatmap(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier,
    cellSpacing: Dp = 2.dp
) {
    val colors = AppTheme.colors
    val lowIntensityColor = colors.success.copy(alpha = 0.3f)
    val mediumIntensityColor = colors.success.copy(alpha = 0.6f)
    val highIntensityColor = colors.success
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val availableWidth = maxWidth

        // Target around 10dp cells, calculate how many weeks fit
        val targetCellSize = 10.dp
        val weeksCount = ((availableWidth + cellSpacing) / (targetCellSize + cellSpacing)).toInt().coerceAtLeast(1)

        // Data weeks (ceiling division)
        val dataWeeksCount = (days.size + 6) / 7

        // Calculate offset to right-align data (empty weeks on left, data on right)
        val emptyWeeksCount = (weeksCount - dataWeeksCount).coerceAtLeast(0)

        Row(
            horizontalArrangement = Arrangement.spacedBy(cellSpacing)
        ) {
            repeat(weeksCount) { weekIndex ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(cellSpacing)
                ) {
                    repeat(7) { dayIndex ->
                        // Calculate data index with right-alignment offset
                        val dataWeekIndex = weekIndex - emptyWeeksCount
                        val dataIndex = dataWeekIndex * 7 + dayIndex

                        if (dataWeekIndex >= 0 && dataIndex >= 0 && dataIndex < days.size) {
                            val day = days[dataIndex]
                            HeatmapCell(
                                count = day.count,
                                lowIntensityColor = lowIntensityColor,
                                mediumIntensityColor = mediumIntensityColor,
                                highIntensityColor = highIntensityColor,
                                emptyColor = emptyColor
                            )
                        } else {
                            // Empty cell (no data for this position)
                            HeatmapCell(
                                count = 0,
                                lowIntensityColor = lowIntensityColor,
                                mediumIntensityColor = mediumIntensityColor,
                                highIntensityColor = highIntensityColor,
                                emptyColor = emptyColor
                            )
                        }
                    }
                }
            }
        }
    }
}
