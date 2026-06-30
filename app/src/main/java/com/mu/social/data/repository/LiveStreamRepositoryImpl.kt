package com.mu.social.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.mu.social.domain.model.live.*
import com.mu.social.domain.repository.LiveStreamRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LiveStreamRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions
) : LiveStreamRepository {

    override suspend fun createLiveStream(title: String, category: String, isPublic: Boolean): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val userDoc = firestore.collection("users").document(userId).get().await()
            
            val stream = LiveStream(
                id = firestore.collection("live_streams").document().id,
                hostId = userId,
                hostName = userDoc.getString("username") ?: "Unknown",
                hostAvatar = userDoc.getString("profilePicture") ?: "",
                title = title,
                category = category,
                isPublic = isPublic,
                status = StreamStatus.LIVE
            )
            
            firestore.collection("live_streams").document(stream.id).set(stream).await()
            Result.success(stream.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun endLiveStream(streamId: String): Result<Unit> {
        return try {
            firestore.collection("live_streams").document(streamId)
                .update("status", StreamStatus.ENDED, "endedAt", com.google.firebase.Timestamp.now())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActiveLiveStreams(): Flow<List<LiveStream>> = callbackFlow {
        val subscription = firestore.collection("live_streams")
            .whereEqualTo("status", StreamStatus.LIVE)
            .orderBy("startedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val streams = snapshot?.toObjects(LiveStream::class.java) ?: emptyList()
                trySend(streams)
            }
        awaitClose { subscription.remove() }
    }

    override fun getLiveStream(streamId: String): Flow<LiveStream?> = callbackFlow {
        val subscription = firestore.collection("live_streams").document(streamId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(LiveStream::class.java))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun sendChatMessage(streamId: String, message: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val userDoc = firestore.collection("users").document(userId).get().await()
            
            val chatMessage = LiveChatMessage(
                id = firestore.collection("live_streams").document(streamId).collection("live_chat").document().id,
                senderId = userId,
                senderName = userDoc.getString("username") ?: "Unknown",
                senderAvatar = userDoc.getString("profilePicture") ?: "",
                message = message
            )
            
            firestore.collection("live_streams").document(streamId)
                .collection("live_chat").document(chatMessage.id).set(chatMessage).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getChatMessages(streamId: String): Flow<List<LiveChatMessage>> = callbackFlow {
        val subscription = firestore.collection("live_streams").document(streamId)
            .collection("live_chat")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(LiveChatMessage::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun sendLike(streamId: String): Result<Unit> {
        return try {
            firestore.collection("live_streams").document(streamId)
                .update("likesCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendGift(streamId: String, gift: LiveGift): Result<Unit> {
        return try {
            functions
                .getHttpsCallable("sendLiveGift")
                .call(
                    mapOf(
                        "streamId" to streamId,
                        "giftId" to gift.id,
                        "giftName" to gift.name,
                        "coinValue" to gift.coinValue
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getGiftTransactions(streamId: String): Flow<List<LiveGiftTransaction>> = callbackFlow {
        val subscription = firestore.collection("live_streams").document(streamId)
            .collection("live_gifts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val gifts = snapshot?.toObjects(LiveGiftTransaction::class.java) ?: emptyList()
                trySend(gifts)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getAgoraToken(channelName: String, role: Int): Result<String> {
        return try {
            val response = functions
                .getHttpsCallable("generateAgoraToken")
                .call(
                    mapOf(
                        "channelName" to channelName,
                        "role" to role
                    )
                )
                .await()
            val token = (response.data as? Map<*, *>)?.get("token") as? String
            if (token.isNullOrBlank()) {
                Result.failure(IllegalStateException("Agora token service returned an empty token"))
            } else {
                Result.success(token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override val currentUserId: String
        get() = auth.currentUser?.uid ?: ""
}
