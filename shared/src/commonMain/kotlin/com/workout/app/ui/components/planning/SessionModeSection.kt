package com.workout.app.ui.components.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.SessionMode
import com.workout.app.domain.model.SessionParticipant
import com.workout.app.ui.components.chips.ModeChip
import com.workout.app.ui.theme.AppTheme

/**
 * Session mode selector row + participant management section.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionModeSection(
    sessionMode: SessionMode,
    participants: List<SessionParticipant>,
    helperText: String?,
    onModeSelected: (SessionMode) -> Unit,
    onAddParticipantClick: () -> Unit,
    onRemoveParticipant: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        // Mode selector chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            SessionMode.entries.forEach { mode ->
                ModeChip(
                    text = mode.label,
                    isActive = sessionMode == mode,
                    onClick = { onModeSelected(mode) }
                )
            }
        }

        // Participant section (only for Coaching/Group)
        if (sessionMode != SessionMode.SOLO) {
            if (helperText != null) {
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                participants.forEach { participant ->
                    ParticipantChip(
                        name = if (participant.isOwner) "${participant.name} (You)" else participant.name,
                        onRemove = if (participant.isOwner) null
                        else {
                            { onRemoveParticipant(participant.id) }
                        }
                    )
                }

                AddParticipantChip(onClick = onAddParticipantClick)
            }
        }
    }
}

@Composable
private fun ParticipantChip(
    name: String,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
            .padding(
                start = AppTheme.spacing.md,
                end = if (onRemove != null) AppTheme.spacing.xs else AppTheme.spacing.md,
                top = AppTheme.spacing.sm,
                bottom = AppTheme.spacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (onRemove != null) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove $name",
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onRemove),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddParticipantChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(
                horizontal = AppTheme.spacing.md,
                vertical = AppTheme.spacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add participant",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Add",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
