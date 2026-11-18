package com.danstudios.reelnotes.ui.util

/**
 * Formats and localizes structured note metadata strings for UI display.
 */
object NoteTextFormatter {

    private val SERVING_UNIT_REGEX = Regex("""^\s*(\d+(?:[.,-]\d+)?)\s*(personnes?|servings?|pers\.?)\s*$""", RegexOption.IGNORE_CASE)

    fun formatServings(
        rawServings: String,
        isFrench: Boolean,
        formatTemplate: (String) -> String
    ): String {
        val trimmed = rawServings.trim()
        if (trimmed.isEmpty()) return ""

        val unitMatch = SERVING_UNIT_REGEX.find(trimmed)
        if (unitMatch != null) {
            return formatTemplate(unitMatch.groupValues[1])
        }

        return if (trimmed.any { it.isLetter() }) {
            trimmed
        } else {
            formatTemplate(trimmed)
        }
    }
}
