package com.mu.social.domain.model

data class Reel(
    val reelId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfilePicture: String = "",
    val videoUrl: String = "",
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val audioTitle: String = "Original Audio"
)
