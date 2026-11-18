package com.danstudios.reelnotes.domain.util

object CaptionSanitizer {

    private val BOILERPLATE_PATTERNS = listOf(
        "allow the use of cookies from instagram",
        "use cookies and similar technologies",
        "decline optional cookies",
        "allow all cookies",
        "essential cookies",
        "autoriser l’utilisation des cookies",
        "autoriser l'utilisation des cookies",
        "refuser les cookies optionnels",
        "sorry, this page isn't available",
        "sorry, this page isn’t available",
        "this content isn't available",
        "this content isn’t available",
        "cette page n’est malheureusement pas disponible",
        "cette page n'est malheureusement pas disponible",
        "contenu indisponible",
        "log in to continue",
        "log in to instagram",
        "connectez-vous pour continuer"
    )

    /**
     * Detects Instagram cookie consent dialogs, login walls, and unavailable-page
     * boilerplate so they are never treated as real Reel captions or summarized as notes.
     */
    fun isInstagramBoilerplateOrUnavailable(text: String): Boolean {
        if (text.isBlank()) return false
        val lower = text.lowercase()
        return BOILERPLATE_PATTERNS.any { lower.contains(it) }
    }

    /**
     * Extracts the caption of strictly the FIRST (target) Reel from the raw bodyText of an
     * Instagram Reels feed. It locates the first Follow/Suivre header and cuts at the first
     * "... more", like/comment counters, or next author.
     */
    fun extractFirstReelCaptionFromFeedText(bodyText: String): String {
        if (bodyText.isBlank()) return ""
        val followRegex = Regex("""(?:^|\r?\n)\s*(?:Suivre|Follow|Following|Abonné\(e\)|S’abonner)\s*(?:\r?\n)""", RegexOption.IGNORE_CASE)
        val followMatch = followRegex.find(bodyText) ?: return sanitize(bodyText)
        val afterFollow = bodyText.substring(followMatch.range.last + 1)

        val moreRegex = Regex("""(?:\r?\n|^)\s*(?:…\s*more|…\s*plus|\.\.\.\s*more|\.\.\.\s*plus)\b""", RegexOption.IGNORE_CASE)
        val countsRegex = Regex("""\r?\n\s*[0-9.,]+[KkMmb]?\s*\r?\n\s*[0-9.,]+[KkMmb]?\s*\r?\n""")
        val nextAuthorRegex = Regex("""(?:\r?\n|^)\s*[A-Za-z0-9_.]+\s*(?:\r?\n|\s*•\s*)(?:Suivre|Follow)""", RegexOption.IGNORE_CASE)
        val likeBtnRegex = Regex("""\r?\n\s*(?:J’aime|Like|Comments?|Commentaires?)\b""", RegexOption.IGNORE_CASE)

        val cutIndices = listOfNotNull(
            moreRegex.find(afterFollow)?.range?.first,
            countsRegex.find(afterFollow)?.range?.first,
            nextAuthorRegex.find(afterFollow)?.range?.first,
            likeBtnRegex.find(afterFollow)?.range?.first
        )

        val trimmedCandidate = if (cutIndices.isNotEmpty()) {
            afterFollow.substring(0, cutIndices.minOrNull() ?: afterFollow.length).trim()
        } else {
            afterFollow.take(500).trim()
        }

        return sanitize(trimmedCandidate)
    }

    /**
     * Sanitizes raw text extracted from Instagram Web or feed to ensure that
     * only the caption of the target Reel is kept, and any subsequent feed reels,
     * UI header buttons (Follow/Suivre), or trailing counter lines are stripped.
     */
    fun sanitize(rawCaption: String, author: String? = null): String {
        if (rawCaption.isBlank()) return ""
        if (isInstagramBoilerplateOrUnavailable(rawCaption)) return ""
        var text = rawCaption.trim()

        val cleanAuthor = author?.removePrefix("@")?.trim()

        // 1. Cut off at the start of any subsequent reel in the infinite feed.
        val nextReelRegex = Regex("""(?:\r?\n)+\s*([A-Za-z0-9_.]+)\s*(?:\r?\n)+\s*(?:Follow|Suivre|Following|Abonné\(e\)|S’abonner|Abonnements?)""", RegexOption.IGNORE_CASE)

        val matches = nextReelRegex.findAll(text)
        for (m in matches) {
            val matchedUsername = m.groupValues[1]
            if (cleanAuthor == null || !matchedUsername.equals(cleanAuthor, ignoreCase = true) || m.range.first > 50) {
                text = text.substring(0, m.range.first).trim()
                break
            }
        }

        // 2. Remove trailing counter lines, "... more" / "... plus", and like/comment buttons in a loop until stable
        var prev: String
        do {
            prev = text
            text = text.replace(Regex("""(?:\r?\n)+\s*(?:…\s*more|…\s*plus|\.\.\.\s*more|\.\.\.\s*plus)\s*$""", RegexOption.IGNORE_CASE), "").trim()
            text = text.replace(Regex("""(?:\r?\n+\s*[0-9.,]+[KkMmb]?\s*)+$"""), "").trim()
            text = text.replace(Regex("""(?:\r?\n+\s*(?:J’aime|Like|Comments?|Commentaires?)\s*.*)+$""", RegexOption.IGNORE_CASE), "").trim()
        } while (text != prev)

        // 3. Remove leading header lines (e.g. "For you", "Pour vous", "<author>", "Follow", location)
        val headerKeywords = listOf("For you", "Pour vous", "Reels", "Explore", "Suivre", "Follow", "Following", "Abonné(e)", "S’abonner")
        val lines = text.lines().map { it.trim() }.toMutableList()
        while (lines.isNotEmpty()) {
            val first = lines.first()
            if (headerKeywords.any { first.equals(it, ignoreCase = true) } ||
                (cleanAuthor != null && first.equals(cleanAuthor, ignoreCase = true)) ||
                first.isBlank()
            ) {
                lines.removeAt(0)
            } else if (lines.size > 1 && (lines[1].equals("Follow", ignoreCase = true) || lines[1].equals("Suivre", ignoreCase = true))) {
                lines.removeAt(0)
                lines.removeAt(0)
            } else if (lines.size > 2 && (lines[2].equals("Follow", ignoreCase = true) || lines[2].equals("Suivre", ignoreCase = true))) {
                lines.removeAt(0)
                lines.removeAt(0)
                lines.removeAt(0)
            } else {
                break
            }
        }
        text = lines.joinToString("\n").trim()

        return text
    }
}
