package com.halal.scanner.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Holt Produkt-Infos von den vier Open-Datenbanken der OpenFoodFacts-Familie.
 * Alle vier kostenlos, kein API-Key. Sequenziell durchprobiert bis Treffer.
 */
class OpenFoodFactsClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    sealed class Result {
        data class Found(val product: Product, val source: String) : Result()
        object NotFound : Result()
        data class Error(val message: String) : Result()
    }

    private val sources = listOf(
        "world.openfoodfacts.org" to "Open Food Facts",
        "world.openbeautyfacts.org" to "Open Beauty Facts",
        "world.openproductsfacts.org" to "Open Products Facts",
        "world.openpetfoodfacts.org" to "Open Pet Food Facts",
    )

    suspend fun fetchProduct(barcode: String): Result = withContext(Dispatchers.IO) {
        var lastError: Result.Error? = null
        for ((host, label) in sources) {
            when (val r = fetchFromHost(barcode, host, label)) {
                is Result.Found -> return@withContext r
                is Result.Error -> lastError = r
                Result.NotFound -> { /* try next */ }
            }
        }
        lastError ?: Result.NotFound
    }

    private fun fetchFromHost(barcode: String, host: String, label: String): Result {
        return try {
            val url = "https://$host/api/v2/product/$barcode.json" +
                "?fields=code,product_name,product_name_de,product_name_en,brands," +
                "image_front_url,image_url,ingredients_text,ingredients_text_de," +
                "ingredients_text_en,labels_tags,countries_tags,nova_group,nutriscore_grade"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "HalalScanner-Android/1.0")
                .build()
            client.newCall(req).execute().use { resp ->
                if (resp.code == 404) return Result.NotFound
                if (!resp.isSuccessful) return Result.Error("HTTP ${resp.code} from $host")
                val body = resp.body?.string() ?: return Result.Error("empty body")
                val json = JSONObject(body)
                if (json.optInt("status") != 1) return Result.NotFound

                val p = json.getJSONObject("product")
                val name = p.optString("product_name_de").ifBlank {
                    p.optString("product_name_en").ifBlank {
                        p.optString("product_name").ifBlank { null }
                    }
                }
                val ingredients = p.optString("ingredients_text_de").ifBlank {
                    p.optString("ingredients_text_en").ifBlank {
                        p.optString("ingredients_text").ifBlank { null }
                    }
                }
                val lang = when {
                    p.optString("ingredients_text_de").isNotBlank() -> "de"
                    p.optString("ingredients_text_en").isNotBlank() -> "en"
                    else -> null
                }

                Result.Found(
                    Product(
                        barcode = barcode,
                        name = name,
                        brand = p.optString("brands").ifBlank { null },
                        imageUrl = p.optString("image_front_url").ifBlank {
                            p.optString("image_url").ifBlank { null }
                        },
                        ingredientsText = ingredients,
                        ingredientsLanguage = lang,
                        labels = (p.optJSONArray("labels_tags") ?: JSONArray()).toStringList(),
                        countries = (p.optJSONArray("countries_tags") ?: JSONArray()).toStringList(),
                        novaGroup = p.optInt("nova_group", 0).takeIf { it > 0 },
                        nutriScore = p.optString("nutriscore_grade").ifBlank { null },
                    ),
                    source = label,
                )
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "unknown error")
        }
    }

    /**
     * Volltextsuche in OpenFoodFacts (für Front-Seite-Scanner).
     * Liefert eine Liste von Treffern, sortiert nach Relevanz.
     */
    suspend fun search(query: String, limit: Int = 10): List<Product> = withContext(Dispatchers.IO) {
        try {
            val q = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://world.openfoodfacts.org/cgi/search.pl?" +
                "search_terms=$q&search_simple=1&action=process&json=1&page_size=$limit" +
                "&fields=code,product_name,product_name_de,product_name_en,brands," +
                "image_front_url,image_url,ingredients_text,ingredients_text_de," +
                "ingredients_text_en"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "HalalScanner-Android/1.0")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val body = resp.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val arr = json.optJSONArray("products") ?: return@withContext emptyList()
                val out = mutableListOf<Product>()
                for (i in 0 until arr.length()) {
                    val p = arr.getJSONObject(i)
                    val name = (p.optString("product_name_de").ifBlank {
                        p.optString("product_name_en").ifBlank {
                            p.optString("product_name").ifBlank { null }
                        }
                    }) ?: continue
                    out += Product(
                        barcode = p.optString("code"),
                        name = name,
                        brand = p.optString("brands").ifBlank { null },
                        imageUrl = p.optString("image_front_url").ifBlank {
                            p.optString("image_url").ifBlank { null }
                        },
                        ingredientsText = p.optString("ingredients_text_de").ifBlank {
                            p.optString("ingredients_text_en").ifBlank {
                                p.optString("ingredients_text").ifBlank { null }
                            }
                        },
                        ingredientsLanguage = null,
                        labels = emptyList(),
                        countries = emptyList(),
                        novaGroup = null,
                        nutriScore = null,
                    )
                }
                out
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun JSONArray.toStringList(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until length()) list += optString(i)
        return list.filter { it.isNotBlank() }
    }
}
