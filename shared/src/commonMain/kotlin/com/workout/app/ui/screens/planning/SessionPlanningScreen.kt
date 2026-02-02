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
import com.workout.app.data.repository.TemplateRepository
import com.workout.app.domain.model.TemplateExercise
import com.workout.app.presentation.planning.SessionPlanningState
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
 * @param state The ViewModel state containing exercises and added exercises
 * @param templateId Optional template ID to pre-populate exercises
 * @param onBackClick Callback when back button is clicked
 * @param onTemplatesClick Callback when templates button is clicked
 * @param onStartSession Callback when start session button is clicked
 * @param onToggleExercise Callback when exercise is toggled (added/removed)
 * @param onAddExercise Callback to add an exercise with specific set count
 * @param modifier Modifier to be applied to the screen
 */
@Composable
fun SessionPlanningScreen(
    state: SessionPlanningState,
    templateId: String? = null,
    onBackClick: () -> Unit,
    onTemplatesClick: () -> Unit,
    onStartSession: () -> Unit,
    onToggleExercise: (String) -> Unit,
    onAddExercise: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val templateRepository: TemplateRepository = koinInject()
    val scope = rememberCoroutineScope()

    // Local UI state for muscle group filter
    var selectedMuscleGroup by remember { mutableStateOf(MuscleGroup.ALL) }

    // Track if template has been loaded to avoid reloading
    var templateLoaded by remember { mutableStateOf(false) }

    // Load template exercises if templateId is provided (only once)
    LaunchedEffect(templateId, state.allExercises) {
        if (templateId != null && state.allExercises.isNotEmpty() && !templateLoaded) {
            val result = templateRepository.getById(templateId)
            if (result is com.workout.app.domain.model.Result.Success && result.data != null) {
                val template = result.data
                val templateExercises = TemplateExercise.fromJsonArray(template.exercises)
                val exerciseMap = state.allExercises.associateBy { it.id }

                // Add each template exercise to the ViewModel
                templateExercises.forEach { templateExercise ->
                    if (exerciseMap.containsKey(templateExercise.exerciseId)) {
                        onAddExercise(templateExercise.exerciseId, templateExercise.defaultSets)
                    }
                }

                templateLoaded = true

                // Update template's last used timestamp
                scope.launch {
                    templateRepository.updateLastUsed(templateId)
                }
            }
        }
    }

    // Filter exercises by selected muscle group
    val filteredExercises = if (selectedMuscleGroup == MuscleGroup.ALL) {
        state.allExercises
    } else {
        state.allExercises.filter {
            it.muscleGroup.equals(selectedMuscleGroup.name, ignoreCase = true)
        }
    }

    // Calculate session summary
    val sessionSummary = if (state.addedExercises.isNotEmpty()) {
        SessionSummary(
            duration = "0:00",
            sets = state.totalSets,
            exercises = state.addedExercises.size
        )
    } else {
        null
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomActionBar(
                actionText = "Start Session",
                onActionClick = onStartSession,
                sessionSummary = sessionSummary,
                actionEnabled = state.canStartSession
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
                    val isAdded = state.addedExercises.containsKey(exercise.id)
                    val categoryDisplay = listOfNotNull(
                        exercise.muscleGroup,
                        exercise.category
                    ).joinToString(" - ")

                    ExerciseSelectionCard(
                        exerciseName = exercise.name,
                        exerciseCategory = categoryDisplay,
                        isAdded = isAdded,
                        history = emptyList(), // TODO: Load history from repository
                        onToggle = { onToggleExercise(exercise.id) }
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

