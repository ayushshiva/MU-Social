package com.mu.social.presentation.search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.User
import com.mu.social.domain.repository.SocialRepository
import com.mu.social.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _state = mutableStateOf(SearchState())
    val state: State<SearchState> = _state

    private var searchJob: Job? = null

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
            if (query.isNotBlank()) {
                searchUsers(query)
            } else {
                _state.value = SearchState()
            }
        }
    }

    private fun searchUsers(query: String) {
        socialRepository.searchUsers(query).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = SearchState(users = result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _state.value = SearchState(error = result.message ?: "An unexpected error occurred")
                }
                is Resource.Loading -> {
                    _state.value = SearchState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}

data class SearchState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)
