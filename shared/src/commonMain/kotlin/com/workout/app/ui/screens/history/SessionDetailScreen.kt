package com.workout.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.Participant
import com.workout.app.presentation.history.SessionDetailState
import com.workout.app.presentation.history.SessionDetailViewModel
import com.workout.app.ui.components.buttons.ParticipantSelector
import com.workout.app.ui.components.dataviz.MetricsGrid
import com.workout.app.ui.components.dataviz.MusclesTargetedSection
import com.workout.app.ui.components.exercise.ExerciseBreakdownSection
import com.workout.app.ui.components.inputs.RPEProgressDisplay
import com.workout.app.ui.theme.AppTheme
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * Session Detail Screen - displays historical workout details in read-only mode.
 *
 * Features:
 * - Workout summary header with date/time
 * - Metrics grid (duration, volume, sets, PRs)
 * - Muscles targeted with intensity bars
 * - Collapsible exercise breakdown with sets
 * - Me/Partner toggle for partner workouts
 * - Session notes and RPE display
 *
 * @param workoutId ID of the workout to display
 * @param onBackClick Callback when back button is pressed
 * @param onShareClick Callback when share button is pressed
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    workoutId: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: SessionDetailViewModel = koinInject { parametersOf(workoutId) }
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Session Details",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
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
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            state.error != null -> {
                ErrorState(
                    error = state.error!!,
                    onRetry = viewModel::refresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            state.workout != null -> {
                SessionDetailContent(
                    state = state,
                    onParticipantSelect = viewModel::selectParticipant,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SessionDetailContent(
    state: SessionDetailState,
    onParticipantSelect: (Participant) -> Unit,
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

        // Completion header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = workout.name,
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

        // Notes section (if available)
        if (!workout.notes.isNullOrBlank()) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Session Notes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                Text(
                    text = workout.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
        }

        // RPE display (if available)
        if (workout.rpe != null) {
            RPEProgressDisplay(
                rpe = workout.rpe,
                onRpeChange = null,
                label = "Session Difficulty",
                interactive = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Text(
                text = "Error loading session",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            com.workout.app.ui.components.buttons.PrimaryButton(
                text = "Retry",
                onClick = onRetry
            )
        }
    }
}
