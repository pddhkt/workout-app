package com.workout.app.domain.model

/**
 * Domain models for the AI assistant chat feature.
 */

/**
 * Represents a single message in a chat conversation.
 *
 * @param id Unique identifier for the message
 * @param role Who sent the message (user, assistant, or system)
 * @param content The text content of the message
 * @param metadata Optional structured data attached to the message (choices, proposals)
 * @param createdAt Timestamp when the message was created (epoch millis)
 * @param selectedOptionId ID of the selected option for multiple-choice messages
 */
data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val metadata: ChatMessageMetadata? = null,
    val createdAt: Long = 0,
    val selectedOptionId: String? = null
)

/**
 * The role of a chat message sender.
 */
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

/**
 * Sealed class representing structured metadata attached to an assistant message.
 * Each variant determines how the message is rendered in the chat UI.
 */
sealed class ChatMessageMetadata {

    /**
     * A multiple-choice question with tappable options.
     * Rendered as a list of option buttons below the message text.
     *
     * @param options List of selectable options
     */
    data class MultipleChoice(
        val options: List<ChatOption>
    ) : ChatMessageMetadata()

    /**
     * A proposed workout template for the user to review and save.
     * Rendered as a card with template details and a "Save Template" button.
     *
     * @param name Template name
     * @param description Optional template description
     * @param exercises List of exercises in the template
     * @param estimatedDuration Estimated workout duration in minutes
     */
    data class TemplateProposal(
        val name: String,
        val description: String?,
        val exercises: List<TemplateExerciseInfo>,
        val estimatedDuration: Int?
    ) : ChatMessageMetadata()

    /**
     * A proposed exercise for the user to review and save.
     * Rendered as a card with exercise details and a "Save Exercise" button.
     *
     * @param name Exercise name
     * @param muscleGroup Target muscle group
     * @param category Exercise category (e.g., compound, isolation)
     * @param equipment Equipment needed
     * @param difficulty Difficulty level
     * @param instructions Exercise instructions / form cues
     */
    data class ExerciseProposal(
        val name: String,
        val muscleGroup: String,
        val category: String?,
        val equipment: String?,
        val difficulty: String?,
        val instructions: String?,
        val recordingFields: List<RecordingField>? = null
    ) : ChatMessageMetadata()
}

/**
 * A selectable option in a multiple-choice message.
 *
 * @param id Unique identifier for the option
 * @param label Display text for the option
 */
data class ChatOption(
    val id: String,
    val label: String
)

/**
 * Exercise information within a template proposal.
 *
 * @param name Exercise name
 * @param sets Number of sets
 * @param reps Rep range or count (e.g., "8-12", "10")
 * @param muscleGroup Target muscle group
 */
data class TemplateExerciseInfo(
    val name: String,
    val sets: Int = 3,
    val reps: String = "8-12",
    val muscleGroup: String = "",
    val recordingFields: List<RecordingField>? = null,
    val targetValues: Map<String, String>? = null
)
