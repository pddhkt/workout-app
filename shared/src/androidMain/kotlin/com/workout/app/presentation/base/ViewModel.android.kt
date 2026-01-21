package com.workout.app.presentation.base

import androidx.lifecycle.ViewModel as AndroidViewModel
import androidx.lifecycle.viewModelScope as androidViewModelScope
import kotlinx.coroutines.CoroutineScope

/**
 * Android implementation of ViewModel using AndroidX ViewModel.
 */
actual abstract class ViewModel : AndroidViewModel() {
    actual val viewModelScope: CoroutineScope
        get() = androidViewModelScope

    actual override fun onCleared() {
        super.onCleared()
    }
}
