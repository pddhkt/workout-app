package com.workout.app.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme

/**
 * Secondary button component with outlined style.
 * Based on mockup elements EL-38, EL-83.
 *
 * @param text Button label text
 * @param onClick Callback invoked when button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled or disabled
 * @param fullWidth Whether the button should fill max width
 * @param destructive Whether the button represents a destructive action (uses error color)
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    destructive: Boolean = false
) {
    val contentColor = if (destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(AppTheme.spacing.sm),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) {
                contentColor
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
            }
        ),
        contentPadding = PaddingValues(
            horizontal = AppTheme.spacing.lg,
            vertical = AppTheme.spacing.md
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
