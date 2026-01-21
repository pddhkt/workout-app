package com.workout.app.data.repository

import com.workout.app.database.Template
import com.workout.app.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository for workout template management.
 * Provides CRUD operations for workout templates.
 */
interface TemplateRepository {

    /**
     * Observe all templates ordered by name.
     * @return Flow of all templates
     */
    fun observeAll(): Flow<List<Template>>

    /**
     * Observe favorite templates.
     * @return Flow of favorite templates
     */
    fun observeFavorites(): Flow<List<Template>>

    /**
     * Observe recently used templates.
     * @param limit Maximum number of templates to return
     * @return Flow of recently used templates
     */
    fun observeRecentlyUsed(limit: Long): Flow<List<Template>>

    /**
     * Observe most used templates.
     * @param limit Maximum number of templates to return
     * @return Flow of most used templates
     */
    fun observeMostUsed(limit: Long): Flow<List<Template>>

    /**
     * Get a single template by ID.
     * @param id Template ID
     * @return Result containing the template or null if not found
     */
    suspend fun getById(id: String): Result<Template?>

    /**
     * Get all templates (non-reactive).
     * @return Result containing list of all templates
     */
    suspend fun getAll(): Result<List<Template>>

    /**
     * Get default (pre-defined) templates.
     * @return Result containing default templates
     */
    suspend fun getDefaults(): Result<List<Template>>

    /**
     * Get custom (user-created) templates.
     * @return Result containing custom templates
     */
    suspend fun getCustom(): Result<List<Template>>

    /**
     * Create a new workout template.
     * @param name Template name
     * @param description Template description (optional)
     * @param exercises JSON array of exercise configurations
     * @param estimatedDuration Estimated duration in minutes (optional)
     * @param isDefault Whether this is a pre-defined template
     * @return Result containing the created template ID
     */
    suspend fun create(
        name: String,
        description: String? = null,
        exercises: String,
        estimatedDuration: Long? = null,
        isDefault: Boolean = false
    ): Result<String>

    /**
     * Update an existing template.
     * @param template Template to update
     * @return Result indicating success or failure
     */
    suspend fun update(template: Template): Result<Unit>

    /**
     * Toggle favorite status of a template.
     * @param id Template ID
     * @return Result indicating success or failure
     */
    suspend fun toggleFavorite(id: String): Result<Unit>

    /**
     * Update last used timestamp and increment use count.
     * @param id Template ID
     * @return Result indicating success or failure
     */
    suspend fun updateLastUsed(id: String): Result<Unit>

    /**
     * Delete a custom template.
     * @param id Template ID (must be a custom template)
     * @return Result indicating success or failure
     */
    suspend fun delete(id: String): Result<Unit>

    /**
     * Get template counts by type.
     * @return Result containing counts of default and custom templates
     */
    suspend fun getCountByType(): Result<Pair<Long, Long>>
}
