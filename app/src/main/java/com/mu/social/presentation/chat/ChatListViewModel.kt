package com.mu.social.presentation.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.Chat

import com.mu.social.domain.repository.AuthRepository
import com.mu.social.domain.repository.ChatRepository
import com.mu.social.presentation.chat.ChatListUiState
import com.mu.social.utils.LogTag
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListUiState())
    val state: StateFlow<ChatListUiState> = _state

    private val currentUserId: String? get() = authRepository.getUserId()

    init {
        Log.d(LogTag.CHAT_LIST, "ChatListViewModel init")
        Log.d(LogTag.CHAT_LIST, "ChatListViewModel currentUserId=${currentUserId ?: "null"}")
        updatePresence(true)
        getChats()
    }

    private fun getChats() {
        val userId = authRepository.getUserId()
        if (userId.isNullOrBlank()) {
            _state.update { it.copy(isLoading = false, error = "User not logged in") }
            return
        }

        Log.d(LogTag.CHAT_LIST, "ChatListViewModel getChats start for userId=$userId")
        chatRepository.getChats(userId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val chats = result.data.orEmpty()
                        Log.d(LogTag.CHAT_LIST, "ChatListViewModel getChats success docCount=${chats.size}")
                        _state.update { it.copy(chats = chats, isLoading = false, error = null) }
                    }
                    is Resource.Error -> {
                        Log.d(LogTag.CHAT_LIST, "ChatListViewModel getChats error=${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "An unexpected error occurred"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        Log.d(LogTag.CHAT_LIST, "ChatListViewModel getChats loading")
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                }
            }
            .catch { e ->
                Log.d(LogTag.CHAT_LIST, "ChatListViewModel getChats flow exception=${e.localizedMessage}")
                _state.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Failed to load chats") }
            }
            .launchIn(viewModelScope)
    }

    fun getChatPartner(participants: List<String>) =
        chatRepository.getChatPartner(participants, authRepository.getUserId() ?: "")

    private fun updatePresence(isOnline: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.getUserId() ?: return@launch
            Log.d(LogTag.CHAT_LIST, "updatePresence userId=$userId isOnline=$isOnline")
            chatRepository.updateUserPresence(userId, isOnline)
        }
    }

    override fun onCleared() {
        super.onCleared()
        updatePresence(false)
    }
}

