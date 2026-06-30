package com.mu.social.domain.model

enum class StoryType {
    IMAGE, VIDEO, TEXT
}

data class Story(
    val storyId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfilePicture: String = "",
    val mediaUrl: String = "",
    val storyType: StoryType = StoryType.IMAGE,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = timestamp + (24 * 60 * 60 * 1000), // 24 hours later
    val viewers: List<String> = emptyList(),
    val likes: List<String> = emptyList()
)
