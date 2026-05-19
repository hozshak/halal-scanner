package com.halal.scanner.halal

/**
 * Datenbank für Halal-Status von Zutaten.
 *
 * Quellenbasis: weit verbreiteter Konsens islamischer Speisegesetze
 *   - Schweinefleisch + Derivate: haram (Quran)
 *   - Alkohol (Khamr): haram (Quran)
 *   - Tier aus nicht-halal Schlachtung: haram
 *   - Insekten-Farbstoffe (Carmin, Schellack): haram nach den meisten Schulen
 *
 * Mushbooh = zweifelhaft - Quelle der Zutat oft nicht eindeutig (z.B. Gelatine
 * kann aus Schwein, Rind oder Fisch sein; Mono-/Diglyceride aus tier. oder pflanz.
 * Quellen). User soll selbst prüfen.
 *
 * Diese Liste ist informativ, keine fatwa. Im Zweifel Hersteller kontaktieren
 * oder zertifiziertes Halal-Produkt kaufen.
 */
object IngredientDatabase {

    /** Definitiv verboten (Haram). */
    val HARAM = listOf(
        // Schwein und Derivate
        IngredientRule(
            listOf("schwein", "schweine", "pork", "porc", "pig",
                   "speck", "bacon", "ham", "schinken",
                   "schmalz", "lard", "saindoux",
                   "schweinefett", "schweinegelatine", "porcine"),
            HalalStatus.HARAM,
            "Schweinefleisch oder daraus gewonnenes Produkt"
        ),
        // Alkohol
        IngredientRule(
            listOf("alkohol", "alcohol", "ethanol", "äthanol",
                   "rum", "wein ", "wine ", "weinessig", "weinbrand",
                   "bier", "beer",
                   "whisky", "vodka", "wodka", "gin",
                   "spirituose", "spirits", "liquor",
                   "weingeist"),
            HalalStatus.HARAM,
            "Alkoholisches Getränk oder Ethanol-Zutat"
        ),
        // Karmin / Cochenille
        IngredientRule(
            listOf("karmin", "carmine", "cochenille", "cochineal",
                   "e120", "e 120", "karminsäure"),
            HalalStatus.HARAM,
            "Karmin (E120) - Farbstoff aus Cochenille-Schildläusen, von den meisten Gelehrten als haram eingestuft"
        ),
        // Schellack
        IngredientRule(
            listOf("schellack", "shellac", "e904", "e 904"),
            HalalStatus.HARAM,
            "Schellack (E904) - Insekten-Sekret"
        ),
        // Tierisches Lab (animal rennet)
        IngredientRule(
            listOf("tierisches lab", "animal rennet", "kälberlab", "calf rennet"),
            HalalStatus.HARAM,
            "Tierisches Lab aus nicht-halal Schlachtung"
        ),
        // Blut
        IngredientRule(
            listOf("blutwurst", "blood sausage", "blutplasma", "blood plasma"),
            HalalStatus.HARAM,
            "Blut-Produkt"
        ),
        // L-Cystein aus Haar/Schweineborsten
        IngredientRule(
            listOf("l-cystein", "l-cysteine", "e920", "e 920"),
            HalalStatus.HARAM,
            "L-Cystein (E920) - oft aus Schweineborsten oder Menschenhaaren"
        ),
        // Pepsin (oft Schwein)
        IngredientRule(
            listOf("pepsin"),
            HalalStatus.HARAM,
            "Pepsin - meist aus Schweinemagen"
        ),
    )

    /** Zweifelhaft (Mushbooh) - Quelle prüfen. */
    val MUSHBOOH = listOf(
        IngredientRule(
            listOf("gelatine", "gelatin", "gélatine"),
            HalalStatus.MUSHBOOH,
            "Gelatine - Quelle (Schwein/Rind/Fisch) muss geprüft werden"
        ),
        IngredientRule(
            listOf("e441", "e 441"),
            HalalStatus.MUSHBOOH,
            "E441 - Gelatine, Quelle muss geprüft werden"
        ),
        IngredientRule(
            listOf("mono- und diglyceride", "mono and diglycerides", "mono-diglyceride",
                   "e471", "e 471", "e472", "e 472", "e473", "e 473"),
            HalalStatus.MUSHBOOH,
            "Mono-/Diglyceride (E471-E473) - können tierischen oder pflanzlichen Ursprungs sein"
        ),
        IngredientRule(
            listOf("e542", "e 542", "knochenphosphat", "bone phosphate"),
            HalalStatus.MUSHBOOH,
            "E542 (Knochenphosphat) - oft aus Schweineknochen"
        ),
        IngredientRule(
            listOf("e631", "e 631", "natriuminosinat", "sodium inosinate"),
            HalalStatus.MUSHBOOH,
            "E631 - kann aus Fleisch oder Fisch gewonnen werden"
        ),
        IngredientRule(
            listOf("e635", "e 635"),
            HalalStatus.MUSHBOOH,
            "E635 - oft tierischen Ursprungs"
        ),
        IngredientRule(
            listOf("e1518", "e 1518", "triacetin"),
            HalalStatus.MUSHBOOH,
            "E1518 (Triacetin) - kann aus tierischem Glycerin sein"
        ),
        IngredientRule(
            listOf("e422", "e 422", "glycerin", "glycerol", "glycérol"),
            HalalStatus.MUSHBOOH,
            "Glycerin (E422) - kann aus tierischem Fett sein"
        ),
        IngredientRule(
            listOf("e470", "e 470", "speisefettsäuren", "fatty acid salts"),
            HalalStatus.MUSHBOOH,
            "E470 (Speisefettsäure-Salze) - Quelle prüfen"
        ),
        IngredientRule(
            listOf("e481", "e 481", "e482", "e 482"),
            HalalStatus.MUSHBOOH,
            "E481/E482 (Stearoyllaktylate) - kann aus tierischen Fettsäuren sein"
        ),
        IngredientRule(
            listOf("e491", "e 491", "e492", "e 492", "e493", "e 493", "e494", "e 494",
                   "e495", "e 495", "sorbitan"),
            HalalStatus.MUSHBOOH,
            "Sorbitan-Ester (E491-E495) - Fettsäure-Quelle prüfen"
        ),
        IngredientRule(
            listOf("natürliche aromen", "natural flavor", "natural flavour",
                   "natürliches aroma", "natural aroma"),
            HalalStatus.MUSHBOOH,
            "Natürliches Aroma - Quelle nicht spezifiziert, kann Alkohol oder tierische Bestandteile enthalten"
        ),
        IngredientRule(
            listOf("lab", "rennet", "labferment"),
            HalalStatus.MUSHBOOH,
            "Lab/Rennet - tierisch oder mikrobiell, Quelle prüfen"
        ),
        IngredientRule(
            listOf("molke", "whey"),
            HalalStatus.MUSHBOOH,
            "Molke - bei Käse-Verarbeitung mit tierischem Lab problematisch"
        ),
        IngredientRule(
            listOf("emulgator", "emulsifier", "émulsifiant"),
            HalalStatus.MUSHBOOH,
            "Emulgator - Quelle muss geprüft werden"
        ),
        IngredientRule(
            listOf("rindergelatine", "beef gelatin", "bovine gelatin"),
            HalalStatus.MUSHBOOH,
            "Rindergelatine - nur halal wenn aus halal-geschlachtetem Rind"
        ),
        IngredientRule(
            listOf("rinderfett", "beef tallow", "tallow", "rindertalg"),
            HalalStatus.MUSHBOOH,
            "Rinderfett - nur halal wenn aus halal-Schlachtung"
        ),
        IngredientRule(
            listOf("hühnerfett", "chicken fat", "geflügelfett"),
            HalalStatus.MUSHBOOH,
            "Geflügelfett - nur halal wenn aus halal-Schlachtung"
        ),
        IngredientRule(
            listOf("aromaextrakt", "flavor extract", "flavour extract"),
            HalalStatus.MUSHBOOH,
            "Aroma-Extrakt - Trägerstoff kann Alkohol sein"
        ),
        IngredientRule(
            listOf("vanilleextrakt", "vanilla extract"),
            HalalStatus.MUSHBOOH,
            "Vanille-Extrakt - meist auf Alkohol-Basis hergestellt"
        ),
    )

    /** Eindeutige Halal-Indikatoren (positiv). */
    val HALAL_INDICATORS = listOf(
        "vegan", "végan", "100% pflanzlich", "rein pflanzlich", "plant-based",
        "halal", "halal certified", "halal-zertifiziert",
    )

    /** Bewertet einen Ingredient-Text und liefert eine Analyse. */
    fun analyze(text: String, labels: List<String> = emptyList()): HalalAnalysis {
        if (text.isBlank() && labels.isEmpty()) {
            return HalalAnalysis(
                status = HalalStatus.UNKNOWN,
                reasons = listOf("Keine Zutatenliste verfügbar"),
                halalIndicators = emptyList()
            )
        }
        val haystack = (text + " " + labels.joinToString(" ")).lowercase()

        val haramHits = HARAM.flatMap { rule ->
            rule.keywords.mapNotNull { kw ->
                if (haystack.contains(kw)) ReasonItem(kw, rule.reason) else null
            }
        }
        val mushboohHits = MUSHBOOH.flatMap { rule ->
            rule.keywords.mapNotNull { kw ->
                if (haystack.contains(kw)) ReasonItem(kw, rule.reason) else null
            }
        }
        val halalHits = HALAL_INDICATORS.filter { haystack.contains(it) }

        val status = when {
            haramHits.isNotEmpty() -> HalalStatus.HARAM
            mushboohHits.isNotEmpty() -> HalalStatus.MUSHBOOH
            halalHits.isNotEmpty() -> HalalStatus.HALAL
            else -> HalalStatus.LIKELY_HALAL
        }

        val reasons = when (status) {
            HalalStatus.HARAM -> haramHits.distinctBy { it.reason }.map { it.reason }
            HalalStatus.MUSHBOOH -> mushboohHits.distinctBy { it.reason }.map { it.reason }
            HalalStatus.HALAL -> halalHits.map { "Halal-Indikator gefunden: \"$it\"" }
            HalalStatus.LIKELY_HALAL -> listOf(
                "Keine haram- oder zweifelhaften Zutaten in der Liste erkannt. " +
                "Dies ist keine Halal-Zertifizierung - bei tierischen Produkten Quelle prüfen."
            )
            HalalStatus.UNKNOWN -> listOf("Zutatenliste fehlt")
        }

        return HalalAnalysis(
            status = status,
            reasons = reasons,
            halalIndicators = halalHits,
            haramTriggers = haramHits.map { it.keyword }.distinct(),
            mushboohTriggers = mushboohHits.map { it.keyword }.distinct(),
        )
    }
}

data class IngredientRule(
    val keywords: List<String>,
    val status: HalalStatus,
    val reason: String,
)

data class ReasonItem(val keyword: String, val reason: String)

enum class HalalStatus {
    HALAL,
    LIKELY_HALAL,
    MUSHBOOH,
    HARAM,
    UNKNOWN,
}

data class HalalAnalysis(
    val status: HalalStatus,
    val reasons: List<String>,
    val halalIndicators: List<String> = emptyList(),
    val haramTriggers: List<String> = emptyList(),
    val mushboohTriggers: List<String> = emptyList(),
)
