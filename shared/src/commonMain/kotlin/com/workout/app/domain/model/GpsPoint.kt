package com.workout.app.domain.model

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = 0L
)
