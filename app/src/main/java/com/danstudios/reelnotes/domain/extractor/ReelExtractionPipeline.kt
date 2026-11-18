package com.danstudios.reelnotes.domain.extractor

import android.content.Context
import androidx.annotation.StringRes
import com.danstudios.reelnotes.R
import com.danstudios.reelnotes.data.network.GeminiAiOutput
import com.danstudios.reelnotes.data.network.GeminiSummarizer
import com.danstudios.reelnotes.data.network.InstagramMetadataFetcher
import com.danstudios.reelnotes.domain.model.NoteCategory
import com.danstudios.reelnotes.domain.model.ReelNote
import com.danstudios.reelnotes.domain.model.StructuredNoteData
import com.danstudios.reelnotes.domain.util.CaptionSanitizer
import com.danstudios.reelnotes.domain.util.UrlParser

enum class ProcessingStage(@StringRes val labelRes: Int) {
    FETCHING(R.string.status_fetching),
    ANALYZING_MEDIA(R.string.status_analyzing_media),
    ANALYZING_TEXT(R.string.status_analyzing_text),
    GENERATING(R.string.status_generating),
    ANALYZING_OFFLINE(R.string.status_analyzing_offline),
    SAVING(R.string.status_saving)
}

enum class ExtractionErrorType(@StringRes val messageRes: Int) {
    INVALID_URL(R.string.error_reel_url_not_found),
    REEL_UNAVAILABLE(R.string.error_reel_unavailable),
    PROCESSING_FAILED(R.string.default_processing_error)
}

class ReelExtractionException(
    val errorType: ExtractionErrorType,
    message: String
) : Exception(message)

object ReelExtractionPipeline {

    suspend fun processReel(
        sharedInput: String,
        preferredLanguage: String = "fr",
        manualCaption: String? = null,
        context: Context? = null,
        onStageUpdate: ((ProcessingStage) -> Unit)? = null
    ): ReelNote {
        val reelInfo = UrlParser.extractReelInfo(sharedInput)
            ?: throw ReelExtractionException(
                ExtractionErrorType.INVALID_URL,
                "No valid Instagram Reel link found in input."
            )

        val cleanUrl = reelInfo.cleanUrl
        val shortcode = reelInfo.shortcode
        val initialCaption = UrlParser.extractCaptionFromSharedText(sharedInput)

        // 1. Fetch metadata & media stream
        onStageUpdate?.invoke(ProcessingStage.FETCHING)
        val fetchedMeta = InstagramMetadataFetcher.fetch(shortcode, cleanUrl, context)

        // 2. Author resolution (fetched metadata takes precedence, then shared text)
        val author = fetchedMeta.author ?: UrlParser.extractAuthorFromSharedText(sharedInput)

        // 3. Caption sanitization (guarantees no subsequent feed reels or cookie banners leak into prompt)
        val effectiveManualCaption = manualCaption?.trim().orEmpty()
        val sanitizedFetchedCaption = CaptionSanitizer.sanitize(fetchedMeta.caption, author)
        val combinedCaption = when {
            effectiveManualCaption.isNotBlank() -> effectiveManualCaption
            sanitizedFetchedCaption.isNotBlank() -> sanitizedFetchedCaption
            initialCaption.isNotBlank() -> CaptionSanitizer.sanitize(initialCaption, author)
            else -> ""
        }

        if ((fetchedMeta.isAgeRestricted || fetchedMeta.isLoginRequired) &&
            combinedCaption.isBlank() &&
            fetchedMeta.mediaBytes == null
        ) {
            throw InstagramRestrictedException(
                "Reel unavailable: private or age-restricted content."
            )
        }

        if (fetchedMeta.isUnavailable &&
            combinedCaption.isBlank() &&
            fetchedMeta.mediaBytes == null
        ) {
            throw ReelExtractionException(
                ExtractionErrorType.REEL_UNAVAILABLE,
                "This Instagram Reel is unavailable or the link is broken."
            )
        }

        val thumb = fetchedMeta.thumbnailUrl

        var aiResult: GeminiAiOutput? = null

        // 3. Multimodal analysis via backend proxy (watching and listening to the video / audio stream)
        if (fetchedMeta.mediaBytes != null && fetchedMeta.mediaMimeType != null) {
            onStageUpdate?.invoke(ProcessingStage.ANALYZING_MEDIA)
            aiResult = GeminiSummarizer.summarizeMultimodal(
                mediaBytes = fetchedMeta.mediaBytes,
                mimeType = fetchedMeta.mediaMimeType,
                captionContext = combinedCaption,
                preferredLanguage = preferredLanguage
            )
        }

        // 4. Text-only fallback via backend proxy if multimodal wasn't applicable or failed
        if (aiResult == null && combinedCaption.isNotBlank()) {
            onStageUpdate?.invoke(ProcessingStage.ANALYZING_TEXT)
            aiResult = GeminiSummarizer.summarize(combinedCaption, preferredLanguage)
        }

        // 5. Build structured note if AI succeeded (reject boilerplate/unavailable hallucinated notes)
        if (aiResult != null &&
            !CaptionSanitizer.isInstagramBoilerplateOrUnavailable("${aiResult.title} ${aiResult.summary}")
        ) {
            onStageUpdate?.invoke(ProcessingStage.GENERATING)
            val category = NoteCategory.fromString(aiResult.category)
            val structuredData = StructuredNoteData(
                summary = aiResult.summary,
                ingredients = aiResult.ingredients,
                steps = aiResult.steps,
                keyTakeaways = aiResult.keyTakeaways,
                prepTime = aiResult.prepTime,
                cookTime = aiResult.cookTime,
                servings = aiResult.servings,
                tips = aiResult.tips
            )

            val markdown = buildAiMarkdown(
                title = aiResult.title,
                author = author,
                reelUrl = cleanUrl,
                category = category,
                structuredData = structuredData,
                tags = aiResult.tags
            )

            return ReelNote(
                reelUrl = cleanUrl,
                shortcode = shortcode,
                author = author,
                title = aiResult.title.ifBlank { "${category.label} - Reel $shortcode" },
                category = category,
                summary = aiResult.summary.ifBlank { combinedCaption.take(120) },
                rawCaption = combinedCaption,
                markdownContent = markdown,
                structuredData = structuredData,
                thumbnailUrl = thumb,
                tags = aiResult.tags
            )
        }

        // 6. If neither media nor caption could be obtained
        if (combinedCaption.isBlank() && fetchedMeta.mediaBytes == null) {
            throw InstagramRestrictedException(
                "Reel inaccessible: neither media stream nor caption could be retrieved."
            )
        }

        // 7. Offline Heuristic NLP Extractor
        onStageUpdate?.invoke(ProcessingStage.ANALYZING_OFFLINE)
        return OfflineHeuristicExtractor.extract(
            caption = combinedCaption.ifBlank { "Reel Instagram $shortcode" },
            reelUrl = cleanUrl,
            shortcode = shortcode,
            author = author,
            thumbnailUrl = thumb,
            preferredLanguage = preferredLanguage
        )
    }

    private fun buildAiMarkdown(
        title: String,
        author: String?,
        reelUrl: String,
        category: NoteCategory,
        structuredData: StructuredNoteData,
        tags: List<String>
    ): String {
        val sb = StringBuilder()
        sb.append("# $title\n\n")
        if (!author.isNullOrBlank()) {
            sb.append("**Auteur :** $author  \n")
        }
        sb.append("**Catégorie :** ${category.iconEmoji} ${category.label}  \n")
        sb.append("**Lien Reel :** [Voir sur Instagram]($reelUrl)\n\n")

        if (structuredData.summary.isNotBlank()) {
            sb.append("### Résumé\n${structuredData.summary}\n\n")
        }

        val metaTimes = listOfNotNull(
            structuredData.prepTime?.let { "⏱️ Préparation : $it" },
            structuredData.cookTime?.let { "🔥 Cuisson : $it" },
            structuredData.servings?.let { "👥 Portions : $it" }
        )
        if (metaTimes.isNotEmpty()) {
            sb.append(metaTimes.joinToString(" • ")).append("\n\n")
        }

        if (structuredData.ingredients.isNotEmpty()) {
            sb.append("## Ingrédients\n")
            for (ing in structuredData.ingredients) {
                val qty = listOfNotNull(ing.amount, ing.unit).filter { it.isNotBlank() }.joinToString(" ")
                if (qty.isNotBlank()) {
                    sb.append("- [ ] **$qty** ${ing.name}\n")
                } else {
                    sb.append("- [ ] ${ing.name}\n")
                }
            }
            sb.append("\n")
        }

        if (structuredData.steps.isNotEmpty()) {
            sb.append("## Étapes / Préparation\n")
            for (s in structuredData.steps) {
                sb.append("${s.stepNumber}. ${s.instruction}\n")
            }
            sb.append("\n")
        }

        if (structuredData.tips.isNotEmpty()) {
            sb.append("## Astuces du chef\n")
            for (tip in structuredData.tips) {
                sb.append("- 💡 $tip\n")
            }
            sb.append("\n")
        }

        if (structuredData.keyTakeaways.isNotEmpty()) {
            sb.append("## Points clés\n")
            for (pt in structuredData.keyTakeaways) {
                sb.append("- $pt\n")
            }
            sb.append("\n")
        }

        if (tags.isNotEmpty()) {
            sb.append("### Tags\n")
            sb.append(tags.joinToString(" ") { "#$it" }).append("\n")
        }

        return sb.toString().trim()
    }
}
