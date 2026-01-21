package com.workout.app.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.workout.app.database.WorkoutDatabase
import com.workout.app.domain.model.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TemplateRepositoryTest {

    private lateinit var driver: SqlDriver
    private lateinit var database: WorkoutDatabase
    private lateinit var repository: TemplateRepository

    @BeforeTest
    fun setup() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        WorkoutDatabase.Schema.create(driver)
        database = WorkoutDatabase(driver)
        repository = TemplateRepositoryImpl(database.templateQueries)
    }

    @AfterTest
    fun teardown() {
        driver.close()
    }

    @Test
    fun testGetAll_returnsSeededTemplates() = runTest {
        val result = repository.getAll()

        assertIs<Result.Success<List<*>>>(result)
        val templates = result.data
        assertTrue(templates.isNotEmpty(), "Should have seeded templates")
        assertTrue(templates.size >= 5, "Should have at least 5 default templates")
    }

    @Test
    fun testObserveAll_emitsTemplates() = runTest {
        val templates = repository.observeAll().first()

        assertTrue(templates.isNotEmpty(), "Should have seeded templates")
    }

    @Test
    fun testGetById_existingTemplate_returnsTemplate() = runTest {
        val allTemplates = (repository.getAll() as Result.Success).data
        val firstTemplate = allTemplates.first()

        val result = repository.getById(firstTemplate.id)

        assertIs<Result.Success<*>>(result)
        assertNotNull(result.data)
        assertEquals(firstTemplate.id, result.data!!.id)
        assertEquals(firstTemplate.name, result.data!!.name)
    }

    @Test
    fun testGetById_nonExistentTemplate_returnsNull() = runTest {
        val result = repository.getById("non_existent_id")

        assertIs<Result.Success<*>>(result)
        assertNull(result.data)
    }

    @Test
    fun testCreate_createsCustomTemplate() = runTest {
        val exercisesJson = """[{"id":"ex1","sets":3},{"id":"ex2","sets":4}]"""

        val result = repository.create(
            name = "Test Template",
            description = "A test workout template",
            exercises = exercisesJson,
            estimatedDuration = 45,
            isDefault = false
        )

        assertIs<Result.Success<String>>(result)
        val templateId = result.data
        assertTrue(templateId.isNotEmpty())

        // Verify template was created
        val getResult = repository.getById(templateId)
        assertIs<Result.Success<*>>(getResult)
        assertNotNull(getResult.data)
        assertEquals("Test Template", getResult.data.name)
        assertEquals("A test workout template", getResult.data.description)
        assertEquals(exercisesJson, getResult.data.exercises)
        assertEquals(45L, getResult.data.estimatedDuration)
        assertEquals(0L, getResult.data.isDefault, "Should be custom template")
    }

    @Test
    fun testUpdate_updatesTemplate() = runTest {
        val exercisesJson = """[{"id":"ex1","sets":3}]"""
        val createResult = repository.create(
            name = "Original Template",
            exercises = exercisesJson
        )
        val templateId = (createResult as Result.Success).data

        val template = (repository.getById(templateId) as Result.Success).data!!
        val updatedExercises = """[{"id":"ex1","sets":4},{"id":"ex2","sets":3}]"""
        val updatedTemplate = template.copy(
            name = "Updated Template",
            description = "Updated description",
            exercises = updatedExercises,
            estimatedDuration = 60
        )

        val updateResult = repository.update(updatedTemplate)
        assertIs<Result.Success<Unit>>(updateResult)

        // Verify update
        val getResult = repository.getById(templateId)
        assertIs<Result.Success<*>>(getResult)
        assertEquals("Updated Template", getResult.data!!.name)
        assertEquals("Updated description", getResult.data!!.description)
        assertEquals(updatedExercises, getResult.data!!.exercises)
        assertEquals(60L, getResult.data!!.estimatedDuration)
    }

    @Test
    fun testToggleFavorite_togglesFavoriteStatus() = runTest {
        val createResult = repository.create(
            name = "Favorite Test",
            exercises = """[]"""
        )
        val templateId = (createResult as Result.Success).data

        // Initially not favorite
        var template = (repository.getById(templateId) as Result.Success).data!!
        assertEquals(0L, template.isFavorite)

        // Toggle to favorite
        repository.toggleFavorite(templateId)
        template = (repository.getById(templateId) as Result.Success).data!!
        assertEquals(1L, template.isFavorite)

        // Toggle back
        repository.toggleFavorite(templateId)
        template = (repository.getById(templateId) as Result.Success).data!!
        assertEquals(0L, template.isFavorite)
    }

    @Test
    fun testUpdateLastUsed_incrementsUseCount() = runTest {
        val templateId = (repository.create(
            name = "Usage Test",
            exercises = """[]"""
        ) as Result.Success).data

        // Initial use count is 0
        var template = (repository.getById(templateId) as Result.Success).data!!
        assertEquals(0L, template.useCount)
        assertNull(template.lastUsed)

        // Update last used
        repository.updateLastUsed(templateId)
        template = (repository.getById(templateId) as Result.Success).data!!
        assertEquals(1L, template.useCount)
        assertNotNull(template.lastUsed)

        // Update again
        repository.updateLastUsed(templateId)
        template = (repository.getById(templateId) as Result.Success).data!!
        assertEquals(2L, template.useCount)
    }

    @Test
    fun testObserveFavorites_returnsOnlyFavorites() = runTest {
        val templateId = (repository.create(
            name = "Favorite Template",
            exercises = """[]"""
        ) as Result.Success).data

        repository.toggleFavorite(templateId)

        val favorites = repository.observeFavorites().first()

        assertTrue(favorites.any { it.id == templateId })
        assertTrue(favorites.all { it.isFavorite == 1L })
    }

    @Test
    fun testObserveRecentlyUsed_limitsResults() = runTest {
        val id1 = (repository.create(name = "Template 1", exercises = """[]""") as Result.Success).data
        val id2 = (repository.create(name = "Template 2", exercises = """[]""") as Result.Success).data
        val id3 = (repository.create(name = "Template 3", exercises = """[]""") as Result.Success).data

        repository.updateLastUsed(id1)
        repository.updateLastUsed(id2)
        repository.updateLastUsed(id3)

        val recentTemplates = repository.observeRecentlyUsed(2).first()

        assertEquals(2, recentTemplates.size)
    }

    @Test
    fun testObserveMostUsed_ordersCorrectly() = runTest {
        val id1 = (repository.create(name = "Template 1", exercises = """[]""") as Result.Success).data
        val id2 = (repository.create(name = "Template 2", exercises = """[]""") as Result.Success).data

        // Use template 2 more times
        repository.updateLastUsed(id1)
        repository.updateLastUsed(id2)
        repository.updateLastUsed(id2)
        repository.updateLastUsed(id2)

        val mostUsed = repository.observeMostUsed(5).first()

        assertTrue(mostUsed.isNotEmpty())
        // Template 2 should be first (most used)
        val template2Index = mostUsed.indexOfFirst { it.id == id2 }
        val template1Index = mostUsed.indexOfFirst { it.id == id1 }
        assertTrue(template2Index < template1Index, "More used template should come first")
    }

    @Test
    fun testGetDefaults_returnsOnlyDefaultTemplates() = runTest {
        val result = repository.getDefaults()

        assertIs<Result.Success<List<*>>>(result)
        assertTrue(result.data!!.isNotEmpty(), "Should have default templates")
        assertTrue(result.data!!.all { it.isDefault == 1L })
    }

    @Test
    fun testGetCustom_returnsOnlyCustomTemplates() = runTest {
        repository.create(
            name = "Custom Template",
            exercises = """[]""",
            isDefault = false
        )

        val result = repository.getCustom()

        assertIs<Result.Success<List<*>>>(result)
        assertTrue(result.data!!.isNotEmpty())
        assertTrue(result.data!!.all { it.isDefault == 0L })
    }

    @Test
    fun testDelete_deletesCustomTemplate() = runTest {
        val templateId = (repository.create(
            name = "Delete Me",
            exercises = """[]""",
            isDefault = false
        ) as Result.Success).data

        // Verify it exists
        var template = (repository.getById(templateId) as Result.Success).data
        assertNotNull(template)

        // Delete it
        val deleteResult = repository.delete(templateId)
        assertIs<Result.Success<Unit>>(deleteResult)

        // Verify it's gone
        template = (repository.getById(templateId) as Result.Success).data
        assertNull(template)
    }

    @Test
    fun testGetCountByType_returnsCorrectCounts() = runTest {
        repository.create(name = "Custom 1", exercises = """[]""", isDefault = false)
        repository.create(name = "Custom 2", exercises = """[]""", isDefault = false)

        val result = repository.getCountByType()

        assertIs<Result.Success<Pair<Long, Long>>>(result)
        val (defaultCount, customCount) = result.data
        assertTrue(defaultCount >= 5, "Should have default templates from seed data")
        assertTrue(customCount >= 2, "Should have at least 2 custom templates we created")
    }
}
