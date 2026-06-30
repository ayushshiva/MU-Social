package com.mu.social.presentation.post

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.Post
import com.mu.social.domain.repository.AIRepository
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.domain.repository.PostRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val aiRepository: AIRepository,
    private val application: Application
) : ViewModel() {

    private val _caption = mutableStateOf("")
    val caption: State<String> = _caption

    private val _isGenerating = mutableStateOf(false)
    val isGenerating: State<Boolean> = _isGenerating

    private val _selectedMediaUris = mutableStateOf<List<Uri>>(emptyList())
    val selectedMediaUris: State<List<Uri>> = _selectedMediaUris

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onCaptionChange(caption: String) {
        _caption.value = caption
    }

    fun onMediaSelected(uris: List<Uri>) {
        _selectedMediaUris.value = uris
    }

    fun generateAICaption() {
        if (_caption.value.isBlank()) return
        viewModelScope.launch {
            _isGenerating.value = true
            when (val result = aiRepository.generateCaption(_caption.value)) {
                is Resource.Success -> {
                    _caption.value = result.data ?: _caption.value
                }
                is Resource.Error -> {
                    _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "AI Generation failed"))
                }
                else -> {}
            }
            _isGenerating.value = false
        }
    }

    fun generateAIHashtags() {
        if (_caption.value.isBlank()) return
        viewModelScope.launch {
            _isGenerating.value = true
            when (val result = aiRepository.generateHashtags(_caption.value)) {
                is Resource.Success -> {
                    val hashtags = result.data?.joinToString(" ") ?: ""
                    _caption.value = "${_caption.value}\n\n$hashtags"
                }
                is Resource.Error -> {
                    _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "AI Generation failed"))
                }
                else -> {}
            }
            _isGenerating.value = false
        }
    }

    fun createPost() {
        viewModelScope.launch {
            if (_selectedMediaUris.value.isEmpty() && _caption.value.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Post cannot be empty"))
                return@launch
            }

            _isLoading.value = true

            // AI Moderation
            if (_caption.value.isNotBlank()) {
                val moderationResult = aiRepository.moderateContent(_caption.value)
                if (moderationResult is Resource.Success && moderationResult.data == false) {
                    _isLoading.value = false
                    _eventFlow.emit(UiEvent.ShowSnackbar("Caption contains inappropriate content"))
                    return@launch
                }
            }

            val userId = authRepository.getUserId()
            if (userId == null) {
                _isLoading.value = false
                _eventFlow.emit(UiEvent.ShowSnackbar("User not logged in"))
                return@launch
            }
            val user = authRepository.getCurrentUser()
            val post = Post(
                userId = userId,
                username = user?.username ?: user?.email ?: "",
                userProfilePicture = user?.profilePictureUrl ?: "",
                caption = _caption.value,
                timestamp = System.currentTimeMillis()
            )

            val mediaBytes = _selectedMediaUris.value.mapNotNull { uri ->
                application.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }

            val result = postRepository.createPost(post, mediaBytes)
            _isLoading.value = false

            when (result) {
                is Resource.Success -> {
                    _eventFlow.emit(UiEvent.PostCreated)
                }
                is Resource.Error -> {
                    _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to create post"))
                }
                is Resource.Loading -> {}
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object PostCreated : UiEvent()
    }
}
