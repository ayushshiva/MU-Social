package com.mu.social.domain.model.live

import com.google.firebase.Timestamp

data class LiveStream(
    val id: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val hostAvatar: String = "",
    val title: String = "",
    val thumbnail: String = "",
    val category: String = "",
    val isPublic: Boolean = true,
    val viewersCount: Int = 0,
    val likesCount: Int = 0,
    val status: StreamStatus = StreamStatus.LIVE,
    val startedAt: Timestamp = Timestamp.now(),
    val endedAt: Timestamp? = null,
    val agoraToken: String? = null
)

enum class StreamStatus {
    LIVE, ENDED
}

data class LiveChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isModerated: Boolean = false
)

data class LiveGift(
    val id: String = "",
    val name: String = "",
    val iconUrl: String = "",
    val coinValue: Int = 0,
    val animationType: String = "BASIC"
)

data class LiveGiftTransaction(
    val id: String = "",
    val streamId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val giftId: String = "",
    val giftName: String = "",
    val coinValue: Int = 0,
    val timestamp: Timestamp = Timestamp.now()
)
