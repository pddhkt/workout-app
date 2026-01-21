package com.workout.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.workout.app.database.SelectRecentlyUsed
import com.workout.app.database.Template
import com.workout.app.database.TemplateQueries
import com.workout.app.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Implementation of TemplateRepository using SQLDelight.
 */
class TemplateRepositoryImpl(
    private val templateQueries: TemplateQueries
) : TemplateRepository {

    override fun observeAll(): Flow<List<Template>> {
        return templateQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override fun observeFavorites(): Flow<List<Template>> {
        return templateQueries.selectFavorites()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override fun observeRecentlyUsed(limit: Long): Flow<List<Template>> {
        return templateQueries.selectRecentlyUsed(limit)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toTemplate() } }
    }

    override fun observeMostUsed(limit: Long): Flow<List<Template>> {
        return templateQueries.selectMostUsed(limit)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Convert SelectRecentlyUsed to Template.
     */
    private fun SelectRecentlyUsed.toTemplate(): Template {
        return Template(
            id = id,
            name = name,
            description = description,
            exercises = exercises,
            estimatedDuration = estimatedDuration,
            isDefault = isDefault,
            isFavorite = isFavorite,
            lastUsed = lastUsed,
            useCount = useCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    override suspend fun getById(id: String): Result<Template?> = withContext(Dispatchers.Default) {
        try {
            val template = templateQueries.selectById(id).executeAsOneOrNull()
            Result.Success(template)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getAll(): Result<List<Template>> = withContext(Dispatchers.Default) {
        try {
            val templates = templateQueries.selectAll().executeAsList()
            Result.Success(templates)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getDefaults(): Result<List<Template>> = withContext(Dispatchers.Default) {
        try {
            val templates = templateQueries.selectDefaults().executeAsList()
            Result.Success(templates)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCustom(): Result<List<Template>> = withContext(Dispatchers.Default) {
        try {
            val templates = templateQueries.selectCustom().executeAsList()
            Result.Success(templates)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun create(
        name: String,
        description: String?,
        exercises: String,
        estimatedDuration: Long?,
        isDefault: Boolean
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val id = generateId()
            val now = Clock.System.now().toEpochMilliseconds()

            templateQueries.insert(
                id = id,
                name = name,
                description = description,
                exercises = exercises,
                estimatedDuration = estimatedDuration,
                isDefault = if (isDefault) 1 else 0,
                isFavorite = 0,
                lastUsed = null,
                useCount = 0,
                createdAt = now,
                updatedAt = now
            )

            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun update(template: Template): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()

            templateQueries.update(
                name = template.name,
                description = template.description,
                exercises = template.exercises,
                estimatedDuration = template.estimatedDuration,
                isFavorite = template.isFavorite,
                updatedAt = now,
                id = template.id
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun toggleFavorite(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            templateQueries.toggleFavorite(updatedAt = now, id = id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateLastUsed(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            templateQueries.updateLastUsed(lastUsed = now, updatedAt = now, id = id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            templateQueries.delete(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCountByType(): Result<Pair<Long, Long>> = withContext(Dispatchers.Default) {
        try {
            val counts = templateQueries.countByType().executeAsOneOrNull()
            val result = Pair(counts?.defaultCount ?: 0, counts?.customCount ?: 0)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Generate a unique ID for a new template.
     */
    private fun generateId(): String {
        return "template_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }
}
