package com.workout.app.di

import com.workout.app.data.remote.AgentApiClient
import com.workout.app.data.repository.AgentChatRepositoryImpl
import com.workout.app.data.repository.ChatRepository
import com.workout.app.presentation.chat.ChatViewModel
import org.koin.dsl.module

/**
 * Koin module for AI chat assistant dependencies.
 */
val chatModule = module {
    // Agent API Client - singleton
    single {
        AgentApiClient(
            baseUrl = "http://72.60.237.113:3141" // Server IP - configurable in settings
        )
    }

    // Chat Repository
    single<ChatRepository> {
        AgentChatRepositoryImpl(apiClient = get())
    }

    // Chat ViewModel - requires optional conversationId parameter
    factory { (conversationId: String?) ->
        ChatViewModel(
            conversationId = conversationId,
            chatRepository = get(),
            templateRepository = get(),
            exerciseRepository = get(),
            goalRepository = get()
        )
    }
}
