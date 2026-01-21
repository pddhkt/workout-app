package com.workout.app.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.Border
import com.workout.app.ui.theme.OnSurfaceVariant
import com.workout.app.ui.theme.SurfaceVariant

/**
 * Search bar with search icon and clear button
 * Element: EL-25
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    searchIcon: ImageVector? = null,
    clearIcon: ImageVector? = null,
    enabled: Boolean = true,
    onSearch: () -> Unit = {}
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = if (enabled) MaterialTheme.colorScheme.onSurface else OnSurfaceVariant
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Search field" },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = SurfaceVariant,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Border,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(
                        horizontal = AppTheme.spacing.lg,
                        vertical = AppTheme.spacing.md
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search icon
                if (searchIcon != null) {
                    Icon(
                        imageVector = searchIcon,
                        contentDescription = "Search",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                }

                // Input field
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                    innerTextField()
                }

                // Clear button
                if (query.isNotEmpty() && clearIcon != null && enabled) {
                    Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                    Icon(
                        imageVector = clearIcon,
                        contentDescription = "Clear search",
                        tint = OnSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onQueryChange("") }
                            )
                    )
                }
            }
        }
    )
}
