package com.workout.app.ui.screens.complete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.Participant
import com.workout.app.presentation.complete.WorkoutCompleteState
import com.workout.app.presentation.complete.WorkoutCompleteViewModel
import com.workout.app.ui.components.buttons.ParticipantSelector
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.components.buttons.ToggleButton
import com.workout.app.ui.components.chips.FilterChip
import com.workout.app.ui.components.dataviz.MetricsGrid
import com.workout.app.ui.components.dataviz.MusclesTargetedSection
import com.workout.app.ui.components.exercise.ExerciseBreakdownSection
import com.workout.app.ui.components.headers.SectionHeader
import com.workout.app.ui.components.inputs.NotesInput
import com.workout.app.ui.components.inputs.RPEProgressDisplay
import com.workout.app.ui.theme.AppTheme
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * Data class representing workout summary information
 *
 * @param duration Total workout duration in seconds
 * @param totalVolume Total volume lifted in kg
 * @param exerciseCount Number of exercises completed
 * @param setCount Total number of sets completed
 * @param muscleGroups List of muscle groups worked during the workout
 */
data class WorkoutSummary(
    val duration: Int,
    val totalVolume: Float,
    val exerciseCount: Int,
    val setCount: Int,
    val muscleGroups: List<String>
)

/**
 * Workout Complete celebration screen
 * Displays workout statistics, muscle groups worked, and allows adding notes
 * Based on mockup screen AN-11 and elements EL-16, EL-22, EL-78, EL-12
 *
 * @param summary Workout summary statistics
 * @param selectedMuscleGroups Currently selected muscle groups for highlighting
 * @param onMuscleGroupToggle Callback when muscle group chip is toggled
 * @param partnerModeEnabled Whether partner mode is enabled
 * @param onPartnerModeToggle Callback when partner mode toggle is clicked
 * @param notes Current notes text
 * @param onNotesChange Callback when notes text changes
 * @param onSave Callback when save button is clicked
 * @param onDone Callback when done button is clicked
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutCompleteScreen(
    summary: WorkoutSummary,
    selectedMuscleGroups: Set<String>,
    onMuscleGroupToggle: (String) -> Unit,
    partnerModeEnabled: Boolean,
    onPartnerModeToggle: () -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(AppTheme.spacing.lg)
    ) {
        // Celebration header
        CelebrationHeader(
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Workout stats summary
        WorkoutStatsSection(
            summary = summary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))

        // Partner mode toggle
        PartnerModeSection(
            enabled = partnerModeEnabled,
            onToggle = onPartnerModeToggle,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Muscle groups worked section
        SectionHeader(
            title = "Muscle Groups Worked",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            summary.muscleGroups.forEach { muscleGroup ->
                FilterChip(
                    text = muscleGroup,
                    isActive = selectedMuscleGroups.contains(muscleGroup),
                    onClick = { onMuscleGroupToggle(muscleGroup) }
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Notes input section
        SectionHeader(
            title = "Session Notes",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        NotesInput(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = "How did the workout feel? Any observations?",
            minLines = 4,
            maxLines = 6,
            maxCharacters = 500,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            SecondaryButton(
                text = "Save Draft",
                onClick = onSave,
                modifier = Modifier.weight(1f)
            )

            PrimaryButton(
                text = "Done",
                onClick = onDone,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
    }
}

/**
 * Celebration header component showing success message
 */
@Composable
private fun CelebrationHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        Text(
            text = "Workout Complete!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

        Text(
            text = "Great job! Here's your workout summary.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Workout statistics summary section
 * Displays duration, volume, exercises, and sets in a grid layout
 */
@Composable
private fun WorkoutStatsSection(
    summary: WorkoutSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        // First row: Duration and Volume
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            StatCard(
                label = "Duration",
                value = formatDuration(summary.duration),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                label = "Total Volume",
                value = "${(summary.totalVolume * 10).toInt() / 10.0} kg",
                modifier = Modifier.weight(1f)
            )
        }

        // Second row: Exercises and Sets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            StatCard(
                label = "Exercises",
                value = summary.exerciseCount.toString(),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                label = "Sets Completed",
                value = summary.setCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual stat card component
 */
@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(AppTheme.spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = AppTheme.colors.primaryText,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Partner mode toggle section
 * Allows toggling partner mode for dual workouts
 */
@Composable
private fun PartnerModeSection(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        SectionHeader(
            title = "Workout Mode"
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToggleButton(
                text = "Solo",
                selected = !enabled,
                onClick = { if (enabled) onToggle() },
                modifier = Modifier.weight(1f)
            )

            ToggleButton(
                text = "Partner",
                selected = enabled,
                onClick = { if (!enabled) onToggle() },
                modifier = Modifier.weight(1f)
            )
        }

        if (enabled) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

            Text(
                text = "This workout will be saved for both partners",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Formats duration in seconds to MM:SS format
 */
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
}

/**
 * Enhanced Workout Complete Screen with ViewModel integration.
 * Provides a richer experience with metrics grid, muscle intensity bars,
 * exercise breakdown, RPE selector, and save as template functionality.
 *
 * @param sessionId ID of the completed session
 * @param onDoneClick Callback when workout is finalized
 * @param onSaveDraft Callback when save draft is clicked
 * @param onShareClick Callback when share is clicked
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedWorkoutCompleteScreen(
    sessionId: String,
    onDoneClick: () -> Unit,
    onSaveDraft: () -> Unit = {},
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: WorkoutCompleteViewModel = koinInject { parametersOf(sessionId) }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show template saved notification
    LaunchedEffect(state.templateSaved) {
        if (state.templateSaved) {
            snackbarHostState.showSnackbar("Template saved successfully!")
            viewModel.dismissTemplateSaved()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Summary") },
                navigationIcon = {
                    IconButton(onClick = onDoneClick) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.workout != null -> {
                EnhancedWorkoutCompleteContent(
                    state = state,
                    onParticipantSelect = viewModel::selectParticipant,
                    onNotesChange = viewModel::updateNotes,
                    onRpeChange = viewModel::updateRpe,
                    onSaveAsTemplate = viewModel::saveAsTemplate,
                    onShare = onShareClick,
                    onFinish = onDoneClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            else -> {
                // Fallback to simple mock version
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No workout data available")
                }
            }
        }
    }
}

@Composable
private fun EnhancedWorkoutCompleteContent(
    state: WorkoutCompleteState,
    onParticipantSelect: (Participant) -> Unit,
    onNotesChange: (String) -> Unit,
    onRpeChange: (Int) -> Unit,
    onSaveAsTemplate: () -> Unit,
    onShare: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val workout = state.workout!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(AppTheme.spacing.lg)
    ) {
        // Partner selector (only for partner workouts)
        if (state.isPartnerWorkout) {
            ParticipantSelector(
                selectedParticipant = state.selectedParticipant,
                onSelect = onParticipantSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.xxl)
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
        }

        // Celebration header with time
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Session Complete!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

            Text(
                text = "${state.formattedStartTime} - ${state.formattedEndTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Metrics grid
        MetricsGrid(
            duration = workout.duration,
            volume = workout.totalVolume,
            sets = workout.totalSets,
            prCount = workout.prCount,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))

        // Muscles targeted section
        if (workout.muscleGroups.isNotEmpty()) {
            MusclesTargetedSection(
                muscleGroups = workout.muscleGroups,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
        }

        // Exercise breakdown section
        if (workout.exercises.isNotEmpty()) {
            ExerciseBreakdownSection(
                exercises = workout.exercises,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
        }

        // RPE Section
        RPEProgressDisplay(
            rpe = state.rpe ?: 5,
            onRpeChange = onRpeChange,
            label = "How hard was this session?",
            interactive = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))

        // Notes section
        SectionHeader(
            title = "Session Notes",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        NotesInput(
            value = state.notes,
            onValueChange = onNotesChange,
            placeholder = "How did the workout feel? Any observations?",
            minLines = 3,
            maxLines = 5,
            maxCharacters = 500,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))

        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            SecondaryButton(
                text = "Save as Template",
                onClick = onSaveAsTemplate,
                enabled = !state.isSavingTemplate,
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryButton(
                text = "Share",
                onClick = onShare,
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryButton(
                text = "Close",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
    }
}

/**
 * Simple version of WorkoutCompleteScreen for navigation compatibility.
 * Uses minimal callbacks and mock data for basic functionality.
 */
@Composable
fun WorkoutCompleteScreen(
    onDoneClick: () -> Unit,
    onSaveDraft: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Use mock data for simple version
    var notes by remember { mutableStateOf("") }
    var partnerModeEnabled by remember { mutableStateOf(false) }
    var selectedMuscleGroups by remember { mutableStateOf(setOf<String>()) }

    val mockSummary = remember {
        WorkoutSummary(
            duration = 3600, // 60 minutes
            totalVolume = 5000f,
            exerciseCount = 6,
            setCount = 18,
            muscleGroups = listOf("Chest", "Triceps", "Shoulders")
        )
    }

    WorkoutCompleteScreen(
        summary = mockSummary,
        selectedMuscleGroups = selectedMuscleGroups,
        onMuscleGroupToggle = { muscleGroup ->
            selectedMuscleGroups = if (selectedMuscleGroups.contains(muscleGroup)) {
                selectedMuscleGroups - muscleGroup
            } else {
                selectedMuscleGroups + muscleGroup
            }
        },
        partnerModeEnabled = partnerModeEnabled,
        onPartnerModeToggle = { partnerModeEnabled = !partnerModeEnabled },
        notes = notes,
        onNotesChange = { notes = it },
        onSave = onSaveDraft,
        onDone = onDoneClick,
        modifier = modifier
    )
}

/**
 * Preview composable for WorkoutCompleteScreen
 */
@Composable
fun WorkoutCompleteScreenPreview() {
    com.workout.app.ui.theme.WorkoutAppTheme {
        WorkoutCompleteScreen(
            summary = WorkoutSummary(
                duration = 3725, // 62:05
                totalVolume = 2450.5f,
                exerciseCount = 6,
                setCount = 18,
                muscleGroups = listOf(
                    "Chest",
                    "Triceps",
                    "Shoulders",
                    "Core"
                )
            ),
            selectedMuscleGroups = setOf("Chest", "Triceps"),
            onMuscleGroupToggle = {},
            partnerModeEnabled = false,
            onPartnerModeToggle = {},
            notes = "Great workout today! Felt strong on bench press.",
            onNotesChange = {},
            onSave = {},
            onDone = {}
        )
    }
}
