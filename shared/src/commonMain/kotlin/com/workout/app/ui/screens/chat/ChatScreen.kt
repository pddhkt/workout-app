package com.workout.app.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.domain.model.AgentStatus
import com.workout.app.presentation.chat.ChatViewModel
import com.workout.app.ui.components.chat.AgentStatusIndicator
import com.workout.app.ui.components.chat.ChatBubble
import com.workout.app.ui.components.chat.ChatHistoryDrawer
import com.workout.app.ui.components.chat.ChatInput
import com.workout.app.ui.theme.AppTheme
import kotlinx.coroutines.launch

/**
 * Full-screen chat interface for the AI Workout Assistant.
 *
 * Features:
 * - TopAppBar with "AI Assistant" title and back navigation
 * - ChatInput as bottom bar
 * - LazyColumn of messages with auto-scroll
 * - Empty state with suggested prompts
 * - Typing indicator when agent is responding
 * - Error and disconnected banners
 *
 * @param onBackClick Callback for back navigation
 * @param viewModel ChatViewModel instance (injected via Koin)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    viewModel: ChatViewModel
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    // Animate bottom bar in from the bottom on screen entry
    var bottomBarVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { bottomBarVisible = true }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatHistoryDrawer(
                conversations = state.conversations,
                currentConversationId = state.conversationId,
                isLoading = state.isLoadingConversations,
                onConversationSelected = { id ->
                    viewModel.switchConversation(id)
                    scope.launch { drawerState.close() }
                },
                onNewConversation = {
                    viewModel.startNewConversation()
                    scope.launch { drawerState.close() }
                },
                onDeleteConversation = { id ->
                    viewModel.deleteConversationById(id)
                }
            )
        }
    ) {
    Scaffold(
        modifier = Modifier.imePadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Assistant",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Chat history"
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = bottomBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(200)
                ) + fadeOut(tween(200))
            ) {
                ChatInput(
                    onSendMessage = { viewModel.sendMessage(it) },
                    isSending = state.isSending
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Disconnected banner
            if (!state.serverConnected) {
                DisconnectedBanner()
            }

            // Error banner
            if (state.error != null) {
                ErrorBanner(
                    message = state.error!!,
                    onDismiss = { viewModel.dismissError() }
                )
            }

            // Loading state
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (state.messages.isEmpty()) {
                // Empty state
                EmptyState(
                    onSuggestionClick = { viewModel.sendMessage(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                // Messages list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = AppTheme.spacing.lg,
                        vertical = AppTheme.spacing.md
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    items(
                        items = state.messages,
                        key = { it.id }
                    ) { message ->
                        ChatBubble(
                            message = message,
                            onOptionSelected = { messageId, optionId, optionLabel ->
                                viewModel.selectOption(messageId, optionId, optionLabel)
                            },
                            onMultiOptionSelected = { messageId, questionId, optionId, optionLabel ->
                                viewModel.selectMultiOption(messageId, questionId, optionId, optionLabel)
                            },
                            onSaveTemplate = { messageId ->
                                viewModel.saveTemplate(messageId)
                            },
                            onSaveExercise = { messageId ->
                                viewModel.saveExercise(messageId)
                            },
                            onSaveGoal = { messageId ->
                                viewModel.saveGoal(messageId)
                            }
                        )
                    }

                    // Agent status indicator
                    if (state.agentStatus !is AgentStatus.Idle) {
                        item(key = "status-indicator") {
                            AgentStatusIndicator(status = state.agentStatus)
                        }
                    }
                }
            }
        }
    }
    } // ModalNavigationDrawer
}

/**
 * Empty state shown when there are no messages in the conversation.
 * Displays a sparkle icon, heading, description, and suggested prompt chips.
 */
@Composable
private fun EmptyState(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        "Create a push/pull/legs template",
        "Suggest exercises for back",
        "Build a 30-min HIIT workout"
    )

    Column(
        modifier = modifier.padding(AppTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Sparkle icon
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        // Heading
        Text(
            text = "AI Workout Assistant",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

        // Description
        Text(
            text = "Ask me to create workout templates, suggest exercises, or help plan your training.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Suggested prompts
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            suggestions.forEach { suggestion ->
                SuggestionChip(
                    text = suggestion,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

/**
 * Suggestion chip for the empty state.
 * Surface with border that sends the suggestion text when tapped.
 */
@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppTheme.spacing.md,
                vertical = AppTheme.spacing.md
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Banner displayed when the agent server is not reachable.
 */
@Composable
private fun DisconnectedBanner(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface)
            .padding(
                horizontal = AppTheme.spacing.lg,
                vertical = AppTheme.spacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.surface
        )
        Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
        Text(
            text = "Server disconnected. Check your connection.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * Dismissible error banner displayed at the top of the chat.
 */
@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(
                horizontal = AppTheme.spacing.lg,
                vertical = AppTheme.spacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Dismiss error",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
