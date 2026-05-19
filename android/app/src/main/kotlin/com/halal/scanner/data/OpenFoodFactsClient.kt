package com.halal.scanner.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Holt Produkt-Infos vom OpenFoodFacts-Public-API.
 *
 * Endpoint: https://world.openfoodfacts.org/api/v2/product/<barcode>.json
 * Kostenlos, kein API-Key erforderlich.
 */
class OpenFoodFactsClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    sealed class Result {
        data class Found(val product: Product) : Result()
        object NotFound : Result()
        data class Error(val message: String) : Result()
    }

    suspend fun fetchProduct(barcode: String): Result = withContext(Dispatchers.IO) {
        try {
            // v2 mit ausgewählten Feldern für schnellere Antwort
            val url = "https://world.openfoodfacts.org/api/v2/product/$barcode.json" +
                "?fields=code,product_name,product_name_de,product_name_en,brands," +
                "image_front_url,image_url,ingredients_text,ingredients_text_de," +
                "ingredients_text_en,ingredients_text_with_allergens,labels_tags," +
                "labels,countries_tags,nova_group,nutriscore_grade"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "HalalScanner-Android/1.0")
                .build()
            client.newCall(req).execute().use { resp ->
                // 404 vom Server bedeutet "Produkt unbekannt" - das ist kein Fehler
                if (resp.code == 404) return@withContext Result.NotFound
                if (!resp.isSuccessful) return@withContext Result.Error("HTTP ${resp.code}")
                val body = resp.body?.string() ?: return@withContext Result.Error("empty body")
                val json = JSONObject(body)
                if (json.optInt("status") != 1) return@withContext Result.NotFound

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
                    )
                )
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "unknown error")
        }
    }

    private fun JSONArray.toStringList(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until length()) list += optString(i)
        return list.filter { it.isNotBlank() }
    }
}
