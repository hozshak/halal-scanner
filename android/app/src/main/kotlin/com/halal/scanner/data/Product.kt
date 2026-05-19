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
)
