package com.mu.social.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mu.social.domain.model.User
import com.mu.social.domain.repository.SocialRepository
import com.mu.social.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SocialRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : SocialRepository {

    override suspend fun followUser(currentUserId: String, targetUserId: String): Resource<Unit> {
        return try {
            val batch = firestore.batch()
            
            // Add to current user's following
            val followingRef = firestore.collection("users").document(currentUserId)
                .collection("following").document(targetUserId)
            batch.set(followingRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
            
            // Add to target user's followers
            val followersRef = firestore.collection("users").document(targetUserId)
                .collection("followers").document(currentUserId)
            batch.set(followersRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
            
            // Update counts
            batch.update(firestore.collection("users").document(currentUserId), "followingCount", FieldValue.increment(1))
            batch.update(firestore.collection("users").document(targetUserId), "followersCount", FieldValue.increment(1))
            
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to follow user")
        }
    }

    override suspend fun unfollowUser(currentUserId: String, targetUserId: String): Resource<Unit> {
        return try {
            val batch = firestore.batch()
            
            val followingRef = firestore.collection("users").document(currentUserId)
                .collection("following").document(targetUserId)
            batch.delete(followingRef)
            
            val followersRef = firestore.collection("users").document(targetUserId)
                .collection("followers").document(currentUserId)
            batch.delete(followersRef)
            
            batch.update(firestore.collection("users").document(currentUserId), "followingCount", FieldValue.increment(-1))
            batch.update(firestore.collection("users").document(targetUserId), "followersCount", FieldValue.increment(-1))
            
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to unfollow user")
        }
    }

    override fun getFollowers(userId: String): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("users").document(userId)
            .collection("followers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val followerIds = snapshot?.documents?.map { it.id } ?: emptyList()
                launch {
                    trySend(fetchUsersByIds(followerIds))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getFollowing(userId: String): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("users").document(userId)
            .collection("following")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val followingIds = snapshot?.documents?.map { it.id } ?: emptyList()
                launch {
                    trySend(fetchUsersByIds(followingIds))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun searchUsers(query: String): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThanOrEqualTo("username", query + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(Resource.Success(users))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getUserProfile(userId: String): Resource<User> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) Resource.Success(user) else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch profile")
        }
    }

    override suspend fun updateProfile(user: User, profileImage: ByteArray?, coverImage: ByteArray?): Resource<Unit> {
        return try {
            var updatedUser = user
            
            profileImage?.let {
                val ref = storage.reference.child("users/${user.userId}/profile.jpg")
                ref.putBytes(it).await()
                val url = ref.downloadUrl.await().toString()
                updatedUser = updatedUser.copy(profilePictureUrl = url)
            }
            
            coverImage?.let {
                val ref = storage.reference.child("users/${user.userId}/cover.jpg")
                ref.putBytes(it).await()
                val url = ref.downloadUrl.await().toString()
                updatedUser = updatedUser.copy(coverPhotoUrl = url)
            }
            
            firestore.collection("users").document(user.userId).set(updatedUser).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update profile")
        }
    }

    private suspend fun fetchUsersByIds(userIds: List<String>): Resource<List<User>> {
        return try {
            if (userIds.isEmpty()) {
                return Resource.Success(emptyList())
            }

            val users = userIds.chunked(10).flatMap { chunk ->
                firestore.collection("users")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .toObjects(User::class.java)
            }
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load users")
        }
    }
}
