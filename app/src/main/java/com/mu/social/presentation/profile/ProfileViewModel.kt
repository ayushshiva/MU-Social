package com.mu.social.presentation.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.Post
import com.mu.social.domain.model.User
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.domain.repository.ChatRepository
import com.mu.social.domain.repository.PostRepository
import com.mu.social.domain.repository.SocialRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    private val userId: String? = savedStateHandle.get<String>("userId")

    init {
        loadProfile()
    }

    fun loadProfile() {
        val targetUserId = if (userId == "current_user" || userId == null) {
            authRepository.getUserId()
        } else {
            userId
        }

        if (targetUserId == null) {
            _state.value = ProfileState(error = "User not found")
            return
        }

        val isCurrentUser = targetUserId == authRepository.getUserId()
        _state.value = _state.value.copy(isCurrentUser = isCurrentUser)

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val userResult = socialRepository.getUserProfile(targetUserId)
            when (userResult) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        user = userResult.data,
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = userResult.message ?: "Failed to load profile",
                        isLoading = false
                    )
                }
                is Resource.Loading -> {}
            }

            // Load user's posts (Filtering from all posts for now, ideally separate query)
            postRepository.getPosts().onEach { result ->
                if (result is Resource.Success) {
                    val userPosts = result.data?.filter { it.userId == targetUserId } ?: emptyList()
                    _state.value = _state.value.copy(posts = userPosts)
                }
            }.launchIn(this)
        }
    }

    fun followUser() {
        val currentUserId = authRepository.getUserId() ?: return
        val targetUserId = _state.value.user?.userId ?: return
        
        viewModelScope.launch {
            val result = socialRepository.followUser(currentUserId, targetUserId)
            if (result is Resource.Success) {
                _state.value = _state.value.copy(isFollowing = true)
                loadProfile() // Refresh to get updated counts
            }
        }
    }

    fun unfollowUser() {
        val currentUserId = authRepository.getUserId() ?: return
        val targetUserId = _state.value.user?.userId ?: return
        
        viewModelScope.launch {
            val result = socialRepository.unfollowUser(currentUserId, targetUserId)
            if (result is Resource.Success) {
                _state.value = _state.value.copy(isFollowing = false)
                loadProfile() // Refresh to get updated counts
            }
        }
    }

    suspend fun startChat(targetUserId: String): Resource<String> {
        val currentUserId = authRepository.getUserId() ?: return Resource.Error("User not logged in")
        return chatRepository.createChat(listOf(currentUserId, targetUserId))
    }
}

data class ProfileState(

    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isFollowing: Boolean = false,
    val isCurrentUser: Boolean = false,
    val error: String = ""
)
