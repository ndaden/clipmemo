package com.danstudios.reelnotes.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlLauncherTest {

    @Test
    fun `sanitizeUrl returns null for null or blank input`() {
        assertNull(UrlLauncher.sanitizeUrl(null))
        assertNull(UrlLauncher.sanitizeUrl(""))
        assertNull(UrlLauncher.sanitizeUrl("   "))
    }

    @Test
    fun `sanitizeUrl prepends https when scheme is missing`() {
        val input = "www.instagram.com/reel/DDh2O36IEyL/"
        val expected = "https://www.instagram.com/reel/DDh2O36IEyL/"
        assertEquals(expected, UrlLauncher.sanitizeUrl(input))
    }

    @Test
    fun `sanitizeUrl preserves existing http and https schemes and trims whitespace`() {
        val httpInput = "  http://example.com/reel/123  "
        val expectedHttp = "http://example.com/reel/123"
        assertEquals(expectedHttp, UrlLauncher.sanitizeUrl(httpInput))

        val httpsInput = "  https://www.instagram.com/reel/DDh2O36IEyL/  "
        val expectedHttps = "https://www.instagram.com/reel/DDh2O36IEyL/"
        assertEquals(expectedHttps, UrlLauncher.sanitizeUrl(httpsInput))
    }
}
