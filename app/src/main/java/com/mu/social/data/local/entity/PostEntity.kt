package com.mu.social.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mu.social.domain.model.Post
import com.mu.social.domain.model.PostType

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val postId: String,
    val userId: String,
    val username: String,
    val userProfilePicture: String,
    val caption: String,
    val mediaUrls: String, // Stored as comma-separated or JSON
    val postType: String,
    val timestamp: Long,
    val likesCount: Int,
    val commentsCount: Int,
    val hashtags: String,
    val location: String
) {
    fun toPost(): Post {
        return Post(
            postId = postId,
            userId = userId,
            username = username,
            userProfilePicture = userProfilePicture,
            caption = caption,
            mediaUrls = if (mediaUrls.isBlank()) emptyList() else mediaUrls.split(","),
            postType = PostType.valueOf(postType),
            timestamp = timestamp,
            likesCount = likesCount,
            commentsCount = commentsCount,
            hashtags = if (hashtags.isBlank()) emptyList() else hashtags.split(","),
            location = location
        )
    }

    companion object {
        fun fromPost(post: Post): PostEntity {
            return PostEntity(
                postId = post.postId,
                userId = post.userId,
                username = post.username,
                userProfilePicture = post.userProfilePicture,
                caption = post.caption,
                mediaUrls = post.mediaUrls.joinToString(","),
                postType = post.postType.name,
                timestamp = post.timestamp,
                likesCount = post.likesCount,
                commentsCount = post.commentsCount,
                hashtags = post.hashtags.joinToString(","),
                location = post.location
            )
        }
    }
}
