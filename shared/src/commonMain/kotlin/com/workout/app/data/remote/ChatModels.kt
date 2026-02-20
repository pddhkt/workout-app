package com.workout.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: String,
    val title: String? = null,
    val status: String = "active",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

@Serializable
data class MessageDto(
    val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val metadata: MessageMetadataDto? = null,
    val createdAt: Long = 0
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
data class TemplateExerciseDto(
    val name: String,
    val sets: Int = 3,
    val reps: String = "8-12",
    val muscleGroup: String = ""
)

@Serializable
data class ExerciseProposalDto(
    val name: String,
    val muscleGroup: String,
    val category: String? = null,
    val equipment: String? = null,
    val difficulty: String? = null,
    val instructions: String? = null
)

@Serializable
data class SendMessageRequest(
    val content: String
)
