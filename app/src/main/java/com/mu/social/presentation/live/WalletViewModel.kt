package com.mu.social.presentation.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mu.social.domain.model.live.CoinPackage
import com.mu.social.domain.model.live.LiveGift
import com.mu.social.domain.model.live.Wallet
import com.mu.social.domain.repository.AuthRepository
import com.mu.social.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _wallet = MutableStateFlow<Wallet?>(null)
    val wallet: StateFlow<Wallet?> = _wallet.asStateFlow()

    private val _coinPackages = MutableStateFlow<List<CoinPackage>>(emptyList())
    val coinPackages: StateFlow<List<CoinPackage>> = _coinPackages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        loadWallet()
        loadCoinPackages()
    }

    private fun loadWallet() {
        val currentUserId = authRepository.getUserId()
        if (currentUserId == null) {
            viewModelScope.launch { _error.emit("User not logged in") }
            return
        }
        viewModelScope.launch {
            walletRepository.getWallet(currentUserId).collect {
                _wallet.value = it
            }
        }
    }

    private fun loadCoinPackages() {
        viewModelScope.launch {
            _coinPackages.value = walletRepository.getCoinPackages()
        }
    }

    fun purchasePackage(coinPackage: CoinPackage) {
        val currentUserId = authRepository.getUserId()
        if (currentUserId == null) {
            viewModelScope.launch { _error.emit("User not logged in") }
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            walletRepository.purchaseCoins(currentUserId, coinPackage)
                .onFailure { _error.emit(it.message ?: "Purchase failed") }
            _isLoading.value = false
        }
    }

    fun sendGift(receiverId: String, streamId: String, gift: LiveGift) {
        val currentUserId = authRepository.getUserId()
        if (currentUserId == null) {
            viewModelScope.launch { _error.emit("User not logged in") }
            return
        }
        viewModelScope.launch {
            walletRepository.sendGift(currentUserId, receiverId, streamId, gift)
                .onFailure { _error.emit(it.message ?: "Failed to send gift") }
        }
    }
}
