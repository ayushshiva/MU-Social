package com.mu.social.domain.model

data class UserAnalytics(
    val userId: String = "",
    val profileViews: Long = 0,
    val totalFollowers: Int = 0,
    val followerGrowth: Map<String, Int> = emptyMap(), // Date to count
    val engagementRate: Double = 0.0,
    val reach: Long = 0,
    val impressions: Long = 0
)

data class ContentAnalytics(
    val contentId: String = "",
    val ownerId: String = "",
    val type: String = "post", // post, reel, story
    val views: Long = 0,
    val likes: Int = 0,
    val comments: Int = 0,
    val shares: Int = 0,
    val saved: Int = 0,
    val reach: Long = 0
)

data class DailyMetrics(
    val date: String = "",
    val activeUsers: Int = 0,
    val newPosts: Int = 0,
    val totalEngagement: Int = 0
)
