package com.halal.scanner.halal

/**
 * Halal-Zutaten-Datenbank.
 *
 * Erkennungs-Stichwörter in: Deutsch, Englisch, Französisch und teilweise Arabisch
 * (lateinisch transliteriert). Plus E-Nummern.
 *
 * QUELLEN:
 *   - Allgemeiner islamischer Konsens (Quran 2:173, 5:3, 16:115 zu verbotenem Fleisch + Alkohol)
 *   - Listen u.a. von HMC (Halal Monitoring Committee), JAKIM, MUI, Halal Cert Germany
 *   - E-Nummer-Klassifikation orientiert sich an mehreren etablierten Halal-Authoritäten
 *
 * WICHTIG: Diese App liefert eine **informative** Bewertung, keine fatwa.
 * Im Zweifel Hersteller kontaktieren oder zertifiziertes Produkt wählen.
 */
object IngredientDatabase {

    val HARAM = listOf(
        // ============ Schwein und Derivate ============
        IngredientRule(
            listOf(
                "schwein", "schweine", "pork", "porc", "pig", "porcine",
                "speck", "bacon", "ham ", " ham", "schinken", "jambon",
                "schmalz", "lard", "saindoux",
                "schweinefett", "schweinegelatine", "pork fat", "pork gelatin", "pork gelatine",
                "schweineleberwurst", "leberwurst", "blutwurst", "salami schwein",
                "خنزير", "لحم خنزير",
            ),
            HalalStatus.HARAM,
            "Schweinefleisch / Pork - haram nach Quran 2:173"
        ),
        // ============ Alkohol ============
        IngredientRule(
            listOf(
                "alkohol", "alcohol", "alcoólico", "alcool",
                "ethanol", "äthanol", "éthanol",
                "rum ", " rum",
                "wein ", " wein", "weinessig", "weinbrand", "wine ", " wine",
                "bier", "beer", "bière",
                "whisky", "whiskey", "vodka", "wodka", "gin ", " gin",
                "spirituose", "spirits", "liquor",
                "weingeist", "branntwein", "kirschwasser",
                "sake ", " sake",
                "كحول", "خمر",
            ),
            HalalStatus.HARAM,
            "Alkohol/Ethanol - haram (Khamr) nach Quran 5:90"
        ),
        // ============ Insekten-Farbstoffe ============
        IngredientRule(
            listOf("karmin", "carmine", "cochenille", "cochineal", "e120", "e 120", "karminsäure"),
            HalalStatus.HARAM,
            "Karmin (E120) - Farbstoff aus Cochenille-Schildläusen"
        ),
        IngredientRule(
            listOf("schellack", "shellac", "gomme-laque", "e904", "e 904"),
            HalalStatus.HARAM,
            "Schellack (E904) - Insekten-Sekret"
        ),
        // ============ Animal-derived ohne Halal-Schlachtung ============
        IngredientRule(
            listOf("tierisches lab", "animal rennet", "kälberlab", "calf rennet", "präsentlab"),
            HalalStatus.HARAM,
            "Tierisches Lab aus nicht-halal Schlachtung"
        ),
        IngredientRule(
            listOf("blutwurst", "blood sausage", "blutplasma", "blood plasma", "blut ", " blut"),
            HalalStatus.HARAM,
            "Blut-Produkt - haram nach Quran 2:173"
        ),
        IngredientRule(
            listOf("l-cystein", "l-cysteine", "e920", "e 920", "cysteinhydrochlorid"),
            HalalStatus.HARAM,
            "L-Cystein (E920) - oft aus Schweineborsten oder menschlichem Haar"
        ),
        IngredientRule(
            listOf("pepsin"),
            HalalStatus.HARAM,
            "Pepsin - überwiegend aus Schweinemägen gewonnen"
        ),
        IngredientRule(
            listOf("aspik", "aspic"),
            HalalStatus.HARAM,
            "Aspik - oft auf Schweinefleisch-Basis"
        ),
        // Karneval-Produkt-Klassiker
        IngredientRule(
            listOf("gummibärchen schwein", "weingummi"),
            HalalStatus.HARAM,
            "Enthält Schweinegelatine oder Alkohol"
        ),
        // ============ Tot/Aas ============
        IngredientRule(
            listOf("verendetes tier", "kadaver", "carrion"),
            HalalStatus.HARAM,
            "Aas/Kadaver - haram"
        ),
        // ============ Eindeutige tierische E-Numbers ============
        IngredientRule(
            listOf("e542", "e 542", "knochenphosphat", "bone phosphate"),
            HalalStatus.HARAM,
            "E542 (Knochenphosphat) - aus tierischen Knochen, meist Schwein"
        ),
    )

    val MUSHBOOH = listOf(
        // ============ Gelatine (Quelle unklar) ============
        IngredientRule(
            listOf("gelatine", "gelatin", "gélatine", "جيلاتين"),
            HalalStatus.MUSHBOOH,
            "Gelatine - tierische Quelle (Schwein/Rind/Fisch) muss geprüft werden"
        ),
        IngredientRule(
            listOf("e441", "e 441"),
            HalalStatus.MUSHBOOH,
            "E441 - Gelatine, Quelle prüfen"
        ),
        // ============ Mono-/Diglyceride und Verwandte ============
        IngredientRule(
            listOf("mono- und diglyceride", "mono and diglycerides", "mono-diglyceride",
                   "mono-/diglyceride", "monoglyceride", "diglyceride",
                   "e471", "e 471", "e472", "e 472", "e473", "e 473",
                   "e474", "e 474", "e475", "e 475", "e476", "e 476",
                   "e477", "e 477", "e478", "e 478", "e479", "e 479"),
            HalalStatus.MUSHBOOH,
            "Mono-/Diglyceride und Verwandte (E471-E479) - können tierischer oder pflanzlicher Herkunft sein"
        ),
        IngredientRule(
            listOf("e481", "e 481", "e482", "e 482", "stearoyllaktylat"),
            HalalStatus.MUSHBOOH,
            "E481/E482 (Stearoyllaktylate) - Fettsäure-Quelle muss geprüft werden"
        ),
        IngredientRule(
            listOf("e491", "e 491", "e492", "e 492", "e493", "e 493", "e494", "e 494",
                   "e495", "e 495", "sorbitan"),
            HalalStatus.MUSHBOOH,
            "Sorbitan-Ester (E491-E495) - Fettsäure-Quelle prüfen"
        ),
        IngredientRule(
            listOf("e470", "e 470", "speisefettsäuresalze", "fatty acid salts"),
            HalalStatus.MUSHBOOH,
            "E470 (Speisefettsäure-Salze) - Quelle prüfen"
        ),
        // ============ Geschmacks-Enhancer ============
        IngredientRule(
            listOf("e631", "e 631", "natriuminosinat", "sodium inosinate", "disodium inosinate"),
            HalalStatus.MUSHBOOH,
            "E631 - kann aus Fleisch oder Fisch gewonnen werden"
        ),
        IngredientRule(
            listOf("e635", "e 635", "natriumribonukleotid"),
            HalalStatus.MUSHBOOH,
            "E635 (Natriumribonukleotid) - oft tierischen Ursprungs"
        ),
        IngredientRule(
            listOf("e627", "e 627", "natriumguanylat"),
            HalalStatus.MUSHBOOH,
            "E627 (Natriumguanylat) - kann tierisch sein"
        ),
        // ============ Glycerin ============
        IngredientRule(
            listOf("e422", "e 422", "glycerin", "glycerol", "glycérol", "glycerine", "glyzerin"),
            HalalStatus.MUSHBOOH,
            "Glycerin (E422) - kann aus tierischem Fett, pflanzlich oder synthetisch sein"
        ),
        IngredientRule(
            listOf("e1518", "e 1518", "triacetin"),
            HalalStatus.MUSHBOOH,
            "E1518 (Triacetin) - kann tierisches Glycerin enthalten"
        ),
        IngredientRule(
            listOf("e445", "e 445"),
            HalalStatus.MUSHBOOH,
            "E445 (Glycerolester von Wurzelharz) - Quelle prüfen"
        ),
        // ============ Aromen ============
        IngredientRule(
            listOf("natürliche aromen", "natural flavor", "natural flavour",
                   "natürliches aroma", "natural aroma", "arôme naturel"),
            HalalStatus.MUSHBOOH,
            "Natürliches Aroma - Quelle nicht spezifiziert, kann Alkohol oder tierische Bestandteile als Träger enthalten"
        ),
        IngredientRule(
            listOf("aromaextrakt", "flavor extract", "flavour extract", "extrakt"),
            HalalStatus.MUSHBOOH,
            "Aromen-Extrakt - Träger kann Alkohol enthalten"
        ),
        IngredientRule(
            listOf("vanilleextrakt", "vanilla extract", "vanille-extrakt"),
            HalalStatus.MUSHBOOH,
            "Vanille-Extrakt - meist auf Ethanol-Basis hergestellt"
        ),
        // ============ Lab/Rennet ============
        IngredientRule(
            listOf("lab ", " lab", "rennet", "labferment", "labenzym"),
            HalalStatus.MUSHBOOH,
            "Lab/Rennet - tierisch oder mikrobiell, Quelle prüfen"
        ),
        IngredientRule(
            listOf("molke", "whey", "lactosérum"),
            HalalStatus.MUSHBOOH,
            "Molke - kritisch wenn der Käse mit tierischem Lab hergestellt wurde"
        ),
        // ============ Emulgatoren allgemein ============
        IngredientRule(
            listOf("emulgator", "emulsifier", "émulsifiant"),
            HalalStatus.MUSHBOOH,
            "Emulgator - Quelle muss geprüft werden"
        ),
        IngredientRule(
            listOf("e322", "e 322", "lecithin", "lécithine"),
            HalalStatus.MUSHBOOH,
            "Lecithin (E322) - meist Soja (halal) aber auch Ei-Lecithin möglich"
        ),
        // ============ Tierfett, nicht-halal-Schlachtung ============
        IngredientRule(
            listOf("rindergelatine", "beef gelatin", "bovine gelatin", "rindsgelatine"),
            HalalStatus.MUSHBOOH,
            "Rindergelatine - nur halal wenn aus halal-geschlachtetem Rind"
        ),
        IngredientRule(
            listOf("rinderfett", "beef tallow", "tallow", "rindertalg", "talg"),
            HalalStatus.MUSHBOOH,
            "Rinderfett/Talg - nur halal wenn aus halal-Schlachtung"
        ),
        IngredientRule(
            listOf("hühnerfett", "chicken fat", "geflügelfett", "poultry fat"),
            HalalStatus.MUSHBOOH,
            "Geflügelfett - nur halal wenn aus halal-Schlachtung"
        ),
        IngredientRule(
            listOf("lamm", "lamm-", "schaffett", "lamb fat"),
            HalalStatus.MUSHBOOH,
            "Lamm-/Schaffett - nur halal wenn aus halal-Schlachtung"
        ),
        // ============ Andere fragwürdige ============
        IngredientRule(
            listOf("e904", "e 904"),
            HalalStatus.MUSHBOOH,
            "E904 - meist Schellack, siehe HARAM"
        ),
        IngredientRule(
            listOf("e120", "e 120"),
            HalalStatus.MUSHBOOH,
            "E120 - meist Karmin, siehe HARAM"
        ),
        IngredientRule(
            listOf("e1100", "e 1100", "amylase"),
            HalalStatus.MUSHBOOH,
            "E1100 (Amylasen) - kann tierischen oder mikrobiellen Ursprungs sein"
        ),
        IngredientRule(
            listOf("e1105", "e 1105", "lysozym"),
            HalalStatus.MUSHBOOH,
            "E1105 (Lysozym) - aus Ei oder tierisch"
        ),
        IngredientRule(
            listOf("e441", "e 441"),
            HalalStatus.MUSHBOOH,
            "E441 - Gelatine, Quelle prüfen"
        ),
        IngredientRule(
            listOf("vanillin natur", "vanillin natural"),
            HalalStatus.MUSHBOOH,
            "Natürliches Vanillin - oft auf Alkohol-Basis hergestellt"
        ),
        IngredientRule(
            listOf("propylenglycol", "propylene glycol", "e1520", "e 1520"),
            HalalStatus.MUSHBOOH,
            "E1520 (Propylenglykol) - oft als Träger für Alkohol-haltige Aromen"
        ),
        // Mögliche Schweine-Träger
        IngredientRule(
            listOf("magnesiumstearat", "magnesium stearate"),
            HalalStatus.MUSHBOOH,
            "Magnesiumstearat - Fettsäure-Quelle prüfen (tierisch oder pflanzlich)"
        ),
        IngredientRule(
            listOf("speisefettsäuren", "fatty acids"),
            HalalStatus.MUSHBOOH,
            "Speisefettsäuren - Quelle prüfen"
        ),
    )

    /** Positive Halal-Indikatoren in mehreren Sprachen. */
    val HALAL_INDICATORS = listOf(
        "halal", "حلال",
        "halal certified", "halal-zertifiziert", "halal certifié",
        "vegan", "végan", "vegano", "végétalien",
        "100% pflanzlich", "rein pflanzlich", "plant-based", "plant based", "100% plant",
        "vegetarisch", "vegetarian",
        "kosher", "koscher", "كوشر",
    )

    /** Bewertet einen Ingredient-Text. */
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
                if (haystack.contains(kw.lowercase())) ReasonItem(kw, rule.reason) else null
            }
        }
        val mushboohHits = MUSHBOOH.flatMap { rule ->
            rule.keywords.mapNotNull { kw ->
                if (haystack.contains(kw.lowercase())) ReasonItem(kw, rule.reason) else null
            }
        }
        val halalHits = HALAL_INDICATORS.filter { haystack.contains(it.lowercase()) }

        val status = when {
            haramHits.isNotEmpty() -> HalalStatus.HARAM
            mushboohHits.isNotEmpty() -> HalalStatus.MUSHBOOH
            halalHits.any { it == "halal" || it.contains("halal") } -> HalalStatus.HALAL
            halalHits.isNotEmpty() -> HalalStatus.HALAL
            else -> HalalStatus.LIKELY_HALAL
        }

        val reasons = when (status) {
            HalalStatus.HARAM -> haramHits.distinctBy { it.reason }.map { it.reason }
            HalalStatus.MUSHBOOH -> mushboohHits.distinctBy { it.reason }.map { it.reason }
            HalalStatus.HALAL -> halalHits.map { "Halal-Indikator gefunden: \"$it\"" }
            HalalStatus.LIKELY_HALAL -> listOf(
                "Keine haram- oder zweifelhaften Zutaten in der Liste erkannt. " +
                "Dies ist keine offizielle Halal-Zertifizierung."
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
    HALAL, LIKELY_HALAL, MUSHBOOH, HARAM, UNKNOWN,
}

data class HalalAnalysis(
    val status: HalalStatus,
    val reasons: List<String>,
    val halalIndicators: List<String> = emptyList(),
    val haramTriggers: List<String> = emptyList(),
    val mushboohTriggers: List<String> = emptyList(),
)
