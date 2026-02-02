package com.workout.app

import android.app.Application
import com.workout.app.data.DatabaseSeeder
import com.workout.app.di.sharedModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(sharedModules)
        }

        // Seed database with sample data if empty
        val seeder: DatabaseSeeder by inject()
        applicationScope.launch(Dispatchers.IO) {
            seeder.seedIfEmpty()
        }
    }
}
