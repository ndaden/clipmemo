package com.danstudios.reelnotes.domain.model

import androidx.annotation.StringRes
import com.danstudios.reelnotes.R
import kotlinx.serialization.Serializable

@Serializable
enum class NoteCategory(
    val label: String,
    val iconEmoji: String,
    @StringRes val labelRes: Int
) {
    RECIPE("Recette", "🍳", R.string.category_recipe),
    TUTORIAL("Tutoriel", "🛠️", R.string.category_tutorial),
    WORKOUT("Sport", "💪", R.string.category_workout),
    TIPS_INFO("Astuce", "💡", R.string.category_tips),
    TRAVEL("Voyage", "✈️", R.string.category_travel),
    PRODUCT("Produit", "🛍️", R.string.category_product),
    GENERAL("Autre", "📝", R.string.category_general);

    companion object {
        fun fromString(value: String?): NoteCategory {
            if (value.isNullOrBlank()) return GENERAL
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: GENERAL
        }
    }
}
