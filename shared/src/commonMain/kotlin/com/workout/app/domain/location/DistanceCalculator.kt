package com.workout.app.domain.location

import com.workout.app.domain.model.GpsPoint
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    private fun toRadians(deg: Double): Double = deg * PI / 180.0

    fun distanceBetween(p1: GpsPoint, p2: GpsPoint): Double {
        val dLat = toRadians(p2.latitude - p1.latitude)
        val dLon = toRadians(p2.longitude - p1.longitude)
        val lat1 = toRadians(p1.latitude)
        val lat2 = toRadians(p2.latitude)

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return EARTH_RADIUS_METERS * c
    }

    fun totalDistance(points: List<GpsPoint>): Double {
        if (points.size < 2) return 0.0
        return points.zipWithNext().sumOf { (a, b) -> distanceBetween(a, b) }
    }
}
