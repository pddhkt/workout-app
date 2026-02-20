package com.workout.app.ui.components.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme

/**
 * Primary button component with green accent fill.
 * Based on mockup elements EL-14, EL-15, EL-34, EL-35.
 *
 * @param text Button label text
 * @param onClick Callback invoked when button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled or disabled
 * @param fullWidth Whether the button should fill max width
 * @param isLoading Whether to show a loading indicator instead of text
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(48.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(
            horizontal = AppTheme.spacing.lg,
            vertical = AppTheme.spacing.md
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
