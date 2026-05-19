package com.halal.scanner.halal

import androidx.annotation.StringRes
import com.halal.scanner.R

/**
 * Halal-Zutaten-Datenbank mit echtem Wort-Grenzen-Matching via Regex.
 *
 * Vorher: Substring-Check matchte "ham" in "Hamburg" und "beer" in "Johannisbeeren" -
 * falsch positiv. Jetzt: Lookaround-Regex `(?<![\p{L}\p{N}])kw(?![\p{L}\p{N}])`
 * matcht nur wenn das Keyword als eigenständiges Wort steht (geht auch mit Arabisch
 * dank \p{L} Unicode-Letter-Klasse).
 *
 * Für deutsche Komposita: zusätzliche Compound-Varianten manuell eingetragen
 * (z.B. "schwein" allein matcht NICHT "Schweinefleisch" - daher auch "schweinefleisch"
 * explizit als Keyword).
 */
object IngredientDatabase {

    val HARAM = listOf(
        // Schwein: Basis + häufige deutsche/englische Komposita
        IngredientRule(
            listOf("schwein", "schweine", "schweins",
                   "schweinefleisch", "schweinefett", "schweineschmalz",
                   "schweinegelatine", "schweinerippe", "schweinemett",
                   "schweinerücken", "schweinekotelett",
                   "pork", "porc", "porcine",
                   "speck", "bacon", "ham", "schinken", "jambon",
                   "schmalz", "lard", "saindoux",
                   "pork fat", "pork gelatin", "pork gelatine",
                   "blutwurst", "schweineleberwurst", "leberwurst",
                   "خنزير", "لحم خنزير"),
            HalalStatus.HARAM,
            R.string.reason_pork
        ),
        // Alkohol: Wort-Grenze sorgt automatisch dafür dass "alkoholfrei" NICHT matched
        // (weil nach "alkohol" Buchstaben kommen)
        IngredientRule(
            listOf("alkohol", "alcohol", "alcoólico",
                   "ethanol", "äthanol",
                   "rum", "wein", "weinessig", "weinbrand", "weingeist",
                   "glühwein", "rotwein", "weißwein", "schaumwein",
                   "wine", "bier", "beer", "bière",
                   "whisky", "whiskey", "vodka", "wodka",
                   "spirituose", "spirits", "liquor",
                   "branntwein", "kirschwasser",
                   "كحول", "خمر"),
            HalalStatus.HARAM,
            R.string.reason_alcohol
        ),
        IngredientRule(
            listOf("karmin", "carmine", "cochenille", "cochineal", "e120", "karminsäure"),
            HalalStatus.HARAM,
            R.string.reason_carmine
        ),
        IngredientRule(
            listOf("schellack", "shellac", "gomme-laque", "e904"),
            HalalStatus.HARAM,
            R.string.reason_shellac
        ),
        IngredientRule(
            listOf("tierisches lab", "animal rennet", "kälberlab", "calf rennet", "präsentlab"),
            HalalStatus.HARAM,
            R.string.reason_animal_rennet
        ),
        IngredientRule(
            listOf("blutwurst", "blood sausage", "blutplasma", "blood plasma"),
            HalalStatus.HARAM,
            R.string.reason_blood
        ),
        IngredientRule(
            listOf("l-cystein", "l-cysteine", "e920", "cysteinhydrochlorid"),
            HalalStatus.HARAM,
            R.string.reason_lcysteine
        ),
        IngredientRule(
            listOf("pepsin"),
            HalalStatus.HARAM,
            R.string.reason_pepsin
        ),
        IngredientRule(
            listOf("aspik", "aspic"),
            HalalStatus.HARAM,
            R.string.reason_aspic
        ),
        IngredientRule(
            listOf("e542", "knochenphosphat", "bone phosphate"),
            HalalStatus.HARAM,
            R.string.reason_bone_phosphate
        ),
    )

    val MUSHBOOH = listOf(
        IngredientRule(
            listOf("gelatine", "gelatin", "gélatine", "جيلاتين", "e441"),
            HalalStatus.MUSHBOOH,
            R.string.reason_gelatin
        ),
        IngredientRule(
            listOf("mono- und diglyceride", "mono and diglycerides", "mono-diglyceride",
                   "mono-/diglyceride", "monoglyceride", "diglyceride", "diglyceriden",
                   "e471", "e472", "e473", "e474", "e475", "e476", "e477", "e478", "e479"),
            HalalStatus.MUSHBOOH,
            R.string.reason_diglycerides
        ),
        IngredientRule(
            listOf("e481", "e482", "stearoyllaktylat", "stearoyllaktylate"),
            HalalStatus.MUSHBOOH,
            R.string.reason_stearoyl_lactylate
        ),
        IngredientRule(
            listOf("e491", "e492", "e493", "e494", "e495", "sorbitan"),
            HalalStatus.MUSHBOOH,
            R.string.reason_sorbitan
        ),
        IngredientRule(
            listOf("e470", "speisefettsäuresalze", "fatty acid salts"),
            HalalStatus.MUSHBOOH,
            R.string.reason_fatty_acid_salts
        ),
        IngredientRule(
            listOf("e631", "natriuminosinat", "sodium inosinate", "disodium inosinate"),
            HalalStatus.MUSHBOOH,
            R.string.reason_sodium_inosinate
        ),
        IngredientRule(
            listOf("e635", "natriumribonukleotid"),
            HalalStatus.MUSHBOOH,
            R.string.reason_sodium_ribonucleotide
        ),
        IngredientRule(
            listOf("e627", "natriumguanylat"),
            HalalStatus.MUSHBOOH,
            R.string.reason_sodium_guanylate
        ),
        IngredientRule(
            listOf("e422", "glycerin", "glycerol", "glycérol", "glycerine", "glyzerin"),
            HalalStatus.MUSHBOOH,
            R.string.reason_glycerin
        ),
        IngredientRule(
            listOf("e1518", "triacetin"),
            HalalStatus.MUSHBOOH,
            R.string.reason_triacetin
        ),
        IngredientRule(
            listOf("e445"),
            HalalStatus.MUSHBOOH,
            R.string.reason_glycerol_ester
        ),
        IngredientRule(
            listOf("natürliche aromen", "natural flavor", "natural flavour",
                   "natürliches aroma", "natural aroma", "arôme naturel"),
            HalalStatus.MUSHBOOH,
            R.string.reason_natural_flavor
        ),
        IngredientRule(
            listOf("aromaextrakt", "flavor extract", "flavour extract"),
            HalalStatus.MUSHBOOH,
            R.string.reason_flavor_extract
        ),
        IngredientRule(
            listOf("vanilleextrakt", "vanilla extract", "vanille-extrakt"),
            HalalStatus.MUSHBOOH,
            R.string.reason_vanilla_extract
        ),
        IngredientRule(
            listOf("lab", "rennet", "labferment", "labenzym"),
            HalalStatus.MUSHBOOH,
            R.string.reason_rennet
        ),
        IngredientRule(
            listOf("molke", "whey", "lactosérum"),
            HalalStatus.MUSHBOOH,
            R.string.reason_whey
        ),
        IngredientRule(
            listOf("emulgator", "emulgatoren", "emulsifier", "émulsifiant"),
            HalalStatus.MUSHBOOH,
            R.string.reason_emulsifier
        ),
        IngredientRule(
            listOf("e322", "lecithin", "lécithine", "sojalecithin"),
            HalalStatus.MUSHBOOH,
            R.string.reason_lecithin
        ),
        IngredientRule(
            listOf("rindergelatine", "beef gelatin", "bovine gelatin", "rindsgelatine"),
            HalalStatus.MUSHBOOH,
            R.string.reason_beef_gelatin
        ),
        IngredientRule(
            listOf("rinderfett", "beef tallow", "tallow", "rindertalg"),
            HalalStatus.MUSHBOOH,
            R.string.reason_beef_tallow
        ),
        IngredientRule(
            listOf("hühnerfett", "chicken fat", "geflügelfett", "poultry fat"),
            HalalStatus.MUSHBOOH,
            R.string.reason_chicken_fat
        ),
        IngredientRule(
            listOf("schaffett", "lamb fat"),
            HalalStatus.MUSHBOOH,
            R.string.reason_lamb_fat
        ),
        IngredientRule(
            listOf("e1100", "amylase"),
            HalalStatus.MUSHBOOH,
            R.string.reason_amylase
        ),
        IngredientRule(
            listOf("e1105", "lysozym"),
            HalalStatus.MUSHBOOH,
            R.string.reason_lysozyme
        ),
        IngredientRule(
            listOf("propylenglycol", "propylene glycol", "e1520"),
            HalalStatus.MUSHBOOH,
            R.string.reason_propylene_glycol
        ),
        IngredientRule(
            listOf("magnesiumstearat", "magnesium stearate"),
            HalalStatus.MUSHBOOH,
            R.string.reason_magnesium_stearate
        ),
    )

    val HALAL_INDICATORS = listOf(
        "halal", "حلال",
        "halal certified", "halal-zertifiziert", "halal certifié",
        "vegan", "végan", "vegano",
        "100% pflanzlich", "rein pflanzlich", "plant-based", "plant based",
        "kosher", "koscher",
    )

    /**
     * Echtes Wort-Matching mit Unicode-Letter/Number-Klassen.
     * `(?<![\p{L}\p{N}])kw(?![\p{L}\p{N}])` matcht nur wenn keyword als komplettes
     * Wort steht. Vermeidet "ham" in "Hamburg" oder "beer" in "Johannisbeeren".
     */
    private fun matchesWord(haystack: String, keyword: String): Boolean {
        val k = keyword.lowercase().trim()
        if (k.isEmpty()) return false
        val pattern = "(?<![\\p{L}\\p{N}])" + Regex.escape(k) + "(?![\\p{L}\\p{N}])"
        return Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(haystack)
    }

    fun analyze(text: String, labels: List<String> = emptyList()): HalalAnalysis {
        if (text.isBlank() && labels.isEmpty()) {
            return HalalAnalysis(
                status = HalalStatus.UNKNOWN,
                reasonResIds = listOf(R.string.reason_no_ingredients),
                halalIndicators = emptyList()
            )
        }
        val haystack = (text + " " + labels.joinToString(" ")).lowercase()

        val haramHits = HARAM.flatMap { rule ->
            rule.keywords.mapNotNull { kw ->
                if (matchesWord(haystack, kw)) ReasonHit(kw, rule.reasonResId) else null
            }
        }
        val mushboohHits = MUSHBOOH.flatMap { rule ->
            rule.keywords.mapNotNull { kw ->
                if (matchesWord(haystack, kw)) ReasonHit(kw, rule.reasonResId) else null
            }
        }
        val halalHits = HALAL_INDICATORS.filter { matchesWord(haystack, it) }

        val status = when {
            haramHits.isNotEmpty() -> HalalStatus.HARAM
            mushboohHits.isNotEmpty() -> HalalStatus.MUSHBOOH
            halalHits.isNotEmpty() -> HalalStatus.HALAL
            else -> HalalStatus.LIKELY_HALAL
        }

        val reasonResIds = when (status) {
            HalalStatus.HARAM -> haramHits.distinctBy { it.reasonResId }.map { it.reasonResId }
            HalalStatus.MUSHBOOH -> mushboohHits.distinctBy { it.reasonResId }.map { it.reasonResId }
            HalalStatus.HALAL -> listOf(R.string.reason_halal_indicator)
            HalalStatus.LIKELY_HALAL -> listOf(R.string.reason_likely_halal)
            HalalStatus.UNKNOWN -> listOf(R.string.reason_no_ingredients)
        }

        return HalalAnalysis(
            status = status,
            reasonResIds = reasonResIds,
            halalIndicators = halalHits,
            haramTriggers = haramHits.map { it.keyword }.distinct(),
            mushboohTriggers = mushboohHits.map { it.keyword }.distinct(),
        )
    }
}

data class IngredientRule(
    val keywords: List<String>,
    val status: HalalStatus,
    @StringRes val reasonResId: Int,
)

data class ReasonHit(val keyword: String, @StringRes val reasonResId: Int)

enum class HalalStatus {
    HALAL, LIKELY_HALAL, MUSHBOOH, HARAM, UNKNOWN,
}

data class HalalAnalysis(
    val status: HalalStatus,
    val reasonResIds: List<Int>,
    val halalIndicators: List<String> = emptyList(),
    val haramTriggers: List<String> = emptyList(),
    val mushboohTriggers: List<String> = emptyList(),
)
