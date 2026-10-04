package com.danstudios.reelnotes.data.network

import android.util.Base64
import android.util.Log
import com.danstudios.reelnotes.BuildConfig
import com.danstudios.reelnotes.domain.model.IngredientItem
import com.danstudios.reelnotes.domain.model.NoteCategory
import com.danstudios.reelnotes.domain.model.StepItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@Serializable
data class GeminiAiOutput(
    val category: String = "GENERAL",
    val title: String = "",
    val summary: String = "",
    val prepTime: String? = null,
    val cookTime: String? = null,
    val servings: String? = null,
    val ingredients: List<IngredientItem> = emptyList(),
    val steps: List<StepItem> = emptyList(),
    val keyTakeaways: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val tags: List<String> = emptyList()
) {
    val categoryEnum: NoteCategory get() = NoteCategory.fromString(category)
}

object GeminiSummarizer {
    private const val TAG = "GeminiSummarizer"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parseAiJson(rawJson: String): GeminiAiOutput? {
        var clean = rawJson.trim()
        val codeBlockMatch = Regex("""```(?:json)?\s*([\s\S]*?)\s*```""").find(clean)
        if (codeBlockMatch != null) {
            clean = codeBlockMatch.groupValues[1].trim()
        } else {
            val firstBrace = clean.indexOf('{')
            val lastBrace = clean.lastIndexOf('}')
            if (firstBrace != -1 && lastBrace > firstBrace) {
                clean = clean.substring(firstBrace, lastBrace + 1).trim()
            }
        }
        return try {
            jsonParser.decodeFromString<GeminiAiOutput>(clean)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse AI output JSON: $clean", e)
            null
        }
    }

    suspend fun summarizeMultimodal(
        mediaBytes: ByteArray,
        mimeType: String,
        captionContext: String,
        preferredLanguage: String = "fr"
    ): GeminiAiOutput? = withContext(Dispatchers.IO) {
        if (mediaBytes.isEmpty()) return@withContext null

        val base64Data = Base64.encodeToString(mediaBytes, Base64.NO_WRAP)
        val payload = JSONObject().apply {
            put("mediaBase64", base64Data)
            put("mimeType", mimeType)
            put("captionContext", captionContext)
            put("preferredLanguage", preferredLanguage)
        }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/summarize-multimodal"
        val requestBuilder = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))

        if (BuildConfig.BACKEND_APP_KEY.isNotBlank()) {
            requestBuilder.header("X-App-Key", BuildConfig.BACKEND_APP_KEY)
        }

        try {
            Log.d(TAG, "Calling backend multimodal endpoint at $url (${mediaBytes.size} bytes)...")
            client.newCall(requestBuilder.build()).execute().use { resp ->
                val body = resp.body?.string() ?: return@use null
                if (!resp.isSuccessful) {
                    Log.e(TAG, "Backend multimodal failed with HTTP ${resp.code}: $body")
                    return@use null
                }
                parseAiJson(body)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backend multimodal call exception", e)
            null
        }
    }

    suspend fun summarize(
        caption: String,
        preferredLanguage: String = "fr"
    ): GeminiAiOutput? = withContext(Dispatchers.IO) {
        if (caption.isBlank()) return@withContext null

        val payload = JSONObject().apply {
            put("caption", caption)
            put("preferredLanguage", preferredLanguage)
        }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/summarize"
        val requestBuilder = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))

        if (BuildConfig.BACKEND_APP_KEY.isNotBlank()) {
            requestBuilder.header("X-App-Key", BuildConfig.BACKEND_APP_KEY)
        }

        try {
            Log.d(TAG, "Calling backend summarize endpoint at $url...")
            client.newCall(requestBuilder.build()).execute().use { resp ->
                val body = resp.body?.string() ?: return@use null
                if (!resp.isSuccessful) {
                    Log.e(TAG, "Backend text call failed with HTTP ${resp.code}: $body")
                    return@use null
                }
                parseAiJson(body)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backend text call exception", e)
            null
        }
    }
}
