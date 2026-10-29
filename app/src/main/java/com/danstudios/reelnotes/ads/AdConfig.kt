package com.danstudios.reelnotes.ads

import com.danstudios.reelnotes.BuildConfig

object AdConfig {
    /**
     * Kill-switch global des publicités AdMob.
     * Maintenu à `false` tant que l'application est en étape de validation de tests fermés
     * sur Google Play afin d'éviter tout trafic invalide et blocage du compte AdMob.
     * Passer à `true` uniquement une fois l'application publiée en production ouverte.
     */
    const val ADS_ENABLED = false

    const val APP_ID = "ca-app-pub-6318635608591557~6617736972"

    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val PROD_BANNER_ID = "ca-app-pub-6318635608591557/4419124059"

    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val PROD_INTERSTITIAL_ID = "ca-app-pub-6318635608591557/1703573212"

    fun getBannerId(isDebug: Boolean = BuildConfig.DEBUG): String {
        return if (isDebug) TEST_BANNER_ID else PROD_BANNER_ID
    }

    fun getInterstitialId(isDebug: Boolean = BuildConfig.DEBUG): String {
        return if (isDebug) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID
    }

    val bannerAdUnitId: String
        get() = getBannerId()

    val interstitialAdUnitId: String
        get() = getInterstitialId()
}
