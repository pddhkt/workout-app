package com.workout.app.ui.components.dataviz

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import com.workout.app.ui.theme.Success
import com.workout.app.ui.theme.Warning

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
 * @param days List of HeatmapDay data in chronological order (oldest to newest)
 * @param modifier Optional modifier for customization
 * @param cellSize Size of each heatmap cell
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
    cellSize: Dp = 12.dp,
    cellSpacing: Dp = 2.dp,
    lowIntensityColor: Color = Warning.copy(alpha = 0.3f),
    mediumIntensityColor: Color = Success.copy(alpha = 0.6f),
    highIntensityColor: Color = Success,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showDayLabels: Boolean = true,
    title: String? = null
) {
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

        // GitHub-style heatmap: days as rows, weeks as columns
        val weeksCount = (days.size + 6) / 7 // Ceiling division for weeks
        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(cellSpacing)
        ) {
            // Day labels column (left side)
            if (showDayLabels) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(cellSpacing),
                    horizontalAlignment = Alignment.End
                ) {
                    dayLabels.forEach { label ->
                        Box(
                            modifier = Modifier.height(cellSize),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = AppTheme.spacing.xs)
                            )
                        }
                    }
                }
            }

            // Heatmap grid - each week is a column
            repeat(weeksCount) { weekIndex ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(cellSpacing)
                ) {
                    repeat(7) { dayIndex ->
                        val dataIndex = weekIndex * 7 + dayIndex
                        if (dataIndex < days.size) {
                            val day = days[dataIndex]
                            HeatmapCell(
                                count = day.count,
                                size = cellSize,
                                lowIntensityColor = lowIntensityColor,
                                mediumIntensityColor = mediumIntensityColor,
                                highIntensityColor = highIntensityColor,
                                emptyColor = emptyColor
                            )
                        } else {
                            // Empty placeholder for incomplete last week
                            Spacer(modifier = Modifier.height(cellSize).width(cellSize))
                        }
                    }
                }
            }
        }

        // Legend
        HeatmapLegend(
            emptyColor = emptyColor,
            lowIntensityColor = lowIntensityColor,
            mediumIntensityColor = mediumIntensityColor,
            highIntensityColor = highIntensityColor
        )
    }
}

/**
 * Individual cell in the heatmap grid
 * Color intensity is determined by workout count
 *
 * @param count Number of workouts
 * @param size Size of the cell
 * @param lowIntensityColor Color for 1 workout
 * @param mediumIntensityColor Color for 2-3 workouts
 * @param highIntensityColor Color for 4+ workouts
 * @param emptyColor Color for 0 workouts
 */
@Composable
private fun HeatmapCell(
    count: Int,
    size: Dp,
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
            .width(size)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(AppTheme.spacing.xs))
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
    highIntensityColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Less",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    .clip(RoundedCornerShape(AppTheme.spacing.xs))
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.width(AppTheme.spacing.xs))

        Text(
            text = "More",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Compact version of ConsistencyHeatmap without labels and legend
 * Useful for dashboard cards or preview displays
 * Uses GitHub-style layout (days as rows, weeks as columns)
 *
 * @param days List of HeatmapDay data in chronological order
 * @param modifier Optional modifier for customization
 * @param cellSize Size of each cell
 * @param cellSpacing Spacing between cells
 */
@Composable
fun CompactConsistencyHeatmap(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier,
    cellSize: Dp = 10.dp,
    cellSpacing: Dp = 2.dp
) {
    val weeksCount = (days.size + 6) / 7

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(cellSpacing)
    ) {
        repeat(weeksCount) { weekIndex ->
            Column(
                verticalArrangement = Arrangement.spacedBy(cellSpacing)
            ) {
                repeat(7) { dayIndex ->
                    val dataIndex = weekIndex * 7 + dayIndex
                    if (dataIndex < days.size) {
                        val day = days[dataIndex]
                        HeatmapCell(
                            count = day.count,
                            size = cellSize,
                            lowIntensityColor = Warning.copy(alpha = 0.3f),
                            mediumIntensityColor = Success.copy(alpha = 0.6f),
                            highIntensityColor = Success,
                            emptyColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.height(cellSize).width(cellSize))
                    }
                }
            }
        }
    }
}
