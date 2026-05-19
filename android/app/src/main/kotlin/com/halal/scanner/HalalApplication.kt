package com.halal.scanner

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.halal.scanner.util.LocalePrefs

class HalalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Persistierte Sprache anwenden bevor Activities starten
        val tag = LocalePrefs(this).languageTag()
        if (!tag.isNullOrBlank()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    }
}
