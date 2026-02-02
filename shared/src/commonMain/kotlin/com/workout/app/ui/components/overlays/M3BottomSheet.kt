package com.workout.app.ui.components.overlays

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.workout.app.ui.theme.AppTheme

/**
 * Material3 ModalBottomSheet wrapper for comparison testing.
 *
 * This uses the native M3 ModalBottomSheet implementation which provides:
 * - Built-in scrim handling
 * - Native drag-to-dismiss with predictive back support
 * - Default M3 shape and styling
 * - Built-in safe area handling
 *
 * @param visible Whether the bottom sheet is visible
 * @param onDismiss Callback invoked when sheet is dismissed
 * @param skipPartiallyExpanded Whether to skip the partially expanded state and go straight to full height
 * @param modifier Modifier to be applied to the sheet
 * @param content Bottom sheet content composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    skipPartiallyExpanded: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = skipPartiallyExpanded
            ),
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppTheme.spacing.lg)
            ) {
                content()
            }
        }
    }
}
