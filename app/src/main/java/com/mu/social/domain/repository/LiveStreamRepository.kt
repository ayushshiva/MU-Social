package com.mu.social.domain.repository

import com.mu.social.domain.model.live.LiveChatMessage
import com.mu.social.domain.model.live.LiveGift
import com.mu.social.domain.model.live.LiveGiftTransaction
import com.mu.social.domain.model.live.LiveStream
import kotlinx.coroutines.flow.Flow

interface LiveStreamRepository {
    suspend fun createLiveStream(title: String, category: String, isPublic: Boolean): Result<String>
    suspend fun endLiveStream(streamId: String): Result<Unit>
    fun getActiveLiveStreams(): Flow<List<LiveStream>>
    fun getLiveStream(streamId: String): Flow<LiveStream?>
    
    suspend fun sendChatMessage(streamId: String, message: String): Result<Unit>
    fun getChatMessages(streamId: String): Flow<List<LiveChatMessage>>
    
    suspend fun sendLike(streamId: String): Result<Unit>
    suspend fun sendGift(streamId: String, gift: LiveGift): Result<Unit>
    fun getGiftTransactions(streamId: String): Flow<List<LiveGiftTransaction>>
    
    suspend fun getAgoraToken(channelName: String, role: Int): Result<String>

    val currentUserId: String
}
