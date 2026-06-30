package com.mu.social.domain.model

enum class ReportType {
    POST, USER, COMMENT, STORY, REEL
}

enum class ReportReason {
    SPAM, HATE_SPEECH, HARASSMENT, INAPPROPRIATE_CONTENT, VIOLENCE, OTHER
}

enum class ModerationStatus {
    PENDING, REVIEWED, ACTION_TAKEN, DISMISSED
}

data class Report(
    val reportId: String = "",
    val reporterId: String = "",
    val reportedEntityId: String = "", // userId, postId etc
    val type: ReportType = ReportType.POST,
    val reason: ReportReason = ReportReason.OTHER,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: ModerationStatus = ModerationStatus.PENDING,
    val moderatorNotes: String = ""
)

data class ModerationAction(
    val actionId: String = "",
    val targetUserId: String = "",
    val actionType: String = "", // BAN, SUSPEND, WARNING
    val reason: String = "",
    val moderatorId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null
)
