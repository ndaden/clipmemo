package com.danstudios.reelnotes.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdConfigTest {

    @Test
    fun testAdMobAppIdFormat() {
        val appId = AdConfig.APP_ID
        assertTrue("App ID should start with ca-app-pub- and contain tilde (~)", appId.startsWith("ca-app-pub-") && appId.contains("~"))
        assertEquals("ca-app-pub-6318635608591557~6617736972", appId)
    }

    @Test
    fun testAdUnitIdFormats() {
        val testBanner = AdConfig.TEST_BANNER_ID
        val prodBanner = AdConfig.PROD_BANNER_ID
        val testInterstitial = AdConfig.TEST_INTERSTITIAL_ID
        val prodInterstitial = AdConfig.PROD_INTERSTITIAL_ID

        assertTrue(testBanner.startsWith("ca-app-pub-3940256099942544/"))
        assertTrue(prodBanner.startsWith("ca-app-pub-6318635608591557/"))
        assertTrue(testInterstitial.startsWith("ca-app-pub-3940256099942544/"))
        assertTrue(prodInterstitial.startsWith("ca-app-pub-6318635608591557/"))
    }

    @Test
    fun testResolveBannerAdUnitId() {
        assertEquals(AdConfig.TEST_BANNER_ID, AdConfig.getBannerId(isDebug = true))
        assertEquals(AdConfig.PROD_BANNER_ID, AdConfig.getBannerId(isDebug = false))
    }

    @Test
    fun testResolveInterstitialAdUnitId() {
        assertEquals(AdConfig.TEST_INTERSTITIAL_ID, AdConfig.getInterstitialId(isDebug = true))
        assertEquals(AdConfig.PROD_INTERSTITIAL_ID, AdConfig.getInterstitialId(isDebug = false))
    }
}
