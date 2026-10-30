package com.danstudios.reelnotes.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.danstudios.reelnotes.R
import com.danstudios.reelnotes.data.network.InstagramSessionManager

private const val FALLBACK_MOBILE_LOGIN_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 15; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

internal fun buildInstagramLoginUserAgent(rawDefaultUserAgent: String?): String {
    val raw = rawDefaultUserAgent?.trim().orEmpty()
    if (raw.isEmpty() || !raw.contains("Android", ignoreCase = true)) {
        return FALLBACK_MOBILE_LOGIN_USER_AGENT
    }

    val sanitized = raw
        .replace("; wv)", ")")
        .replace(Regex("""\bVersion/\d+\.\d+\s+"""), "")

    return when {
        sanitized.contains("Mobile Safari/", ignoreCase = true) -> sanitized
        sanitized.contains("Safari/") -> sanitized.replaceFirst("Safari/", "Mobile Safari/")
        else -> "$sanitized Mobile"
    }
}

internal fun shouldBlockExternalAppRedirect(url: String?): Boolean {
    val normalized = url?.trim()?.lowercase() ?: return false
    if (normalized.isEmpty()) return false
    return normalized.startsWith("intent:") ||
        normalized.startsWith("instagram:") ||
        normalized.startsWith("market:") ||
        normalized.startsWith("https://play.google.com/store") ||
        normalized.startsWith("http://play.google.com/store")
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InstagramLoginDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    var isLoading by remember { mutableStateOf(true) }

    // Periodically poll for session cookie so we detect login immediately
    // even if Instagram uses AJAX or redirects without triggering full page reload.
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(800)
            if (InstagramSessionManager.isLoggedIn()) {
                android.util.Log.d("InstaLoginDialog", "Session cookie detected via polling! Login success.")
                CookieManager.getInstance().flush()
                onLoginSuccess()
                break
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                CompositionLocalProvider(
                    LocalContext provides context,
                    LocalConfiguration provides configuration
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.instagram_login_title),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(R.string.instagram_login_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_close))
                            }
                        }

                        if (isLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        layoutParams = android.view.ViewGroup.LayoutParams(
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                        )

                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            databaseEnabled = true
                                            useWideViewPort = true
                                            // Use clean Mobile Android Chrome User-Agent (without "; wv)") so Instagram
                                            // directly renders the mobile username/password form instead of the
                                            // "Get the full experience with the tablet app / Open Instagram" interstitial.
                                            val rawUa = runCatching { WebSettings.getDefaultUserAgent(ctx) }
                                                .getOrNull() ?: userAgentString
                                            userAgentString = buildInstagramLoginUserAgent(rawUa)
                                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        }

                                        val cookieMgr = CookieManager.getInstance()
                                        cookieMgr.setAcceptCookie(true)
                                        cookieMgr.setAcceptThirdPartyCookies(this, true)

                                        webChromeClient = object : android.webkit.WebChromeClient() {
                                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                                android.util.Log.d("InstaLoginDialog", "CONSOLE: [${consoleMessage?.messageLevel()}] ${consoleMessage?.message()}")
                                                return super.onConsoleMessage(consoleMessage)
                                            }
                                        }

                                        // Aucun script n'est injecte dans la page Instagram : l'utilisateur
                                        // interagit lui-meme avec la banniere de cookies et le formulaire.
                                        // Cliquer programmatiquement sur les boutons d'un service tiers
                                        // constitue une interaction automatisee, signalee aussi bien par
                                        // la revue Play que par la detection anti-robot de Meta.
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                super.onPageStarted(view, url, favicon)
                                                android.util.Log.d("InstaLoginDialog", "onPageStarted: $url")
                                                isLoading = true
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                android.util.Log.d("InstaLoginDialog", "onPageFinished: $url")
                                                isLoading = false

                                                if (InstagramSessionManager.isLoggedIn()) {
                                                    android.util.Log.d("InstaLoginDialog", "User logged in! Triggering onLoginSuccess")
                                                    CookieManager.getInstance().flush()
                                                    onLoginSuccess()
                                                }
                                            }

                                            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                                val reqUrl = request?.url?.toString() ?: return false
                                                // Block intent://, instagram://, market://, or Play Store redirects
                                                if (shouldBlockExternalAppRedirect(reqUrl)) {
                                                    android.util.Log.d("InstaLoginDialog", "Blocked external redirect: $reqUrl")
                                                    return true
                                                }
                                                return false
                                            }
                                        }

                                        loadUrl("https://www.instagram.com/accounts/login/")
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
