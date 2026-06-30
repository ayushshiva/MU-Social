package com.mu.social.domain.model

enum class NotificationType {
    FOLLOW, LIKE, COMMENT, STORY_REACTION, MESSAGE, SYSTEM
}

data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val senderProfilePicture: String = "",
    val type: NotificationType = NotificationType.SYSTEM,
    val contentId: String? = null, // PostId, ReelId, StoryId etc
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
