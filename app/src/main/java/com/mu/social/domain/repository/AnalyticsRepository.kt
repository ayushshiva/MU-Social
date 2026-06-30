package com.mu.social.domain.repository

import com.mu.social.domain.model.ContentAnalytics
import com.mu.social.domain.model.UserAnalytics
import com.mu.social.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    suspend fun trackEvent(userId: String, eventType: String, metadata: Map<String, Any> = emptyMap()): Resource<Unit>
    fun getUserAnalytics(userId: String): Flow<Resource<UserAnalytics>>
    fun getContentAnalytics(contentId: String): Flow<Resource<ContentAnalytics>>
    suspend fun incrementView(contentId: String, contentType: String): Resource<Unit>
    fun getCreatorDashboardMetrics(userId: String): Flow<Resource<List<ContentAnalytics>>>
}
