package com.halal.scanner.db

import android.content.Context
import com.halal.scanner.data.Product
import com.halal.scanner.halal.HalalStatus
import org.json.JSONArray
import org.json.JSONObject

/**
 * Speichert gescannte Produkte in SharedPreferences (einfach, kein Room).
 * Max 50 Einträge - älteste werden überschrieben.
 */
class HistoryStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("halal_history", Context.MODE_PRIVATE)

    data class Entry(
        val barcode: String,
        val name: String?,
        val brand: String?,
        val imageUrl: String?,
        val status: HalalStatus,
        val scannedAt: Long,
    )

    fun add(product: Product, status: HalalStatus) {
        val list = list().toMutableList()
        // Duplikate (gleiche Barcode) raus
        list.removeAll { it.barcode == product.barcode }
        list.add(0, Entry(
            barcode = product.barcode,
            name = product.name,
            brand = product.brand,
            imageUrl = product.imageUrl,
            status = status,
            scannedAt = System.currentTimeMillis(),
        ))
        // Auf 50 Einträge limitieren
        val trimmed = list.take(50)
        save(trimmed)
    }

    fun list(): List<Entry> {
        val raw = prefs.getString("entries", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Entry(
                    barcode = o.getString("barcode"),
                    name = o.optString("name").ifBlank { null },
                    brand = o.optString("brand").ifBlank { null },
                    imageUrl = o.optString("imageUrl").ifBlank { null },
                    status = HalalStatus.valueOf(o.optString("status", "UNKNOWN")),
                    scannedAt = o.getLong("scannedAt"),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clear() {
        prefs.edit().remove("entries").apply()
    }

    private fun save(entries: List<Entry>) {
        val arr = JSONArray()
        for (e in entries) {
            arr.put(JSONObject().apply {
                put("barcode", e.barcode)
                put("name", e.name ?: "")
                put("brand", e.brand ?: "")
                put("imageUrl", e.imageUrl ?: "")
                put("status", e.status.name)
                put("scannedAt", e.scannedAt)
            })
        }
        prefs.edit().putString("entries", arr.toString()).apply()
    }
}
