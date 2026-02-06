package com.workout.app.ui.components.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.Active
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.Completed
import com.workout.app.ui.theme.Pending

/**
 * State of a workout set
 */
enum class SetState {
    /** Set is completed */
    COMPLETED,
    /** Set is currently active/in progress */
    ACTIVE,
    /** Set is pending/not started */
    PENDING
}

/**
 * SetChip component for displaying workout set status
 * Shows set number with color-coded state indicator
 * Based on elements EL-17/18/19 from mockups
 *
 * @param setNumber The set number to display (e.g., 1, 2, 3)
 * @param state Current state of the set
 * @param onClick Optional callback when chip is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun SetChip(
    setNumber: Int,
    state: SetState,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val stateColor = when (state) {
        SetState.COMPLETED -> Completed
        SetState.ACTIVE -> Active
        SetState.PENDING -> Pending
    }

    val backgroundColor = when (state) {
        SetState.COMPLETED -> Completed.copy(alpha = 0.15f)
        SetState.ACTIVE -> Active.copy(alpha = 0.15f)
        SetState.PENDING -> MaterialTheme.colorScheme.surface
    }

    val textColor = when (state) {
        SetState.COMPLETED, SetState.ACTIVE -> stateColor
        SetState.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = when (state) {
        SetState.COMPLETED, SetState.ACTIVE -> Color.Transparent
        SetState.PENDING -> MaterialTheme.colorScheme.outline
    }

    val chipModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Row(
        modifier = chipModifier
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(2.dp)
            )
            .padding(
                horizontal = AppTheme.spacing.md,
                vertical = AppTheme.spacing.sm
            ),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // State indicator dot
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(stateColor)
        )

        // Set number
        Text(
            text = "Set $setNumber",
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}
