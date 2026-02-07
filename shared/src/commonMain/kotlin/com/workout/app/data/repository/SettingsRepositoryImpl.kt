package com.workout.app.data.repository

import com.workout.app.database.SettingsQueries

/**
 * Implementation of SettingsRepository using SQLDelight.
 */
class SettingsRepositoryImpl(
    private val settingsQueries: SettingsQueries
) : SettingsRepository
