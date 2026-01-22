package com.workout.app.presentation.settings

import com.workout.app.data.repository.SettingsRepository
import com.workout.app.data.repository.ThemeMode
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings screen.
 * Manages settings UI state and user actions.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.observeThemeMode().collect { mode ->
                _state.update { it.copy(themeMode = mode) }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }
}

/**
 * UI state for Settings screen.
 */
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)
