package com.workout.app.ui.components.gps

import androidx.compose.runtime.Composable

@Composable
actual fun RequestLocationPermission(
    onPermissionResult: (Boolean) -> Unit
) {
    // No-op on iOS for now
}
