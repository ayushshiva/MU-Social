package com.mu.social.domain.repository

import com.mu.social.domain.model.live.CoinPackage
import com.mu.social.domain.model.live.LiveGift
import com.mu.social.domain.model.live.Wallet
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getWallet(userId: String): Flow<Wallet?>
    suspend fun purchaseCoins(userId: String, coinPackage: CoinPackage): Result<Unit>
    suspend fun sendGift(senderId: String, receiverId: String, streamId: String, gift: LiveGift): Result<Unit>
    suspend fun getCoinPackages(): List<CoinPackage>
}
