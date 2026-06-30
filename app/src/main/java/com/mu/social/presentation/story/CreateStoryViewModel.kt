package com.mu.social.presentation.story

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.Story
import com.mu.social.domain.model.StoryType
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.domain.repository.StoryRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateStoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(CreateStoryState())
    val state: State<CreateStoryState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: CreateStoryEvent) {
        when (event) {
            is CreateStoryEvent.OnMediaSelected -> {
                _state.value = _state.value.copy(
                    selectedMediaUri = event.uri,
                    storyType = if (event.isVideo) StoryType.VIDEO else StoryType.IMAGE
                )
            }
            is CreateStoryEvent.OnTextChanged -> {
                _state.value = _state.value.copy(text = event.text)
            }
            is CreateStoryEvent.OnBackgroundColorChanged -> {
                _state.value = _state.value.copy(backgroundColor = event.color)
            }
            is CreateStoryEvent.OnStoryTypeChanged -> {
                _state.value = _state.value.copy(storyType = event.type)
            }
            is CreateStoryEvent.UploadStory -> {
                uploadStory()
            }
        }
    }

    private fun uploadStory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val userId = authRepository.getUserId() ?: return@launch
            val user = authRepository.getCurrentUser()
            
            val story = Story(
                userId = userId,
                username = user?.username ?: "",
                userProfilePicture = user?.profilePictureUrl ?: "",
                storyType = _state.value.storyType,
                text = _state.value.text,
                // backgroundColor could be stored in a metadata field if needed, 
                // but for now let's assume text story uses the 'text' field.
                // If it's a text story, we might use a specific format for the text field or another field.
            )

            val result = storyRepository.uploadStory(story, _state.value.selectedMediaUri)
            
            when (result) {
                is Resource.Success -> {
                    _eventFlow.emit(UiEvent.StoryUploaded)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to upload story"
                    )
                }
                else -> Unit
            }
        }
    }

    sealed class UiEvent {
        object StoryUploaded : UiEvent()
    }
}

data class CreateStoryState(
    val isLoading: Boolean = false,
    val selectedMediaUri: Uri? = null,
    val text: String = "",
    val backgroundColor: Color = Color.Black,
    val storyType: StoryType = StoryType.IMAGE,
    val error: String = ""
)

sealed class CreateStoryEvent {
    data class OnMediaSelected(val uri: Uri, val isVideo: Boolean) : CreateStoryEvent()
    data class OnTextChanged(val text: String) : CreateStoryEvent()
    data class OnBackgroundColorChanged(val color: Color) : CreateStoryEvent()
    data class OnStoryTypeChanged(val type: StoryType) : CreateStoryEvent()
    object UploadStory : CreateStoryEvent()
}
