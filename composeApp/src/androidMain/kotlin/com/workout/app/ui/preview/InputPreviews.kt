package com.workout.app.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.inputs.AppTextField
import com.workout.app.ui.components.inputs.CompactRPESelector
import com.workout.app.ui.components.inputs.DecimalNumberStepper
import com.workout.app.ui.components.inputs.NotesInput
import com.workout.app.ui.components.inputs.NumberStepper
import com.workout.app.ui.components.inputs.RPESelector
import com.workout.app.ui.components.inputs.SearchBar
import com.workout.app.ui.theme.WorkoutAppTheme

@Preview(showBackground = true)
@Composable
fun TextFieldPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var name by remember { mutableStateOf("") }
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                placeholder = "Enter your name",
                leadingIcon = Icons.Default.Person,
                modifier = Modifier.fillMaxWidth()
            )

            var email by remember { mutableStateOf("") }
            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "your@email.com",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )

            var errorField by remember { mutableStateOf("") }
            AppTextField(
                value = errorField,
                onValueChange = { errorField = it },
                label = "With Error",
                placeholder = "Enter text",
                isError = true,
                errorMessage = "This field is required",
                modifier = Modifier.fillMaxWidth()
            )

            var disabledField by remember { mutableStateOf("Disabled value") }
            AppTextField(
                value = disabledField,
                onValueChange = { disabledField = it },
                label = "Disabled",
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NumberStepperPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            var reps by remember { mutableIntStateOf(10) }
            NumberStepper(
                value = reps,
                onValueChange = { reps = it },
                label = "Reps",
                minValue = 1,
                maxValue = 100,
                step = 1,
                modifier = Modifier.fillMaxWidth()
            )

            var sets by remember { mutableIntStateOf(3) }
            NumberStepper(
                value = sets,
                onValueChange = { sets = it },
                label = "Sets",
                minValue = 1,
                maxValue = 10,
                modifier = Modifier.fillMaxWidth()
            )

            var weight by remember { mutableFloatStateOf(20f) }
            DecimalNumberStepper(
                value = weight,
                onValueChange = { weight = it },
                label = "Weight",
                minValue = 0f,
                maxValue = 500f,
                step = 2.5f,
                unit = "kg",
                decimalPlaces = 1,
                modifier = Modifier.fillMaxWidth()
            )

            var bodyWeight by remember { mutableFloatStateOf(75.5f) }
            DecimalNumberStepper(
                value = bodyWeight,
                onValueChange = { bodyWeight = it },
                label = "Body Weight",
                minValue = 30f,
                maxValue = 300f,
                step = 0.1f,
                unit = "kg",
                decimalPlaces = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var query1 by remember { mutableStateOf("") }
            SearchBar(
                query = query1,
                onQueryChange = { query1 = it },
                placeholder = "Search exercises...",
                searchIcon = Icons.Default.Search,
                clearIcon = Icons.Default.Clear,
                modifier = Modifier.fillMaxWidth()
            )

            var query2 by remember { mutableStateOf("Bench press") }
            SearchBar(
                query = query2,
                onQueryChange = { query2 = it },
                placeholder = "Search workouts...",
                searchIcon = Icons.Default.Search,
                clearIcon = Icons.Default.Clear,
                modifier = Modifier.fillMaxWidth()
            )

            var query3 by remember { mutableStateOf("") }
            SearchBar(
                query = query3,
                onQueryChange = { query3 = it },
                placeholder = "Search...",
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesInputPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var notes1 by remember { mutableStateOf("") }
            NotesInput(
                value = notes1,
                onValueChange = { notes1 = it },
                label = "Workout Notes",
                placeholder = "How did this workout feel?",
                modifier = Modifier.fillMaxWidth()
            )

            var notes2 by remember {
                mutableStateOf("Felt strong today. Increased weight on squats by 5kg. Need to focus on form during the descent phase.")
            }
            NotesInput(
                value = notes2,
                onValueChange = { notes2 = it },
                label = "Exercise Notes",
                placeholder = "Add notes...",
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            var notes3 by remember { mutableStateOf("This is a long note") }
            NotesInput(
                value = notes3,
                onValueChange = { notes3 = it },
                label = "Limited Notes",
                maxCharacters = 50,
                modifier = Modifier.fillMaxWidth()
            )

            var notes4 by remember { mutableStateOf("") }
            NotesInput(
                value = notes4,
                onValueChange = { notes4 = it },
                label = "Error State",
                isError = true,
                errorMessage = "Notes cannot be empty",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun RPESelectorPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            var rpe1 by remember { mutableIntStateOf(7) }
            RPESelector(
                selectedRPE = rpe1,
                onRPESelected = { rpe1 = it },
                label = "How hard was this set?",
                showDescription = true,
                modifier = Modifier.fillMaxWidth()
            )

            var rpe2: Int? by remember { mutableStateOf(null) }
            RPESelector(
                selectedRPE = rpe2,
                onRPESelected = { rpe2 = it },
                label = "Rate your overall workout",
                showDescription = false,
                modifier = Modifier.fillMaxWidth()
            )

            var rpe3 by remember { mutableIntStateOf(5) }
            CompactRPESelector(
                selectedRPE = rpe3,
                onRPESelected = { rpe3 = it },
                label = "RPE",
                modifier = Modifier.fillMaxWidth()
            )

            var rpe4: Int? by remember { mutableStateOf(null) }
            CompactRPESelector(
                selectedRPE = rpe4,
                onRPESelected = { rpe4 = it },
                label = "Effort Level",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
fun AllInputsPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            var exerciseName by remember { mutableStateOf("") }
            AppTextField(
                value = exerciseName,
                onValueChange = { exerciseName = it },
                label = "Exercise Name",
                placeholder = "e.g., Bench Press",
                modifier = Modifier.fillMaxWidth()
            )

            var searchQuery by remember { mutableStateOf("") }
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search exercises...",
                searchIcon = Icons.Default.Search,
                clearIcon = Icons.Default.Clear,
                modifier = Modifier.fillMaxWidth()
            )

            var reps by remember { mutableIntStateOf(10) }
            NumberStepper(
                value = reps,
                onValueChange = { reps = it },
                label = "Reps",
                minValue = 1,
                maxValue = 100,
                modifier = Modifier.fillMaxWidth()
            )

            var weight by remember { mutableFloatStateOf(60f) }
            DecimalNumberStepper(
                value = weight,
                onValueChange = { weight = it },
                label = "Weight",
                step = 2.5f,
                unit = "kg",
                modifier = Modifier.fillMaxWidth()
            )

            var rpe by remember { mutableIntStateOf(8) }
            CompactRPESelector(
                selectedRPE = rpe,
                onRPESelected = { rpe = it },
                label = "RPE",
                modifier = Modifier.fillMaxWidth()
            )

            var notes by remember { mutableStateOf("") }
            NotesInput(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                placeholder = "How did this exercise feel?",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
