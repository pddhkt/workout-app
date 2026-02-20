package com.workout.app.data.repository

import com.workout.app.data.remote.ConversationDto
import com.workout.app.data.remote.MessageDto
import com.workout.app.domain.model.Result

interface ChatRepository {
    suspend fun createConversation(): Result<ConversationDto>
    suspend fun getConversations(): Result<List<ConversationDto>>
    suspend fun getMessages(conversationId: String): Result<List<MessageDto>>
    suspend fun sendMessage(conversationId: String, content: String): Result<MessageDto>
    suspend fun deleteConversation(id: String): Result<Unit>
}
