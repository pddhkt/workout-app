package com.workout.app.data

import com.workout.app.database.ExerciseQueries
import com.workout.app.database.SessionExerciseQueries
import com.workout.app.database.SessionQueries
import com.workout.app.database.SetQueries
import com.workout.app.database.WorkoutQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

/**
 * Seeds the database with sample data for development and testing.
 * Creates workout records with full exercise and set data.
 */
class DatabaseSeeder(
    private val workoutQueries: WorkoutQueries,
    private val sessionQueries: SessionQueries,
    private val sessionExerciseQueries: SessionExerciseQueries,
    private val setQueries: SetQueries,
    private val exerciseQueries: ExerciseQueries
) {
    /**
     * Seeds the database with sample data if the database is empty.
     */
    suspend fun seedIfEmpty() = withContext(Dispatchers.Default) {
        val existingWorkouts = workoutQueries.selectAll().executeAsList().size
        val existingExercises = exerciseQueries.selectAll().executeAsList().size

        if (existingExercises == 0) {
            seedExercises()
        }

        if (existingWorkouts == 0) {
            seedWorkoutsWithExercises()
        }
    }

    /**
     * Seeds the exercise library with common exercises.
     */
    private fun seedExercises() {
        val now = Clock.System.now().toEpochMilliseconds()

        // Chest exercises
        exerciseQueries.insert(
            id = "ex_bench_press",
            name = "Bench Press",
            muscleGroup = "Chest",
            category = "Compound",
            equipment = "Barbell",
            difficulty = "Intermediate",
            instructions = "Lie on bench, grip barbell, lower to chest, press up.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 1,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_incline_db_press",
            name = "Incline Dumbbell Press",
            muscleGroup = "Chest",
            category = "Compound",
            equipment = "Dumbbells",
            difficulty = "Intermediate",
            instructions = "Set bench to 30-45 degrees, press dumbbells up.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_cable_fly",
            name = "Cable Fly",
            muscleGroup = "Chest",
            category = "Isolation",
            equipment = "Cable",
            difficulty = "Beginner",
            instructions = "Stand between cables, bring handles together in arc motion.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        // Triceps exercises
        exerciseQueries.insert(
            id = "ex_tricep_pushdown",
            name = "Tricep Pushdown",
            muscleGroup = "Triceps",
            category = "Isolation",
            equipment = "Cable",
            difficulty = "Beginner",
            instructions = "Push cable down while keeping elbows at sides.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_overhead_extension",
            name = "Overhead Tricep Extension",
            muscleGroup = "Triceps",
            category = "Isolation",
            equipment = "Dumbbell",
            difficulty = "Beginner",
            instructions = "Hold dumbbell overhead, lower behind head, extend.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        // Shoulder exercises
        exerciseQueries.insert(
            id = "ex_shoulder_press",
            name = "Shoulder Press",
            muscleGroup = "Shoulders",
            category = "Compound",
            equipment = "Dumbbells",
            difficulty = "Intermediate",
            instructions = "Press dumbbells overhead from shoulder height.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 1,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_lateral_raise",
            name = "Lateral Raise",
            muscleGroup = "Shoulders",
            category = "Isolation",
            equipment = "Dumbbells",
            difficulty = "Beginner",
            instructions = "Raise dumbbells to sides until parallel to floor.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        // Back exercises
        exerciseQueries.insert(
            id = "ex_lat_pulldown",
            name = "Lat Pulldown",
            muscleGroup = "Back",
            category = "Compound",
            equipment = "Cable",
            difficulty = "Beginner",
            instructions = "Pull bar down to chest, squeeze lats.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 1,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_barbell_row",
            name = "Barbell Row",
            muscleGroup = "Back",
            category = "Compound",
            equipment = "Barbell",
            difficulty = "Intermediate",
            instructions = "Bend over, pull barbell to lower chest.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_seated_row",
            name = "Seated Cable Row",
            muscleGroup = "Back",
            category = "Compound",
            equipment = "Cable",
            difficulty = "Beginner",
            instructions = "Pull handle to torso, squeeze shoulder blades.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        // Biceps exercises
        exerciseQueries.insert(
            id = "ex_barbell_curl",
            name = "Barbell Curl",
            muscleGroup = "Biceps",
            category = "Isolation",
            equipment = "Barbell",
            difficulty = "Beginner",
            instructions = "Curl barbell up, keeping elbows at sides.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_hammer_curl",
            name = "Hammer Curl",
            muscleGroup = "Biceps",
            category = "Isolation",
            equipment = "Dumbbells",
            difficulty = "Beginner",
            instructions = "Curl with neutral grip (palms facing each other).",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        // Leg exercises
        exerciseQueries.insert(
            id = "ex_squat",
            name = "Barbell Squat",
            muscleGroup = "Legs",
            category = "Compound",
            equipment = "Barbell",
            difficulty = "Intermediate",
            instructions = "Bar on upper back, squat down until thighs parallel.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 1,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_leg_press",
            name = "Leg Press",
            muscleGroup = "Legs",
            category = "Compound",
            equipment = "Machine",
            difficulty = "Beginner",
            instructions = "Push platform away, don't lock knees.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_romanian_deadlift",
            name = "Romanian Deadlift",
            muscleGroup = "Legs",
            category = "Compound",
            equipment = "Barbell",
            difficulty = "Intermediate",
            instructions = "Hip hinge with slight knee bend, feel hamstring stretch.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_leg_curl",
            name = "Leg Curl",
            muscleGroup = "Legs",
            category = "Isolation",
            equipment = "Machine",
            difficulty = "Beginner",
            instructions = "Curl weight toward glutes, squeeze hamstrings.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_leg_extension",
            name = "Leg Extension",
            muscleGroup = "Legs",
            category = "Isolation",
            equipment = "Machine",
            difficulty = "Beginner",
            instructions = "Extend legs until straight, squeeze quads.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )

        exerciseQueries.insert(
            id = "ex_calf_raise",
            name = "Calf Raise",
            muscleGroup = "Legs",
            category = "Isolation",
            equipment = "Machine",
            difficulty = "Beginner",
            instructions = "Rise up on toes, squeeze calves at top.",
            videoUrl = null,
            isCustom = 0,
            isFavorite = 0,
            createdAt = now,
            updatedAt = now
        )
    }

    /**
     * Seeds workouts with full exercise and set data.
     */
    private fun seedWorkoutsWithExercises() {
        val now = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()

        // Workout 1: Push Day - Today
        seedPushDayWorkout(
            workoutId = "1",
            sessionId = "session_1",
            timestamp = now.toEpochMilliseconds(),
            durationSeconds = 3480L // 58 minutes
        )

        // Workout 2: Pull Day - 2 days ago
        val twoDaysAgo = now.minus(2, DateTimeUnit.DAY, timeZone)
        seedPullDayWorkout(
            workoutId = "2",
            sessionId = "session_2",
            timestamp = twoDaysAgo.toEpochMilliseconds(),
            durationSeconds = 3120L // 52 minutes
        )

        // Workout 3: Leg Day - 4 days ago
        val fourDaysAgo = now.minus(4, DateTimeUnit.DAY, timeZone)
        seedLegDayWorkout(
            workoutId = "3",
            sessionId = "session_3",
            timestamp = fourDaysAgo.toEpochMilliseconds(),
            durationSeconds = 4080L // 68 minutes
        )
    }

    private fun seedPushDayWorkout(workoutId: String, sessionId: String, timestamp: Long, durationSeconds: Long) {
        val endTime = timestamp + (durationSeconds * 1000)

        // Create Workout summary
        workoutQueries.insert(
            id = workoutId,
            name = "Push Day",
            createdAt = timestamp,
            duration = durationSeconds,
            notes = "Great workout! Felt strong on bench press.",
            isPartnerWorkout = 0,
            totalVolume = 8500,
            totalSets = 18,
            exerciseCount = 6,
            exerciseNames = "Bench Press, Incline Dumbbell Press, Shoulder Press, Cable Fly, Lateral Raise, Tricep Pushdown"
        )

        // Create Session
        sessionQueries.insert(
            id = sessionId,
            workoutId = workoutId,
            templateId = null,
            name = "Push Day",
            startTime = timestamp,
            endTime = endTime,
            status = "completed",
            notes = "Great workout! Felt strong on bench press.",
            isPartnerWorkout = 0,
            currentExerciseIndex = 0,
            createdAt = timestamp,
            updatedAt = endTime
        )

        // Add exercises with sets
        addSessionExercise(sessionId, "ex_bench_press", 0, timestamp,
            listOf(
                SetData(80.0, 10),
                SetData(90.0, 8),
                SetData(95.0, 6),
                SetData(95.0, 6)
            )
        )

        addSessionExercise(sessionId, "ex_incline_db_press", 1, timestamp,
            listOf(
                SetData(30.0, 12),
                SetData(32.5, 10),
                SetData(35.0, 8)
            )
        )

        addSessionExercise(sessionId, "ex_shoulder_press", 2, timestamp,
            listOf(
                SetData(25.0, 10),
                SetData(27.5, 8),
                SetData(27.5, 8)
            )
        )

        addSessionExercise(sessionId, "ex_cable_fly", 3, timestamp,
            listOf(
                SetData(15.0, 15),
                SetData(17.5, 12),
                SetData(17.5, 12)
            )
        )

        addSessionExercise(sessionId, "ex_lateral_raise", 4, timestamp,
            listOf(
                SetData(10.0, 15),
                SetData(12.5, 12),
                SetData(12.5, 10)
            )
        )

        addSessionExercise(sessionId, "ex_tricep_pushdown", 5, timestamp,
            listOf(
                SetData(25.0, 15),
                SetData(30.0, 12)
            )
        )
    }

    private fun seedPullDayWorkout(workoutId: String, sessionId: String, timestamp: Long, durationSeconds: Long) {
        val endTime = timestamp + (durationSeconds * 1000)

        workoutQueries.insert(
            id = workoutId,
            name = "Pull Day",
            createdAt = timestamp,
            duration = durationSeconds,
            notes = "Good session. Focused on mind-muscle connection.",
            isPartnerWorkout = 0,
            totalVolume = 7200,
            totalSets = 15,
            exerciseCount = 5,
            exerciseNames = "Lat Pulldown, Barbell Row, Seated Cable Row, Barbell Curl, Hammer Curl"
        )

        sessionQueries.insert(
            id = sessionId,
            workoutId = workoutId,
            templateId = null,
            name = "Pull Day",
            startTime = timestamp,
            endTime = endTime,
            status = "completed",
            notes = "Good session. Focused on mind-muscle connection.",
            isPartnerWorkout = 0,
            currentExerciseIndex = 0,
            createdAt = timestamp,
            updatedAt = endTime
        )

        addSessionExercise(sessionId, "ex_lat_pulldown", 0, timestamp,
            listOf(
                SetData(60.0, 12),
                SetData(65.0, 10),
                SetData(70.0, 8)
            )
        )

        addSessionExercise(sessionId, "ex_barbell_row", 1, timestamp,
            listOf(
                SetData(60.0, 10),
                SetData(70.0, 8),
                SetData(75.0, 6)
            )
        )

        addSessionExercise(sessionId, "ex_seated_row", 2, timestamp,
            listOf(
                SetData(50.0, 12),
                SetData(55.0, 10),
                SetData(55.0, 10)
            )
        )

        addSessionExercise(sessionId, "ex_barbell_curl", 3, timestamp,
            listOf(
                SetData(25.0, 12),
                SetData(30.0, 10),
                SetData(30.0, 8)
            )
        )

        addSessionExercise(sessionId, "ex_hammer_curl", 4, timestamp,
            listOf(
                SetData(12.5, 12),
                SetData(15.0, 10),
                SetData(15.0, 10)
            )
        )
    }

    private fun seedLegDayWorkout(workoutId: String, sessionId: String, timestamp: Long, durationSeconds: Long) {
        val endTime = timestamp + (durationSeconds * 1000)

        workoutQueries.insert(
            id = workoutId,
            name = "Leg Day",
            createdAt = timestamp,
            duration = durationSeconds,
            notes = "Intense leg session. Squats felt heavy but managed all sets.",
            isPartnerWorkout = 0,
            totalVolume = 12450,
            totalSets = 21,
            exerciseCount = 7,
            exerciseNames = "Barbell Squat, Leg Press, Romanian Deadlift, Leg Curl, Leg Extension, Calf Raise, Lateral Raise"
        )

        sessionQueries.insert(
            id = sessionId,
            workoutId = workoutId,
            templateId = null,
            name = "Leg Day",
            startTime = timestamp,
            endTime = endTime,
            status = "completed",
            notes = "Intense leg session. Squats felt heavy but managed all sets.",
            isPartnerWorkout = 0,
            currentExerciseIndex = 0,
            createdAt = timestamp,
            updatedAt = endTime
        )

        addSessionExercise(sessionId, "ex_squat", 0, timestamp,
            listOf(
                SetData(80.0, 10),
                SetData(100.0, 8),
                SetData(110.0, 6),
                SetData(115.0, 5)
            )
        )

        addSessionExercise(sessionId, "ex_leg_press", 1, timestamp,
            listOf(
                SetData(150.0, 12),
                SetData(180.0, 10),
                SetData(200.0, 8)
            )
        )

        addSessionExercise(sessionId, "ex_romanian_deadlift", 2, timestamp,
            listOf(
                SetData(60.0, 12),
                SetData(70.0, 10),
                SetData(80.0, 8)
            )
        )

        addSessionExercise(sessionId, "ex_leg_curl", 3, timestamp,
            listOf(
                SetData(40.0, 12),
                SetData(45.0, 10),
                SetData(50.0, 10)
            )
        )

        addSessionExercise(sessionId, "ex_leg_extension", 4, timestamp,
            listOf(
                SetData(50.0, 15),
                SetData(55.0, 12),
                SetData(60.0, 10)
            )
        )

        addSessionExercise(sessionId, "ex_calf_raise", 5, timestamp,
            listOf(
                SetData(80.0, 15),
                SetData(90.0, 12),
                SetData(100.0, 12)
            )
        )

        addSessionExercise(sessionId, "ex_lateral_raise", 6, timestamp,
            listOf(
                SetData(10.0, 15),
                SetData(12.5, 12)
            )
        )
    }

    private data class SetData(val weight: Double, val reps: Int)

    private fun addSessionExercise(
        sessionId: String,
        exerciseId: String,
        orderIndex: Int,
        baseTimestamp: Long,
        sets: List<SetData>
    ) {
        val sessionExerciseId = "${sessionId}_${exerciseId}"
        val now = baseTimestamp + (orderIndex * 300000) // 5 min apart

        sessionExerciseQueries.insert(
            id = sessionExerciseId,
            sessionId = sessionId,
            exerciseId = exerciseId,
            orderIndex = orderIndex.toLong(),
            targetSets = sets.size.toLong(),
            completedSets = sets.size.toLong(),
            notes = null,
            createdAt = now,
            updatedAt = now
        )

        sets.forEachIndexed { setIndex, setData ->
            val setId = "${sessionExerciseId}_set_${setIndex}"
            val completedAt = now + (setIndex * 60000) // 1 min apart

            setQueries.insert(
                id = setId,
                sessionId = sessionId,
                sessionExerciseId = sessionExerciseId,
                exerciseId = exerciseId,
                setNumber = (setIndex + 1).toLong(),
                weight = setData.weight,
                reps = setData.reps.toLong(),
                rpe = null,
                isWarmup = 0,
                restTime = 90,
                notes = null,
                completedAt = completedAt,
                createdAt = completedAt
            )
        }
    }
}
