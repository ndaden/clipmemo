package com.danstudios.reelnotes

import android.app.Application
import com.danstudios.reelnotes.ads.AdConfig
import com.danstudios.reelnotes.ads.InterstitialAdController
import com.danstudios.reelnotes.billing.BillingManager
import com.danstudios.reelnotes.billing.RevenueCatBillingManager
import com.danstudios.reelnotes.data.local.AppDatabase
import com.danstudios.reelnotes.data.local.PreferencesManager
import com.danstudios.reelnotes.data.repository.ReelNoteRepository
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReelNotesApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: ReelNoteRepository
        private set

    lateinit var preferences: PreferencesManager
        private set

    lateinit var billingManager: BillingManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = ReelNoteRepository(database.reelNoteDao())
        preferences = PreferencesManager(this)
        billingManager = RevenueCatBillingManager(preferences)
        billingManager.initialize(this)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            android.webkit.WebView.setWebContentsDebuggingEnabled(true)
        }

        // Only initialize Google Mobile Ads SDK if ads are enabled and user is not Pro
        if (AdConfig.ADS_ENABLED && !preferences.isPro) {
            CoroutineScope(Dispatchers.IO).launch {
                MobileAds.initialize(this@ReelNotesApp) {
                    InterstitialAdController.preload(this@ReelNotesApp)
                }
            }
        }
    }
}
