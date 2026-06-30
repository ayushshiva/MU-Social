package com.mu.social.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.mu.social.data.local.dao.PostDao
import com.mu.social.data.local.entity.PostEntity
import com.mu.social.domain.model.Comment
import com.mu.social.domain.model.Post
import com.mu.social.domain.model.PostType
import com.mu.social.domain.repository.PostRepository
import com.mu.social.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val postDao: PostDao
) : PostRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override suspend fun createPost(post: Post, mediaBytes: List<ByteArray>): Resource<Unit> {
        return try {
            val uploadedUrls = mutableListOf<String>()
            val postId = firestore.collection("posts").document().id
            for (bytes in mediaBytes) {
                val fileName = UUID.randomUUID().toString()
                val ref = storage.reference.child("posts/${post.userId}/$postId/$fileName")
                ref.putBytes(bytes).await()
                val url = ref.downloadUrl.await().toString()
                uploadedUrls.add(url)
            }

            val finalPost = post.copy(
                postId = postId,
                mediaUrls = uploadedUrls,
                postType = if (uploadedUrls.size > 1) PostType.CAROUSEL else if (uploadedUrls.isNotEmpty()) PostType.IMAGE else PostType.TEXT
            )

            firestore.collection("posts").document(postId).set(finalPost).await()
            // Cache locally
            postDao.insertPosts(listOf(PostEntity.fromPost(finalPost)))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create post")
        }
    }

    override fun getPosts(): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())
        
        val subscription = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(Post::class.java) ?: emptyList()
                
                // Update local cache
                repositoryScope.launch {
                    postDao.clearPosts()
                    postDao.insertPosts(posts.map { PostEntity.fromPost(it) })
                }

                trySend(Resource.Success(posts))
            }
        awaitClose { subscription.remove() }
    }

    override fun getFollowingPosts(followingIds: List<String>): Flow<Resource<List<Post>>> = callbackFlow {
        if (followingIds.isEmpty()) {
            trySend(Resource.Success(emptyList()))
            return@callbackFlow
        }
        trySend(Resource.Loading())
        val subscription = firestore.collection("posts")
            .whereIn("userId", followingIds)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(Post::class.java) ?: emptyList()
                trySend(Resource.Success(posts))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getPostById(postId: String): Resource<Post> {
        return try {
            val doc = firestore.collection("posts").document(postId).get().await()
            val post = doc.toObject(Post::class.java)
            if (post != null) Resource.Success(post) else Resource.Error("Post not found")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch post")
        }
    }

    override fun getComments(postId: String): Flow<Resource<List<Comment>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val comments = snapshot?.toObjects(Comment::class.java) ?: emptyList()
                trySend(Resource.Success(comments))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun addComment(comment: Comment): Resource<Unit> {
        return try {
            val commentId = firestore.collection("comments").document().id
            val finalComment = comment.copy(commentId = commentId)
            val postRef = firestore.collection("posts").document(comment.postId)
            val commentRef = firestore.collection("comments").document(commentId)

            firestore.runBatch { batch ->
                batch.set(commentRef, finalComment)
                batch.update(postRef, "commentsCount", FieldValue.increment(1))
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to add comment")
        }
    }

    override suspend fun likePost(postId: String, userId: String): Resource<Unit> {
        return try {
            val postRef = firestore.collection("posts").document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.getLong("likesCount") ?: 0
                transaction.update(postRef, "likesCount", currentLikes + 1)
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to like post")
        }
    }

    override suspend fun deletePost(postId: String): Resource<Unit> {
        return try {
            firestore.collection("posts").document(postId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete post")
        }
    }

    override fun getTrendingPosts(): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("posts")
            .orderBy("likesCount", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(Post::class.java) ?: emptyList()
                trySend(Resource.Success(posts))
            }
        awaitClose { subscription.remove() }
    }
}
