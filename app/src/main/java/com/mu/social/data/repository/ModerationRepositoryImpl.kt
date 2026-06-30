package com.mu.social.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mu.social.domain.model.ModerationAction
import com.mu.social.domain.model.ModerationStatus
import com.mu.social.domain.model.Report
import com.mu.social.domain.repository.ModerationRepository
import com.mu.social.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ModerationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ModerationRepository {

    override suspend fun reportContent(report: Report): Resource<Unit> {
        return try {
            val docRef = firestore.collection("reports").document()
            val finalReport = report.copy(reportId = docRef.id)
            docRef.set(finalReport).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to submit report")
        }
    }

    override fun getModerationQueue(): Flow<Resource<List<Report>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("reports")
            .whereEqualTo("status", ModerationStatus.PENDING.name)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error fetching reports"))
                    return@addSnapshotListener
                }
                val reports = snapshot?.toObjects(Report::class.java) ?: emptyList()
                trySend(Resource.Success(reports))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateReportStatus(reportId: String, status: String, notes: String): Resource<Unit> {
        return try {
            firestore.collection("reports").document(reportId)
                .update(
                    "status", status,
                    "moderatorNotes", notes
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update report")
        }
    }

    override suspend fun takeAction(action: ModerationAction): Resource<Unit> {
        return try {
            val docRef = firestore.collection("moderation_actions").document()
            val finalAction = action.copy(actionId = docRef.id)
            
            firestore.runBatch { batch ->
                batch.set(firestore.collection("moderation_actions").document(finalAction.actionId), finalAction)
                
                // Update user document if it's a ban/suspension
                val userRef = firestore.collection("users").document(action.targetUserId)
                if (action.actionType == "BAN") {
                    batch.update(userRef, "isBanned", true)
                } else if (action.actionType == "SUSPEND") {
                    batch.update(userRef, "suspensionExpiresAt", action.expiresAt)
                }
            }.await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to take moderation action")
        }
    }

    override suspend fun isUserBanned(userId: String): Resource<Boolean> {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            val isBanned = snapshot.getBoolean("isBanned") ?: false
            val suspensionExpiresAt = snapshot.getLong("suspensionExpiresAt")
            
            val isSuspended = suspensionExpiresAt != null && suspensionExpiresAt > System.currentTimeMillis()
            
            Resource.Success(isBanned || isSuspended)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error checking ban status")
        }
    }
}
