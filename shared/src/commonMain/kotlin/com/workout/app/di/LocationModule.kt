package com.workout.app.di

import com.workout.app.domain.location.LocationTracker
import org.koin.dsl.module

val locationModule = module {
    single<LocationTracker> { createLocationTracker() }
}

expect fun createLocationTracker(): LocationTracker
