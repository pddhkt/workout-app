package com.workout.app.ui.components.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.components.chips.Badge
import com.workout.app.ui.components.chips.BadgeVariant
import com.workout.app.ui.components.headers.SectionHeader
import com.workout.app.ui.components.inputs.SearchBar
import com.workout.app.ui.theme.AppTheme

/**
 * Multi-select exercise picker content for template creation and editing.
 * Based on ExercisePickerContent but with multi-selection support.
 *
 * @param exercises List of exercises to display
 * @param selectedExerciseIds Set of selected exercise IDs
 * @param templateName Current template name input
 * @param onTemplateNameChange Callback when template name changes
 * @param onExerciseToggle Callback when an exercise is selected/deselected
 * @param onCreateTemplate Callback when create/save template button is clicked
 * @param isEditMode Whether this is editing an existing template (changes header and button text)
 * @param onDeleteTemplate Callback when delete button is clicked (only shown in edit mode)
 * @param modifier Optional modifier for customization
 */
@Composable
fun MultiSelectExercisePickerContent(
    exercises: List<LibraryExercise> = getMockLibraryExercises(),
    selectedExerciseIds: Set<String>,
    templateName: String,
    onTemplateNameChange: (String) -> Unit,
    onExerciseToggle: (LibraryExercise) -> Unit,
    onCreateTemplate: () -> Unit,
    isEditMode: Boolean = false,
    onDeleteTemplate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf("All") }

    // Filter exercises by search query and muscle group
    val filteredExercises = exercises.filter { exercise ->
        val matchesSearch = exercise.name.contains(searchQuery, ignoreCase = true) ||
                exercise.muscleGroup.contains(searchQuery, ignoreCase = true) ||
                exercise.category.contains(searchQuery, ignoreCase = true)

        val matchesMuscleGroup = selectedMuscleGroup == "All" ||
                exercise.muscleGroup == selectedMuscleGroup

        matchesSearch && matchesMuscleGroup
    }

    // Group exercises by category
    val groupedExercises = filteredExercises.groupBy { it.category }

    Column(modifier = modifier.fillMaxSize()) {
        // Header - dynamic based on mode
        Text(
            text = if (isEditMode) "Edit Template" else "Create Template",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Template Name Input
        TemplateNameInput(
            value = templateName,
            onValueChange = onTemplateNameChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { },
            placeholder = "Search exercises...",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Filter Chips
        MuscleGroupFilters(
            selectedMuscleGroup = selectedMuscleGroup,
            onMuscleGroupSelected = { selectedMuscleGroup = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Exercise List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            groupedExercises.forEach { (category, categoryExercises) ->
                item(key = "header_$category") {
                    SectionHeader(
                        title = category,
                        modifier = Modifier.padding(
                            top = AppTheme.spacing.sm,
                            bottom = AppTheme.spacing.xs
                        )
                    )
                }

                items(
                    items = categoryExercises,
                    key = { it.id }
                ) { exercise ->
                    MultiSelectExerciseItem(
                        exercise = exercise,
                        isSelected = selectedExerciseIds.contains(exercise.id),
                        onClick = { onExerciseToggle(exercise) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Delete Button (only in edit mode)
        if (isEditMode && onDeleteTemplate != null) {
            SecondaryButton(
                text = "Delete Template",
                onClick = onDeleteTemplate,
                destructive = true,
                fullWidth = true,
                modifier = Modifier.padding(bottom = AppTheme.spacing.sm)
            )
        }

        // Create/Save Template Button - dynamic text based on mode
        PrimaryButton(
            text = when {
                selectedExerciseIds.isEmpty() -> "Select Exercises"
                isEditMode -> "Save Changes"
                else -> "Create Template (${selectedExerciseIds.size})"
            },
            onClick = onCreateTemplate,
            enabled = templateName.isNotBlank() && selectedExerciseIds.isNotEmpty(),
            fullWidth = true,
            modifier = Modifier.padding(bottom = AppTheme.spacing.lg)
        )
    }
}

/**
 * Template name input field with label
 */
@Composable
private fun TemplateNameInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Template Name",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = AppTheme.spacing.xs)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .padding(
                            horizontal = AppTheme.spacing.lg,
                            vertical = AppTheme.spacing.md
                        )
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Enter template name...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

/**
 * Multi-select exercise item with checkmark indicator
 */
@Composable
private fun MultiSelectExerciseItem(
    exercise: LibraryExercise,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = AppTheme.spacing.md,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Exercise info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (exercise.isCustom) {
                        Badge(
                            text = "Custom",
                            variant = BadgeVariant.INFO
                        )
                    }
                }

                Text(
                    text = exercise.muscleGroup,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right side: Selection indicator or favorite star
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else if (exercise.isFavorite) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Favorite",
                    tint = AppTheme.colors.primaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
