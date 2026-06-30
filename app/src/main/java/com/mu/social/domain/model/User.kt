package com.mu.social.domain.model

data class User(
    val userId: String = "",
    val username: String = "",
    val fullName: String = "",
    val bio: String = "",
    val profilePictureUrl: String = "",
    val coverPhotoUrl: String = "",
    val website: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isVerified: Boolean = false,
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val fcmToken: String = ""
)
