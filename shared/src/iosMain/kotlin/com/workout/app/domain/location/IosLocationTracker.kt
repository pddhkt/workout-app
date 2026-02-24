package com.workout.app.domain.location

import com.workout.app.domain.model.GpsPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IosLocationTracker : LocationTracker {
    override val points: StateFlow<List<GpsPoint>> = MutableStateFlow(emptyList())
    override val isTracking: StateFlow<Boolean> = MutableStateFlow(false)
    override val distanceMeters: StateFlow<Double> = MutableStateFlow(0.0)
    override val hasPermission: StateFlow<Boolean> = MutableStateFlow(false)

    override fun startTracking() {}
    override fun stopTracking() {}
    override fun reset() {}
    override fun checkPermission() {}
}
