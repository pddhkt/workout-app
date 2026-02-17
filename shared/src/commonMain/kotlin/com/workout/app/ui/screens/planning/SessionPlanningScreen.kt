package com.workout.app.ui.screens.planning

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.data.repository.TemplateRepository
import com.workout.app.domain.model.TemplateExercise
import com.workout.app.presentation.planning.SessionPlanningState
import com.workout.app.ui.components.buttons.AppIconButton
import com.workout.app.ui.components.exercise.ExerciseSelectionCard
import com.workout.app.ui.components.navigation.BottomActionBar
import com.workout.app.ui.components.navigation.SessionSummary
import com.workout.app.ui.components.recovery.MuscleRecoveryCard
import com.workout.app.ui.components.planning.AddParticipantSheet
import com.workout.app.ui.components.planning.SessionModeSection
import com.workout.app.ui.components.templates.TemplateListItem
import com.workout.app.ui.theme.AppTheme
import com.workout.app.domain.model.SessionMode
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
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
 *
 * Features:
 * - Header with back navigation, title, and templates icon button
 * - Accordion muscle groups card with recovery bars and filter selection
 * - Exercise list with add/remove functionality
 * - Session summary showing exercises and total sets
 * - Start Session button in bottom bar
 * - Template pre-loading when templateId is provided
 */
@Composable
fun SessionPlanningScreen(
    state: SessionPlanningState,
    templateId: String? = null,
    onBackClick: () -> Unit,
    onStartSession: () -> Unit,
    onToggleExercise: (String) -> Unit,
    onAddExercise: (String, Int) -> Unit,
    onToggleTimeRange: () -> Unit,
    onModeSelected: (SessionMode) -> Unit = {},
    onAddParticipant: (String) -> Unit = {},
    onRemoveParticipant: (String) -> Unit = {},
    onShowAddParticipantSheet: () -> Unit = {},
    onHideAddParticipantSheet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val templateRepository: TemplateRepository = koinInject()
    val scope = rememberCoroutineScope()

    // Local UI state for muscle group filter
    var selectedMuscleGroup by remember { mutableStateOf<String?>(null) }

    // Tab state: 0 = Exercises, 1 = Templates
    var selectedTab by remember { mutableIntStateOf(0) }

    // Observe templates for the Templates tab
    val templates by templateRepository.observeAll().collectAsState(initial = emptyList())

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
    val filteredExercises = if (selectedMuscleGroup == null) {
        state.allExercises
    } else {
        state.allExercises.filter {
            it.muscleGroup.equals(selectedMuscleGroup, ignoreCase = true)
        }
    }

    // Exercise header label
    val exerciseHeaderTitle = if (selectedMuscleGroup != null) {
        "Exercises ($selectedMuscleGroup)"
    } else {
        "Exercises"
    }

    // Calculate session summary (estimate 5 minutes per set)
    val sessionSummary = if (state.addedExercises.isNotEmpty()) {
        val estimatedMinutes = state.totalSets * 5
        val hours = estimatedMinutes / 60
        val mins = estimatedMinutes % 60
        val durationText = if (hours > 0) "$hours:${mins.toString().padStart(2, '0')}" else "$mins:00"
        SessionSummary(
            duration = durationText,
            sets = state.totalSets,
            exercises = state.addedExercises.size
        )
    } else {
        null
    }

    // Build list of selected exercise names (in addition order)
    val exerciseMap = state.allExercises.associateBy { it.id }
    val selectedExerciseNames = state.addedExercises.keys.mapNotNull { id ->
        exerciseMap[id]?.name
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomActionBar(
                actionText = "Start Session",
                onActionClick = onStartSession,
                exerciseNames = selectedExerciseNames,
                sessionSummary = sessionSummary,
                actionEnabled = state.canStartSession
            )
        }
    ) { paddingValues ->
        @OptIn(ExperimentalFoundationApi::class)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            // Header
            item(key = "header") {
                SessionPlanningHeader(
                    onBackClick = onBackClick,
                    modifier = Modifier.padding(
                        horizontal = AppTheme.spacing.lg,
                        vertical = AppTheme.spacing.md
                    )
                )
            }

            // Session Mode Selector + Participants
            item(key = "session_mode") {
                SessionModeSection(
                    sessionMode = state.sessionMode,
                    participants = state.participants,
                    helperText = state.participantHelperText,
                    onModeSelected = onModeSelected,
                    onAddParticipantClick = onShowAddParticipantSheet,
                    onRemoveParticipant = onRemoveParticipant,
                    modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            }

            // Muscle Recovery Card
            item(key = "recovery") {
                MuscleRecoveryCard(
                    muscleRecoveryList = state.muscleRecovery,
                    selectedMuscleGroup = selectedMuscleGroup,
                    onMuscleGroupSelected = { selectedMuscleGroup = it },
                    modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            }

            // Sticky tab row
            stickyHeader(key = "tab_header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = if (selectedMuscleGroup != null)
                                        "Exercises ($selectedMuscleGroup)"
                                    else "Exercises",
                                    fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.onBackground,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = "Templates",
                                    fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.onBackground,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                // Exercise items
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
                        history = emptyList(),
                        onToggle = { onToggleExercise(exercise.id) },
                        modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                    )
                }
            } else {
                // Template items
                if (templates.isEmpty()) {
                    item(key = "empty_templates") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = AppTheme.spacing.lg,
                                    vertical = AppTheme.spacing.xl
                                ),
                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                        ) {
                            Text(
                                text = "No templates yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Create templates from the Templates screen to quickly load exercises.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(templates, key = { it.id }) { template ->
                        val exerciseCount = TemplateExercise.fromJsonArray(template.exercises).size
                        val lastUsedFormatted = formatLastUsed(template.lastUsed)

                        TemplateListItem(
                            name = template.name,
                            exerciseCount = exerciseCount,
                            lastUsed = lastUsedFormatted,
                            isFavorite = template.isFavorite == 1L,
                            onClick = {
                                // Load template exercises into the session
                                val templateExercises = TemplateExercise.fromJsonArray(template.exercises)
                                val exMap = state.allExercises.associateBy { it.id }
                                templateExercises.forEach { te ->
                                    if (exMap.containsKey(te.exerciseId) &&
                                        !state.addedExercises.containsKey(te.exerciseId)
                                    ) {
                                        onAddExercise(te.exerciseId, te.defaultSets)
                                    }
                                }
                                scope.launch {
                                    templateRepository.updateLastUsed(template.id)
                                }
                                // Switch back to exercises tab
                                selectedTab = 0
                            },
                            onLongPress = {},
                            onFavoriteClick = {
                                scope.launch {
                                    templateRepository.toggleFavorite(template.id)
                                }
                            },
                            modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                        )
                    }
                }
            }
        }
    }

    // Add Participant Bottom Sheet
    AddParticipantSheet(
        visible = state.showAddParticipantSheet,
        recentPartners = state.recentPartners,
        existingNames = state.participants.map { it.name },
        onAddParticipant = onAddParticipant,
        onDismiss = onHideAddParticipantSheet
    )
}

/**
 * Session planning header with back navigation and title.
 */
@Composable
private fun SessionPlanningHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
 * Format lastUsed timestamp to readable string
 */
private fun formatLastUsed(lastUsed: Long?): String? {
    if (lastUsed == null) return null
    val now = Clock.System.now().toEpochMilliseconds()
    val diff = now - lastUsed
    val days = diff / (1000 * 60 * 60 * 24)
    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        days < 30 -> "${days / 7} weeks ago"
        else -> "${days / 30} months ago"
    }
}
