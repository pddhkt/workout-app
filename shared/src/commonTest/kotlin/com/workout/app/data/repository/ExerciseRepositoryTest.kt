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

class ExerciseRepositoryTest {

    private lateinit var driver: SqlDriver
    private lateinit var database: WorkoutDatabase
    private lateinit var repository: ExerciseRepository

    @BeforeTest
    fun setup() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        WorkoutDatabase.Schema.create(driver)
        database = WorkoutDatabase(driver)
        repository = ExerciseRepositoryImpl(database.exerciseQueries)
    }

    @AfterTest
    fun teardown() {
        driver.close()
    }

    @Test
    fun testGetAll_returnsSeededExercises() = runTest {
        val result = repository.getAll()

        assertIs<Result.Success<List<*>>>(result)
        val exercises = result.data
        assertTrue(exercises.isNotEmpty(), "Should have seeded exercises")
        assertTrue(exercises.size >= 20, "Should have at least 20 seeded exercises")
    }

    @Test
    fun testObserveAll_emitsExercises() = runTest {
        val exercises = repository.observeAll().first()

        assertTrue(exercises.isNotEmpty(), "Should have seeded exercises")
    }

    @Test
    fun testGetById_existingExercise_returnsExercise() = runTest {
        val allExercises = (repository.getAll() as Result.Success).data
        val firstExercise = allExercises.first()

        val result = repository.getById(firstExercise.id)

        assertIs<Result.Success<*>>(result)
        assertNotNull(result.data)
        assertEquals(firstExercise.id, result.data!!.id)
        assertEquals(firstExercise.name, result.data!!.name)
    }

    @Test
    fun testGetById_nonExistentExercise_returnsNull() = runTest {
        val result = repository.getById("non_existent_id")

        assertIs<Result.Success<*>>(result)
        assertNull(result.data)
    }

    @Test
    fun testCreate_createsCustomExercise() = runTest {
        val result = repository.create(
            name = "Test Exercise",
            muscleGroup = "Chest",
            category = "Isolation",
            equipment = "Dumbbells",
            difficulty = "Intermediate",
            instructions = "Test instructions",
            videoUrl = "https://example.com/video"
        )

        assertIs<Result.Success<String>>(result)
        val exerciseId = result.data
        assertTrue(exerciseId.isNotEmpty())

        // Verify exercise was created
        val getResult = repository.getById(exerciseId)
        assertIs<Result.Success<*>>(getResult)
        assertNotNull(getResult.data)
        assertEquals("Test Exercise", getResult.data.name)
        assertEquals("Chest", getResult.data.muscleGroup)
        assertEquals(1L, getResult.data.isCustom, "Should be marked as custom")
    }

    @Test
    fun testUpdate_updatesExercise() = runTest {
        val createResult = repository.create(
            name = "Test Exercise",
            muscleGroup = "Chest"
        )
        val exerciseId = (createResult as Result.Success).data

        val exercise = (repository.getById(exerciseId) as Result.Success).data!!
        val updatedExercise = exercise.copy(
            name = "Updated Exercise",
            muscleGroup = "Back"
        )

        val updateResult = repository.update(updatedExercise)
        assertIs<Result.Success<Unit>>(updateResult)

        // Verify update
        val getResult = repository.getById(exerciseId)
        assertIs<Result.Success<*>>(getResult)
        assertEquals("Updated Exercise", getResult.data!!.name)
        assertEquals("Back", getResult.data!!.muscleGroup)
    }

    @Test
    fun testToggleFavorite_togglesFavoriteStatus() = runTest {
        val createResult = repository.create(
            name = "Test Exercise",
            muscleGroup = "Chest"
        )
        val exerciseId = (createResult as Result.Success).data

        // Initially not favorite
        var exercise = (repository.getById(exerciseId) as Result.Success).data!!
        assertEquals(0L, exercise.isFavorite)

        // Toggle to favorite
        repository.toggleFavorite(exerciseId)
        exercise = (repository.getById(exerciseId) as Result.Success).data!!
        assertEquals(1L, exercise.isFavorite)

        // Toggle back
        repository.toggleFavorite(exerciseId)
        exercise = (repository.getById(exerciseId) as Result.Success).data!!
        assertEquals(0L, exercise.isFavorite)
    }

    @Test
    fun testObserveFavorites_returnsOnlyFavorites() = runTest {
        // Create a custom exercise and mark as favorite
        val exerciseId = (repository.create(
            name = "Favorite Exercise",
            muscleGroup = "Arms"
        ) as Result.Success).data

        repository.toggleFavorite(exerciseId)

        val favorites = repository.observeFavorites().first()

        assertTrue(favorites.any { it.id == exerciseId })
        assertTrue(favorites.all { it.isFavorite == 1L })
    }

    @Test
    fun testObserveCustom_returnsOnlyCustomExercises() = runTest {
        val exerciseId = (repository.create(
            name = "Custom Exercise",
            muscleGroup = "Legs"
        ) as Result.Success).data

        val customExercises = repository.observeCustom().first()

        assertTrue(customExercises.any { it.id == exerciseId })
        assertTrue(customExercises.all { it.isCustom == 1L })
    }

    @Test
    fun testObserveByMuscleGroup_filtersCorrectly() = runTest {
        repository.create(name = "Chest Exercise 1", muscleGroup = "Chest")
        repository.create(name = "Chest Exercise 2", muscleGroup = "Chest")

        val chestExercises = repository.observeByMuscleGroup("Chest").first()

        assertTrue(chestExercises.isNotEmpty())
        assertTrue(chestExercises.all { it.muscleGroup == "Chest" })
    }

    @Test
    fun testSearch_findsExercisesByName() = runTest {
        val result = repository.search("Bench")

        assertIs<Result.Success<List<*>>>(result)
        assertTrue(result.data!!.isNotEmpty())
        assertTrue(result.data!!.any { it.name.contains("Bench", ignoreCase = true) })
    }

    @Test
    fun testSearch_findsExercisesByMuscleGroup() = runTest {
        val result = repository.search("Chest")

        assertIs<Result.Success<List<*>>>(result)
        assertTrue(result.data!!.isNotEmpty())
        assertTrue(result.data!!.any { it.muscleGroup.contains("Chest", ignoreCase = true) })
    }

    @Test
    fun testDelete_deletesCustomExercise() = runTest {
        val exerciseId = (repository.create(
            name = "Exercise to Delete",
            muscleGroup = "Shoulders"
        ) as Result.Success).data

        // Verify it exists
        var exercise = (repository.getById(exerciseId) as Result.Success).data
        assertNotNull(exercise)

        // Delete it
        val deleteResult = repository.delete(exerciseId)
        assertIs<Result.Success<Unit>>(deleteResult)

        // Verify it's gone
        exercise = (repository.getById(exerciseId) as Result.Success).data
        assertNull(exercise)
    }

    @Test
    fun testGetCountByMuscleGroup_returnsCorrectCounts() = runTest {
        repository.create(name = "Chest Ex 1", muscleGroup = "Chest")
        repository.create(name = "Chest Ex 2", muscleGroup = "Chest")
        repository.create(name = "Back Ex 1", muscleGroup = "Back")

        val result = repository.getCountByMuscleGroup()

        assertIs<Result.Success<Map<String, Long>>>(result)
        val counts = result.data
        assertTrue(counts.containsKey("Chest"))
        assertTrue(counts.containsKey("Back"))
        assertTrue(counts["Chest"]!! >= 2)
        assertTrue(counts["Back"]!! >= 1)
    }
}
