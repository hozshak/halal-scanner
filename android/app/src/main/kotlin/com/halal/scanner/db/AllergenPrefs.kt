package com.halal.scanner.db

import android.content.Context

/**
 * Speichert die vom Nutzer ausgewählten Allergene zur Markierung im Result-Screen.
 *
 * Die Werte entsprechen den `allergens_tags` von OpenFoodFacts ohne Sprach-Präfix:
 *   "gluten", "milk", "eggs", "soybeans", "nuts", "peanuts", "fish",
 *   "crustaceans", "molluscs", "celery", "mustard", "sesame-seeds",
 *   "sulphur-dioxide-and-sulphites", "lupin"
 */
class AllergenPrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("halal_allergens", Context.MODE_PRIVATE)

    fun selected(): Set<String> = prefs.getStringSet(KEY, emptySet()) ?: emptySet()
    fun setSelected(set: Set<String>) {
        prefs.edit().putStringSet(KEY, set).apply()
    }
    fun toggle(key: String): Boolean {
        val cur = selected().toMutableSet()
        val nowSelected = if (cur.contains(key)) { cur.remove(key); false } else { cur.add(key); true }
        setSelected(cur)
        return nowSelected
    }

    companion object {
        private const val KEY = "selected"
        val ALL = listOf(
            "gluten", "milk", "eggs", "soybeans", "nuts", "peanuts",
            "fish", "crustaceans", "molluscs", "celery", "mustard",
            "sesame-seeds", "sulphur-dioxide-and-sulphites", "lupin",
        )
    }
}
