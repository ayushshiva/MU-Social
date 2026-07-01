package com.mu.social.presentation.chat

import com.mu.social.domain.model.Chat

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

