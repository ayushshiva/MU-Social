package com.mu.social.domain.repository

import com.mu.social.domain.model.Report
import com.mu.social.domain.model.ModerationAction
import com.mu.social.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ModerationRepository {
    suspend fun reportContent(report: Report): Resource<Unit>
    fun getModerationQueue(): Flow<Resource<List<Report>>>
    suspend fun updateReportStatus(reportId: String, status: String, notes: String): Resource<Unit>
    suspend fun takeAction(action: ModerationAction): Resource<Unit>
    suspend fun isUserBanned(userId: String): Resource<Boolean>
}
