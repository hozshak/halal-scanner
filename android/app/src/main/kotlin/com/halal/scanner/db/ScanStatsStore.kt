package com.halal.scanner.db

import android.content.Context
import com.halal.scanner.halal.HalalStatus

/**
 * Zählt Scans nach Halal-Status für die Stats-Cards in den Einstellungen.
 */
class ScanStatsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("halal_stats", Context.MODE_PRIVATE)

    fun recordScan(status: HalalStatus) {
        val total = prefs.getInt(KEY_TOTAL, 0) + 1
        val halal = prefs.getInt(KEY_HALAL, 0) + if (status == HalalStatus.HALAL || status == HalalStatus.LIKELY_HALAL) 1 else 0
        val haram = prefs.getInt(KEY_HARAM, 0) + if (status == HalalStatus.HARAM) 1 else 0
        prefs.edit()
            .putInt(KEY_TOTAL, total)
            .putInt(KEY_HALAL, halal)
            .putInt(KEY_HARAM, haram)
            .apply()
    }

    fun total(): Int = prefs.getInt(KEY_TOTAL, 0)
    fun halal(): Int = prefs.getInt(KEY_HALAL, 0)
    fun haram(): Int = prefs.getInt(KEY_HARAM, 0)

    companion object {
        private const val KEY_TOTAL = "total"
        private const val KEY_HALAL = "halal"
        private const val KEY_HARAM = "haram"
    }
}
