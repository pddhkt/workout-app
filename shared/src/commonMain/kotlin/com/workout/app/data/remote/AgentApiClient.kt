package com.workout.app.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AgentApiClient(private val baseUrl: String) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
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

    suspend fun sendMessageStream(
        conversationId: String,
        content: String,
        onStatus: (String) -> Unit
    ): MessageDto {
        return client.preparePost("$baseUrl/conversations/$conversationId/messages/stream") {
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest(content))
            timeout {
                requestTimeoutMillis = 180_000
                socketTimeoutMillis = 180_000
            }
        }.execute { httpResponse ->
            val channel = httpResponse.bodyAsChannel()
            var finalMessage: MessageDto? = null

            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ")
                    try {
                        val jsonObj = json.parseToJsonElement(data).jsonObject
                        when (jsonObj["type"]?.jsonPrimitive?.contentOrNull) {
                            "status" -> {
                                val statusText = jsonObj["text"]?.jsonPrimitive?.contentOrNull ?: ""
                                onStatus(statusText)
                            }
                            "done" -> {
                                val messageJson = jsonObj["message"]
                                if (messageJson != null) {
                                    finalMessage = json.decodeFromJsonElement(
                                        MessageDto.serializer(),
                                        messageJson
                                    )
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Skip malformed SSE lines
                    }
                }
            }
            finalMessage ?: throw Exception("Stream ended without done event")
        }
    }

    fun close() { client.close() }
}
