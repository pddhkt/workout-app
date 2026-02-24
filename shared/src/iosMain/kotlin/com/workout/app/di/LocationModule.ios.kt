package com.workout.app.di

import com.workout.app.domain.location.IosLocationTracker
import com.workout.app.domain.location.LocationTracker

actual fun createLocationTracker(): LocationTracker {
    return IosLocationTracker()
}
