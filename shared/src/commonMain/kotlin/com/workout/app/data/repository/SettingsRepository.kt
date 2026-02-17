package com.workout.app.data.repository

/**
 * Repository for app settings.
 * Provides access to key-value preferences stored in SQLDelight.
 */
interface SettingsRepository {
    suspend fun getString(key: String): String?
    suspend fun setString(key: String, value: String)
}
