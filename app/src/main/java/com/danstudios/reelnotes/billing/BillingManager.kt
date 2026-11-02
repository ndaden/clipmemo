package com.danstudios.reelnotes.billing

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface BillingManager {
    val isPro: StateFlow<Boolean>
    val lifetimePriceString: StateFlow<String?>
    val isBillingLoading: StateFlow<Boolean>

    fun initialize(context: Context)
    suspend fun refreshCustomerInfo()
    suspend fun purchasePro(activity: Activity): Result<Boolean>
    suspend fun restorePurchases(): Result<Boolean>
}
