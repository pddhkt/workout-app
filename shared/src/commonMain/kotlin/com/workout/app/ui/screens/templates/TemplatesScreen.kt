package com.workout.app.ui.screens.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.workout.app.data.repository.TemplateRepository
import com.workout.app.database.Template
import com.workout.app.domain.model.TemplateExercise
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.exercise.MultiSelectExercisePickerContent
import com.workout.app.ui.components.exercise.getMockLibraryExercises
import com.workout.app.ui.components.navigation.BottomNavBar
import com.workout.app.ui.components.overlays.M3BottomSheet
import com.workout.app.ui.components.templates.TemplateListItem
import com.workout.app.ui.theme.AppTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.compose.koinInject

/**
 * Templates screen for managing workout templates.
 * Users can create templates by selecting multiple exercises.
 * Click a template to use it in session planning.
 * Long-press a template to edit or delete it.
 *
 * @param onTemplateClick Callback when a template is clicked (navigates to SessionPlanning)
 * @param onNavigate Callback for bottom navigation
 * @param onAddClick Callback when the add button in bottom nav is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun TemplatesScreen(
    onTemplateClick: (String) -> Unit,
    onNavigate: (Int) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val templateRepository: TemplateRepository = koinInject()
    val scope = rememberCoroutineScope()

    // State
    var selectedNavIndex by remember { mutableIntStateOf(2) } // Templates tab selected
    var showCreateSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<Template?>(null) }
    var selectedExercises by remember { mutableStateOf(setOf<String>()) }
    var templateName by remember { mutableStateOf("") }

    // Observe templates
    val templates by templateRepository.observeAll().collectAsState(initial = emptyList())

    // Exercise data for picker
    val exercises = remember { getMockLibraryExercises() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedNavIndex,
                onItemSelected = { index ->
                    selectedNavIndex = index
                    onNavigate(index)
                },
                onAddClick = onAddClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            TemplatesHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg)
                    .padding(top = AppTheme.spacing.xl, bottom = AppTheme.spacing.md)
            )

            // Create Template Button
            PrimaryButton(
                text = "Create Template",
                onClick = {
                    // Reset state for new template
                    templateName = ""
                    selectedExercises = emptySet()
                    showCreateSheet = true
                },
                fullWidth = true,
                modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

            // Template List
            if (templates.isEmpty()) {
                EmptyTemplatesMessage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = AppTheme.spacing.lg,
                        end = AppTheme.spacing.lg,
                        bottom = AppTheme.spacing.xl
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    items(
                        items = templates,
                        key = { it.id }
                    ) { template ->
                        val exerciseCount = parseExerciseCount(template.exercises)
                        val lastUsedFormatted = formatLastUsed(template.lastUsed)

                        TemplateListItem(
                            name = template.name,
                            exerciseCount = exerciseCount,
                            lastUsed = lastUsedFormatted,
                            isFavorite = template.isFavorite == 1L,
                            onClick = { onTemplateClick(template.id) },
                            onLongPress = {
                                // Set up edit mode
                                editingTemplate = template
                                templateName = template.name
                                selectedExercises = TemplateExercise.fromJsonArray(template.exercises)
                                    .map { it.exerciseId }
                                    .toSet()
                                showEditSheet = true
                            },
                            onFavoriteClick = {
                                scope.launch {
                                    templateRepository.toggleFavorite(template.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Create Template Bottom Sheet
    M3BottomSheet(
        visible = showCreateSheet,
        onDismiss = { showCreateSheet = false }
    ) {
        MultiSelectExercisePickerContent(
            exercises = exercises,
            selectedExerciseIds = selectedExercises,
            templateName = templateName,
            onTemplateNameChange = { templateName = it },
            onExerciseToggle = { exercise ->
                selectedExercises = if (selectedExercises.contains(exercise.id)) {
                    selectedExercises - exercise.id
                } else {
                    selectedExercises + exercise.id
                }
            },
            onCreateTemplate = {
                if (templateName.isNotBlank() && selectedExercises.isNotEmpty()) {
                    scope.launch {
                        // Create exercise JSON
                        val templateExercises = selectedExercises.mapIndexed { index, exerciseId ->
                            TemplateExercise(
                                exerciseId = exerciseId,
                                order = index,
                                defaultSets = 3
                            )
                        }
                        val exercisesJson = TemplateExercise.toJsonArray(templateExercises)

                        // Create template
                        templateRepository.create(
                            name = templateName,
                            exercises = exercisesJson
                        )

                        // Close sheet
                        showCreateSheet = false
                    }
                }
            },
            isEditMode = false
        )
    }

    // Edit Template Bottom Sheet
    M3BottomSheet(
        visible = showEditSheet,
        onDismiss = {
            showEditSheet = false
            editingTemplate = null
        }
    ) {
        MultiSelectExercisePickerContent(
            exercises = exercises,
            selectedExerciseIds = selectedExercises,
            templateName = templateName,
            onTemplateNameChange = { templateName = it },
            onExerciseToggle = { exercise ->
                selectedExercises = if (selectedExercises.contains(exercise.id)) {
                    selectedExercises - exercise.id
                } else {
                    selectedExercises + exercise.id
                }
            },
            onCreateTemplate = {
                // Save changes
                val template = editingTemplate
                if (template != null && templateName.isNotBlank() && selectedExercises.isNotEmpty()) {
                    scope.launch {
                        // Create exercise JSON
                        val templateExercises = selectedExercises.mapIndexed { index, exerciseId ->
                            TemplateExercise(
                                exerciseId = exerciseId,
                                order = index,
                                defaultSets = 3
                            )
                        }
                        val exercisesJson = TemplateExercise.toJsonArray(templateExercises)

                        // Update template
                        val updatedTemplate = template.copy(
                            name = templateName,
                            exercises = exercisesJson
                        )
                        templateRepository.update(updatedTemplate)

                        // Close sheet
                        showEditSheet = false
                        editingTemplate = null
                    }
                }
            },
            isEditMode = true,
            onDeleteTemplate = {
                val template = editingTemplate
                if (template != null) {
                    scope.launch {
                        templateRepository.delete(template.id)
                        showEditSheet = false
                        editingTemplate = null
                    }
                }
            }
        )
    }
}

/**
 * Templates header with title and subtitle
 */
@Composable
private fun TemplatesHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Text(
            text = "Templates",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Create and manage your workout templates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Empty state message when no templates exist
 */
@Composable
private fun EmptyTemplatesMessage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        Text(
            text = "No templates yet",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Create a template to quickly start workouts with your favorite exercises.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Parse exercise count from JSON string
 */
private fun parseExerciseCount(exercisesJson: String): Int {
    return TemplateExercise.fromJsonArray(exercisesJson).size
}

/**
 * Format lastUsed timestamp to readable string
 */
private fun formatLastUsed(lastUsed: Long?): String? {
    if (lastUsed == null) return null

    // Simple formatting - in a real app you'd use proper date formatting
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
