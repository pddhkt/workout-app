package com.workout.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.workout.app.database.SettingsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementation of SettingsRepository using SQLDelight.
 */
class SettingsRepositoryImpl(
    private val settingsQueries: SettingsQueries
) : SettingsRepository {

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
    }

    override fun observeThemeMode(): Flow<ThemeMode> {
        return settingsQueries.getSetting(KEY_THEME_MODE)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { value ->
                value?.let { parseThemeMode(it) } ?: ThemeMode.SYSTEM
            }
    }

    override suspend fun setThemeMode(mode: ThemeMode) = withContext(Dispatchers.Default) {
        settingsQueries.upsertSetting(KEY_THEME_MODE, mode.name)
    }

    private fun parseThemeMode(value: String): ThemeMode {
        return try {
            ThemeMode.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
}
