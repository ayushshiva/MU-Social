package com.mu.social.presentation.chat

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.Message
import com.mu.social.domain.model.MessageType
import com.mu.social.domain.model.User
import com.mu.social.domain.repository.AIRepository
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.domain.repository.ChatRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val aiRepository: AIRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(ChatDetailState())
    val state: State<ChatDetailState> = _state

    private val _messageText = mutableStateOf("")
    val messageText: State<String> = _messageText

    val chatId: String? = savedStateHandle.get<String>("chatId")
    val currentUserId = authRepository.getUserId() ?: ""

    private var typingJob: Job? = null

    init {
        getMessages()
        getChatDetails()
    }

    private fun getMessages() {
        val id = chatId
        if (id.isNullOrBlank()) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Missing or invalid chatId"
            )
            return
        }

        chatRepository.getMessages(id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val messages = result.data ?: emptyList()
                    _state.value = _state.value.copy(
                        messages = messages,
                        isLoading = false
                    )
                    markLastMessageAsSeen(messages)
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

    private fun getChatDetails() {
        chatId?.let { id ->
            chatRepository.getChats(currentUserId).onEach { result ->
                if (result is Resource.Success) {
                    val chat = result.data?.find { it.chatId == id }
                    chat?.let {
                        _state.value = _state.value.copy(chat = it)
                        observePartnerPresence(it.participants)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun observePartnerPresence(participants: List<String>) {
        val partnerId = participants.find { it != currentUserId } ?: return
        chatRepository.getUserPresence(partnerId).onEach { result ->
            if (result is Resource.Success) {
                _state.value = _state.value.copy(partnerUser = result.data)
            }
        }.launchIn(viewModelScope)
    }

    fun onMessageChange(text: String) {
        _messageText.value = text
        setTypingStatus(true)
        
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(2000)
            setTypingStatus(false)
        }
    }

    private fun setTypingStatus(isTyping: Boolean) {
        chatId?.let {
            viewModelScope.launch {
                chatRepository.setTypingStatus(it, currentUserId, isTyping)
            }
        }
    }

    fun sendMessage(mediaUri: Uri? = null, type: MessageType = MessageType.TEXT) {
        val text = _messageText.value.trim()
        if (text.isEmpty() && mediaUri == null || chatId == null) return

        viewModelScope.launch {
            val message = Message(
                chatId = chatId,
                senderId = currentUserId,
                text = text,
                timestamp = System.currentTimeMillis(),
                messageType = type,
                replyToMessageId = _state.value.replyingTo?.messageId,
                replyToText = _state.value.replyingTo?.text
            )
            
            _messageText.value = ""
            _state.value = _state.value.copy(replyingTo = null)
            setTypingStatus(false)
            
            val result = chatRepository.sendMessage(chatId, message, mediaUri)
            if (result is Resource.Error) {
                _state.value = _state.value.copy(error = result.message ?: "Failed to send")
            }
        }
    }

    private fun markLastMessageAsSeen(messages: List<Message>) {
        val id = chatId ?: return
        val lastMessage = messages.lastOrNull()
        if (lastMessage != null && lastMessage.senderId != currentUserId && !lastMessage.seen) {
            viewModelScope.launch {
                chatRepository.markMessageAsSeen(id, lastMessage.messageId)
            }
        }
    }

    fun deleteMessage(messageId: String, forEveryone: Boolean) {
        val id = chatId ?: return
        viewModelScope.launch {
            chatRepository.deleteMessage(id, messageId, forEveryone)
        }
    }

    fun setReplyTo(message: Message?) {
        _state.value = _state.value.copy(replyingTo = message)
    }

    fun moderateAndSendMessage() {
        val text = _messageText.value.trim()
        if (text.isEmpty()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isModerating = true)
            val moderationResult = aiRepository.moderateContent(text)
            _state.value = _state.value.copy(isModerating = false)

            if (moderationResult is Resource.Success && moderationResult.data == true) {
                sendMessage()
            } else {
                _state.value = _state.value.copy(error = "Message contains inappropriate content and cannot be sent.")
            }
        }
    }
}

data class ChatDetailState(
    val messages: List<Message> = emptyList(),
    val chat: com.mu.social.domain.model.Chat? = null,
    val partnerUser: User? = null,
    val replyingTo: Message? = null,
    val isLoading: Boolean = false,
    val isModerating: Boolean = false,
    val error: String = ""
)
