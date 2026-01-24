package com.workout.app.ui.screens.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workout.app.data.repository.TemplateRepository
import com.workout.app.domain.model.TemplateExercise
import com.workout.app.ui.components.buttons.AppIconButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.components.chips.FilterChip
import com.workout.app.ui.components.exercise.ExerciseSelectionCard
import com.workout.app.ui.components.exercise.PreviousRecord
import com.workout.app.ui.components.headers.SectionHeader
import com.workout.app.ui.components.navigation.BottomActionBar
import com.workout.app.ui.components.navigation.SessionSummary
import com.workout.app.ui.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Exercise data model for session planning
 */
data class Exercise(
    val id: String,
    val name: String,
    val category: String,
    val muscleGroup: MuscleGroup,
    val history: List<PreviousRecord> = emptyList()
)

/**
 * Muscle group filter categories
 */
enum class MuscleGroup(val displayName: String) {
    ALL("All"),
    CHEST("Chest"),
    BACK("Back"),
    LEGS("Legs"),
    SHOULDERS("Shoulders"),
    ARMS("Arms"),
    CORE("Core")
}

/**
 * State holder for an added exercise
 */
data class AddedExercise(
    val exercise: Exercise,
    val setCount: Int
)

/**
 * Session Planning screen for creating a workout plan.
 * Based on mockup elements EL-37, EL-38, EL-26/27, EL-39/40, EL-45, EL-46, EL-15.
 *
 * Features:
 * - Header with back navigation and title
 * - Templates quick-access button
 * - Muscle group filter chips
 * - Exercise list with add/remove functionality
 * - Session summary showing exercises and total sets
 * - Start Session button in bottom bar
 * - Template pre-loading when templateId is provided
 *
 * @param templateId Optional template ID to pre-populate exercises
 * @param onBackClick Callback when back button is clicked
 * @param onTemplatesClick Callback when templates button is clicked
 * @param onStartSession Callback when start session button is clicked
 * @param modifier Modifier to be applied to the screen
 */
@Composable
fun SessionPlanningScreen(
    templateId: String? = null,
    onBackClick: () -> Unit,
    onTemplatesClick: () -> Unit,
    onStartSession: (List<AddedExercise>) -> Unit,
    modifier: Modifier = Modifier
) {
    val templateRepository: TemplateRepository = koinInject()
    val scope = rememberCoroutineScope()

    // State management
    var selectedMuscleGroup by remember { mutableStateOf(MuscleGroup.ALL) }
    var addedExercises by remember { mutableStateOf<Map<String, AddedExercise>>(emptyMap()) }

    // Mock exercise data
    val exercises = remember { getMockExercises() }

    // Load template exercises if templateId is provided
    LaunchedEffect(templateId) {
        if (templateId != null) {
            val result = templateRepository.getById(templateId)
            if (result is com.workout.app.domain.model.Result.Success && result.data != null) {
                val template = result.data
                val templateExercises = TemplateExercise.fromJsonArray(template.exercises)
                val exerciseMap = exercises.associateBy { it.id }

                addedExercises = templateExercises
                    .mapNotNull { templateExercise ->
                        exerciseMap[templateExercise.exerciseId]?.let { exercise ->
                            exercise.id to AddedExercise(
                                exercise = exercise,
                                setCount = templateExercise.defaultSets
                            )
                        }
                    }
                    .toMap()

                // Update template's last used timestamp
                scope.launch {
                    templateRepository.updateLastUsed(templateId)
                }
            }
        }
    }

    // Filter exercises by selected muscle group
    val filteredExercises = if (selectedMuscleGroup == MuscleGroup.ALL) {
        exercises
    } else {
        exercises.filter { it.muscleGroup == selectedMuscleGroup }
    }

    // Calculate session summary
    val sessionSummary = if (addedExercises.isNotEmpty()) {
        SessionSummary(
            duration = "0:00",
            sets = addedExercises.values.sumOf { it.setCount },
            exercises = addedExercises.size
        )
    } else {
        null
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomActionBar(
                actionText = "Start Session",
                onActionClick = { onStartSession(addedExercises.values.toList()) },
                sessionSummary = sessionSummary,
                actionEnabled = addedExercises.isNotEmpty()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header (EL-37)
            SessionPlanningHeader(
                onBackClick = onBackClick,
                modifier = Modifier.padding(
                    horizontal = AppTheme.spacing.lg,
                    vertical = AppTheme.spacing.md
                )
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

            // Templates Button (EL-38)
            SecondaryButton(
                text = "Browse Templates",
                onClick = onTemplatesClick,
                fullWidth = true,
                modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

            // Filter Chips Section (EL-26/27)
            Column(
                modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
            ) {
                SectionHeader(
                    title = "Muscle Groups",
                    modifier = Modifier.padding(bottom = AppTheme.spacing.md)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    contentPadding = PaddingValues(bottom = AppTheme.spacing.md)
                ) {
                    items(MuscleGroup.entries) { muscleGroup ->
                        FilterChip(
                            text = muscleGroup.displayName,
                            isActive = selectedMuscleGroup == muscleGroup,
                            onClick = { selectedMuscleGroup = muscleGroup }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

            // Exercise Selection Cards (EL-39/40)
            Column(
                modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
            ) {
                SectionHeader(
                    title = "Exercises",
                    modifier = Modifier.padding(bottom = AppTheme.spacing.md)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = AppTheme.spacing.lg,
                    end = AppTheme.spacing.lg,
                    bottom = AppTheme.spacing.lg
                ),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                items(filteredExercises, key = { it.id }) { exercise ->
                    val addedExercise = addedExercises[exercise.id]

                    ExerciseSelectionCard(
                        exerciseName = exercise.name,
                        exerciseCategory = exercise.category,
                        isAdded = addedExercise != null,
                        history = exercise.history,
                        onToggle = {
                            if (addedExercise != null) {
                                addedExercises = addedExercises - exercise.id
                            } else {
                                addedExercises = addedExercises + (exercise.id to AddedExercise(
                                    exercise = exercise,
                                    setCount = 3
                                ))
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Session planning header with back navigation and title.
 * Based on mockup element EL-37.
 */
@Composable
private fun SessionPlanningHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Navigate back",
            onClick = onBackClick,
            tint = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.padding(start = AppTheme.spacing.sm))

        Text(
            text = "Plan Session",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Generate mock exercise data for preview and development
 */
private fun getMockExercises(): List<Exercise> {
    val history = listOf(
        PreviousRecord("Jan 19", "8-10", "85-90 kg"),
        PreviousRecord("Jan 16", "10-12", "80-85 kg"),
        PreviousRecord("Jan 12", "8", "82.5 kg")
    )

    return listOf(
        // Chest exercises
        Exercise(
            id = "1",
            name = "Barbell Bench Press",
            category = "Chest - Compound",
            muscleGroup = MuscleGroup.CHEST,
            history = history
        ),
        Exercise(
            id = "2",
            name = "Dumbbell Fly",
            category = "Chest - Isolation",
            muscleGroup = MuscleGroup.CHEST,
            history = history
        ),
        Exercise(
            id = "3",
            name = "Incline Dumbbell Press",
            category = "Chest - Compound",
            muscleGroup = MuscleGroup.CHEST,
            history = history
        ),

        // Back exercises
        Exercise(
            id = "4",
            name = "Barbell Row",
            category = "Back - Compound",
            muscleGroup = MuscleGroup.BACK,
            history = history
        ),
        Exercise(
            id = "5",
            name = "Pull-ups",
            category = "Back - Compound",
            muscleGroup = MuscleGroup.BACK,
            history = history
        ),
        Exercise(
            id = "6",
            name = "Lat Pulldown",
            category = "Back - Compound",
            muscleGroup = MuscleGroup.BACK,
            history = history
        ),
        Exercise(
            id = "7",
            name = "Seated Cable Row",
            category = "Back - Compound",
            muscleGroup = MuscleGroup.BACK,
            history = history
        ),

        // Legs exercises
        Exercise(
            id = "8",
            name = "Barbell Squat",
            category = "Legs - Compound",
            muscleGroup = MuscleGroup.LEGS,
            history = history
        ),
        Exercise(
            id = "9",
            name = "Romanian Deadlift",
            category = "Legs - Compound",
            muscleGroup = MuscleGroup.LEGS,
            history = history
        ),
        Exercise(
            id = "10",
            name = "Leg Press",
            category = "Legs - Compound",
            muscleGroup = MuscleGroup.LEGS,
            history = history
        ),
        Exercise(
            id = "11",
            name = "Leg Curl",
            category = "Legs - Isolation",
            muscleGroup = MuscleGroup.LEGS,
            history = history
        ),
        Exercise(
            id = "12",
            name = "Leg Extension",
            category = "Legs - Isolation",
            muscleGroup = MuscleGroup.LEGS,
            history = history
        ),

        // Shoulders exercises
        Exercise(
            id = "13",
            name = "Overhead Press",
            category = "Shoulders - Compound",
            muscleGroup = MuscleGroup.SHOULDERS,
            history = history
        ),
        Exercise(
            id = "14",
            name = "Lateral Raise",
            category = "Shoulders - Isolation",
            muscleGroup = MuscleGroup.SHOULDERS,
            history = history
        ),
        Exercise(
            id = "15",
            name = "Face Pulls",
            category = "Shoulders - Isolation",
            muscleGroup = MuscleGroup.SHOULDERS,
            history = history
        ),

        // Arms exercises
        Exercise(
            id = "16",
            name = "Barbell Curl",
            category = "Arms - Isolation",
            muscleGroup = MuscleGroup.ARMS,
            history = history
        ),
        Exercise(
            id = "17",
            name = "Tricep Pushdown",
            category = "Arms - Isolation",
            muscleGroup = MuscleGroup.ARMS,
            history = history
        ),
        Exercise(
            id = "18",
            name = "Hammer Curl",
            category = "Arms - Isolation",
            muscleGroup = MuscleGroup.ARMS,
            history = history
        ),
        Exercise(
            id = "19",
            name = "Overhead Tricep Extension",
            category = "Arms - Isolation",
            muscleGroup = MuscleGroup.ARMS,
            history = history
        ),

        // Core exercises
        Exercise(
            id = "20",
            name = "Plank",
            category = "Core - Stability",
            muscleGroup = MuscleGroup.CORE,
            history = history
        ),
        Exercise(
            id = "21",
            name = "Cable Crunch",
            category = "Core - Isolation",
            muscleGroup = MuscleGroup.CORE,
            history = history
        ),
        Exercise(
            id = "22",
            name = "Russian Twist",
            category = "Core - Rotation",
            muscleGroup = MuscleGroup.CORE,
            history = history
        )
    )
}
