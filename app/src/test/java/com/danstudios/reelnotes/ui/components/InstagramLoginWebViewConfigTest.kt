package com.danstudios.reelnotes.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramLoginWebViewConfigTest {

    @Test
    fun `buildInstagramLoginUserAgent strips WebView markers from phone default UA`() {
        val rawPhoneWebViewUa =
            "Mozilla/5.0 (Linux; Android 15; SM-S928B Build/AP3A.240905.015.A2; wv) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/131.0.6778.200 Mobile Safari/537.36"

        val actual = buildInstagramLoginUserAgent(rawPhoneWebViewUa)

        assertEquals(
            "Mozilla/5.0 (Linux; Android 15; SM-S928B Build/AP3A.240905.015.A2) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.200 Mobile Safari/537.36",
            actual
        )
    }

    @Test
    fun `buildInstagramLoginUserAgent enforces Mobile token on tablet UA to prevent tablet app interstitial`() {
        val rawTabletWebViewUa =
            "Mozilla/5.0 (Linux; Android 14; Pixel Tablet Build/UQ1A.240205.002; wv) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/131.0.6778.200 Safari/537.36"

        val actual = buildInstagramLoginUserAgent(rawTabletWebViewUa)

        assertEquals(
            "Mozilla/5.0 (Linux; Android 14; Pixel Tablet Build/UQ1A.240205.002) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.200 Mobile Safari/537.36",
            actual
        )
    }

    @Test
    fun `buildInstagramLoginUserAgent falls back to mobile Android Chrome UA when raw UA is blank or desktop`() {
        val fromNull = buildInstagramLoginUserAgent(null)
        val fromBlank = buildInstagramLoginUserAgent("   ")
        val fromDesktop = buildInstagramLoginUserAgent(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        )

        listOf(fromNull, fromBlank, fromDesktop).forEach { ua ->
            assertTrue("Expected Android token in UA: $ua", ua.contains("Android"))
            assertTrue("Expected Mobile Safari token in UA: $ua", ua.contains("Mobile Safari/537.36"))
            assertFalse("Must never contain Windows NT: $ua", ua.contains("Windows NT"))
            assertFalse("Must never contain wv marker: $ua", ua.contains("; wv"))
        }
    }

    @Test
    fun `shouldBlockExternalAppRedirect blocks native app schemes and Play Store links`() {
        assertTrue(shouldBlockExternalAppRedirect("intent://instagram.com/#Intent;package=com.instagram.android;scheme=https;end"))
        assertTrue(shouldBlockExternalAppRedirect("INTENT://instagram.com/accounts/login"))
        assertTrue(shouldBlockExternalAppRedirect("instagram://mainfeed"))
        assertTrue(shouldBlockExternalAppRedirect("market://details?id=com.instagram.android"))
        assertTrue(shouldBlockExternalAppRedirect("https://play.google.com/store/apps/details?id=com.instagram.android"))
    }

    @Test
    fun `shouldBlockExternalAppRedirect allows web login and two-factor authentication URLs`() {
        assertFalse(shouldBlockExternalAppRedirect("https://www.instagram.com/accounts/login/"))
        assertFalse(shouldBlockExternalAppRedirect("https://www.instagram.com/accounts/onetap/?next=%2F"))
        assertFalse(shouldBlockExternalAppRedirect("https://www.instagram.com/accounts/login/two_factor"))
        assertFalse(shouldBlockExternalAppRedirect("https://www.instagram.com/challenge/"))
        assertFalse(shouldBlockExternalAppRedirect(null))
    }
}
