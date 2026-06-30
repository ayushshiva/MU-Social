package com.mu.social.domain.repository

import com.mu.social.domain.model.User
import com.mu.social.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun signUp(email: String, password: String, username: String, fullName: String): Resource<User>
    suspend fun googleSignIn(idToken: String): Resource<User>
    suspend fun logout()
    fun getCurrentUser(): User?
    fun isUserLoggedIn(): Boolean
    fun getUserId(): String?
    suspend fun updatePushToken(token: String): Resource<Unit>
}
