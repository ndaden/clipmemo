package com.danstudios.reelnotes.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdPlacementPolicyTest {

    @Test
    fun `does not show interstitial during grace period on first 2 notes`() {
        assertFalse(
            AdPlacementPolicy.shouldShowInterstitial(
                totalNotesCreated = 1,
                notesSinceLastAd = 1,
                lastInterstitialShownAtMs = 0L,
                nowMs = 1_000_000L
            )
        )
        assertFalse(
            AdPlacementPolicy.shouldShowInterstitial(
                totalNotesCreated = 2,
                notesSinceLastAd = 2,
                lastInterstitialShownAtMs = 0L,
                nowMs = 1_000_000L
            )
        )
    }

    @Test
    fun `shows first interstitial on 3rd note when no previous ad was shown`() {
        assertTrue(
            AdPlacementPolicy.shouldShowInterstitial(
                totalNotesCreated = 3,
                notesSinceLastAd = 3,
                lastInterstitialShownAtMs = 0L,
                nowMs = 1_000_000L
            )
        )
    }

    @Test
    fun `does not show interstitial when notesSinceLastAd is less than 3`() {
        assertFalse(
            AdPlacementPolicy.shouldShowInterstitial(
                totalNotesCreated = 4,
                notesSinceLastAd = 1,
                lastInterstitialShownAtMs = 1_000_000L,
                nowMs = 2_000_000L
            )
        )
        assertFalse(
            AdPlacementPolicy.shouldShowInterstitial(
                totalNotesCreated = 5,
                notesSinceLastAd = 2,
                lastInterstitialShownAtMs = 1_000_000L,
                nowMs = 2_000_000L
            )
        )
    }

    @Test
    fun `does not show interstitial when 5 minute cooldown has not elapsed`() {
        val lastShown = 1_000_000L
        val fourMinutesLater = lastShown + 4 * 60 * 1000L

        assertFalse(
            AdPlacementPolicy.shouldShowInterstitial(
                totalNotesCreated = 6,
                notesSinceLastAd = 3,
                lastInterstitialShownAtMs = lastShown,
                nowMs = fourMinutesLater
            )
        )
    }

    @Test
    fun `shows interstitial when 3 notes created since last ad and 5 minute cooldown has elapsed`() {
        val lastShown = 1_000_000L
        val fiveMinutesLater = lastShown + 5 * 60 * 1000L

        assertTrue(
            AdPlacementPolicy.shouldShowInterstitial(
                totalNotesCreated = 6,
                notesSinceLastAd = 3,
                lastInterstitialShownAtMs = lastShown,
                nowMs = fiveMinutesLater
            )
        )
    }

    @Test
    fun `shows interstitial on subsequent note after cooldown if 6th note was skipped by cooldown`() {
        val lastShown = 1_000_000L
        val sixMinutesLater = lastShown + 6 * 60 * 1000L

        assertTrue(
            AdPlacementPolicy.shouldShowInterstitial(
                totalNotesCreated = 7,
                notesSinceLastAd = 4,
                lastInterstitialShownAtMs = lastShown,
                nowMs = sixMinutesLater
            )
        )
    }

    @Test
    fun `hides home banner when note library is empty and shows it when not empty`() {
        assertFalse(AdPlacementPolicy.shouldShowHomeBanner(totalNotesCount = 0))
        assertTrue(AdPlacementPolicy.shouldShowHomeBanner(totalNotesCount = 1))
        assertTrue(AdPlacementPolicy.shouldShowHomeBanner(totalNotesCount = 10))
    }
}
