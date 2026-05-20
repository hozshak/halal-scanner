package com.halal.scanner.db

import android.content.Context
import com.halal.scanner.data.Product
import com.halal.scanner.halal.HalalStatus
import org.json.JSONArray
import org.json.JSONObject

/**
 * Speichert gebookmarkte Produkte (Watchlist) in SharedPreferences.
 * Wie HistoryStore, aber separat, sodass die Watchlist nicht durch History-Rotation
 * verloren geht.
 */
class BookmarkStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("halal_bookmarks", Context.MODE_PRIVATE)

    data class Entry(
        val barcode: String,
        val name: String?,
        val brand: String?,
        val imageUrl: String?,
        val status: HalalStatus,
        val savedAt: Long,
    )

    fun toggle(product: Product, status: HalalStatus): Boolean {
        val list = list().toMutableList()
        val existing = list.indexOfFirst { it.barcode == product.barcode }
        return if (existing >= 0) {
            list.removeAt(existing); save(list); false
        } else {
            list.add(0, Entry(
                barcode = product.barcode,
                name = product.name,
                brand = product.brand,
                imageUrl = product.imageUrl,
                status = status,
                savedAt = System.currentTimeMillis(),
            ))
            save(list); true
        }
    }

    fun isBookmarked(barcode: String): Boolean = list().any { it.barcode == barcode }

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
                    savedAt = o.getLong("savedAt"),
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun remove(barcode: String) {
        val list = list().toMutableList()
        list.removeAll { it.barcode == barcode }
        save(list)
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
                put("savedAt", e.savedAt)
            })
        }
        prefs.edit().putString("entries", arr.toString()).apply()
    }
}
