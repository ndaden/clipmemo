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
