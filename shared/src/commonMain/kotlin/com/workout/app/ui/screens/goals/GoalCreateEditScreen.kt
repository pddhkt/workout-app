package com.workout.app.ui.screens.goals

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.GoalFrequency
import com.workout.app.domain.model.GoalMetric
import com.workout.app.presentation.goals.ExerciseSelection
import com.workout.app.presentation.goals.GoalCreateEditViewModel
import com.workout.app.presentation.goals.GoalFormState
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.chips.FilterChip
import com.workout.app.ui.components.inputs.AppTextField
import com.workout.app.ui.theme.AppTheme
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * Goal Create/Edit Screen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GoalCreateEditScreen(
    goalId: String?,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GoalCreateEditViewModel = koinInject { parametersOf(goalId) }
    val state by viewModel.state.collectAsState()
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Edit Goal" else "New Goal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
            ) {
                item { Spacer(modifier = Modifier.height(AppTheme.spacing.sm)) }

                // Goal Name
                item {
                    FormSection(title = "Goal Name") {
                        AppTextField(
                            value = state.name,
                            onValueChange = { viewModel.updateName(it) },
                            placeholder = "e.g. Run 25km per week",
                            modifier = Modifier.fillMaxWidth(),
                            isError = hasAttemptedSubmit && state.name.isBlank()
                        )
                        if (hasAttemptedSubmit && state.nameError != null) {
                            Text(
                                text = state.nameError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Exercise Selection
                item {
                    FormSection(title = "Linked Exercises") {
                        if (state.selectedExercises.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                                state.selectedExercises.forEach { exercise ->
                                    ExerciseRow(
                                        exercise = exercise,
                                        isSelected = true,
                                        onClick = { viewModel.toggleExercise(exercise.id) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                        }

                        if (hasAttemptedSubmit && state.exerciseError != null) {
                            Text(
                                text = state.exerciseError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                        }

                        Text(
                            text = if (state.showExercisePicker) "Hide exercises" else "Select exercises...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { viewModel.toggleShowExercisePicker() }
                        )
                    }
                }

                // Exercise picker list (expandable)
                if (state.showExercisePicker) {
                    val unselected = state.availableExercises.filter { it.id !in state.selectedExerciseIds }
                    items(unselected, key = { "picker_${it.id}" }) { exercise ->
                        ExerciseRow(
                            exercise = exercise,
                            isSelected = false,
                            onClick = { viewModel.toggleExercise(exercise.id) }
                        )
                    }
                }

                // Metric Selection
                item {
                    FormSection(title = "Target Metric") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                        ) {
                            state.availableMetrics.forEach { metric ->
                                FilterChip(
                                    text = metric.label,
                                    isActive = state.metric == metric,
                                    onClick = { viewModel.setMetric(metric) }
                                )
                            }
                        }
                    }
                }

                // Target Value
                item {
                    FormSection(title = "Target Value") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppTextField(
                                value = state.targetValue,
                                onValueChange = { viewModel.updateTargetValue(it) },
                                placeholder = "e.g. 25",
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Decimal,
                                isError = hasAttemptedSubmit && state.targetError != null
                            )
                            Text(
                                text = state.targetUnit,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (hasAttemptedSubmit && state.targetError != null) {
                            Text(
                                text = state.targetError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Frequency
                item {
                    FormSection(title = "Frequency") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                        ) {
                            GoalFrequency.entries.forEach { freq ->
                                FilterChip(
                                    text = freq.label,
                                    isActive = state.frequency == freq,
                                    onClick = { viewModel.setFrequency(freq) }
                                )
                            }
                        }
                    }
                }

                // Duration
                item {
                    FormSection(title = "Duration") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ongoing (no end date)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = state.isOngoing,
                                onCheckedChange = { viewModel.setOngoing(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                // Auto-track toggle
                item {
                    FormSection(title = "Auto-tracking") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-track from workouts",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Progress updates automatically when you complete linked exercises",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.autoTrack,
                                onCheckedChange = { viewModel.setAutoTrack(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                // Save button
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    PrimaryButton(
                        text = if (state.isEditing) "Save Changes" else "Create Goal",
                        onClick = {
                            hasAttemptedSubmit = true
                            viewModel.save { onSaved() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )

                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                        Text(
                            text = state.error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
                }
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        content()
    }
}

@Composable
private fun ExerciseRow(
    exercise: ExerciseSelection,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = exercise.muscleGroup,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = AppTheme.spacing.sm)
            )
        }
    }
}
