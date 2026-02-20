package com.workout.app.presentation.chat

import com.workout.app.data.remote.MessageDto
import com.workout.app.data.remote.MessageMetadataDto
import com.workout.app.data.repository.ChatRepository
import com.workout.app.data.repository.ExerciseRepository
import com.workout.app.data.repository.TemplateRepository
import com.workout.app.domain.model.ChatMessage
import com.workout.app.domain.model.ChatMessageMetadata
import com.workout.app.domain.model.ChatOption
import com.workout.app.domain.model.MessageRole
import com.workout.app.domain.model.Result
import com.workout.app.domain.model.TemplateExerciseInfo
import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * ViewModel for the AI Assistant chat screen.
 * Manages conversation state, message sending/receiving, and saving proposals.
 *
 * @param conversationId Existing conversation ID to resume, or null for a new conversation
 * @param chatRepository Repository for chat API operations
 * @param templateRepository Repository for saving workout templates locally
 * @param exerciseRepository Repository for saving exercises locally
 */
class ChatViewModel(
    private var conversationId: String?,
    private val chatRepository: ChatRepository,
    private val templateRepository: TemplateRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        if (conversationId != null) {
            _state.update { it.copy(conversationId = conversationId) }
            loadMessages()
        }
        checkServerConnection()
    }

    /**
     * Send a user message to the AI assistant.
     * Creates a new conversation if one doesn't exist yet.
     * Adds an optimistic user message immediately, then streams the assistant response.
     */
    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSending = true, error = null) }

            // Create conversation if needed
            if (conversationId == null) {
                when (val result = chatRepository.createConversation()) {
                    is Result.Success -> {
                        conversationId = result.data
                        _state.update { it.copy(conversationId = result.data) }
                    }
                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                isSending = false,
                                error = "Failed to create conversation: ${result.exception.message}"
                            )
                        }
                        return@launch
                    }
                    is Result.Loading -> { /* Should not happen */ }
                }
            }

            val currentConversationId = conversationId ?: return@launch

            // Add optimistic user message
            val userMessage = ChatMessage(
                id = "temp-${Clock.System.now().toEpochMilliseconds()}",
                role = MessageRole.USER,
                content = content,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
            _state.update { it.copy(messages = it.messages + userMessage) }

            // Send message and stream response
            chatRepository.sendMessage(currentConversationId, content)
                .onStart {
                    _state.update { it.copy(isAgentTyping = true) }
                }
                .onCompletion {
                    _state.update { it.copy(isSending = false, isAgentTyping = false) }
                }
                .catch { error ->
                    _state.update {
                        it.copy(
                            isSending = false,
                            isAgentTyping = false,
                            error = "Failed to send message: ${error.message}"
                        )
                    }
                }
                .collect { messageDto ->
                    val domainMessage = mapDtoToDomain(messageDto)
                    _state.update { currentState ->
                        // Replace the optimistic user message if we get the real one back,
                        // or add the assistant message
                        val updatedMessages = if (messageDto.role == "user") {
                            // Replace the temp user message with the server-confirmed one
                            currentState.messages.map { msg ->
                                if (msg.id == userMessage.id) domainMessage else msg
                            }
                        } else {
                            // Check if this assistant message already exists (streaming update)
                            val existingIndex = currentState.messages.indexOfFirst {
                                it.id == domainMessage.id
                            }
                            if (existingIndex >= 0) {
                                currentState.messages.toMutableList().apply {
                                    set(existingIndex, domainMessage)
                                }
                            } else {
                                currentState.messages + domainMessage
                            }
                        }
                        currentState.copy(messages = updatedMessages)
                    }
                }
        }
    }

    /**
     * Select an option from a multiple-choice message.
     * Marks the option as selected locally and sends the label as a new user message.
     *
     * @param messageId ID of the message containing the options
     * @param optionId ID of the selected option
     * @param optionLabel Display label of the selected option (sent as message content)
     */
    fun selectOption(messageId: String, optionId: String, optionLabel: String) {
        // Mark the option as selected in the message
        _state.update { currentState ->
            val updatedMessages = currentState.messages.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(selectedOptionId = optionId)
                } else {
                    msg
                }
            }
            currentState.copy(messages = updatedMessages)
        }

        // Send the selected option label as a new user message
        sendMessage(optionLabel)
    }

    /**
     * Save a template proposal from a chat message to the local database.
     *
     * @param messageId ID of the message containing the template proposal
     */
    fun saveTemplate(messageId: String) {
        val message = _state.value.messages.find { it.id == messageId } ?: return
        val proposal = message.metadata as? ChatMessageMetadata.TemplateProposal ?: return

        viewModelScope.launch {
            // Convert exercise list to JSON format expected by TemplateRepository
            val exercisesJson = buildJsonArray {
                proposal.exercises.forEach { exercise ->
                    add(buildJsonObject {
                        put("name", exercise.name)
                        put("sets", exercise.sets)
                        put("reps", exercise.reps)
                        put("muscleGroup", exercise.muscleGroup)
                    })
                }
            }.toString()

            when (val result = templateRepository.create(
                name = proposal.name,
                description = proposal.description,
                exercises = exercisesJson,
                estimatedDuration = proposal.estimatedDuration?.toLong(),
                isDefault = false
            )) {
                is Result.Success -> {
                    _state.update {
                        it.copy(error = null)
                    }
                    // Send a confirmation message
                    val confirmMessage = ChatMessage(
                        id = "saved-template-${Clock.System.now().toEpochMilliseconds()}",
                        role = MessageRole.SYSTEM,
                        content = "Template \"${proposal.name}\" saved successfully!",
                        createdAt = Clock.System.now().toEpochMilliseconds()
                    )
                    _state.update { it.copy(messages = it.messages + confirmMessage) }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(error = "Failed to save template: ${result.exception.message}")
                    }
                }
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }

    /**
     * Save an exercise proposal from a chat message to the local database.
     *
     * @param messageId ID of the message containing the exercise proposal
     */
    fun saveExercise(messageId: String) {
        val message = _state.value.messages.find { it.id == messageId } ?: return
        val proposal = message.metadata as? ChatMessageMetadata.ExerciseProposal ?: return

        viewModelScope.launch {
            when (val result = exerciseRepository.create(
                name = proposal.name,
                muscleGroup = proposal.muscleGroup,
                category = proposal.category,
                equipment = proposal.equipment,
                difficulty = proposal.difficulty,
                instructions = proposal.instructions
            )) {
                is Result.Success -> {
                    _state.update {
                        it.copy(error = null)
                    }
                    // Send a confirmation message
                    val confirmMessage = ChatMessage(
                        id = "saved-exercise-${Clock.System.now().toEpochMilliseconds()}",
                        role = MessageRole.SYSTEM,
                        content = "Exercise \"${proposal.name}\" saved successfully!",
                        createdAt = Clock.System.now().toEpochMilliseconds()
                    )
                    _state.update { it.copy(messages = it.messages + confirmMessage) }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(error = "Failed to save exercise: ${result.exception.message}")
                    }
                }
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }

    /**
     * Load existing messages when resuming a conversation.
     */
    fun loadMessages() {
        val id = conversationId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = chatRepository.getMessages(id)) {
                is Result.Success -> {
                    val messages = result.data.map { mapDtoToDomain(it) }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            messages = messages
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load messages: ${result.exception.message}"
                        )
                    }
                }
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }

    /**
     * Check if the agent server is reachable.
     */
    private fun checkServerConnection() {
        viewModelScope.launch {
            when (chatRepository.getConversations()) {
                is Result.Success -> {
                    _state.update { it.copy(serverConnected = true) }
                }
                is Result.Error -> {
                    _state.update { it.copy(serverConnected = false) }
                }
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }

    /**
     * Dismiss the current error message.
     */
    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Map a MessageDto from the data layer to a domain ChatMessage.
     */
    private fun mapDtoToDomain(dto: MessageDto): ChatMessage {
        return ChatMessage(
            id = dto.id,
            role = when (dto.role) {
                "user" -> MessageRole.USER
                "assistant" -> MessageRole.ASSISTANT
                "system" -> MessageRole.SYSTEM
                else -> MessageRole.ASSISTANT
            },
            content = dto.content,
            metadata = dto.metadata?.let { mapMetadataDtoToDomain(it) },
            createdAt = dto.createdAt
        )
    }

    /**
     * Map metadata DTO to domain metadata based on the type field.
     */
    private fun mapMetadataDtoToDomain(dto: MessageMetadataDto): ChatMessageMetadata? {
        return when (dto.type) {
            "multiple_choice" -> {
                val options = dto.options?.map { option ->
                    ChatOption(
                        id = option.id,
                        label = option.label
                    )
                } ?: emptyList()
                ChatMessageMetadata.MultipleChoice(options)
            }
            "template_proposal" -> {
                val templateData = dto.templateData ?: return null
                ChatMessageMetadata.TemplateProposal(
                    name = templateData.name,
                    description = templateData.description,
                    exercises = templateData.exercises.map { exercise ->
                        TemplateExerciseInfo(
                            name = exercise.name,
                            sets = exercise.sets,
                            reps = exercise.reps,
                            muscleGroup = exercise.muscleGroup
                        )
                    },
                    estimatedDuration = templateData.estimatedDuration
                )
            }
            "exercise_proposal" -> {
                val exerciseData = dto.exerciseData ?: return null
                ChatMessageMetadata.ExerciseProposal(
                    name = exerciseData.name,
                    muscleGroup = exerciseData.muscleGroup,
                    category = exerciseData.category,
                    equipment = exerciseData.equipment,
                    difficulty = exerciseData.difficulty,
                    instructions = exerciseData.instructions
                )
            }
            else -> null
        }
    }
}

/**
 * UI state for the Chat screen.
 *
 * @param messages All messages in the current conversation
 * @param isLoading Whether messages are being loaded (initial load)
 * @param isSending Whether a message is currently being sent
 * @param isAgentTyping Whether the AI assistant is currently generating a response
 * @param error Current error message, or null if no error
 * @param serverConnected Whether the agent server is reachable
 * @param conversationId The current conversation ID, or null for a new conversation
 */
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isAgentTyping: Boolean = false,
    val error: String? = null,
    val serverConnected: Boolean = true,
    val conversationId: String? = null
)
