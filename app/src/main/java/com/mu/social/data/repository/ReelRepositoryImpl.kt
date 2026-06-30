package com.mu.social.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.mu.social.domain.model.Reel
import com.mu.social.domain.repository.ReelRepository
import com.mu.social.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ReelRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ReelRepository {

    override suspend fun uploadReel(reel: Reel, videoBytes: ByteArray): Resource<Unit> {
        return try {
            val fileName = UUID.randomUUID().toString() + ".mp4"
            val reelId = firestore.collection("reels").document().id
            val ref = storage.reference.child("reels/${reel.userId}/$reelId/$fileName")
            ref.putBytes(videoBytes).await()
            val url = ref.downloadUrl.await().toString()

            val finalReel = reel.copy(
                reelId = reelId,
                videoUrl = url
            )

            firestore.collection("reels").document(finalReel.reelId).set(finalReel).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to upload reel")
        }
    }

    override fun getReels(): Flow<Resource<List<Reel>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("reels")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val reels = snapshot?.toObjects(Reel::class.java) ?: emptyList()
                trySend(Resource.Success(reels))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun likeReel(reelId: String, userId: String): Resource<Unit> {
        return try {
            val reelRef = firestore.collection("reels").document(reelId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(reelRef)
                val currentLikes = snapshot.getLong("likesCount") ?: 0
                transaction.update(reelRef, "likesCount", currentLikes + 1)
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to like reel")
        }
    }
}
