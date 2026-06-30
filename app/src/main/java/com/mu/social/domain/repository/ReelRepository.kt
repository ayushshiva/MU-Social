package com.mu.social.domain.repository

import com.mu.social.domain.model.Reel
import com.mu.social.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ReelRepository {
    suspend fun uploadReel(reel: Reel, videoBytes: ByteArray): Resource<Unit>
    fun getReels(): Flow<Resource<List<Reel>>>
    suspend fun likeReel(reelId: String, userId: String): Resource<Unit>
}
