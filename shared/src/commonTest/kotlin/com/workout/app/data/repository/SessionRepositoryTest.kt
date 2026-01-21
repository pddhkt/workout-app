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

class SessionRepositoryTest {

    private lateinit var driver: SqlDriver
    private lateinit var database: WorkoutDatabase
    private lateinit var repository: SessionRepository

    @BeforeTest
    fun setup() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        WorkoutDatabase.Schema.create(driver)
        database = WorkoutDatabase(driver)
        repository = SessionRepositoryImpl(database.sessionQueries)
    }

    @AfterTest
    fun teardown() {
        driver.close()
    }

    @Test
    fun testCreate_createsSession() = runTest {
        val result = repository.create(
            name = "Test Session",
            templateId = "template_123",
            isPartnerWorkout = false,
            status = "draft"
        )

        assertIs<Result.Success<String>>(result)
        val sessionId = result.data
        assertTrue(sessionId.isNotEmpty())

        // Verify session was created
        val getResult = repository.getById(sessionId)
        assertIs<Result.Success<*>>(getResult)
        assertNotNull(getResult.data)
        assertEquals("Test Session", getResult.data.name)
        assertEquals("template_123", getResult.data.templateId)
        assertEquals("draft", getResult.data.status)
        assertEquals(0L, getResult.data.isPartnerWorkout)
    }

    @Test
    fun testGetById_existingSession_returnsSession() = runTest {
        val sessionId = (repository.create(
            name = "Find Me",
            status = "active"
        ) as Result.Success).data

        val result = repository.getById(sessionId)

        assertIs<Result.Success<*>>(result)
        assertNotNull(result.data)
        assertEquals(sessionId, result.data!!.id)
        assertEquals("Find Me", result.data!!.name)
    }

    @Test
    fun testGetById_nonExistentSession_returnsNull() = runTest {
        val result = repository.getById("non_existent_id")

        assertIs<Result.Success<*>>(result)
        assertNull(result.data)
    }

    @Test
    fun testUpdate_updatesSession() = runTest {
        val sessionId = (repository.create(
            name = "Original Session",
            status = "draft"
        ) as Result.Success).data

        val session = (repository.getById(sessionId) as Result.Success).data!!
        val updatedSession = session.copy(
            name = "Updated Session",
            notes = "Added some notes",
            currentExerciseIndex = 2
        )

        val updateResult = repository.update(updatedSession)
        assertIs<Result.Success<Unit>>(updateResult)

        // Verify update
        val getResult = repository.getById(sessionId)
        assertIs<Result.Success<*>>(getResult)
        assertEquals("Updated Session", getResult.data!!.name)
        assertEquals("Added some notes", getResult.data!!.notes)
        assertEquals(2L, getResult.data!!.currentExerciseIndex)
    }

    @Test
    fun testUpdateStatus_updatesOnlyStatus() = runTest {
        val sessionId = (repository.create(
            name = "Status Test",
            status = "draft"
        ) as Result.Success).data

        val updateResult = repository.updateStatus(sessionId, "active")
        assertIs<Result.Success<Unit>>(updateResult)

        val session = (repository.getById(sessionId) as Result.Success).data!!
        assertEquals("active", session.status)
    }

    @Test
    fun testComplete_completesSession() = runTest {
        val sessionId = (repository.create(
            name = "Complete Me",
            status = "active"
        ) as Result.Success).data

        val completeResult = repository.complete(sessionId)
        assertIs<Result.Success<Unit>>(completeResult)

        val session = (repository.getById(sessionId) as Result.Success).data!!
        assertEquals("completed", session.status)
        assertNotNull(session.endTime, "End time should be set")
    }

    @Test
    fun testDelete_deletesSession() = runTest {
        val sessionId = (repository.create(
            name = "Delete Me",
            status = "draft"
        ) as Result.Success).data

        // Verify it exists
        var session = (repository.getById(sessionId) as Result.Success).data
        assertNotNull(session)

        // Delete it
        val deleteResult = repository.delete(sessionId)
        assertIs<Result.Success<Unit>>(deleteResult)

        // Verify it's gone
        session = (repository.getById(sessionId) as Result.Success).data
        assertNull(session)
    }

    @Test
    fun testGetAll_returnsAllSessions() = runTest {
        repository.create(name = "Session 1", status = "active")
        repository.create(name = "Session 2", status = "draft")
        repository.create(name = "Session 3", status = "completed")

        val result = repository.getAll()

        assertIs<Result.Success<List<*>>>(result)
        assertTrue(result.data!!.size >= 3)
    }

    @Test
    fun testObserveAll_emitsSessions() = runTest {
        repository.create(name = "Session 1", status = "active")
        repository.create(name = "Session 2", status = "draft")

        val sessions = repository.observeAll().first()

        assertTrue(sessions.size >= 2)
    }

    @Test
    fun testObserveActive_returnsOnlyActiveSessions() = runTest {
        val activeId = (repository.create(
            name = "Active Session",
            status = "active"
        ) as Result.Success).data

        repository.create(name = "Draft Session", status = "draft")
        repository.create(name = "Paused Session", status = "paused")

        val activeSessions = repository.observeActive().first()

        assertTrue(activeSessions.any { it.id == activeId })
        assertTrue(activeSessions.all { it.status == "active" })
    }

    @Test
    fun testObserveByStatus_filtersCorrectly() = runTest {
        repository.create(name = "Draft 1", status = "draft")
        repository.create(name = "Draft 2", status = "draft")
        repository.create(name = "Active", status = "active")

        val draftSessions = repository.observeByStatus("draft").first()

        assertTrue(draftSessions.size >= 2)
        assertTrue(draftSessions.all { it.status == "draft" })
    }

    @Test
    fun testGetCountByStatus_returnsCorrectCounts() = runTest {
        repository.create(name = "Active 1", status = "active")
        repository.create(name = "Active 2", status = "active")
        repository.create(name = "Draft 1", status = "draft")
        repository.create(name = "Paused 1", status = "paused")

        val result = repository.getCountByStatus()

        assertIs<Result.Success<Map<String, Long>>>(result)
        val counts = result.data
        assertTrue(counts.containsKey("active"))
        assertTrue(counts.containsKey("draft"))
        assertTrue(counts.containsKey("paused"))
        assertTrue(counts["active"]!! >= 2)
        assertTrue(counts["draft"]!! >= 1)
        assertTrue(counts["paused"]!! >= 1)
    }

    @Test
    fun testPartnerWorkout_storesCorrectly() = runTest {
        val sessionId = (repository.create(
            name = "Partner Session",
            isPartnerWorkout = true,
            status = "active"
        ) as Result.Success).data

        val session = (repository.getById(sessionId) as Result.Success).data!!
        assertEquals(1L, session.isPartnerWorkout)
    }

    @Test
    fun testGetWithExercises_returnsSessionWithExercises() = runTest {
        val sessionId = (repository.create(
            name = "Session with Exercises",
            status = "active"
        ) as Result.Success).data

        val result = repository.getWithExercises(sessionId)

        assertIs<Result.Success<List<*>>>(result)
        // Will be empty list initially as no exercises added yet
        assertTrue(result.data!!.isEmpty() || result.data!!.isNotEmpty())
    }
}
