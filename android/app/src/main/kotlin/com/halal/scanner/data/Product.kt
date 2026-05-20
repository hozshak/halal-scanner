package com.halal.scanner.data

data class Product(
    val barcode: String,
    val name: String?,
    val brand: String?,
    val imageUrl: String?,
    val ingredientsText: String?,
    val ingredientsLanguage: String?,
    val labels: List<String>,
    val countries: List<String>,
    val novaGroup: Int?,
    val nutriScore: String?,
    // Erweitert:
    val categories: List<String> = emptyList(),
    val manufacturer: String?  = null,
    val nutriments: Nutriments? = null,
    /** Eco-Score-Buchstabe a..e (lowercase). */
    val ecoScoreGrade: String? = null,
    /** Eco-Score-Zahlenwert 0..100 (optional, von OFF). */
    val ecoScoreValue: Int? = null,
    /** Allergene aus OFF, normalisiert (z.B. "milk", "gluten", "soybeans"). */
    val allergens: List<String> = emptyList(),
)

/** Nährwerte pro 100g. Werte = null heißt nicht erfasst. */
data class Nutriments(
    val energyKcal: Double?,
    val sugarsG:   Double?,
    val fatG:      Double?,
    val saturatedFatG: Double?,
    val proteinG:  Double?,
    val saltG:     Double?,
    val fiberG:    Double?,
    val carbohydratesG: Double?,
)
