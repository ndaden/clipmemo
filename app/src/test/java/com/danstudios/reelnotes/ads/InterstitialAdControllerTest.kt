package com.danstudios.reelnotes.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InterstitialAdControllerTest {

    @Before
    fun setUp() {
        InterstitialAdController.resetForTesting()
    }

    @Test
    fun testShowInterstitialExecutesCallbackWhenNoAdLoaded() {
        var callbackExecuted = false
        InterstitialAdController.showInterstitial(activity = null) {
            callbackExecuted = true
        }
        assertTrue("Callback must execute immediately when no interstitial is available", callbackExecuted)
    }

    @Test
    fun testIsAdReadyInitiallyFalse() {
        assertFalse(InterstitialAdController.isAdReady())
    }
}
