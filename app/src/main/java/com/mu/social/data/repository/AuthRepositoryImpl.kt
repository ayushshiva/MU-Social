package com.mu.social.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.mu.social.domain.model.User
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.utils.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private var cachedUser: User? = null

    override suspend fun googleSignIn(idToken: String): Resource<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Resource.Error("Google Sign-In failed")
            
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            var user = userDoc.toObject(User::class.java)
            
            if (user == null) {
                // New user via Google
                user = User(
                    userId = firebaseUser.uid,
                    username = firebaseUser.email?.substringBefore("@") ?: "user_${firebaseUser.uid.take(5)}",
                    fullName = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    profilePictureUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                firestore.collection("users").document(firebaseUser.uid).set(user).await()
            }
            
            cachedUser = user
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Google Sign-In failed")
        }
    }

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Resource.Error("User ID not found")
            
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
            
            if (user != null) {
                cachedUser = user
                Resource.Success(user)
            } else {
                Resource.Error("User data not found in database")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Login failed")
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        username: String,
        fullName: String
    ): Resource<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Resource.Error("User creation failed")
            
            val newUser = User(
                userId = userId,
                username = username,
                fullName = fullName,
                email = email
            )
            
            result.user?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
            )?.await()
            firestore.collection("users").document(userId).set(newUser).await()
            cachedUser = newUser
            Resource.Success(newUser)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Sign up failed")
        }
    }

    override suspend fun logout() {
        cachedUser = null
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): User? {
        cachedUser?.let { return it }
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(
            userId = firebaseUser.uid,
            username = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@").orEmpty(),
            fullName = firebaseUser.displayName.orEmpty(),
            email = firebaseUser.email.orEmpty(),
            profilePictureUrl = firebaseUser.photoUrl?.toString().orEmpty()
        )
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun updatePushToken(token: String): Resource<Unit> {
        val userId = getUserId() ?: return Resource.Error("User not logged in")
        return try {
            firestore.collection("users").document(userId)
                .update("fcmToken", token).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update token")
        }
    }
}
