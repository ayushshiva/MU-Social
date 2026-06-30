package com.mu.social.presentation.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.Post
import com.mu.social.domain.model.Report
import com.mu.social.domain.model.ReportReason
import com.mu.social.domain.model.ReportType
import com.mu.social.domain.repository.*
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val moderationRepository: ModerationRepository
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        getPosts()
        getUnreadNotificationCount()
    }

    private fun getUnreadNotificationCount() {
        val userId = authRepository.getUserId() ?: return
        notificationRepository.getUnreadCount(userId).onEach { count ->
            _state.value = _state.value.copy(unreadCount = count)
        }.launchIn(viewModelScope)
    }

    fun getPosts() {
        viewModelScope.launch {
            postRepository.getPosts().onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = HomeState(posts = result.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        _state.value = HomeState(error = result.message ?: "An unexpected error occurred")
                    }
                    is Resource.Loading -> {
                        _state.value = HomeState(isLoading = true)
                    }
                }
            }.launchIn(this)
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            val userId = authRepository.getUserId() ?: return@launch
            postRepository.likePost(postId, userId)
        }
    }

    fun trackPostView(postId: String) {
        viewModelScope.launch {
            analyticsRepository.incrementView(postId, "post")
        }
    }

    fun reportPost(postId: String, reason: ReportReason, description: String) {
        viewModelScope.launch {
            val userId = authRepository.getUserId() ?: return@launch
            val report = Report(
                reporterId = userId,
                reportedEntityId = postId,
                type = ReportType.POST,
                reason = reason,
                description = description
            )
            moderationRepository.reportContent(report)
        }
    }
}

data class HomeState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val unreadCount: Int = 0,
    val error: String = ""
)
