package com.mu.social.presentation.auth.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _fullName = mutableStateOf("")
    val fullName: State<String> = _fullName

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEmailChange(email: String) { _email.value = email }
    fun onUsernameChange(username: String) { _username.value = username }
    fun onFullNameChange(fullName: String) { _fullName.value = fullName }
    fun onPasswordChange(password: String) { _password.value = password }

    fun signUp() {
        viewModelScope.launch {
            if (_email.value.isBlank() || _username.value.isBlank() || 
                _fullName.value.isBlank() || _password.value.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Please fill in all fields"))
                return@launch
            }
            _isLoading.value = true
            val result = authRepository.signUp(
                _email.value, _password.value, _username.value, _fullName.value
            )
            _isLoading.value = false
            when (result) {
                is Resource.Success -> {
                    _eventFlow.emit(UiEvent.SignUpSuccess)
                }
                is Resource.Error -> {
                    _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "An error occurred"))
                }
                is Resource.Loading -> { _isLoading.value = true }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SignUpSuccess : UiEvent()
    }
}
