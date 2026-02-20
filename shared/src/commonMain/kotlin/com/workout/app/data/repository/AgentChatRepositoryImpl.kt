package com.workout.app.data.repository

import com.workout.app.data.remote.AgentApiClient
import com.workout.app.data.remote.ConversationDto
import com.workout.app.data.remote.MessageDto
import com.workout.app.domain.model.Result

class AgentChatRepositoryImpl(
    private val apiClient: AgentApiClient
) : ChatRepository {

    override suspend fun createConversation(): Result<ConversationDto> {
        return try {
            Result.Success(apiClient.createConversation())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getConversations(): Result<List<ConversationDto>> {
        return try {
            Result.Success(apiClient.getConversations())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getMessages(conversationId: String): Result<List<MessageDto>> {
        return try {
            Result.Success(apiClient.getMessages(conversationId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun sendMessage(conversationId: String, content: String): Result<MessageDto> {
        return try {
            Result.Success(apiClient.sendMessage(conversationId, content))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteConversation(id: String): Result<Unit> {
        return try {
            apiClient.deleteConversation(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
