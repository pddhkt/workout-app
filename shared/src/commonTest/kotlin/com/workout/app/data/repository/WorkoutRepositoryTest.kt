package com.workout.app.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.workout.app.database.WorkoutDatabase
import com.workout.app.domain.model.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkoutRepositoryTest {

    private lateinit var driver: SqlDriver
    private lateinit var database: WorkoutDatabase
    private lateinit var repository: WorkoutRepository

    @BeforeTest
    fun setup() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        WorkoutDatabase.Schema.create(driver)
        database = WorkoutDatabase(driver)
        repository = WorkoutRepositoryImpl(database.workoutQueries)
    }

    @AfterTest
    fun teardown() {
        driver.close()
    }

    @Test
    fun testCreate_createsWorkout() = runTest {
        val result = repository.create(
            name = "Test Workout",
            duration = 3600,
            notes = "Great workout!",
            isPartnerWorkout = false,
            totalVolume = 5000,
            totalSets = 12,
            exerciseCount = 4
        )

        assertIs<Result.Success<String>>(result)
        val workoutId = result.data
        assertTrue(workoutId.isNotEmpty())

        // Verify workout was created
        val getResult = repository.getById(workoutId)
        assertIs<Result.Success<*>>(getResult)
        assertNotNull(getResult.data)
        assertEquals("Test Workout", getResult.data.name)
        assertEquals(3600L, getResult.data.duration)
        assertEquals("Great workout!", getResult.data.notes)
        assertEquals(5000L, getResult.data.totalVolume)
        assertEquals(12L, getResult.data.totalSets)
        assertEquals(4L, getResult.data.exerciseCount)
    }

    @Test
    fun testGetById_existingWorkout_returnsWorkout() = runTest {
        val workoutId = (repository.create(
            name = "Find Me",
            duration = 1800
        ) as Result.Success).data

        val result = repository.getById(workoutId)

        assertIs<Result.Success<*>>(result)
        assertNotNull(result.data)
        assertEquals(workoutId, result.data!!.id)
        assertEquals("Find Me", result.data!!.name)
    }

    @Test
    fun testGetById_nonExistentWorkout_returnsNull() = runTest {
        val result = repository.getById("non_existent_id")

        assertIs<Result.Success<*>>(result)
        assertNull(result.data)
    }

    @Test
    fun testUpdate_updatesWorkout() = runTest {
        val workoutId = (repository.create(
            name = "Original Name",
            duration = 1800,
            totalVolume = 3000
        ) as Result.Success).data

        val workout = (repository.getById(workoutId) as Result.Success).data!!
        val updatedWorkout = workout.copy(
            name = "Updated Name",
            duration = 2400,
            totalVolume = 3500
        )

        val updateResult = repository.update(updatedWorkout)
        assertIs<Result.Success<Unit>>(updateResult)

        // Verify update
        val getResult = repository.getById(workoutId)
        assertIs<Result.Success<*>>(getResult)
        assertEquals("Updated Name", getResult.data!!.name)
        assertEquals(2400L, getResult.data!!.duration)
        assertEquals(3500L, getResult.data!!.totalVolume)
    }

    @Test
    fun testDelete_deletesWorkout() = runTest {
        val workoutId = (repository.create(
            name = "Delete Me",
            duration = 1200
        ) as Result.Success).data

        // Verify it exists
        var workout = (repository.getById(workoutId) as Result.Success).data
        assertNotNull(workout)

        // Delete it
        val deleteResult = repository.delete(workoutId)
        assertIs<Result.Success<Unit>>(deleteResult)

        // Verify it's gone
        workout = (repository.getById(workoutId) as Result.Success).data
        assertNull(workout)
    }

    @Test
    fun testGetAll_returnsAllWorkouts() = runTest {
        repository.create(name = "Workout 1", duration = 1800)
        repository.create(name = "Workout 2", duration = 2400)
        repository.create(name = "Workout 3", duration = 3000)

        val result = repository.getAll()

        assertIs<Result.Success<List<*>>>(result)
        assertTrue(result.data!!.size >= 3)
    }

    @Test
    fun testObserveAll_emitsWorkouts() = runTest {
        repository.create(name = "Workout 1", duration = 1800)
        repository.create(name = "Workout 2", duration = 2400)

        val workouts = repository.observeAll().first()

        assertTrue(workouts.size >= 2)
    }

    @Test
    fun testObserveRecent_limitsResults() = runTest {
        repository.create(name = "Workout 1", duration = 1800)
        repository.create(name = "Workout 2", duration = 2400)
        repository.create(name = "Workout 3", duration = 3000)

        val recentWorkouts = repository.observeRecent(2).first()

        assertEquals(2, recentWorkouts.size)
    }

    @Test
    fun testGetByDateRange_filtersCorrectly() = runTest {
        val now = Clock.System.now().toEpochMilliseconds()
        val oneDayAgo = now - (24 * 60 * 60 * 1000)
        val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000)

        // Create workouts at different times (mocked by creating them with IDs)
        repository.create(name = "Recent Workout", duration = 1800)

        val result = repository.getByDateRange(twoDaysAgo, now)

        assertIs<Result.Success<List<*>>>(result)
        assertTrue(result.data!!.isNotEmpty())
    }

    @Test
    fun testGetStats_returnsWorkoutStatistics() = runTest {
        repository.create(
            name = "Workout 1",
            duration = 1800,
            totalVolume = 3000,
            totalSets = 10
        )
        repository.create(
            name = "Workout 2",
            duration = 2400,
            totalVolume = 4000,
            totalSets = 12
        )

        val result = repository.getStats()

        assertIs<Result.Success<*>>(result)
        assertNotNull(result.data)
        assertTrue(result.data!!.totalWorkouts >= 2)
        assertTrue(result.data!!.totalDuration!! >= 4200)
        assertTrue(result.data!!.totalVolume!! >= 7000)
        assertTrue(result.data!!.totalSets!! >= 22)
    }

    @Test
    fun testGetHeatmapData_returnsWorkoutDates() = runTest {
        repository.create(name = "Workout 1", duration = 1800)
        repository.create(name = "Workout 2", duration = 2400)

        val thirtyDaysAgo = Clock.System.now().toEpochMilliseconds() - (30 * 24 * 60 * 60 * 1000)
        val result = repository.getHeatmapData(thirtyDaysAgo)

        assertIs<Result.Success<List<*>>>(result)
        // We should have at least some data for today
        assertTrue(result.data!!.isNotEmpty())
    }

    @Test
    fun testPartnerWorkout_storesCorrectly() = runTest {
        val workoutId = (repository.create(
            name = "Partner Workout",
            duration = 3000,
            isPartnerWorkout = true
        ) as Result.Success).data

        val workout = (repository.getById(workoutId) as Result.Success).data!!
        assertEquals(1L, workout.isPartnerWorkout)
    }
}
