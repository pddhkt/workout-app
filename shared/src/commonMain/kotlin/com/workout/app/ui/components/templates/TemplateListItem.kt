package com.workout.app.ui.components.templates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.AppIconButton
import com.workout.app.ui.theme.AppTheme

/**
 * Template list item card showing template name, exercise count, last used, and favorite star.
 * Based on ExerciseLibraryItem pattern.
 *
 * @param name Template name
 * @param exerciseCount Number of exercises in the template
 * @param lastUsed Formatted last used date string (e.g., "Jan 19" or null if never used)
 * @param isFavorite Whether the template is marked as favorite
 * @param onClick Callback when the template card is clicked
 * @param onLongPress Callback when the template card is long-pressed (for edit mode)
 * @param onFavoriteClick Callback when the favorite star is clicked
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TemplateListItem(
    name: String,
    exerciseCount: Int,
    lastUsed: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(12.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(cardShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Template info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
            ) {
                // Template name
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Exercise count and last used
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    Text(
                        text = "$exerciseCount exercises",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (lastUsed != null) {
                        Text(
                            text = "Last used: $lastUsed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Right side: Favorite star
            AppIconButton(
                icon = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                onClick = onFavoriteClick,
                tint = if (isFavorite) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
