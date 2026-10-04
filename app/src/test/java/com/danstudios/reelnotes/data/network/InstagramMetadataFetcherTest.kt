package com.danstudios.reelnotes.data.network

import com.danstudios.reelnotes.domain.extractor.InstagramRestrictedException
import com.danstudios.reelnotes.domain.model.NoteCategory
import org.junit.Assert.*
import org.junit.Test

class InstagramMetadataFetcherTest {

    @Test
    fun `parseHtml extracts caption, author and thumbnail from embed HTML`() {
        val sampleHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Recette de saison par @chef_mario</title>
                <meta property="og:image" content="https://instagram.cdn/thumb123.jpg" />
            </head>
            <body>
                <a class="FeedbackAuthor-author">chef_mario</a>
                <div class="Caption">
                    Délicieuse tarte tatin aux pommes caramélisées !<br>
                    Ingrédients : 4 pommes, 100g de sucre, 50g de beurre.
                </div>
            </body>
            </html>
        """.trimIndent()

        val meta = InstagramMetadataFetcher.parseHtml(sampleHtml)
        assertEquals("@chef_mario", meta.author)
        assertEquals("https://instagram.cdn/thumb123.jpg", meta.thumbnailUrl)
        assertTrue(meta.caption.contains("tarte tatin aux pommes"))
        assertTrue(meta.caption.contains("4 pommes"))
        assertFalse(meta.isAgeRestricted)
        assertFalse(meta.isLoginRequired)
    }

    @Test
    fun `parseHtml extracts caption and author from OpenGraph tags with Instagram prefix`() {
        val sampleHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta property="og:url" content="https://www.instagram.com/seizemay/reel/C_JGaVLuiU0/" />
                <meta property="og:description" content="45K likes, 107 comments - seizemay on August 26, 2024: &quot;Un Burger Maison qui n'a rien à envier au Burger de Fastfood !! 🤤🍔 Ingrédients : 2 pains burger, 300g de viande...&quot;." />
                <meta property="og:image" content="https://scontent.cdn/burger.jpg" />
                <meta property="og:title" content="JORDAN M. on Instagram: &quot;Un Burger Maison...&quot;" />
            </head>
            <body></body>
            </html>
        """.trimIndent()

        val meta = InstagramMetadataFetcher.parseHtml(sampleHtml)
        assertEquals("@seizemay", meta.author)
        assertEquals("https://scontent.cdn/burger.jpg", meta.thumbnailUrl)
        assertFalse(meta.caption.contains("45K likes"))
        assertTrue(meta.caption.contains("Un Burger Maison"))
        assertTrue(meta.caption.contains("2 pains burger"))
    }

    @Test
    fun `parseHtml handles inverted attribute order and singular likes comment prefix`() {
        val sampleHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta content="https://www.instagram.com/chef_pierre/reel/C_XYZ12345/" property="og:url" />
                <meta content="1 like, 1 comment - chef_pierre on January 15, 2024: &quot;Recette de crêpes rapides.&quot;" property="og:description" />
                <meta content="https://scontent.cdn/crepes.jpg" property="og:image" />
                <meta content="Pierre on Instagram: &quot;Recette de crêpes&quot;" property="og:title" />
            </head>
            <body></body>
            </html>
        """.trimIndent()

        val meta = InstagramMetadataFetcher.parseHtml(sampleHtml)
        assertEquals("@chef_pierre", meta.author)
        assertEquals("https://scontent.cdn/crepes.jpg", meta.thumbnailUrl)
        assertEquals("Recette de crêpes rapides.", meta.caption)
        assertEquals("Pierre on Instagram: \"Recette de crêpes\"", meta.title)
    }

    @Test
    fun `parseHtml detects age-restricted reel content`() {
        val restrictedHtml = """
            <!DOCTYPE html>
            <html>
            <body>
                <div>Contenu soumis à des restrictions d’âge</div>
                <div>Ce contenu est soumis à des restrictions d’âge en fonction de votre âge ou des paramètres de votre compte. Connectez-vous pour continuer.</div>
            </body>
            </html>
        """.trimIndent()

        val meta = InstagramMetadataFetcher.parseHtml(restrictedHtml)
        assertTrue(meta.isAgeRestricted)
    }

    @Test
    fun `parseHtml detects login required page`() {
        val loginHtml = """
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Instagram</h1>
                <p>Connectez-vous pour continuer</p>
                <form action="/accounts/login/"></form>
            </body>
            </html>
        """.trimIndent()

        val meta = InstagramMetadataFetcher.parseHtml(loginHtml)
        assertTrue(meta.isLoginRequired)
    }

    @Test
    fun `InstagramRestrictedException holds clear descriptive message`() {
        val exception = InstagramRestrictedException("Ce Reel nécessite d'être connecté à Instagram.")
        assertEquals("Ce Reel nécessite d'être connecté à Instagram.", exception.message)
    }
}
