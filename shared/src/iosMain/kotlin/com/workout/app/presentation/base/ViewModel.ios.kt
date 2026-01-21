package com.workout.app.presentation.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

/**
 * iOS implementation of ViewModel with manual scope management.
 */
actual abstract class ViewModel {
    actual val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    actual open fun onCleared() {
        viewModelScope.cancel()
    }

    /**
     * Must be called when ViewModel is no longer needed.
     */
    fun clear() {
        onCleared()
    }
}
