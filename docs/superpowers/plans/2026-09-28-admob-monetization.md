# Google AdMob Monetization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate Google Mobile Ads (AdMob) into ClipMemo with an anchored adaptive banner at the bottom of screens and a full-screen interstitial video ad triggered after each Instagram Reel processing.

**Architecture:** A dedicated `com.danstudios.reelnotes.ads` package encapsulates `AdConfig` (environment-based unit IDs with Google test IDs in Debug and real IDs in Release), `InterstitialAdController` (background preload and safe callback dispatch), and `AdaptiveBannerAd` (Jetpack Compose adaptive anchored banner). `MainActivity` triggers the interstitial upon `newlyCreatedNoteId` emissions before navigating to the note details.

**Tech Stack:** Kotlin 2.0.21, Android SDK 36 (minSdk 26), Jetpack Compose (Material 3), Google Mobile Ads SDK (`play-services-ads:23.6.0`), Coroutines, JUnit 4.

**Spec:** [docs/superpowers/specs/2026-09-28-admob-monetization-design.md](file:///Users/nabil/dev/reelnotes/docs/superpowers/specs/2026-09-28-admob-monetization-design.md)

## Global Constraints

- AdMob Application ID: `ca-app-pub-6318635608591557~6617736972`
- Production Banner Unit ID: `ca-app-pub-6318635608591557/4419124059`
- Production Interstitial Unit ID: `ca-app-pub-6318635608591557/1703573212`
- Test Banner Unit ID: `ca-app-pub-3940256099942544/6300978111`
- Test Interstitial Unit ID: `ca-app-pub-3940256099942544/1033173712`
- In `DEBUG` builds (`BuildConfig.DEBUG == true`), always use Google official test IDs to prevent account suspension.
- In `RELEASE` builds, use production IDs.
- User navigation must NEVER be blocked if an ad fails to load, fails to display, or the device is offline.
- FloatingBottomBar and note cards must remain fully visible and clickable without obstruction.

---

### Task 1: Google Mobile Ads Dependency, Manifest Declaration & AdConfig

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/danstudios/reelnotes/ads/AdConfig.kt`
- Test: `app/src/test/java/com/danstudios/reelnotes/ads/AdConfigTest.kt`

**Interfaces:**
- Produces: `AdConfig.bannerAdUnitId: String`, `AdConfig.interstitialAdUnitId: String`, `AdConfig.appId: String`

- [ ] **Step 1: Write the failing unit test for AdConfig**

Create `app/src/test/java/com/danstudios/reelnotes/ads/AdConfigTest.kt`:
```kotlin
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.danstudios.reelnotes.ads.AdConfigTest"`
Expected: FAIL with compilation error (Unresolved reference: `AdConfig`).

- [ ] **Step 3: Add SDK dependency in libs.versions.toml and app/build.gradle.kts**

In `gradle/libs.versions.toml`:
```toml
[versions]
...
playServicesAds = "23.6.0"

[libraries]
...
play-services-ads = { group = "com.google.android.gms", name = "play-services-ads", version.ref = "playServicesAds" }
```

In `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(libs.play.services.ads)
    ...
}
```

- [ ] **Step 4: Declare AdMob App ID in AndroidManifest.xml**

In `app/src/main/AndroidManifest.xml` inside `<application>`:
```xml
        <!-- Google AdMob Application ID -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-6318635608591557~6617736972" />
```

- [ ] **Step 5: Implement AdConfig.kt**

Create `app/src/main/java/com/danstudios/reelnotes/ads/AdConfig.kt`:
```kotlin
package com.danstudios.reelnotes.ads

import com.danstudios.reelnotes.BuildConfig

object AdConfig {
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
```

- [ ] **Step 6: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "com.danstudios.reelnotes.ads.AdConfigTest"`
Expected: PASS (4 tests passed).

- [ ] **Step 7: Commit Task 1**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/java/com/danstudios/reelnotes/ads/AdConfig.kt app/src/test/java/com/danstudios/reelnotes/ads/AdConfigTest.kt
git commit -m "feat(ads): add Google Mobile Ads SDK dependency and AdConfig"
```

---

### Task 2: SDK Initialization & InterstitialAdController

**Files:**
- Create: `app/src/main/java/com/danstudios/reelnotes/ads/InterstitialAdController.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ReelNotesApp.kt`
- Test: `app/src/test/java/com/danstudios/reelnotes/ads/InterstitialAdControllerTest.kt`

**Interfaces:**
- Consumes: `AdConfig.interstitialAdUnitId`
- Produces: `InterstitialAdController.preload(context: Context)`, `InterstitialAdController.showInterstitial(activity: Activity, onDismissedOrFailed: () -> Unit)`

- [ ] **Step 1: Write the unit test for InterstitialAdController**

Create `app/src/test/java/com/danstudios/reelnotes/ads/InterstitialAdControllerTest.kt`:
```kotlin
package com.danstudios.reelnotes.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InterstitialAdControllerTest {

    @Before
    fun setUp() {
        InterstitialAdController.resetForTesting()
    }

    @Test
    fun testShowInterstitialExecutesCallbackWhenNoAdLoaded() {
        var callbackExecuted = false
        InterstitialAdController.showInterstitial(activity = null) {
            callbackExecuted = true
        }
        assertTrue("Callback must execute immediately when no interstitial is available", callbackExecuted)
    }

    @Test
    fun testIsAdReadyInitiallyFalse() {
        assertFalse(InterstitialAdController.isAdReady())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.danstudios.reelnotes.ads.InterstitialAdControllerTest"`
Expected: FAIL (Unresolved reference: `InterstitialAdController`).

- [ ] **Step 3: Implement InterstitialAdController.kt**

Create `app/src/main/java/com/danstudios/reelnotes/ads/InterstitialAdController.kt`:
```kotlin
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

    fun resetForTesting() {
        interstitialAd = null
        isLoading = false
    }

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

        ad.show(activity)
    }
}
```

- [ ] **Step 4: Initialize Mobile Ads in ReelNotesApp.kt**

In `app/src/main/java/com/danstudios/reelnotes/ReelNotesApp.kt`:
```kotlin
package com.danstudios.reelnotes

import android.app.Application
import com.danstudios.reelnotes.ads.InterstitialAdController
import com.danstudios.reelnotes.data.local.AppDatabase
import com.danstudios.reelnotes.data.preferences.UserPreferences
import com.danstudios.reelnotes.data.repository.ReelNotesRepositoryImpl
import com.danstudios.reelnotes.domain.repository.ReelNotesRepository
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReelNotesApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferences: UserPreferences
        private set

    lateinit var repository: ReelNotesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        preferences = UserPreferences(this)
        repository = ReelNotesRepositoryImpl(database.reelNoteDao(), preferences)

        // Asynchronously initialize Google Mobile Ads SDK and preload first interstitial
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@ReelNotesApp) {}
            InterstitialAdController.preload(this@ReelNotesApp)
        }
    }
}
```

- [ ] **Step 5: Run tests and verify PASS**

Run: `./gradlew testDebugUnitTest --tests "com.danstudios.reelnotes.ads.InterstitialAdControllerTest"`
Expected: PASS (2 tests passed).

- [ ] **Step 6: Commit Task 2**

```bash
git add app/src/main/java/com/danstudios/reelnotes/ads/InterstitialAdController.kt app/src/main/java/com/danstudios/reelnotes/ReelNotesApp.kt app/src/test/java/com/danstudios/reelnotes/ads/InterstitialAdControllerTest.kt
git commit -m "feat(ads): implement InterstitialAdController and initialize SDK in ReelNotesApp"
```

---

### Task 3: Anchored Adaptive Banner Composable (AdaptiveBannerAd.kt)

**Files:**
- Create: `app/src/main/java/com/danstudios/reelnotes/ads/AdaptiveBannerAd.kt`
- Create: `app/src/test/java/com/danstudios/reelnotes/ads/AdaptiveBannerAdTest.kt`

**Interfaces:**
- Consumes: `AdConfig.bannerAdUnitId`
- Produces: `@Composable fun AdaptiveBannerAd(modifier: Modifier = Modifier)`

- [ ] **Step 1: Write unit test for banner ad size / config helper**

Create `app/src/test/java/com/danstudios/reelnotes/ads/AdaptiveBannerAdTest.kt`:
```kotlin
package com.danstudios.reelnotes.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AdaptiveBannerAdTest {

    @Test
    fun testBannerAdUnitIdIsAvailable() {
        val id = AdConfig.bannerAdUnitId
        assertNotNull(id)
        assertEquals(AdConfig.getBannerId(), id)
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "com.danstudios.reelnotes.ads.AdaptiveBannerAdTest"`
Expected: PASS.

- [ ] **Step 3: Implement AdaptiveBannerAd.kt Composable**

Create `app/src/main/java/com/danstudios/reelnotes/ads/AdaptiveBannerAd.kt`:
```kotlin
package com.danstudios.reelnotes.ads

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.danstudios.reelnotes.ui.theme.DarkBackground
import com.danstudios.reelnotes.ui.theme.DarkBorder
import com.danstudios.reelnotes.ui.theme.TextPlaceholder
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdaptiveBannerAd(
    modifier: Modifier = Modifier
) {
    val isInPreview = LocalInspectionMode.current
    if (isInPreview) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(DarkBackground)
                .border(0.5.dp, DarkBorder),
            contentAlignment = Alignment.Center
        ) {
            Text("AdMob Adaptive Banner", color = TextPlaceholder, fontSize = 12.sp)
        }
        return
    }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val adView = remember(screenWidthDp) {
        AdView(context).apply {
            adUnitId = AdConfig.bannerAdUnitId
            setAdSize(getAdaptiveAdSize(context, screenWidthDp))
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .border(0.5.dp, DarkBorder)
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun getAdaptiveAdSize(context: Context, widthDp: Int): AdSize {
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
}
```

- [ ] **Step 4: Run unit tests and assembleDebug**

Run: `./gradlew testDebugUnitTest --tests "com.danstudios.reelnotes.ads.*" assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit Task 3**

```bash
git add app/src/main/java/com/danstudios/reelnotes/ads/AdaptiveBannerAd.kt app/src/test/java/com/danstudios/reelnotes/ads/AdaptiveBannerAdTest.kt
git commit -m "feat(ui): implement AdaptiveBannerAd composable with adaptive size and lifecycle cleanup"
```

---

### Task 4: Screen Integration (MainActivity, NotesListScreen, NoteDetailScreen)

**Files:**
- Modify: `app/src/main/java/com/danstudios/reelnotes/MainActivity.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/screens/NotesListScreen.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/screens/NoteDetailScreen.kt`

**Interfaces:**
- Consumes: `InterstitialAdController.showInterstitial`, `AdaptiveBannerAd`

- [ ] **Step 1: Wire interstitial trigger in MainActivity.kt**

In `app/src/main/java/com/danstudios/reelnotes/MainActivity.kt`:
Import `com.danstudios.reelnotes.ads.InterstitialAdController`.
Update the newly created note listener:
```kotlin
                        // Automatically show interstitial ad then navigate to note details when a new note is processed
                        LaunchedEffect(Unit) {
                            viewModel.newlyCreatedNoteId.collectLatest { noteId ->
                                InterstitialAdController.showInterstitial(this@MainActivity) {
                                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                                }
                            }
                        }
```

- [ ] **Step 2: Add AdaptiveBannerAd to NotesListScreen.kt**

In `app/src/main/java/com/danstudios/reelnotes/ui/screens/NotesListScreen.kt`:
Import `com.danstudios.reelnotes.ads.AdaptiveBannerAd`.
Position `AdaptiveBannerAd` at the bottom of the root `Box`:
```kotlin
        // Bottom Layout: FloatingBottomBar + Adaptive Banner Ad
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    when (tab) {
                        BottomBarTab.NOTES -> {
                            if (onlyFavorites) viewModel.toggleOnlyFavorites()
                            selectedCategory = null
                        }
                        BottomBarTab.FAVORITES -> {
                            if (!onlyFavorites) viewModel.toggleOnlyFavorites()
                        }
                        BottomBarTab.SETTINGS -> {
                            onSettingsClick()
                        }
                    }
                },
                onAddClick = { showAddDialog = true },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AdaptiveBannerAd()
        }
```
Adjust the `LazyColumn` contentPadding bottom from `110.dp` to `160.dp` to guarantee cards are never occluded:
```kotlin
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 160.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            )
```

- [ ] **Step 3: Add AdaptiveBannerAd to NoteDetailScreen.kt**

In `app/src/main/java/com/danstudios/reelnotes/ui/screens/NoteDetailScreen.kt`:
Import `com.danstudios.reelnotes.ads.AdaptiveBannerAd`.
Anchor `AdaptiveBannerAd` at the bottom of the screen in a `Column` or docked `Box` at `Alignment.BottomCenter`.
Add bottom spacer in the scrollable column (`Spacer(modifier = Modifier.height(70.dp))`) so the last action card is never covered by the banner.

- [ ] **Step 4: Run all unit tests and assembleDebug**

Run: `./gradlew testDebugUnitTest assembleDebug`
Expected: 100% tests pass, compilation successful with 0 errors.

- [ ] **Step 5: Commit Task 4**

```bash
git add app/src/main/java/com/danstudios/reelnotes/MainActivity.kt app/src/main/java/com/danstudios/reelnotes/ui/screens/NotesListScreen.kt app/src/main/java/com/danstudios/reelnotes/ui/screens/NoteDetailScreen.kt
git commit -m "feat(ads): integrate post-processing interstitial in MainActivity and adaptive banner in screens"
```

---

### Task 5: End-to-End Verification & Production Build Validation

**Files:**
- Verify: Full test suite (`testDebugUnitTest`, `testReleaseUnitTest`)
- Verify: Full release bundle creation (`bundleRelease`)

- [ ] **Step 1: Run complete unit test suite**

Run: `./gradlew testDebugUnitTest testReleaseUnitTest`
Expected: ALL TESTS PASS.

- [ ] **Step 2: Build release bundle to verify ProGuard / Release integrity**

Run: `./gradlew bundleRelease`
Expected: BUILD SUCCESSFUL, producing signed `app-release.aab`.

- [ ] **Step 3: Final branch verification and status report**

Run `git status` to verify working tree is clean.
