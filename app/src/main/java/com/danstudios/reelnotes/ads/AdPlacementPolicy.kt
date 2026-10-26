package com.danstudios.reelnotes.ads

/**
 * Politique d'affichage des publicités (Approche 1 + 2) :
 * - Période de grâce d'onboarding : aucun interstitiel sur les [GRACE_NOTES_COUNT] premières notes créées.
 * - Fréquence : au plus 1 interstitiel toutes les [NOTES_FREQUENCY_INTERVAL] notes créées.
 * - Cooldown : au moins [MIN_COOLDOWN_MS] (5 minutes) entre deux interstitiels.
 * - Bannière d'accueil : masquée tant que la bibliothèque de notes est vide.
 */
object AdPlacementPolicy {
    const val GRACE_NOTES_COUNT = 2
    const val NOTES_FREQUENCY_INTERVAL = 3
    const val MIN_COOLDOWN_MS = 5 * 60 * 1000L

    fun shouldShowInterstitial(
        totalNotesCreated: Int,
        notesSinceLastAd: Int,
        lastInterstitialShownAtMs: Long,
        nowMs: Long
    ): Boolean {
        if (totalNotesCreated <= GRACE_NOTES_COUNT) return false
        if (notesSinceLastAd < NOTES_FREQUENCY_INTERVAL) return false
        if (lastInterstitialShownAtMs > 0L && (nowMs - lastInterstitialShownAtMs) < MIN_COOLDOWN_MS) {
            return false
        }
        return true
    }

    fun shouldShowHomeBanner(totalNotesCount: Int): Boolean {
        return totalNotesCount > 0
    }
}
