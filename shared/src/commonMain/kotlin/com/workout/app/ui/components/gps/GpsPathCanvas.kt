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
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.GpsPoint
import kotlin.math.PI
import kotlin.math.cos

@Composable
fun GpsPathCanvas(
    points: List<GpsPoint>,
    distanceKm: Double,
    isTracking: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.1f))
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
                    color = Color.White,
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

                // End dot (white - current position)
                val last = toCanvasOffset(points.last())
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = last
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isTracking) "Waiting for GPS..." else "Start set to track route",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            }
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
                text = "${"%.2f".format(distanceKm)} km",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
