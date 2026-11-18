package com.danstudios.reelnotes.billing

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.StateFlow

sealed interface ProOfferState {
    data object Loading : ProOfferState
    data class Available(val priceString: String) : ProOfferState
    data object Unavailable : ProOfferState
}

interface BillingManager {
    val isPro: StateFlow<Boolean>
    val lifetimePriceString: StateFlow<String?>
    val offerState: StateFlow<ProOfferState>
    val isBillingLoading: StateFlow<Boolean>

    fun initialize(context: Context)
    fun refreshOfferings()
    suspend fun refreshCustomerInfo()
    suspend fun purchasePro(activity: Activity): Result<Boolean>
    suspend fun restorePurchases(): Result<Boolean>
}
