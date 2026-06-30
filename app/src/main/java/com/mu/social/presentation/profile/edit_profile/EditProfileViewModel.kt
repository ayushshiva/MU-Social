package com.mu.social.presentation.profile.edit_profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.User
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.domain.repository.SocialRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    private val _fullName = mutableStateOf("")
    val fullName: State<String> = _fullName

    private val _bio = mutableStateOf("")
    val bio: State<String> = _bio

    private val _website = mutableStateOf("")
    val website: State<String> = _website

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _profileImageBytes = mutableStateOf<ByteArray?>(null)
    private val _coverImageBytes = mutableStateOf<ByteArray?>(null)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val userId = authRepository.getUserId() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = socialRepository.getUserProfile(userId)) {
                is Resource.Success -> {
                    val userData = result.data
                    _user.value = userData
                    _fullName.value = userData?.fullName ?: ""
                    _bio.value = userData?.bio ?: ""
                    _website.value = userData?.website ?: ""
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun onFullNameChange(name: String) { _fullName.value = name }
    fun onBioChange(bio: String) { _bio.value = bio }
    fun onWebsiteChange(url: String) { _website.value = url }
    fun onProfileImageSelected(bytes: ByteArray) { _profileImageBytes.value = bytes }
    fun onCoverImageSelected(bytes: ByteArray) { _coverImageBytes.value = bytes }

    fun updateProfile() {
        val currentUser = _user.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updatedUser = currentUser.copy(
                fullName = _fullName.value,
                bio = _bio.value,
                website = _website.value
            )
            
            val result = socialRepository.updateProfile(
                updatedUser,
                _profileImageBytes.value,
                _coverImageBytes.value
            )
            
            _isLoading.value = false
            when (result) {
                is Resource.Success -> {
                    _eventFlow.emit(UiEvent.UpdateSuccess)
                }
                is Resource.Error -> {
                    _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to update profile"))
                }
                else -> {}
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object UpdateSuccess : UiEvent()
    }
}
