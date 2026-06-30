package com.mu.social.security

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntegrityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val integrityManager = IntegrityManagerFactory.create(context)

    suspend fun checkIntegrity(nonce: String): Result<String> {
        return try {
            val integrityTokenResponse: IntegrityTokenResponse = integrityManager.requestIntegrityToken(
                IntegrityTokenRequest.builder()
                    .setNonce(nonce)
                    .build()
            ).await()
            Result.success(integrityTokenResponse.token())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
