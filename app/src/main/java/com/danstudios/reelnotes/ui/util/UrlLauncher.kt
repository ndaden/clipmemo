package com.danstudios.reelnotes.ui.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.danstudios.reelnotes.R

object UrlLauncher {

    /**
     * Sanitizes a URL string by trimming whitespace and ensuring it has an http:// or https:// scheme.
     * Returns null if the URL is blank or empty.
     */
    fun sanitizeUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return null
        return if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            "https://$trimmed"
        } else {
            trimmed
        }
    }

    /**
     * Unwraps context hierarchy to locate the host Activity if available.
     */
    private fun findActivity(context: Context): Activity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    /**
     * Safely opens an Instagram Reel.
     *
     * 1. Sanitizes the URL (ensures valid scheme).
     * 2. Attempts to open via the native Instagram app (com.instagram.android) if installed.
     * 3. Falls back gracefully to the device web browser if Instagram is unavailable.
     * 4. Displays a friendly Toast if no handler is installed, preventing any unhandled crash.
     */
    fun openInstagramReel(context: Context, rawUrl: String?) {
        val sanitized = sanitizeUrl(rawUrl)
        if (sanitized == null) {
            Toast.makeText(context, context.getString(R.string.error_invalid_url), Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.parse(sanitized)
        val activity = findActivity(context)
        val launchContext = activity ?: context

        // 1. Try launching directly in the Instagram app
        val instagramIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.instagram.android")
            if (activity == null) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        try {
            launchContext.startActivity(instagramIntent)
            return
        } catch (_: Exception) {
            // Instagram app is not installed or could not handle the intent, fall through to browser
        }

        // 2. Fallback to default web browser
        val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            if (activity == null) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        try {
            launchContext.startActivity(browserIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_no_browser), Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, context.getString(R.string.error_cannot_open_link), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Safely opens a generic web URL in the device browser.
     */
    fun openWebUrl(context: Context, rawUrl: String?) {
        val sanitized = sanitizeUrl(rawUrl)
        if (sanitized == null) {
            Toast.makeText(context, context.getString(R.string.error_invalid_url), Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.parse(sanitized)
        val activity = findActivity(context)
        val launchContext = activity ?: context

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            if (activity == null) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        try {
            launchContext.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_no_browser), Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, context.getString(R.string.error_cannot_open_link), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Safely opens the system share sheet.
     */
    fun shareText(context: Context, title: String, text: String, chooserTitle: String = "Partager") {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }

        val activity = findActivity(context)
        val launchContext = activity ?: context

        val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
            if (activity == null) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        try {
            launchContext.startActivity(chooser)
        } catch (_: Exception) {
            Toast.makeText(context, context.getString(R.string.error_cannot_share), Toast.LENGTH_SHORT).show()
        }
    }
}
