package com.workout.app.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Theme mode options for the app.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

/**
 * Repository for app settings.
 * Provides reactive access to user preferences.
 */
interface SettingsRepository {

    /**
     * Observe the current theme mode.
     * @return Flow of ThemeMode updates
     */
    fun observeThemeMode(): Flow<ThemeMode>

    /**
     * Set the theme mode.
     * @param mode The theme mode to set
     */
    suspend fun setThemeMode(mode: ThemeMode)
}
