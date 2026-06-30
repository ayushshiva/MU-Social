package com.mu.social.data.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.mu.social.domain.model.Story
import com.mu.social.domain.model.StoryType
import com.mu.social.domain.repository.StoryRepository
import com.mu.social.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class StoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : StoryRepository {

    override suspend fun uploadStory(story: Story, mediaUri: Uri?): Resource<Unit> {
        return try {
            var downloadUrl = ""
            if (mediaUri != null && story.storyType != StoryType.TEXT) {
                val fileName = "${UUID.randomUUID()}"
                val ref = storage.reference.child("stories/$fileName")
                ref.putFile(mediaUri).await()
                downloadUrl = ref.downloadUrl.await().toString()
            }

            val storyId = firestore.collection("stories").document().id
            val currentTime = System.currentTimeMillis()
            val finalStory = story.copy(
                storyId = storyId,
                mediaUrl = downloadUrl,
                timestamp = currentTime,
                expiresAt = currentTime + (24 * 60 * 60 * 1000)
            )

            firestore.collection("stories").document(storyId).set(finalStory).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to upload story")
        }
    }

    override fun getActiveStories(): Flow<Resource<List<Story>>> = callbackFlow {
        trySend(Resource.Loading())
        val currentTime = System.currentTimeMillis()
        val subscription = firestore.collection("stories")
            .whereGreaterThan("expiresAt", currentTime)
            .orderBy("expiresAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val stories = snapshot?.toObjects(Story::class.java) ?: emptyList()
                trySend(Resource.Success(stories))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun viewStory(storyId: String, userId: String): Resource<Unit> {
        return try {
            firestore.collection("stories").document(storyId)
                .update("viewers", FieldValue.arrayUnion(userId)).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update story viewers")
        }
    }

    override suspend fun likeStory(storyId: String, userId: String): Resource<Unit> {
        return try {
            val storyRef = firestore.collection("stories").document(storyId)
            val story = storyRef.get().await().toObject(Story::class.java)
            
            val isLiked = story?.likes?.contains(userId) == true
            if (isLiked) {
                storyRef.update("likes", FieldValue.arrayRemove(userId)).await()
            } else {
                storyRef.update("likes", FieldValue.arrayUnion(userId)).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to like story")
        }
    }

    override suspend fun sendStoryReply(
        storyId: String,
        ownerId: String,
        senderId: String,
        message: String
    ): Resource<Unit> {
        return try {
            val notificationId = firestore.collection("notifications").document().id
            val notification = mapOf(
                "notificationId" to notificationId,
                "type" to "STORY_REACTION",
                "senderId" to senderId,
                "receiverId" to ownerId,
                "content" to message,
                "targetId" to storyId,
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false
            )
            firestore.collection("notifications").document(notificationId).set(notification).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to send reply")
        }
    }
}
