package com.mu.social.domain.repository

import android.net.Uri
import com.mu.social.domain.model.Chat
import com.mu.social.domain.model.Message
import com.mu.social.domain.model.User
import com.mu.social.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(
        chatId: String,
        message: Message,
        mediaUri: Uri? = null
    ): Resource<Unit>
    
    fun getMessages(chatId: String): Flow<Resource<List<Message>>>
    fun getChats(userId: String): Flow<Resource<List<Chat>>>
    suspend fun createChat(participants: List<String>): Resource<String>
    suspend fun markMessageAsSeen(chatId: String, messageId: String): Resource<Unit>
    suspend fun deleteMessage(chatId: String, messageId: String, forEveryone: Boolean): Resource<Unit>
    suspend fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean): Resource<Unit>
    fun getUserPresence(userId: String): Flow<Resource<User>>
    suspend fun updateUserPresence(userId: String, isOnline: Boolean): Resource<Unit>
    fun getChatPartner(participants: List<String>, currentUserId: String): Flow<Resource<User>>
}
