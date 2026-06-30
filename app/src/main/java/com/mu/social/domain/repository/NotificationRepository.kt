package com.mu.social.domain.repository

import com.mu.social.domain.model.Notification
import com.mu.social.utils.Resource
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<Resource<List<Notification>>>
    suspend fun markAsRead(notificationId: String): Resource<Unit>
    suspend fun deleteNotification(notificationId: String): Resource<Unit>
    suspend fun sendNotification(notification: Notification): Resource<Unit>
    fun getUnreadCount(userId: String): Flow<Int>
}
