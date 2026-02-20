package com.workout.app.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class AgentApiClient(private val baseUrl: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 120_000
        }
    }

    suspend fun getConversations(): List<ConversationDto> {
        return client.get("$baseUrl/conversations").body()
    }

    suspend fun createConversation(): ConversationDto {
        return client.post("$baseUrl/conversations") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }.body()
    }

    suspend fun deleteConversation(id: String) {
        client.delete("$baseUrl/conversations/$id")
    }

    suspend fun getMessages(conversationId: String): List<MessageDto> {
        return client.get("$baseUrl/conversations/$conversationId/messages").body()
    }

    suspend fun sendMessage(conversationId: String, content: String): MessageDto {
        return client.post("$baseUrl/conversations/$conversationId/messages") {
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest(content))
        }.body()
    }

    fun close() { client.close() }
}
