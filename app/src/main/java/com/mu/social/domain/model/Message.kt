package com.mu.social.domain.model

enum class MessageType {
    TEXT, IMAGE, VIDEO, VOICE, DOCUMENT
}

data class Message(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: MessageType = MessageType.TEXT,
    val seen: Boolean = false,
    val reactions: Map<String, String> = emptyMap(), // userId to emoji
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val isDeletedForEveryone: Boolean = false,
    val deletedByUsers: List<String> = emptyList() // For "Delete for me"
)

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val lastMessageSenderId: String = "",
    val isGroup: Boolean = false,
    val groupName: String? = null,
    val groupIcon: String? = null,
    val typingStatus: Map<String, Boolean> = emptyMap() // userId to isTyping
)
