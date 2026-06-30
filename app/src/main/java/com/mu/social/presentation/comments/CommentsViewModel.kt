package com.mu.social.presentation.comments

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.Comment
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.domain.repository.PostRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(CommentsState())
    val state: State<CommentsState> = _state

    private val _commentText = mutableStateOf("")
    val commentText: State<String> = _commentText

    private val postId: String? = savedStateHandle.get<String>("postId")

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadComments()
    }

    private fun loadComments() {
        val id = postId ?: return
        postRepository.getComments(id).onEach { result ->
            when (result) {
                is Resource.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    comments = result.data ?: emptyList()
                )
                is Resource.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message ?: "Failed to load comments"
                )
                is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
            }
        }.launchIn(viewModelScope)
    }

    fun onCommentChange(text: String) {
        _commentText.value = text
    }

    fun postComment() {
        val text = _commentText.value.trim()
        if (text.isEmpty() || postId == null) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isPosting = true)
            val userId = authRepository.getUserId() ?: return@launch
            val user = authRepository.getCurrentUser()
            val comment = Comment(
                postId = postId,
                userId = userId,
                username = user?.username ?: user?.email ?: "",
                userProfilePicture = user?.profilePictureUrl ?: "",
                text = text
            )

            when (val result = postRepository.addComment(comment)) {
                is Resource.Success -> {
                    _commentText.value = ""
                    _state.value = _state.value.copy(isPosting = false)
                }
                is Resource.Error -> _state.value = _state.value.copy(
                    isPosting = false,
                    error = result.message ?: "Failed to post comment"
                )
                is Resource.Loading -> Unit
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}

data class CommentsState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val error: String = ""
)
