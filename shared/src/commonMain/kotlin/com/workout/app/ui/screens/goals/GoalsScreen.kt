package com.workout.app.ui.screens.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.GoalWithProgress
import com.workout.app.presentation.goals.GoalsState
import com.workout.app.presentation.goals.GoalsViewModel
import com.workout.app.ui.components.goals.GoalCard
import com.workout.app.ui.theme.AppTheme
import org.koin.compose.koinInject

/**
 * Goals management screen with ViewModel integration.
 */
@Composable
fun GoalsScreenWithViewModel(
    onGoalClick: (String) -> Unit,
    onCreateGoal: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: GoalsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    GoalsScreen(
        state = state,
        onGoalClick = onGoalClick,
        onCreateGoal = onCreateGoal,
        onToggleActive = { goalId, active -> viewModel.toggleGoalActive(goalId, active) },
        onDeleteGoal = viewModel::deleteGoal,
        onCloneGoal = viewModel::cloneGoal,
        onBackClick = onBackClick
    )
}

/**
 * Goals management screen.
 * Shows active, paused, and completed goals with management actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    state: GoalsState,
    onGoalClick: (String) -> Unit,
    onCreateGoal: () -> Unit,
    onToggleActive: (String, Boolean) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onCloneGoal: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Goals",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateGoal,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "New Goal"
                )
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (state.activeGoals.isEmpty() && state.pausedGoals.isEmpty() && state.completedGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    Text(
                        text = "No goals yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap + to create your first goal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                // Active Goals
                if (state.activeGoals.isNotEmpty()) {
                    item {
                        GoalSectionHeader(
                            title = "Active",
                            count = state.activeGoals.size
                        )
                    }
                    items(state.activeGoals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onClick = { onGoalClick(goal.id) },
                            onToggleActive = { active ->
                                onToggleActive(goal.id, active)
                            }
                        )
                    }
                }

                // Paused Goals
                if (state.pausedGoals.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                        GoalSectionHeader(
                            title = "Paused",
                            count = state.pausedGoals.size
                        )
                    }
                    items(state.pausedGoals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onClick = { onGoalClick(goal.id) },
                            onToggleActive = { active ->
                                onToggleActive(goal.id, active)
                            }
                        )
                    }
                }

                // Completed Goals
                if (state.completedGoals.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                        GoalSectionHeader(
                            title = "Completed",
                            count = state.completedGoals.size
                        )
                    }
                    items(state.completedGoals, key = { it.id }) { goal ->
                        CompletedGoalCard(
                            goal = goal,
                            onClick = { onGoalClick(goal.id) },
                            onClone = { onCloneGoal(goal.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalSectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$title ($count)",
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(vertical = AppTheme.spacing.xs)
    )
}

@Composable
private fun CompletedGoalCard(
    goal: GoalWithProgress,
    onClick: () -> Unit,
    onClone: () -> Unit,
    modifier: Modifier = Modifier
) {
    GoalCard(
        goal = goal,
        onClick = onClick,
        modifier = modifier
    )
}
