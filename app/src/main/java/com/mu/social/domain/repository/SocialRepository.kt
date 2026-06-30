package com.mu.social.domain.repository

import com.mu.social.domain.model.User
import com.mu.social.utils.Resource
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    suspend fun followUser(currentUserId: String, targetUserId: String): Resource<Unit>
    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Resource<Unit>
    fun getFollowers(userId: String): Flow<Resource<List<User>>>
    fun getFollowing(userId: String): Flow<Resource<List<User>>>
    fun searchUsers(query: String): Flow<Resource<List<User>>>
    suspend fun getUserProfile(userId: String): Resource<User>
    suspend fun updateProfile(user: User, profileImage: ByteArray?, coverImage: ByteArray?): Resource<Unit>
}
