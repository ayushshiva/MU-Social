package com.mu.social.presentation.live

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.live.LiveChatMessage
import com.mu.social.domain.model.live.LiveGift
import com.mu.social.domain.model.live.LiveStream
import com.mu.social.domain.repository.LiveStreamRepository
import com.mu.social.domain.repository.AIRepository
import com.mu.social.domain.repository.WalletRepository
import com.mu.social.BuildConfig
import com.mu.social.live.AgoraManager
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveStreamViewModel @Inject constructor(
    private val repository: LiveStreamRepository,
    private val aiRepository: AIRepository,
    private val walletRepository: WalletRepository,
    val agoraManager: AgoraManager
) : ViewModel() {

    private val _activeStreams = MutableStateFlow<List<LiveStream>>(emptyList())
    val activeStreams: StateFlow<List<LiveStream>> = _activeStreams.asStateFlow()

    private val _currentStream = MutableStateFlow<LiveStream?>(null)
    val currentStream: StateFlow<LiveStream?> = _currentStream.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<LiveChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<LiveChatMessage>> = _chatMessages.asStateFlow()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isMuted = mutableStateOf(false)
    val isMuted: State<Boolean> = _isMuted

    init {
        loadActiveStreams()
    }

    private fun loadActiveStreams() {
        viewModelScope.launch {
            repository.getActiveLiveStreams().collect { streams ->
                _activeStreams.value = streams
            }
        }
    }

    fun startLiveStream(title: String, category: String, isPublic: Boolean, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createLiveStream(title, category, isPublic).onSuccess { streamId ->
                onSuccess(streamId)
            }
            _isLoading.value = false
        }
    }

    fun joinStream(streamId: String) {
        viewModelScope.launch {
            repository.getLiveStream(streamId).collect { stream ->
                _currentStream.value = stream
            }
        }
        viewModelScope.launch {
            repository.getChatMessages(streamId).collect { messages ->
                _chatMessages.value = messages
            }
        }
    }

    fun joinAgoraChannel(context: Context, streamId: String, isBroadcaster: Boolean) {
        viewModelScope.launch {
            val role = if (isBroadcaster) 1 else 2
            repository.getAgoraToken(streamId, role)
                .onSuccess { token ->
                    agoraManager.initEngine(context, BuildConfig.AGORA_APP_ID)
                    agoraManager.joinChannel(token, streamId, 0, isBroadcaster)
                }
        }
    }

    fun sendChatMessage(streamId: String, message: String) {
        viewModelScope.launch {
            // AI Moderation Hook
            val moderationResult = aiRepository.moderateContent(message)
            if (moderationResult is Resource.Success && moderationResult.data == true) {
                repository.sendChatMessage(streamId, message)
            } else {
                // Optionally show a "Message blocked by AI" snackbar
            }
        }
    }

    fun sendLike(streamId: String) {
        viewModelScope.launch {
            repository.sendLike(streamId)
        }
    }

    fun sendGift(streamId: String, gift: LiveGift) {
        viewModelScope.launch {
            val stream = _currentStream.value ?: return@launch
            walletRepository.sendGift(
                senderId = repository.currentUserId,
                receiverId = stream.hostId,
                streamId = streamId,
                gift = gift
            )
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        agoraManager.toggleMicrophone(_isMuted.value)
    }

    fun endStream(streamId: String) {
        viewModelScope.launch {
            repository.endLiveStream(streamId)
            agoraManager.leaveChannel()
        }
    }

    override fun onCleared() {
        super.onCleared()
        agoraManager.leaveChannel()
    }
}
