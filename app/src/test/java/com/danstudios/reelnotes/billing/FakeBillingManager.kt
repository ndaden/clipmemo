package com.danstudios.reelnotes.billing

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBillingManager(
    initialIsPro: Boolean = false,
    initialPrice: String? = "2,99 €"
) : BillingManager {

    val _isPro = MutableStateFlow(initialIsPro)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    val _lifetimePriceString = MutableStateFlow<String?>(initialPrice)
    override val lifetimePriceString: StateFlow<String?> = _lifetimePriceString.asStateFlow()

    val _isBillingLoading = MutableStateFlow(false)
    override val isBillingLoading: StateFlow<Boolean> = _isBillingLoading.asStateFlow()

    var purchaseResult: Result<Boolean> = Result.success(true)
    var restoreResult: Result<Boolean> = Result.success(true)

    override fun initialize(context: Context) {}

    override suspend fun refreshCustomerInfo() {}

    override suspend fun purchasePro(activity: Activity): Result<Boolean> {
        return purchaseResult.also {
            if (it.isSuccess && it.getOrNull() == true) {
                _isPro.value = true
            }
        }
    }

    override suspend fun restorePurchases(): Result<Boolean> {
        return restoreResult.also {
            if (it.isSuccess && it.getOrNull() == true) {
                _isPro.value = true
            }
        }
    }
}
