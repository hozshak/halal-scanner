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
