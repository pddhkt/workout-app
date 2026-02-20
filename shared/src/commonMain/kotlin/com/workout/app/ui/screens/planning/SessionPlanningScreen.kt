package com.workout.app.ui.screens.planning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.data.repository.TemplateRepository
import com.workout.app.domain.model.TemplateExercise
import com.workout.app.presentation.planning.SessionPlanningState
import com.workout.app.ui.components.buttons.AppIconButton
import com.workout.app.ui.components.exercise.ExerciseSelectionCard
import com.workout.app.ui.components.navigation.BottomActionBar
import com.workout.app.ui.components.navigation.SessionSummary
import com.workout.app.domain.model.MuscleRecovery
import com.workout.app.ui.components.recovery.MuscleRecoveryCard
import com.workout.app.ui.components.recovery.MuscleRecoveryDetailView
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
 * Equipment filter categories
 */
enum class EquipmentFilter(val displayName: String) {
    ALL("All"),
    BARBELL("Barbell"),
    DUMBBELLS("Dumbbells"),
    CABLE("Cable"),
    MACHINE("Machine"),
    BODYWEIGHT("Bodyweight")
}

/**
 * Movement type (category) filter
 */
enum class MovementFilter(val displayName: String) {
    ALL("All"),
    COMPOUND("Compound"),
    ISOLATION("Isolation"),
    STABILITY("Stability"),
    ROTATION("Rotation")
}

/**
 * Session Planning screen for creating a workout plan.
 *
 * Features:
 * - Header with back navigation, title, and templates icon button
 * - Accordion muscle groups card with recovery bars and filter selection
 * - Search bar and equipment/movement filter chips
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

    // Local state for muscle recovery detail overlay
    var detailMuscle by remember { mutableStateOf<MuscleRecovery?>(null) }

    // Local UI state for search and filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedEquipment by remember { mutableStateOf<String?>(null) }
    var selectedMovement by remember { mutableStateOf<String?>(null) }

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

    // Filter exercises by all criteria (AND together)
    val filteredExercises = state.allExercises.filter { exercise ->
        val matchesMuscleGroup = selectedMuscleGroup == null ||
            exercise.muscleGroup.equals(selectedMuscleGroup, ignoreCase = true)
        val matchesEquipment = selectedEquipment == null ||
            exercise.equipment?.equals(selectedEquipment, ignoreCase = true) == true
        val matchesMovement = selectedMovement == null ||
            exercise.category?.equals(selectedMovement, ignoreCase = true) == true
        val matchesSearch = searchQuery.isBlank() ||
            exercise.name.contains(searchQuery, ignoreCase = true)
        matchesMuscleGroup && matchesEquipment && matchesMovement && matchesSearch
    }

    // Selected exercise count for tab badge
    val selectedCount = state.addedExercises.size

    // Calculate session summary (estimate 5 minutes per set)
    val sessionSummary = if (state.addedExercises.isNotEmpty()) {
        val estimatedMinutes = state.totalSets * 5
        val hours = estimatedMinutes / 60
        val mins = estimatedMinutes % 60
        val durationText = if (hours > 0) "${hours}h${mins}m" else "${mins}m"
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

    // Animate bottom bar in from the bottom on screen entry
    var bottomBarVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { bottomBarVisible = true }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = bottomBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(200)
                ) + fadeOut(tween(200))
            ) {
                BottomActionBar(
                    actionText = "Start Session",
                    onActionClick = onStartSession,
                    exerciseNames = selectedExerciseNames,
                    sessionSummary = sessionSummary,
                    actionEnabled = state.canStartSession,
                    isLoading = state.isCreatingSession
                )
            }
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
                    onDetailClick = { detailMuscle = it },
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
                                    text = if (selectedCount > 0)
                                        "Exercises ($selectedCount)"
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
                // Search bar
                item(key = "search_bar") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search exercises...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppTheme.spacing.lg)
                    )
                }

                // Equipment filter chips
                item(key = "equipment_chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = AppTheme.spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                    ) {
                        EquipmentFilter.entries.forEach { filter ->
                            val isActive = when (filter) {
                                EquipmentFilter.ALL -> selectedEquipment == null
                                else -> selectedEquipment.equals(filter.displayName, ignoreCase = true)
                            }
                            FilterChip(
                                text = filter.displayName,
                                isActive = isActive,
                                onClick = {
                                    selectedEquipment = if (filter == EquipmentFilter.ALL) null
                                    else filter.displayName
                                }
                            )
                        }
                    }
                }

                // Movement type filter chips
                item(key = "movement_chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = AppTheme.spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                    ) {
                        MovementFilter.entries.forEach { filter ->
                            val isActive = when (filter) {
                                MovementFilter.ALL -> selectedMovement == null
                                else -> selectedMovement.equals(filter.displayName, ignoreCase = true)
                            }
                            FilterChip(
                                text = filter.displayName,
                                isActive = isActive,
                                onClick = {
                                    selectedMovement = if (filter == MovementFilter.ALL) null
                                    else filter.displayName
                                }
                            )
                        }
                    }
                }

                if (state.isLoading) {
                    // Loading skeleton cards
                    items(5) { index ->
                        ExerciseSkeletonCard(
                            modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                        )
                    }
                } else if (filteredExercises.isEmpty()) {
                    // Empty state
                    item(key = "empty_exercises") {
                        val message = when {
                            searchQuery.isNotBlank() -> "No exercises match \"$searchQuery\""
                            selectedEquipment != null && selectedMovement != null ->
                                "No $selectedMovement exercises with $selectedEquipment found"
                            selectedEquipment != null -> "No exercises with $selectedEquipment found"
                            selectedMovement != null -> "No $selectedMovement exercises found"
                            selectedMuscleGroup != null -> "No exercises for $selectedMuscleGroup"
                            else -> "No exercises found"
                        }
                        val suggestion = "Try adjusting your filters or search query."

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = AppTheme.spacing.lg,
                                    vertical = AppTheme.spacing.xl
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                        ) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
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
                            onToggle = { onToggleExercise(exercise.id) },
                            equipmentType = exercise.equipment,
                            modifier = Modifier
                                .padding(horizontal = AppTheme.spacing.lg)
                                .animateItemPlacement()
                        )
                    }
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

    // Full-screen muscle recovery detail overlay
    AnimatedVisibility(
        visible = detailMuscle != null,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300)
        ) + fadeIn(tween(300)),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(200)
        ) + fadeOut(tween(200))
    ) {
        detailMuscle?.let { recovery ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(AppTheme.spacing.lg)
            ) {
                MuscleRecoveryDetailView(
                    recovery = recovery,
                    onBackClick = { detailMuscle = null }
                )
            }
        }
    }
}

/**
 * Filter chip for equipment and movement type filters.
 */
@Composable
private fun FilterChip(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.Transparent
    }

    val textColor = if (isActive) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (isActive) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.outline
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(
                horizontal = AppTheme.spacing.md,
                vertical = AppTheme.spacing.sm
            )
    )
}

/**
 * Skeleton placeholder card shown while exercises are loading.
 */
@Composable
private fun ExerciseSkeletonCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(AppTheme.spacing.md)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            // Title placeholder
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            )
            // Category placeholder
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            )
        }
    }
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
