package com.danstudios.reelnotes.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.danstudios.reelnotes.BuildConfig
import com.danstudios.reelnotes.data.local.PreferencesManager
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesAreCompletedBy
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class RevenueCatBillingManager(
    private val preferences: PreferencesManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : BillingManager {

    private val _isPro = MutableStateFlow(preferences.isPro)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _lifetimePriceString = MutableStateFlow<String?>(null)
    override val lifetimePriceString: StateFlow<String?> = _lifetimePriceString.asStateFlow()

    private val _isBillingLoading = MutableStateFlow(false)
    override val isBillingLoading: StateFlow<Boolean> = _isBillingLoading.asStateFlow()

    private var cachedLifetimePackage: Package? = null

    override fun initialize(context: Context) {
        val apiKey = BuildConfig.REVENUECAT_PUBLIC_KEY
        if (apiKey.isBlank()) {
            Log.w(TAG, "RevenueCat API key is empty, skipping Purchases configuration")
            return
        }

        try {
            if (!Purchases.isConfigured) {
                Purchases.configure(
                    PurchasesConfiguration.Builder(context, apiKey)
                        .purchasesAreCompletedBy(PurchasesAreCompletedBy.REVENUECAT)
                        .build()
                )
            }

            Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                updateFromCustomerInfo(customerInfo)
            }

            scope.launch {
                refreshCustomerInfo()
                loadOfferings()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize RevenueCat", e)
        }
    }

    override suspend fun refreshCustomerInfo() {
        if (!Purchases.isConfigured) return
        try {
            val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()
            updateFromCustomerInfo(customerInfo)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch customer info: ${e.message}")
        }
    }

    open suspend fun loadOfferings() {
        if (!Purchases.isConfigured) return
        try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val current = offerings.current
            val lifetimePkg = current?.lifetime ?: current?.availablePackages?.firstOrNull()
            cachedLifetimePackage = lifetimePkg
            val formattedPrice = lifetimePkg?.product?.price?.formatted
            _lifetimePriceString.value = formattedPrice
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch offerings: ${e.message}")
        }
    }

    override suspend fun purchasePro(activity: Activity): Result<Boolean> {
        if (!Purchases.isConfigured) {
            return Result.failure(IllegalStateException("Billing is not configured"))
        }

        val pkgToPurchase = cachedLifetimePackage ?: run {
            loadOfferings()
            cachedLifetimePackage
        } ?: return Result.failure(IllegalStateException("No lifetime package available"))

        _isBillingLoading.value = true
        return try {
            val purchaseResult = Purchases.sharedInstance.awaitPurchase(
                PurchaseParams.Builder(activity, pkgToPurchase).build()
            )
            val customerInfo = purchaseResult.customerInfo
            val active = updateFromCustomerInfo(customerInfo)
            Result.success(active)
        } catch (e: PurchasesTransactionException) {
            if (e.code == PurchasesErrorCode.PurchaseCancelledError) {
                Result.failure(CancellationException("Purchase was cancelled by user"))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isBillingLoading.value = false
        }
    }

    override suspend fun restorePurchases(): Result<Boolean> {
        if (!Purchases.isConfigured) {
            return Result.failure(IllegalStateException("Billing is not configured"))
        }

        _isBillingLoading.value = true
        return try {
            val customerInfo = Purchases.sharedInstance.awaitRestore()
            val active = updateFromCustomerInfo(customerInfo)
            Result.success(active)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isBillingLoading.value = false
        }
    }

    fun updateFromCustomerInfo(customerInfo: CustomerInfo): Boolean {
        val proEntitlementActive = customerInfo.entitlements[ENTITLEMENT_PRO]?.isActive == true
        _isPro.value = proEntitlementActive
        preferences.isPro = proEntitlementActive
        return proEntitlementActive
    }

    companion object {
        private const val TAG = "RevenueCatBilling"
        const val ENTITLEMENT_PRO = "pro"
    }
}
