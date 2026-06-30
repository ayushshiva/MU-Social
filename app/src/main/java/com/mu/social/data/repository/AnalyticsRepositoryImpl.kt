package com.mu.social.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.mu.social.domain.model.ContentAnalytics
import com.mu.social.domain.model.UserAnalytics
import com.mu.social.domain.repository.AnalyticsRepository
import com.mu.social.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AnalyticsRepository {

    override suspend fun trackEvent(userId: String, eventType: String, metadata: Map<String, Any>): Resource<Unit> {
        return try {
            firestore.collection("analytics_events").add(
                mapOf(
                    "userId" to userId,
                    "eventType" to eventType,
                    "metadata" to metadata,
                    "timestamp" to FieldValue.serverTimestamp()
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to track event")
        }
    }

    override fun getUserAnalytics(userId: String): Flow<Resource<UserAnalytics>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("analytics").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error fetching analytics"))
                    return@addSnapshotListener
                }
                val analytics = snapshot?.toObject(UserAnalytics::class.java) ?: UserAnalytics(userId = userId)
                trySend(Resource.Success(analytics))
            }
        awaitClose { subscription.remove() }
    }

    override fun getContentAnalytics(contentId: String): Flow<Resource<ContentAnalytics>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("content_analytics").document(contentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error"))
                    return@addSnapshotListener
                }
                val analytics = snapshot?.toObject(ContentAnalytics::class.java) ?: ContentAnalytics(contentId = contentId)
                trySend(Resource.Success(analytics))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun incrementView(contentId: String, contentType: String): Resource<Unit> {
        return try {
            val ownerId = when (contentType) {
                "post" -> firestore.collection("posts").document(contentId).get().await().getString("userId")
                "reel" -> firestore.collection("reels").document(contentId).get().await().getString("userId")
                "story" -> firestore.collection("stories").document(contentId).get().await().getString("userId")
                else -> null
            }.orEmpty()

            firestore.collection("content_analytics").document(contentId)
                .set(
                    mapOf(
                        "contentId" to contentId,
                        "ownerId" to ownerId,
                        "type" to contentType,
                        "views" to FieldValue.increment(1)
                    ),
                    SetOptions.merge()
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to increment view")
        }
    }

    override fun getCreatorDashboardMetrics(userId: String): Flow<Resource<List<ContentAnalytics>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("content_analytics")
            .whereEqualTo("ownerId", userId)
            .orderBy("views", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error"))
                    return@addSnapshotListener
                }
                val metrics = snapshot?.toObjects(ContentAnalytics::class.java) ?: emptyList()
                trySend(Resource.Success(metrics))
            }
        awaitClose { subscription.remove() }
    }
}
