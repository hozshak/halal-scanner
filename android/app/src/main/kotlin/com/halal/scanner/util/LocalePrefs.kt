package com.halal.scanner.util

import android.content.Context

class LocalePrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("locale", Context.MODE_PRIVATE)

    fun languageTag(): String? = prefs.getString("tag", null)

    fun setLanguageTag(tag: String) {
        prefs.edit().putString("tag", tag).apply()
    }
}
