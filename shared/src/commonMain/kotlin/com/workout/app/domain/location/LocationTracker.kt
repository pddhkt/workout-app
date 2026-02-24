package com.workout.app.domain.location

import com.workout.app.domain.model.GpsPoint
import kotlinx.coroutines.flow.StateFlow

interface LocationTracker {
    val points: StateFlow<List<GpsPoint>>
    val isTracking: StateFlow<Boolean>
    val distanceMeters: StateFlow<Double>
    val hasPermission: StateFlow<Boolean>

    fun startTracking()
    fun stopTracking()
    fun reset()
    fun checkPermission()
}
