package com.danstudios.reelnotes.billing

import com.danstudios.reelnotes.data.local.PreferencesManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BillingManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun `initial isPro value reflects preferences isPro when true`() {
        var backingValue = true
        val mockPrefs = object : PreferencesManager(null) {
            override var isPro: Boolean
                get() = backingValue
                set(value) { backingValue = value }
        }

        val billingManager = RevenueCatBillingManager(mockPrefs, testScope)
        assertTrue(billingManager.isPro.value)
    }

    @Test
    fun `initial isPro value is false when preferences isPro is false`() {
        var backingValue = false
        val mockPrefs = object : PreferencesManager(null) {
            override var isPro: Boolean
                get() = backingValue
                set(value) { backingValue = value }
        }

        val billingManager = RevenueCatBillingManager(mockPrefs, testScope)
        assertFalse(billingManager.isPro.value)
    }

    @Test
    fun `restorePurchases returns failure when Purchases is not configured`() = runTest(testDispatcher) {
        val mockPrefs = PreferencesManager(null)
        val billingManager = RevenueCatBillingManager(mockPrefs, this)

        val result = billingManager.restorePurchases()
        assertTrue(result.isFailure)
    }
}
