package com.mu.social.presentation.dashboard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.ContentAnalytics
import com.mu.social.domain.model.UserAnalytics
import com.mu.social.domain.repository.AnalyticsRepository
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CreatorDashboardViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(DashboardState())
    val state: State<DashboardState> = _state

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        val userId = authRepository.getUserId() ?: return
        
        analyticsRepository.getUserAnalytics(userId).onEach { result ->
            if (result is Resource.Success) {
                _state.value = _state.value.copy(userAnalytics = result.data)
            }
        }.launchIn(viewModelScope)

        analyticsRepository.getCreatorDashboardMetrics(userId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        topContent = result.data ?: emptyList(),
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: "Failed to load metrics",
                        isLoading = false
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}

data class DashboardState(
    val userAnalytics: UserAnalytics? = null,
    val topContent: List<ContentAnalytics> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)
