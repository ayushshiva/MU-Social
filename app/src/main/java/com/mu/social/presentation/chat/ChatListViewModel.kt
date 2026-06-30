package com.mu.social.presentation.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.Chat
import com.mu.social.domain.model.User
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.domain.repository.ChatRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(ChatListState())
    val state: State<ChatListState> = _state

    init {
        updatePresence(true)
        getChats()
    }

    private fun getChats() {
        val userId = authRepository.getUserId() ?: return
        chatRepository.getChats(userId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val chats = result.data ?: emptyList()
                    _state.value = _state.value.copy(chats = chats, isLoading = false)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: "An unexpected error occurred",
                        isLoading = false
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getChatPartner(participants: List<String>) = 
        chatRepository.getChatPartner(participants, authRepository.getUserId() ?: "")

    private fun updatePresence(isOnline: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.getUserId() ?: return@launch
            chatRepository.updateUserPresence(userId, isOnline)
        }
    }

    override fun onCleared() {
        super.onCleared()
        updatePresence(false)
    }
}

data class ChatListState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)
