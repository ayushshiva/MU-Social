package com.mu.social.presentation.story

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.Story
import com.mu.social.domain.model.StoryType
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.domain.repository.StoryRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository,
    private val socialRepository: com.mu.social.domain.repository.SocialRepository
) : ViewModel() {

    private val _state = mutableStateOf(StoryState(currentUserId = authRepository.getUserId() ?: ""))
    val state: State<StoryState> = _state

    init {
        getStories()
    }

    private fun getStories() {
        storyRepository.getActiveStories().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        stories = result.data ?: emptyList(),
                        isLoading = false
                    )
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

    fun uploadStory(mediaUri: Uri?, storyType: StoryType) {
        viewModelScope.launch {
            val userId = authRepository.getUserId() ?: return@launch
            val user = authRepository.getCurrentUser()
            
            val story = Story(
                userId = userId,
                username = user?.username ?: "",
                userProfilePicture = user?.profilePictureUrl ?: "",
                storyType = storyType
            )
            
            _state.value = _state.value.copy(isUploading = true)
            val result = storyRepository.uploadStory(story, mediaUri)
            _state.value = _state.value.copy(isUploading = false)
            
            if (result is Resource.Error) {
                _state.value = _state.value.copy(error = result.message ?: "Upload failed")
            }
        }
    }

    fun viewStory(storyId: String) {
        viewModelScope.launch {
            val userId = authRepository.getUserId() ?: return@launch
            storyRepository.viewStory(storyId, userId)
        }
    }

    fun likeStory(storyId: String) {
        viewModelScope.launch {
            val userId = authRepository.getUserId() ?: return@launch
            storyRepository.likeStory(storyId, userId)
        }
    }

    fun sendReply(storyId: String, ownerId: String, message: String) {
        viewModelScope.launch {
            val senderId = authRepository.getUserId() ?: return@launch
            _state.value = _state.value.copy(isSendingReply = true)
            storyRepository.sendStoryReply(storyId, ownerId, senderId, message)
            _state.value = _state.value.copy(isSendingReply = false)
        }
    }

    fun fetchViewerDetails(viewerIds: List<String>) {
        viewModelScope.launch {
            val users = viewerIds.mapNotNull { id ->
                val result = socialRepository.getUserProfile(id)
                if (result is Resource.Success) result.data else null
            }
            _state.value = _state.value.copy(viewerDetails = users)
        }
    }
}

data class StoryState(
    val isLoading: Boolean = true,
    val isUploading: Boolean = false,
    val isSendingReply: Boolean = false,
    val stories: List<Story> = emptyList(),
    val viewerDetails: List<com.mu.social.domain.model.User> = emptyList(),
    val error: String = "",
    val currentUserId: String = ""
)
