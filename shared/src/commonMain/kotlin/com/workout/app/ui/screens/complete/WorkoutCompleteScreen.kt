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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.components.buttons.ToggleButton
import com.workout.app.ui.components.chips.FilterChip
import com.workout.app.ui.components.headers.SectionHeader
import com.workout.app.ui.components.inputs.NotesInput
import com.workout.app.ui.theme.AppTheme

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
                color = MaterialTheme.colorScheme.primary,
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
