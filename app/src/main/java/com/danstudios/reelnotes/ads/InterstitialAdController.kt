package com.danstudios.reelnotes.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdController {
    private const val TAG = "InterstitialAdCtrl"

    @Volatile
    private var interstitialAd: InterstitialAd? = null

    @Volatile
    private var isLoading: Boolean = false

    fun isAdReady(): Boolean = interstitialAd != null

    @Synchronized
    fun resetForTesting() {
        interstitialAd = null
        isLoading = false
    }

    @Synchronized
    fun preload(context: Context) {
        if (interstitialAd != null || isLoading) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        val adUnitId = AdConfig.interstitialAdUnitId

        InterstitialAd.load(
            context.applicationContext,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    Log.d(TAG, "Interstitial ad successfully loaded and ready.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    Log.w(TAG, "Interstitial failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showInterstitial(activity: Activity?, onDismissedOrFailed: () -> Unit) {
        val ad = interstitialAd
        if (activity == null || ad == null) {
            onDismissedOrFailed()
            activity?.let { preload(it.applicationContext) }
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                Log.d(TAG, "Interstitial dismissed by user.")
                preload(activity.applicationContext)
                onDismissedOrFailed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                Log.w(TAG, "Interstitial failed to show: ${adError.message}")
                preload(activity.applicationContext)
                onDismissedOrFailed()
            }
        }

        try {
            ad.show(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Exception showing interstitial ad: ${e.message}", e)
            interstitialAd = null
            preload(activity.applicationContext)
            onDismissedOrFailed()
        }
    }
}
