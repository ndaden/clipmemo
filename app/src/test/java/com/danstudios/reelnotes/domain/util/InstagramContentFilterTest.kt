package com.danstudios.reelnotes.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramContentFilterTest {

    @Test
    fun `detects English and French Instagram cookie consent banners`() {
        val enCookie = """
            Allow the use of cookies from Instagram on this browser?
            We use cookies and similar technologies to help provide and improve content on Meta Products.
            Essential cookies
            Allow all cookies
            Decline optional cookies
        """.trimIndent()

        val frCookie = """
            Autoriser l’utilisation des cookies d’Instagram dans ce navigateur ?
            Nous utilisons des cookies et des technologies similaires.
            Tout autoriser
            Refuser les cookies optionnels
        """.trimIndent()

        assertTrue(CaptionSanitizer.isInstagramBoilerplateOrUnavailable(enCookie))
        assertTrue(CaptionSanitizer.isInstagramBoilerplateOrUnavailable(frCookie))
    }

    @Test
    fun `detects Instagram unavailable and login wall pages`() {
        assertTrue(CaptionSanitizer.isInstagramBoilerplateOrUnavailable("Sorry, this page isn't available. The link you followed may be broken."))
        assertTrue(CaptionSanitizer.isInstagramBoilerplateOrUnavailable("Cette page n’est malheureusement pas disponible."))
        assertTrue(CaptionSanitizer.isInstagramBoilerplateOrUnavailable("This content isn't available right now."))
        assertTrue(CaptionSanitizer.isInstagramBoilerplateOrUnavailable("Log in to Instagram to continue watching this reel."))
        assertTrue(CaptionSanitizer.isInstagramBoilerplateOrUnavailable("Connectez-vous pour continuer sur Instagram."))
    }

    @Test
    fun `does not flag real recipe or tip captions as boilerplate`() {
        val realCaption = """
            Creamy Garlic Parmesan Pasta 🍝
            250g pasta, 200ml heavy cream, 50g parmesan, 3 cloves garlic.
            1. Boil pasta. 2. Simmer cream and garlic. 3. Toss with parmesan.
        """.trimIndent()

        assertFalse(CaptionSanitizer.isInstagramBoilerplateOrUnavailable(realCaption))
    }

    @Test
    fun `extractFirstReelCaptionFromFeedText returns empty string for cookie consent modal body`() {
        val cookieBody = """
            English (US)
            Allow the use of cookies from Instagram on this browser?
            We use cookies and similar technologies to help provide and improve content on Meta Products.
            Allow all cookies
            Decline optional cookies
        """.trimIndent()

        assertEquals("", CaptionSanitizer.extractFirstReelCaptionFromFeedText(cookieBody))
    }
}
