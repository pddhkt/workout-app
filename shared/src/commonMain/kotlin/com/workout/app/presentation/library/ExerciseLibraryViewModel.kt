package com.workout.app.presentation.library

import com.workout.app.data.repository.ExerciseRepository
import com.workout.app.database.Exercise
import com.workout.app.domain.model.Result
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Exercise Library screen.
 * Manages exercise list, search, filtering, and favorites.
 */
class ExerciseLibraryViewModel(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExerciseLibraryState())
    val state: StateFlow<ExerciseLibraryState> = _state.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            exerciseRepository.observeAll()
                .catch { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load exercises"
                        )
                    }
                }
                .collect { exercises ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            allExercises = exercises,
                            error = null
                        )
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            // Reset to all exercises
            return
        }

        // Perform search
        viewModelScope.launch {
            when (val result = exerciseRepository.search(query)) {
                is Result.Success -> {
                    _state.update { it.copy(searchResults = result.data) }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(error = result.exception.message ?: "Search failed")
                    }
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }

    fun selectMuscleGroup(muscleGroup: String?) {
        _state.update {
            it.copy(
                selectedMuscleGroup = muscleGroup,
                searchQuery = "", // Clear search when filtering
                searchResults = null
            )
        }
    }

    fun toggleFavorite(exerciseId: String) {
        viewModelScope.launch {
            when (val result = exerciseRepository.toggleFavorite(exerciseId)) {
                is Result.Success -> {
                    // Update is handled by the reactive flow
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(error = result.exception.message ?: "Failed to update favorite")
                    }
                }
                is Result.Loading -> {
                    // Not applicable
                }
            }
        }
    }

    fun clearSearch() {
        _state.update {
            it.copy(
                searchQuery = "",
                searchResults = null
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Show the add custom exercise bottom sheet.
     */
    fun showAddExerciseSheet() {
        _state.update { it.copy(showAddExerciseSheet = true) }
    }

    /**
     * Hide the add custom exercise bottom sheet.
     */
    fun hideAddExerciseSheet() {
        _state.update { it.copy(showAddExerciseSheet = false, createError = null) }
    }

    /**
     * Create a new custom exercise.
     *
     * @param name Exercise name (required)
     * @param muscleGroup Target muscle group (required)
     * @param category Exercise category (optional)
     * @param equipment Equipment needed (optional)
     * @param difficulty Difficulty level (optional)
     * @param instructions Exercise instructions (optional)
     * @param videoUrl Video URL (optional)
     */
    fun createCustomExercise(
        name: String,
        muscleGroup: String,
        category: String? = null,
        equipment: String? = null,
        difficulty: String? = null,
        instructions: String? = null,
        videoUrl: String? = null
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isCreating = true, createError = null) }

            when (val result = exerciseRepository.create(
                name = name,
                muscleGroup = muscleGroup,
                category = category,
                equipment = equipment,
                difficulty = difficulty,
                instructions = instructions,
                videoUrl = videoUrl
            )) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isCreating = false,
                            showAddExerciseSheet = false,
                            createError = null
                        )
                    }
                    // The new exercise will appear via the reactive flow from observeAll()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isCreating = false,
                            createError = result.exception.message ?: "Failed to create exercise"
                        )
                    }
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }
}

/**
 * UI state for Exercise Library screen.
 */
data class ExerciseLibraryState(
    val isLoading: Boolean = false,
    val allExercises: List<Exercise> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Exercise>? = null,
    val selectedMuscleGroup: String? = null,
    val error: String? = null,
    // Custom exercise creation state
    val showAddExerciseSheet: Boolean = false,
    val isCreating: Boolean = false,
    val createError: String? = null
) {
    /**
     * Get exercises to display based on search and filter state.
     */
    val displayedExercises: List<Exercise>
        get() {
            // If searching, show search results
            if (searchQuery.isNotBlank() && searchResults != null) {
                return searchResults
            }

            // If muscle group filter is active, filter by it
            if (selectedMuscleGroup != null) {
                return allExercises.filter { it.muscleGroup == selectedMuscleGroup }
            }

            // Otherwise show all
            return allExercises
        }

    /**
     * Get exercises grouped by category for organized display.
     */
    val exercisesByCategory: Map<String, List<Exercise>>
        get() = displayedExercises.groupBy { it.category ?: "Other" }

    /**
     * Whether search is active.
     */
    val isSearchActive: Boolean
        get() = searchQuery.isNotBlank()

    /**
     * Available muscle groups from exercises.
     */
    val availableMuscleGroups: List<String>
        get() = allExercises.map { it.muscleGroup }.distinct().sorted()
}
