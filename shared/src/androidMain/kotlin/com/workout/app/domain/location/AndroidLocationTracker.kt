package com.workout.app.domain.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.workout.app.domain.model.GpsPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidLocationTracker(
    private val context: Context
) : LocationTracker {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val _points = MutableStateFlow<List<GpsPoint>>(emptyList())
    override val points: StateFlow<List<GpsPoint>> = _points.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0.0)
    override val distanceMeters: StateFlow<Double> = _distanceMeters.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    override val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        2000L
    ).setMinUpdateDistanceMeters(3f).build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                val point = GpsPoint(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    timestamp = System.currentTimeMillis()
                )
                val updated = _points.value + point
                _points.value = updated
                _distanceMeters.value = DistanceCalculator.totalDistance(updated)
            }
        }
    }

    override fun startTracking() {
        checkPermission()
        if (!_hasPermission.value) return

        _isTracking.value = true
        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            _isTracking.value = false
        }
    }

    override fun stopTracking() {
        _isTracking.value = false
        fusedClient.removeLocationUpdates(locationCallback)
    }

    override fun reset() {
        stopTracking()
        _points.value = emptyList()
        _distanceMeters.value = 0.0
    }

    override fun checkPermission() {
        _hasPermission.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
