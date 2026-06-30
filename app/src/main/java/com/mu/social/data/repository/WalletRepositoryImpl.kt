package com.mu.social.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.mu.social.domain.model.live.CoinPackage
import com.mu.social.domain.model.live.LiveGift
import com.mu.social.domain.model.live.LiveGiftTransaction
import com.mu.social.domain.model.live.Wallet
import com.mu.social.domain.repository.WalletRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) : WalletRepository {

    override fun getWallet(userId: String): Flow<Wallet?> = callbackFlow {
        val listener = firestore.collection("wallets").document(userId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(Wallet::class.java))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun purchaseCoins(userId: String, coinPackage: CoinPackage): Result<Unit> = try {
        Result.failure(IllegalStateException("Google Play purchase verification is not configured."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun sendGift(
        senderId: String,
        receiverId: String,
        streamId: String,
        gift: LiveGift
    ): Result<Unit> = try {
        functions
            .getHttpsCallable("sendLiveGift")
            .call(
                mapOf(
                    "receiverId" to receiverId,
                    "streamId" to streamId,
                    "giftId" to gift.id,
                    "giftName" to gift.name,
                    "coinValue" to gift.coinValue
                )
            )
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCoinPackages(): List<CoinPackage> {
        return listOf(
            CoinPackage("1", 100, "$0.99", "coins_100"),
            CoinPackage("2", 500, "$4.99", "coins_500"),
            CoinPackage("3", 1000, "$8.99", "coins_1000"),
            CoinPackage("4", 5000, "$39.99", "coins_5000")
        )
    }
}
