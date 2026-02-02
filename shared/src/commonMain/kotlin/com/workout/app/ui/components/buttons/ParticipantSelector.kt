package com.workout.app.ui.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.Participant
import com.workout.app.ui.theme.AppTheme

/**
 * Toggle selector for switching between Me/Partner views in partner workouts.
 *
 * @param selectedParticipant Currently selected participant
 * @param onSelect Callback when participant is selected
 * @param modifier Optional modifier
 */
@Composable
fun ParticipantSelector(
    selectedParticipant: Participant,
    onSelect: (Participant) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        ToggleButton(
            text = "Me",
            selected = selectedParticipant == Participant.ME,
            onClick = { onSelect(Participant.ME) },
            modifier = Modifier.weight(1f)
        )

        ToggleButton(
            text = "Partner",
            selected = selectedParticipant == Participant.PARTNER,
            onClick = { onSelect(Participant.PARTNER) },
            modifier = Modifier.weight(1f)
        )
    }
}
