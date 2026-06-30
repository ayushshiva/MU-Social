package com.mu.social.presentation.reels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.Reel
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.domain.repository.ReelRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReelsViewModel @Inject constructor(
    private val reelRepository: ReelRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(ReelsState())
    val state: State<ReelsState> = _state

    init {
        getReels()
    }

    private fun getReels() {
        reelRepository.getReels().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = ReelsState(reels = result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _state.value = ReelsState(error = result.message ?: "An unexpected error occurred")
                }
                is Resource.Loading -> {
                    _state.value = ReelsState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun likeReel(reelId: String) {
        viewModelScope.launch {
            val userId = authRepository.getUserId() ?: return@launch
            reelRepository.likeReel(reelId, userId)
        }
    }
}

data class ReelsState(
    val reels: List<Reel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)
