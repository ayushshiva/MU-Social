package com.mu.social.presentation.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.ModerationAction
import com.mu.social.domain.model.ModerationStatus
import com.mu.social.domain.model.Report
import com.mu.social.domain.repository.ModerationRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val moderationRepository: ModerationRepository
) : ViewModel() {

    private val _state = mutableStateOf(ModerationState())
    val state: State<ModerationState> = _state

    init {
        getModerationQueue()
    }

    private fun getModerationQueue() {
        moderationRepository.getModerationQueue().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        reports = result.data ?: emptyList(),
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: "Failed to load queue",
                        isLoading = false
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resolveReport(reportId: String, status: ModerationStatus, notes: String) {
        viewModelScope.launch {
            moderationRepository.updateReportStatus(reportId, status.name, notes)
        }
    }

    fun takeAction(targetUserId: String, actionType: String, reason: String, durationDays: Int? = null) {
        viewModelScope.launch {
            val expiresAt = durationDays?.let { System.currentTimeMillis() + (it * 24 * 60 * 60 * 1000L) }
            val action = ModerationAction(
                targetUserId = targetUserId,
                actionType = actionType,
                reason = reason,
                expiresAt = expiresAt
            )
            moderationRepository.takeAction(action)
        }
    }
}

data class ModerationState(
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)
