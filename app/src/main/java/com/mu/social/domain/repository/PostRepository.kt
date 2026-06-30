package com.mu.social.domain.repository

import com.mu.social.domain.model.Post
import com.mu.social.domain.model.Comment
import com.mu.social.utils.Resource
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun createPost(post: Post, mediaBytes: List<ByteArray>): Resource<Unit>
    fun getPosts(): Flow<Resource<List<Post>>>
    fun getFollowingPosts(followingIds: List<String>): Flow<Resource<List<Post>>>
    suspend fun getPostById(postId: String): Resource<Post>
    fun getComments(postId: String): Flow<Resource<List<Comment>>>
    suspend fun addComment(comment: Comment): Resource<Unit>
    suspend fun likePost(postId: String, userId: String): Resource<Unit>
    suspend fun deletePost(postId: String): Resource<Unit>
    fun getTrendingPosts(): Flow<Resource<List<Post>>>
}
