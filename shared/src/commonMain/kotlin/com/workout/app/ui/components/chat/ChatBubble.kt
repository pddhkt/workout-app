package com.workout.app.ui.components.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.ChatMessage
import com.workout.app.domain.model.ChatMessageMetadata
import com.workout.app.domain.model.MessageRole
import com.workout.app.ui.theme.AppTheme

/**
 * Chat bubble component that renders a single message in the conversation.
 * User messages are right-aligned with yellow background.
 * Assistant messages are left-aligned with surface background and subtle border.
 * System messages are centered with muted styling.
 *
 * Dispatches to sub-components based on metadata type:
 * - MultipleChoice -> MultipleChoiceCard
 * - TemplateProposal -> TemplateProposalCard
 * - ExerciseProposal -> ExerciseProposalCard
 *
 * @param message The chat message to display
 * @param onOptionSelected Callback when a multiple-choice option is selected
 * @param onSaveTemplate Callback when "Save Template" is tapped on a template proposal
 * @param onSaveExercise Callback when "Save Exercise" is tapped on an exercise proposal
 * @param modifier Optional modifier
 */
@Composable
fun ChatBubble(
    message: ChatMessage,
    onOptionSelected: ((messageId: String, optionId: String, optionLabel: String) -> Unit)? = null,
    onMultiOptionSelected: ((messageId: String, questionId: String, optionId: String, optionLabel: String) -> Unit)? = null,
    onSaveTemplate: ((messageId: String) -> Unit)? = null,
    onSaveExercise: ((messageId: String) -> Unit)? = null,
    onSaveGoal: ((messageId: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (message.role) {
        MessageRole.USER -> UserBubble(
            message = message,
            modifier = modifier
        )
        MessageRole.ASSISTANT -> AssistantBubble(
            message = message,
            onOptionSelected = onOptionSelected,
            onMultiOptionSelected = onMultiOptionSelected,
            onSaveTemplate = onSaveTemplate,
            onSaveExercise = onSaveExercise,
            onSaveGoal = onSaveGoal,
            modifier = modifier
        )
        MessageRole.SYSTEM -> SystemBubble(
            message = message,
            modifier = modifier
        )
    }
}

/**
 * User message bubble - right-aligned with yellow (primary) background.
 */
@Composable
private fun UserBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        // Spacer to push content to the right (max 80% width)
        Spacer(modifier = Modifier.width(48.dp))

        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = 12.dp,
                bottomEnd = 4.dp
            ),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(AppTheme.spacing.md)
            )
        }
    }
}

/**
 * Assistant message bubble - left-aligned with surface background and border.
 * Renders metadata sub-components when present.
 */
@Composable
private fun AssistantBubble(
    message: ChatMessage,
    onOptionSelected: ((messageId: String, optionId: String, optionLabel: String) -> Unit)?,
    onMultiOptionSelected: ((messageId: String, questionId: String, optionId: String, optionLabel: String) -> Unit)?,
    onSaveTemplate: ((messageId: String) -> Unit)?,
    onSaveExercise: ((messageId: String) -> Unit)?,
    onSaveGoal: ((messageId: String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            // Message text bubble
            if (message.content.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 12.dp
                    ),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(AppTheme.spacing.md)
                    )
                }
            }

            // Metadata sub-components
            when (val metadata = message.metadata) {
                is ChatMessageMetadata.MultipleChoice -> {
                    MultipleChoiceCard(
                        options = metadata.options,
                        selectedOptionId = message.selectedOptionId,
                        onOptionSelected = { optionId, optionLabel ->
                            onOptionSelected?.invoke(message.id, optionId, optionLabel)
                        }
                    )
                }
                is ChatMessageMetadata.TemplateProposal -> {
                    TemplateProposalCard(
                        name = metadata.name,
                        description = metadata.description,
                        exercises = metadata.exercises,
                        estimatedDuration = metadata.estimatedDuration,
                        onSave = { onSaveTemplate?.invoke(message.id) }
                    )
                }
                is ChatMessageMetadata.ExerciseProposal -> {
                    ExerciseProposalCard(
                        name = metadata.name,
                        muscleGroup = metadata.muscleGroup,
                        category = metadata.category,
                        equipment = metadata.equipment,
                        difficulty = metadata.difficulty,
                        instructions = metadata.instructions,
                        recordingFields = metadata.recordingFields,
                        onSave = { onSaveExercise?.invoke(message.id) }
                    )
                }
                is ChatMessageMetadata.MultiChoice -> {
                    MultiChoiceCard(
                        questions = metadata.questions,
                        selectedOptionIds = message.selectedOptionIds,
                        onOptionSelected = { questionId, optionId, optionLabel ->
                            onMultiOptionSelected?.invoke(message.id, questionId, optionId, optionLabel)
                        }
                    )
                }
                is ChatMessageMetadata.GoalProposal -> {
                    GoalProposalCard(
                        name = metadata.name,
                        exerciseNames = metadata.exerciseNames,
                        metric = metadata.metric,
                        targetValue = metadata.targetValue,
                        targetUnit = metadata.targetUnit,
                        frequency = metadata.frequency,
                        isOngoing = metadata.isOngoing,
                        onSave = { onSaveGoal?.invoke(message.id) }
                    )
                }
                null -> { /* No metadata - text-only message */ }
            }
        }

        // Spacer to prevent content from reaching the right edge
        Spacer(modifier = Modifier.width(48.dp))
    }
}

/**
 * System message bubble - centered with muted styling.
 * Used for confirmations like "Template saved successfully!"
 */
@Composable
private fun SystemBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = AppTheme.spacing.md,
                    vertical = AppTheme.spacing.sm
                )
            )
        }
    }
}
