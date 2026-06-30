package com.mu.social.domain.model.live

data class Wallet(
    val userId: String = "",
    val balance: Int = 0,
    val totalEarned: Int = 0
)

data class CoinPackage(
    val id: String,
    val amount: Int,
    val price: String,
    val sku: String
)
