package com.workout.app.di

/**
 * List of all shared Koin modules.
 * Import this in platform-specific initialization code.
 */
val sharedModules = listOf(
    databaseModule,
    dataModule,
    viewModelModule
)
