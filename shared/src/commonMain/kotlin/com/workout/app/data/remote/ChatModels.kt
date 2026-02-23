package com.workout.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: String,
    val title: String? = null,
    val status: String = "active",
    @SerialName("created_at") val createdAt: Long = 0,
    @SerialName("updated_at") val updatedAt: Long = 0
)

@Serializable
data class MessageDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    val role: String,
    val content: String,
    val metadata: MessageMetadataDto? = null,
    @SerialName("created_at") val createdAt: Long = 0
)

@Serializable
data class MessageMetadataDto(
    val type: String? = null,
    val options: List<ChoiceOptionDto>? = null,
    val templateData: TemplateProposalDto? = null,
    val exerciseData: ExerciseProposalDto? = null
)

@Serializable
data class ChoiceOptionDto(
    val id: String,
    val label: String
)

@Serializable
data class TemplateProposalDto(
    val name: String,
    val description: String? = null,
    val exercises: List<TemplateExerciseDto> = emptyList(),
    val estimatedDuration: Int? = null
)

@Serializable
data class RecordingFieldDto(
    val key: String,
    val label: String,
    val type: String,
    val unit: String = "",
    val required: Boolean = true
)

@Serializable
data class TemplateExerciseDto(
    val name: String,
    val sets: Int = 3,
    val reps: String = "8-12",
    val muscleGroup: String = "",
    val recordingFields: List<RecordingFieldDto>? = null,
    val targetValues: Map<String, String>? = null
)

@Serializable
data class ExerciseProposalDto(
    val name: String,
    val muscleGroup: String,
    val category: String? = null,
    val equipment: String? = null,
    val difficulty: String? = null,
    val instructions: String? = null,
    val recordingFields: List<RecordingFieldDto>? = null
)

@Serializable
data class SendMessageRequest(
    val content: String
)
