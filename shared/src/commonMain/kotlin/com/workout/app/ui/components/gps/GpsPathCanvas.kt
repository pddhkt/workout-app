package com.workout.app.ui.components.gps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.GpsPoint
import com.workout.app.domain.location.DistanceCalculator
import kotlin.math.PI
import kotlin.math.cos

/**
 * Draws a GPS path as a polyline on a small canvas.
 *
 * @param points List of GPS coordinates to draw
 * @param distanceKm Total distance in km (if null, calculated from points)
 * @param isTracking Whether GPS is actively tracking (controls placeholder text)
 * @param placeholderText Custom placeholder when no points; null hides placeholder entirely
 * @param height Canvas height
 * @param pathColor Color of the polyline
 * @param backgroundColor Background color of the canvas
 */
@Composable
fun GpsPathCanvas(
    points: List<GpsPoint>,
    distanceKm: Double? = null,
    isTracking: Boolean = false,
    placeholderText: String? = null,
    height: Dp = 120.dp,
    pathColor: Color = Color.White,
    backgroundColor: Color = Color.Black.copy(alpha = 0.1f),
    modifier: Modifier = Modifier
) {
    val effectiveDistance = distanceKm
        ?: (DistanceCalculator.totalDistance(points) / 1000.0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
    ) {
        if (points.size >= 2) {
            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                val minLat = points.minOf { it.latitude }
                val maxLat = points.maxOf { it.latitude }
                val minLng = points.minOf { it.longitude }
                val maxLng = points.maxOf { it.longitude }

                val latRange = (maxLat - minLat).coerceAtLeast(0.0001)
                val lngRange = (maxLng - minLng).coerceAtLeast(0.0001)

                val latMid = (minLat + maxLat) / 2.0
                val lngCorrectionFactor = cos(latMid * PI / 180.0)

                val effectiveLngRange = lngRange * lngCorrectionFactor
                val scale = maxOf(latRange, effectiveLngRange)

                fun toCanvasOffset(point: GpsPoint): Offset {
                    val x = ((point.longitude - minLng) * lngCorrectionFactor / scale) * size.width
                    val y = ((maxLat - point.latitude) / scale) * size.height
                    return Offset(x.toFloat(), y.toFloat())
                }

                val path = Path()
                val first = toCanvasOffset(points.first())
                path.moveTo(first.x, first.y)
                for (i in 1 until points.size) {
                    val p = toCanvasOffset(points[i])
                    path.lineTo(p.x, p.y)
                }

                drawPath(
                    path = path,
                    color = pathColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Start dot (green)
                drawCircle(
                    color = Color(0xFF22C55E),
                    radius = 5.dp.toPx(),
                    center = first
                )

                // End dot
                val last = toCanvasOffset(points.last())
                drawCircle(
                    color = pathColor,
                    radius = 5.dp.toPx(),
                    center = last
                )
            }

            // Distance overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${"%.2f".format(effectiveDistance)} km",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else if (placeholderText != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isTracking) "Waiting for GPS..." else placeholderText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Parse a serialized GPS path string back to GpsPoint list.
 * Format: "lat,lng;lat,lng;..."
 */
fun parseGpsPath(pathStr: String): List<GpsPoint> {
    return pathStr.split(";").mapNotNull { segment ->
        val parts = segment.split(",")
        if (parts.size == 2) {
            val lat = parts[0].toDoubleOrNull() ?: return@mapNotNull null
            val lng = parts[1].toDoubleOrNull() ?: return@mapNotNull null
            GpsPoint(lat, lng)
        } else null
    }
}
