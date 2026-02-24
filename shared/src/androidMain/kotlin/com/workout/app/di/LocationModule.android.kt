package com.workout.app.di

import android.content.Context
import com.workout.app.domain.location.AndroidLocationTracker
import com.workout.app.domain.location.LocationTracker
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun createLocationTracker(): LocationTracker {
    return AndroidLocationTrackerFactory().create()
}

class AndroidLocationTrackerFactory : KoinComponent {
    fun create(): LocationTracker {
        val context = get<Context>()
        return AndroidLocationTracker(context)
    }
}
