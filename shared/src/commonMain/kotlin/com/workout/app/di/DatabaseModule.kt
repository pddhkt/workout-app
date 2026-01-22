package com.workout.app.di

import app.cash.sqldelight.db.SqlDriver
import com.workout.app.database.WorkoutDatabase
import org.koin.dsl.module

/**
 * Koin module for database dependencies.
 * Provides the SQLDelight database and query objects.
 */
val databaseModule = module {
    // Database driver is provided by platform-specific modules (expect/actual)
    single<SqlDriver> { createDatabaseDriver() }

    // Database instance
    single {
        WorkoutDatabase(
            driver = get()
        )
    }

    // Query objects
    single { get<WorkoutDatabase>().exerciseQueries }
    single { get<WorkoutDatabase>().workoutQueries }
    single { get<WorkoutDatabase>().sessionQueries }
    single { get<WorkoutDatabase>().sessionExerciseQueries }
    single { get<WorkoutDatabase>().setQueries }
    single { get<WorkoutDatabase>().templateQueries }
    single { get<WorkoutDatabase>().settingsQueries }
}

/**
 * Platform-specific database driver creation.
 * Implementations are provided in androidMain and iosMain.
 */
expect fun createDatabaseDriver(): SqlDriver
