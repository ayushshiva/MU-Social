package com.mu.social.domain.model

enum class PostType {
    IMAGE, VIDEO, CAROUSEL, TEXT
}

data class Post(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfilePicture: String = "",
    val caption: String = "",
    val mediaUrls: List<String> = emptyList(),
    val postType: PostType = PostType.IMAGE,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val hashtags: List<String> = emptyList(),
    val location: String = ""
)
