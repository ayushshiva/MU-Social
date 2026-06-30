package com.mu.social.domain.repository

import android.net.Uri
import com.mu.social.domain.model.Story
import com.mu.social.utils.Resource
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    suspend fun uploadStory(story: Story, mediaUri: Uri?): Resource<Unit>
    fun getActiveStories(): Flow<Resource<List<Story>>>
    suspend fun viewStory(storyId: String, userId: String): Resource<Unit>
    suspend fun likeStory(storyId: String, userId: String): Resource<Unit>
    suspend fun sendStoryReply(storyId: String, ownerId: String, senderId: String, message: String): Resource<Unit>
}
