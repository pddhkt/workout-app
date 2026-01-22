package com.workout.app.presentation.settings

import com.workout.app.data.repository.SettingsRepository
import com.workout.app.data.repository.ThemeMode
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * App-wide singleton ViewModel for theme state.
 * Provides reactive theme mode that can be observed at the app root.
 */
class ThemeViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /**
     * Current theme mode as StateFlow.
     * Defaults to SYSTEM while loading.
     */
    val themeMode: StateFlow<ThemeMode> = settingsRepository.observeThemeMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    /**
     * Update the theme mode.
     * @param mode The new theme mode to set
     */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }
}
