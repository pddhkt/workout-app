package com.workout.app.data.repository

import com.workout.app.database.SettingsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of SettingsRepository using SQLDelight.
 */
class SettingsRepositoryImpl(
    private val settingsQueries: SettingsQueries
) : SettingsRepository {

    override suspend fun getString(key: String): String? =
        withContext(Dispatchers.Default) {
            settingsQueries.getSetting(key).executeAsOneOrNull()
        }

    override suspend fun setString(key: String, value: String) =
        withContext(Dispatchers.Default) {
            settingsQueries.upsertSetting(key, value)
        }
}
