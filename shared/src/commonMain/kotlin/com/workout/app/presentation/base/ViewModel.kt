package com.workout.app.presentation.base

import kotlinx.coroutines.CoroutineScope

/**
 * Base ViewModel class for KMP.
 * Platform-specific implementations provide lifecycle-aware scope.
 */
expect abstract class ViewModel() {
    /**
     * Lifecycle-aware coroutine scope tied to this ViewModel.
     * Automatically cancelled when ViewModel is cleared.
     */
    val viewModelScope: CoroutineScope

    /**
     * Called when this ViewModel is no longer needed.
     * Override to clean up resources.
     */
    protected open fun onCleared()
}
