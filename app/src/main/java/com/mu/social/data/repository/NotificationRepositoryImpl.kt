package com.mu.social.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mu.social.domain.model.Notification
import com.mu.social.domain.repository.NotificationRepository
import com.mu.social.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<Resource<List<Notification>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("notifications")
            .whereEqualTo("recipientId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50) // Basic pagination limit
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val notifications = snapshot?.toObjects(Notification::class.java) ?: emptyList()
                trySend(Resource.Success(notifications))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun markAsRead(notificationId: String): Resource<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to mark as read")
        }
    }

    override suspend fun deleteNotification(notificationId: String): Resource<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete notification")
        }
    }

    override suspend fun sendNotification(notification: Notification): Resource<Unit> {
        return try {
            val docRef = firestore.collection("notifications").document()
            val finalNotification = notification.copy(id = docRef.id)
            docRef.set(finalNotification).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to send notification")
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val subscription = firestore.collection("notifications")
            .whereEqualTo("recipientId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { subscription.remove() }
    }
}
