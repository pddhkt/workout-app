package com.workout.app.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.workout.app.database.WorkoutDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Android-specific database driver implementation.
 */
actual fun createDatabaseDriver(): SqlDriver {
    return AndroidDatabaseDriverFactory().createDriver()
}

/**
 * Factory for creating Android SQLDelight driver.
 */
class AndroidDatabaseDriverFactory : KoinComponent {
    fun createDriver(): SqlDriver {
        val context = get<Context>()
        return AndroidSqliteDriver(
            schema = WorkoutDatabase.Schema,
            context = context,
            name = "workout.db"
        )
    }
}
