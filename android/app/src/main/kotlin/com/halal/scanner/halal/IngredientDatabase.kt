package com.halal.scanner.halal

import androidx.annotation.StringRes
import com.halal.scanner.R

/**
 * Halal-Zutaten-Datenbank.
 * Begründungen sind als String-Resources hinterlegt (DE/EN/AR).
 */
object IngredientDatabase {

    val HARAM = listOf(
        IngredientRule(
            listOf("schwein", "schweine", "pork", "porc", "pig", "porcine",
                   "speck", "bacon", "ham ", " ham", "schinken", "jambon",
                   "schmalz", "lard", "saindoux",
                   "schweinefett", "schweinegelatine", "pork fat", "pork gelatin", "pork gelatine",
                   "schweineleberwurst", "leberwurst", "blutwurst",
                   "خنزير", "لحم خنزير"),
            HalalStatus.HARAM,
            R.string.reason_pork
        ),
        IngredientRule(
            listOf("alkohol", "alcohol", "alcoólico", "alcool",
                   "ethanol", "äthanol", "éthanol",
                   "rum ", " rum",
                   "wein ", " wein", "weinessig", "weinbrand", "wine ", " wine",
                   "bier", "beer", "bière",
                   "whisky", "whiskey", "vodka", "wodka",
                   "spirituose", "spirits", "liquor",
                   "weingeist", "branntwein", "kirschwasser",
                   "كحول", "خمر"),
            HalalStatus.HARAM,
            R.string.reason_alcohol
        ),
        IngredientRule(
            listOf("karmin", "carmine", "cochenille", "cochineal", "e120", "e 120", "karminsäure"),
            HalalStatus.HARAM,
            R.string.reason_carmine
        ),
        IngredientRule(
            listOf("schellack", "shellac", "gomme-laque", "e904", "e 904"),
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
            listOf("l-cystein", "l-cysteine", "e920", "e 920", "cysteinhydrochlorid"),
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
            listOf("e542", "e 542", "knochenphosphat", "bone phosphate"),
            HalalStatus.HARAM,
            R.string.reason_bone_phosphate
        ),
    )

    val MUSHBOOH = listOf(
        IngredientRule(
            listOf("gelatine", "gelatin", "gélatine", "جيلاتين", "e441", "e 441"),
            HalalStatus.MUSHBOOH,
            R.string.reason_gelatin
        ),
        IngredientRule(
            listOf("mono- und diglyceride", "mono and diglycerides", "mono-diglyceride",
                   "mono-/diglyceride", "monoglyceride", "diglyceride",
                   "e471", "e 471", "e472", "e 472", "e473", "e 473",
                   "e474", "e 474", "e475", "e 475", "e476", "e 476",
                   "e477", "e 477", "e478", "e 478", "e479", "e 479"),
            HalalStatus.MUSHBOOH,
            R.string.reason_diglycerides
        ),
        IngredientRule(
            listOf("e481", "e 481", "e482", "e 482", "stearoyllaktylat"),
            HalalStatus.MUSHBOOH,
            R.string.reason_stearoyl_lactylate
        ),
        IngredientRule(
            listOf("e491", "e 491", "e492", "e 492", "e493", "e 493", "e494", "e 494",
                   "e495", "e 495", "sorbitan"),
            HalalStatus.MUSHBOOH,
            R.string.reason_sorbitan
        ),
        IngredientRule(
            listOf("e470", "e 470", "speisefettsäuresalze", "fatty acid salts"),
            HalalStatus.MUSHBOOH,
            R.string.reason_fatty_acid_salts
        ),
        IngredientRule(
            listOf("e631", "e 631", "natriuminosinat", "sodium inosinate", "disodium inosinate"),
            HalalStatus.MUSHBOOH,
            R.string.reason_sodium_inosinate
        ),
        IngredientRule(
            listOf("e635", "e 635", "natriumribonukleotid"),
            HalalStatus.MUSHBOOH,
            R.string.reason_sodium_ribonucleotide
        ),
        IngredientRule(
            listOf("e627", "e 627", "natriumguanylat"),
            HalalStatus.MUSHBOOH,
            R.string.reason_sodium_guanylate
        ),
        IngredientRule(
            listOf("e422", "e 422", "glycerin", "glycerol", "glycérol", "glycerine", "glyzerin"),
            HalalStatus.MUSHBOOH,
            R.string.reason_glycerin
        ),
        IngredientRule(
            listOf("e1518", "e 1518", "triacetin"),
            HalalStatus.MUSHBOOH,
            R.string.reason_triacetin
        ),
        IngredientRule(
            listOf("e445", "e 445"),
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
            listOf("lab ", " lab", "rennet", "labferment", "labenzym"),
            HalalStatus.MUSHBOOH,
            R.string.reason_rennet
        ),
        IngredientRule(
            listOf("molke", "whey", "lactosérum"),
            HalalStatus.MUSHBOOH,
            R.string.reason_whey
        ),
        IngredientRule(
            listOf("emulgator", "emulsifier", "émulsifiant"),
            HalalStatus.MUSHBOOH,
            R.string.reason_emulsifier
        ),
        IngredientRule(
            listOf("e322", "e 322", "lecithin", "lécithine"),
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
            listOf("e1100", "e 1100", "amylase"),
            HalalStatus.MUSHBOOH,
            R.string.reason_amylase
        ),
        IngredientRule(
            listOf("e1105", "e 1105", "lysozym"),
            HalalStatus.MUSHBOOH,
            R.string.reason_lysozyme
        ),
        IngredientRule(
            listOf("vanillin natur", "vanillin natural"),
            HalalStatus.MUSHBOOH,
            R.string.reason_vanillin
        ),
        IngredientRule(
            listOf("propylenglycol", "propylene glycol", "e1520", "e 1520"),
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
        "vegan", "végan", "vegano", "végétalien",
        "100% pflanzlich", "rein pflanzlich", "plant-based", "plant based", "100% plant",
        "kosher", "koscher", "كوشر",
    )

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
                if (haystack.contains(kw.lowercase())) ReasonHit(kw, rule.reasonResId) else null
            }
        }
        val mushboohHits = MUSHBOOH.flatMap { rule ->
            rule.keywords.mapNotNull { kw ->
                if (haystack.contains(kw.lowercase())) ReasonHit(kw, rule.reasonResId) else null
            }
        }
        val halalHits = HALAL_INDICATORS.filter { haystack.contains(it.lowercase()) }

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
