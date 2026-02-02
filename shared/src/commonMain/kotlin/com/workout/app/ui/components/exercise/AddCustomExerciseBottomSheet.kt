package com.workout.app.ui.components.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.inputs.AppTextField
import com.workout.app.ui.components.inputs.DropdownSelector
import com.workout.app.ui.components.inputs.NotesInput
import com.workout.app.ui.theme.AppTheme

/**
 * Data class to hold the custom exercise form state.
 */
data class CustomExerciseFormState(
    val name: String = "",
    val muscleGroup: String? = null,
    val category: String? = null,
    val equipment: String? = null,
    val difficulty: String? = null,
    val instructions: String = "",
    val videoUrl: String = ""
) {
    val isValid: Boolean
        get() = name.isNotBlank() && muscleGroup != null

    val nameError: String?
        get() = if (name.isBlank()) "Exercise name is required" else null

    val muscleGroupError: String?
        get() = if (muscleGroup == null) "Please select a muscle group" else null
}

/**
 * Predefined options for exercise fields.
 */
object ExerciseOptions {
    val muscleGroups = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio")
    val categories = listOf("Compound", "Isolation", "Stability", "Rotation")
    val equipment = listOf("Barbell", "Dumbbells", "Bodyweight", "Machine", "Cable", "Kettlebell", "Resistance Band")
    val difficulties = listOf("Beginner", "Intermediate", "Advanced")
}

/**
 * Bottom sheet content for adding a custom exercise.
 * Contains a form with fields for exercise details and validation.
 *
 * @param onSave Callback when the exercise is saved, provides form data
 * @param onCancel Callback when the form is cancelled
 * @param isLoading Whether a save operation is in progress
 * @param modifier Modifier for the component
 */
@Composable
fun AddCustomExerciseBottomSheet(
    onSave: (CustomExerciseFormState) -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var formState by remember { mutableStateOf(CustomExerciseFormState()) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(bottom = AppTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Custom Exercise",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

        // Name field (required)
        AppTextField(
            value = formState.name,
            onValueChange = { formState = formState.copy(name = it) },
            label = "Exercise Name *",
            placeholder = "e.g., Bulgarian Split Squat",
            isError = hasAttemptedSubmit && formState.name.isBlank(),
            errorMessage = if (hasAttemptedSubmit) formState.nameError else null,
            modifier = Modifier.fillMaxWidth()
        )

        // Muscle Group dropdown (required)
        DropdownSelector(
            selectedValue = formState.muscleGroup,
            options = ExerciseOptions.muscleGroups,
            onOptionSelected = { formState = formState.copy(muscleGroup = it) },
            label = "Muscle Group *",
            placeholder = "Select muscle group",
            isError = hasAttemptedSubmit && formState.muscleGroup == null,
            errorMessage = if (hasAttemptedSubmit) formState.muscleGroupError else null,
            modifier = Modifier.fillMaxWidth()
        )

        // Category dropdown (optional)
        DropdownSelector(
            selectedValue = formState.category,
            options = ExerciseOptions.categories,
            onOptionSelected = { formState = formState.copy(category = it) },
            label = "Category",
            placeholder = "Select category (optional)",
            modifier = Modifier.fillMaxWidth()
        )

        // Equipment dropdown (optional)
        DropdownSelector(
            selectedValue = formState.equipment,
            options = ExerciseOptions.equipment,
            onOptionSelected = { formState = formState.copy(equipment = it) },
            label = "Equipment",
            placeholder = "Select equipment (optional)",
            modifier = Modifier.fillMaxWidth()
        )

        // Difficulty dropdown (optional)
        DropdownSelector(
            selectedValue = formState.difficulty,
            options = ExerciseOptions.difficulties,
            onOptionSelected = { formState = formState.copy(difficulty = it) },
            label = "Difficulty",
            placeholder = "Select difficulty (optional)",
            modifier = Modifier.fillMaxWidth()
        )

        // Instructions (optional)
        NotesInput(
            value = formState.instructions,
            onValueChange = { formState = formState.copy(instructions = it) },
            label = "Instructions",
            placeholder = "Add exercise instructions (optional)...",
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth()
        )

        // Video URL (optional)
        AppTextField(
            value = formState.videoUrl,
            onValueChange = { formState = formState.copy(videoUrl = it) },
            label = "Video URL",
            placeholder = "https://youtube.com/... (optional)",
            keyboardType = KeyboardType.Uri,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Save button
        PrimaryButton(
            text = if (isLoading) "Saving..." else "Save Exercise",
            onClick = {
                hasAttemptedSubmit = true
                if (formState.isValid) {
                    onSave(formState)
                }
            },
            enabled = !isLoading,
            fullWidth = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
