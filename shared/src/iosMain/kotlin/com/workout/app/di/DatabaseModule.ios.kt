package com.workout.app.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.workout.app.database.WorkoutDatabase

/**
 * iOS-specific database driver implementation.
 */
actual fun createDatabaseDriver(): SqlDriver {
    return NativeSqliteDriver(
        schema = WorkoutDatabase.Schema,
        name = "workout.db"
    )
}
