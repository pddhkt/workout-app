package com.workout.app.ui.components.gps

import androidx.compose.runtime.Composable

@Composable
expect fun RequestLocationPermission(
    onPermissionResult: (Boolean) -> Unit
)
