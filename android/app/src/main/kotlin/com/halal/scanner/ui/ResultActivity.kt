package com.halal.scanner.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import java.io.File
import com.halal.scanner.R
import com.halal.scanner.data.Nutriments
import com.halal.scanner.data.OpenFoodFactsClient
import com.halal.scanner.data.Product
import com.halal.scanner.databinding.ActivityResultBinding
import com.halal.scanner.db.AllergenPrefs
import com.halal.scanner.db.BookmarkStore
import com.halal.scanner.db.HistoryStore
import com.halal.scanner.db.ScanStatsStore
import com.halal.scanner.halal.HalalAnalysis
import com.halal.scanner.halal.HalalStatus
import com.halal.scanner.halal.IngredientDatabase
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.util.Locale

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val client = OpenFoodFactsClient()
    private val history by lazy { HistoryStore(this) }
    private val bookmarks by lazy { BookmarkStore(this) }
    private val stats by lazy { ScanStatsStore(this) }

    /** Aktuell angezeigtes Produkt (null beim OCR-Pfad). */
    private var currentProduct: Product? = null
    /** Aktuelle Halal-Analyse (für Share-Text). */
    private var currentAnalysis: HalalAnalysis? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScanAgain.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
            finish()
        }
        binding.btnBack.setOnClickListener { finish() }

        // Top-Action-Bar
        binding.btnTopBack.setOnClickListener { finish() }
        binding.btnTopShare.setOnClickListener { shareCurrentProduct() }
        binding.btnTopBookmark.setOnClickListener { toggleBookmark() }
        binding.btnTopFlag.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://github.com/hozshak/halal-scanner/issues/new?title=Report:+${currentProduct?.barcode.orEmpty()}")))
            } catch (_: Exception) {}
        }

        val barcode = intent.getStringExtra(EXTRA_BARCODE)
        val ocrText = intent.getStringExtra(EXTRA_OCR_TEXT)

        when {
            !barcode.isNullOrBlank() -> {
                binding.txtBarcode.text = getString(R.string.result_barcode_label, barcode)
                showLoading()
                lifecycleScope.launch {
                    when (val r = client.fetchProduct(barcode)) {
                        is OpenFoodFactsClient.Result.Found    -> showProduct(r.product)
                        is OpenFoodFactsClient.Result.NotFound -> showNotFound(barcode)
                        is OpenFoodFactsClient.Result.Error    -> showError(r.message)
                    }
                }
            }
            !ocrText.isNullOrBlank() -> {
                binding.txtBarcode.text = getString(R.string.result_ocr_source)
                val photoPath = intent.getStringExtra(EXTRA_OCR_PHOTO_PATH)
                showOcrResult(ocrText, photoPath)
            }
            else -> finish()
        }
    }

    // -------------------------------------------------------------------------
    // OCR-Pfad: kein Produkt, nur Text + Foto
    // -------------------------------------------------------------------------
    private fun showOcrResult(text: String, photoPath: String?) {
        val analysis = IngredientDatabase.analyze(text, emptyList())
        stats.recordScan(analysis.status)

        binding.progressBar.visibility = View.GONE
        binding.contentGroup.visibility = View.VISIBLE
        binding.errorBox.visibility = View.GONE

        // Beim OCR-Pfad: Hero-Bereich + Infokacheln + Nährwerte ausblenden, da wir
        // kein OFF-Produkt haben. Nur Status, Reasons, Zutaten anzeigen.
        binding.heroCard.visibility = View.GONE
        binding.txtProductName.visibility = View.GONE
        binding.txtBrand.visibility = View.GONE
        binding.infoCardsRow.visibility = View.GONE
        binding.nutritionSection.visibility = View.GONE
        binding.manufacturerSection.visibility = View.GONE
        binding.ecoScoreCard.visibility = View.GONE
        binding.novaScoreCard.visibility = View.GONE
        binding.allergensSection.visibility = View.GONE
        binding.categoriesSection.visibility = View.GONE
        // Bookmark im OCR-Pfad deaktiviert (es gibt kein Produkt zum Bookmarken)
        binding.btnTopBookmark.alpha = 0.4f

        // OCR-Foto in dedizierter ImageView anzeigen
        if (!photoPath.isNullOrBlank() && File(photoPath).exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(photoPath)
                if (bitmap != null) {
                    binding.ocrPhotoView.setImageBitmap(bitmap)
                    binding.ocrPhotoView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                android.util.Log.e("ResultActivity", "Photo load failed", e)
            }
        }

        renderStatus(analysis)

        // Highlights direkt auf dem OCR-Text
        binding.txtIngredients.text = buildHighlightedText(
            text, analysis.haramTriggers, analysis.mushboohTriggers
        )
        binding.txtIngredients.visibility = View.VISIBLE
        binding.txtIngredientsHeader.text = getString(R.string.result_extracted_text_header)
        binding.txtIngredientsHeader.visibility = View.VISIBLE

        // Reasons-Card (immer sichtbar bei OCR-Pfad)
        binding.reasonsCard.visibility = View.VISIBLE

        // Pros/Cons auch bei OCR aus Analyse + (leeren) Nutriments ableiten
        renderProsCons(analysis, nutriments = null, novaGroup = null)
    }

    // -------------------------------------------------------------------------
    // Barcode-Pfad: vollständiges OFF-Produkt
    // -------------------------------------------------------------------------
    private fun showProduct(product: Product) {
        val analysis = IngredientDatabase.analyze(
            text = product.ingredientsText.orEmpty(),
            labels = product.labels
        )
        history.add(product, analysis.status)
        stats.recordScan(analysis.status)

        binding.progressBar.visibility = View.GONE
        binding.contentGroup.visibility = View.VISIBLE
        binding.errorBox.visibility = View.GONE
        binding.ocrPhotoView.visibility = View.GONE

        // Hero
        binding.heroCard.visibility = View.VISIBLE
        if (!product.imageUrl.isNullOrBlank()) {
            binding.productImage.visibility = View.VISIBLE
            binding.productImage.load(product.imageUrl)
        } else {
            binding.productImage.visibility = View.GONE
        }

        // Produkt-Name + Marke
        binding.txtProductName.visibility = View.VISIBLE
        binding.txtProductName.text = product.name ?: getString(R.string.result_no_name)
        if (product.brand.isNullOrBlank()) {
            binding.txtBrand.visibility = View.GONE
        } else {
            binding.txtBrand.visibility = View.VISIBLE
            binding.txtBrand.text = product.brand
        }

        // 3 Info-Kacheln
        renderInfoCards(product, analysis)

        // Status + Begründung + Zutaten
        renderStatus(analysis)
        renderIngredients(product, analysis)
        binding.reasonsCard.visibility = View.VISIBLE

        // Nährwerte + Hersteller + Pros/Cons
        renderNutrition(product.nutriments)
        renderManufacturer(product.manufacturer)
        renderEcoScore(product)
        renderNovaScore(product)
        renderAllergens(product)
        renderCategories(product)
        renderProsCons(analysis, product.nutriments, product.novaGroup)

        // Top-Action-Bar Status
        currentProduct = product
        currentAnalysis = analysis
        updateBookmarkIcon()
    }

    // -------------------------------------------------------------------------
    // Bookmark + Share Top-Bar-Actions
    // -------------------------------------------------------------------------
    private fun toggleBookmark() {
        val p = currentProduct ?: run {
            android.widget.Toast.makeText(this, R.string.watchlist_needs_product, android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val nowBookmarked = bookmarks.toggle(p, currentAnalysis?.status ?: HalalStatus.UNKNOWN)
        updateBookmarkIcon()
        android.widget.Toast.makeText(
            this,
            if (nowBookmarked) R.string.watchlist_added else R.string.watchlist_removed,
            android.widget.Toast.LENGTH_SHORT,
        ).show()
    }

    private fun updateBookmarkIcon() {
        val p = currentProduct
        if (p == null) {
            binding.btnTopBookmark.setImageResource(R.drawable.ic_bookmark_outline)
            return
        }
        val isMarked = bookmarks.isBookmarked(p.barcode)
        binding.btnTopBookmark.setImageResource(
            if (isMarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
        )
    }

    private fun shareCurrentProduct() {
        val p = currentProduct ?: return
        val statusLabel = currentAnalysis?.let { getString(statusLabelRes(it.status)) } ?: "?"
        val text = buildString {
            append(p.name ?: getString(R.string.result_no_name))
            p.brand?.let { append(" – ").append(it) }
            append("\n\n")
            append(getString(R.string.share_status_line, statusLabel))
            append("\n")
            append("https://world.openfoodfacts.org/product/").append(p.barcode)
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        try {
            startActivity(Intent.createChooser(send, getString(R.string.share_chooser_title)))
        } catch (_: Exception) {}
    }

    private fun statusLabelRes(status: HalalStatus): Int = when (status) {
        HalalStatus.HALAL        -> R.string.status_halal
        HalalStatus.LIKELY_HALAL -> R.string.status_likely_halal
        HalalStatus.MUSHBOOH     -> R.string.status_mushbooh
        HalalStatus.HARAM        -> R.string.status_haram
        HalalStatus.UNKNOWN      -> R.string.status_unknown
    }

    // -------------------------------------------------------------------------
    // Eco-Score, Nova-Score, Categories, Allergens
    // -------------------------------------------------------------------------
    private fun renderEcoScore(product: Product) {
        val grade = product.ecoScoreGrade
        val value = product.ecoScoreValue
        if (grade == null && value == null) {
            binding.ecoScoreCard.visibility = View.GONE
            return
        }
        binding.ecoScoreCard.visibility = View.VISIBLE

        // Position: lieber den Numerik-Score nutzen, sonst Grade A→0.9, E→0.1
        val position = when {
            value != null -> value.coerceIn(0, 100) / 100f
            grade != null -> when (grade) {
                "a" -> 0.9f; "b" -> 0.7f; "c" -> 0.5f; "d" -> 0.3f; "e" -> 0.1f
                else -> 0.5f
            }
            else -> 0.5f
        }
        binding.ecoScoreBar.position = position
        binding.txtEcoScoreGrade.text = (grade ?: "").uppercase()

        val subtitleRes = when (grade) {
            "a", "b" -> R.string.eco_score_subtitle_good
            "c"      -> R.string.eco_score_subtitle_moderate
            "d", "e" -> R.string.eco_score_subtitle_poor
            else     -> R.string.eco_score_subtitle_unknown
        }
        binding.txtEcoScoreSubtitle.text = getString(subtitleRes)
    }

    private fun renderNovaScore(product: Product) {
        val nova = product.novaGroup
        if (nova == null || nova !in 1..4) {
            binding.novaScoreCard.visibility = View.GONE
            return
        }
        binding.novaScoreCard.visibility = View.VISIBLE
        binding.txtNovaGroup.text = nova.toString()

        // Bei NOVA ist KLEIN = gut, GROSS = schlecht.
        // Daher Position invertieren: 1→0.9, 2→0.7, 3→0.3, 4→0.1
        binding.novaScoreBar.position = when (nova) {
            1 -> 0.9f; 2 -> 0.7f; 3 -> 0.3f; 4 -> 0.1f
            else -> 0.5f
        }

        // Farbiger Hintergrund der Nova-Group-Box
        val bgRes = when (nova) {
            1 -> R.drawable.circle_green
            2 -> R.drawable.circle_yellow
            3 -> R.drawable.circle_orange
            4 -> R.drawable.circle_red
            else -> R.drawable.bg_chip_dark
        }
        binding.novaGroupBox.setBackgroundResource(bgRes)

        val subtitleRes = when (nova) {
            1 -> R.string.nova_subtitle_1
            2 -> R.string.nova_subtitle_2
            3 -> R.string.nova_subtitle_3
            4 -> R.string.nova_subtitle_4
            else -> R.string.nova_subtitle_unknown
        }
        binding.txtNovaSubtitle.text = getString(subtitleRes)
    }

    /**
     * Allergen-Chips: zeige alle Produkt-Allergene als Chips.
     * Chips, die in den vom Nutzer ausgewählten Allergenen sind, werden rot
     * hervorgehoben.
     */
    private fun renderAllergens(product: Product) {
        binding.allergenChips.removeAllViews()
        val raw = product.allergens
        if (raw.isEmpty()) {
            binding.allergensSection.visibility = View.GONE
            return
        }
        binding.allergensSection.visibility = View.VISIBLE

        val userAllergens = AllergenPrefs(this).selected()
        for (tag in raw) {
            val key = tag.substringAfter(':')           // "en:milk" -> "milk"
            val label = humanizeAllergen(key)
            val isUser = userAllergens.contains(key)
            val chip = Chip(this).apply {
                text = label
                isClickable = false
                isCheckable = false
                if (isUser) {
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(0xFFE54B4B.toInt())
                    setTextColor(0xFFFFFFFF.toInt())
                } else {
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(0xFF1E2027.toInt())
                    setTextColor(0xFFB0B5BD.toInt())
                }
            }
            binding.allergenChips.addView(chip)
        }
    }

    private fun humanizeAllergen(key: String): String {
        val resId = when (key) {
            "gluten"        -> R.string.allergen_gluten
            "milk"          -> R.string.allergen_milk
            "eggs"          -> R.string.allergen_eggs
            "soybeans"      -> R.string.allergen_soybeans
            "nuts"          -> R.string.allergen_nuts
            "peanuts"       -> R.string.allergen_peanuts
            "fish"          -> R.string.allergen_fish
            "crustaceans"   -> R.string.allergen_crustaceans
            "molluscs"      -> R.string.allergen_molluscs
            "celery"        -> R.string.allergen_celery
            "mustard"       -> R.string.allergen_mustard
            "sesame-seeds"  -> R.string.allergen_sesame
            "sulphur-dioxide-and-sulphites" -> R.string.allergen_sulphites
            "lupin"         -> R.string.allergen_lupin
            else            -> 0
        }
        return if (resId != 0) getString(resId) else key.replace('-', ' ').replaceFirstChar {
            it.uppercase(Locale.getDefault())
        }
    }

    private fun renderCategories(product: Product) {
        binding.categoryChips.removeAllViews()
        // Letzten 8 Kategorien (die spezifischeren) anzeigen
        val cats = product.categories
            .map { stripPrefix(it) }
            .filter { it.isNotBlank() }
            .takeLast(8)
        if (cats.isEmpty()) {
            binding.categoriesSection.visibility = View.GONE
            return
        }
        binding.categoriesSection.visibility = View.VISIBLE
        val palette = intArrayOf(
            0xFF7B2C45.toInt(), // dunkelrot
            0xFF1E4D2B.toInt(), // dunkelgrün
            0xFF7C2F33.toInt(), // dunkelrot 2
            0xFF8B5A2B.toInt(), // braun
            0xFF1B4E8C.toInt(), // blau
            0xFF255A5A.toInt(), // teal
            0xFF55317F.toInt(), // purple
            0xFF6B4226.toInt(), // saddle
        )
        cats.forEachIndexed { i, cat ->
            val chip = Chip(this).apply {
                text = titleCase(cat)
                isClickable = false
                isCheckable = false
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(palette[i % palette.size])
                setTextColor(0xFFFFFFFF.toInt())
            }
            binding.categoryChips.addView(chip)
        }
    }

    // -------------------------------------------------------------------------
    // Renderer-Hilfsfunktionen
    // -------------------------------------------------------------------------
    private fun renderStatus(analysis: HalalAnalysis) {
        val (label, color, iconRes) = when (analysis.status) {
            HalalStatus.HALAL        ->
                Triple(getString(R.string.status_halal),        0xFF2EB872.toInt(), R.drawable.ic_check_big)
            HalalStatus.LIKELY_HALAL ->
                Triple(getString(R.string.status_likely_halal), 0xFF5BA85B.toInt(), R.drawable.ic_check_big)
            HalalStatus.MUSHBOOH     ->
                Triple(getString(R.string.status_mushbooh),     0xFFE6A23C.toInt(), R.drawable.ic_question_big)
            HalalStatus.HARAM        ->
                Triple(getString(R.string.status_haram),        0xFFE54B4B.toInt(), R.drawable.ic_cross_big)
            HalalStatus.UNKNOWN      ->
                Triple(getString(R.string.status_unknown),      0xFF8A929E.toInt(), R.drawable.ic_question_big)
        }

        // Großes Status-Card mit Gradient
        val darker = darken(color, 0.75f)
        val gradient = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
            intArrayOf(color, darker),
        ).apply {
            cornerRadius = 24f * resources.displayMetrics.density
        }
        binding.statusCard.background = gradient
        binding.statusIcon.setImageResource(iconRes)
        binding.txtStatusLabel.text = label
        binding.txtStatusSubtitle.text = getString(R.string.result_status_subtitle).uppercase()

        // Mini-Status in der 3er-Kachelreihe denselben Farbton geben
        val miniBg = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(color)
        }
        binding.infoStatusBadge.background = miniBg
        binding.infoStatusIcon.setImageResource(iconRes)
        binding.txtInfoStatusValue.text = label

        binding.txtReasons.text = analysis.reasonResIds
            .joinToString("\n\n") { "• " + getString(it) }
    }

    private fun renderInfoCards(product: Product, analysis: HalalAnalysis) {
        binding.infoCardsRow.visibility = View.VISIBLE

        // Kategorie: erstes leicht lesbares Tag
        val cat = product.categories
            .map { stripPrefix(it) }
            .firstOrNull { it.isNotBlank() }
            ?.let { titleCase(it) }
        binding.txtInfoCategoryValue.text = cat ?: getString(R.string.info_value_unknown)

        // Land: erstes Country-Tag
        val country = product.countries
            .map { stripPrefix(it) }
            .firstOrNull { it.isNotBlank() }
            ?.let { titleCase(it) }
        binding.txtInfoCountryValue.text = country ?: getString(R.string.info_value_unknown)
    }

    private fun renderNutrition(n: Nutriments?) {
        if (n == null || (n.energyKcal == null && n.sugarsG == null && n.fatG == null && n.proteinG == null)) {
            binding.nutritionSection.visibility = View.GONE
            return
        }
        binding.nutritionSection.visibility = View.VISIBLE
        binding.txtSugarValue.text    = formatG(n.sugarsG)
        binding.txtCaloriesValue.text = formatKcal(n.energyKcal)
        binding.txtProteinValue.text  = formatG(n.proteinG)
        binding.txtFatValue.text      = formatG(n.fatG)
    }

    private fun renderManufacturer(manufacturer: String?) {
        if (manufacturer.isNullOrBlank()) {
            binding.manufacturerSection.visibility = View.GONE
            return
        }
        binding.manufacturerSection.visibility = View.VISIBLE
        binding.txtManufacturerValue.text = manufacturer
    }

    // -------------------------------------------------------------------------
    // Pros / Cons aus Analyse + Nährwerten
    // Schwellenwerte orientieren sich an verbreiteten Nutri-Score-Stufen.
    // -------------------------------------------------------------------------
    private fun renderProsCons(analysis: HalalAnalysis, nutriments: Nutriments?, novaGroup: Int?) {
        val pros = mutableListOf<String>()
        val cons = mutableListOf<String>()

        // Halal-Indikator
        if (analysis.halalIndicators.isNotEmpty()) {
            pros += getString(R.string.pro_halal_label)
        }

        // Status
        when (analysis.status) {
            HalalStatus.HALAL, HalalStatus.LIKELY_HALAL -> {
                if (analysis.haramTriggers.isEmpty() && analysis.mushboohTriggers.isEmpty()) {
                    pros += getString(R.string.pro_no_haram)
                    pros += getString(R.string.pro_natural)
                }
            }
            HalalStatus.HARAM    -> cons += getString(R.string.con_contains_haram)
            HalalStatus.MUSHBOOH -> cons += getString(R.string.con_contains_mushbooh)
            HalalStatus.UNKNOWN  -> { /* nichts */ }
        }

        // Nährwert-Heuristiken (pro 100g)
        nutriments?.let { n ->
            n.sugarsG?.let { s ->
                if (s >= 22.5) cons += getString(R.string.con_high_sugar)
                else if (s <= 5.0) pros += getString(R.string.pro_low_sugar)
            }
            n.fatG?.let { f ->
                if (f >= 17.5) cons += getString(R.string.con_high_fat)
                else if (f <= 3.0) pros += getString(R.string.pro_low_fat)
            }
            n.saturatedFatG?.let { sf ->
                if (sf >= 5.0) cons += getString(R.string.con_high_saturated_fat)
            }
            n.saltG?.let { sa ->
                if (sa >= 1.5) cons += getString(R.string.con_high_salt)
                else if (sa <= 0.3) pros += getString(R.string.pro_low_salt)
            }
            n.proteinG?.let { p ->
                if (p >= 12.0) pros += getString(R.string.pro_high_protein)
            }
            n.fiberG?.let { fb ->
                if (fb >= 6.0) pros += getString(R.string.pro_high_fiber)
            }
            n.energyKcal?.let { kcal ->
                if (kcal >= 400.0) cons += getString(R.string.con_high_calories)
            }
        }

        if (novaGroup == 4) cons += getString(R.string.con_ultra_processed)

        // Render
        if (pros.isEmpty()) {
            binding.prosSection.visibility = View.GONE
        } else {
            binding.prosSection.visibility = View.VISIBLE
            binding.txtProsList.text = pros.distinct().joinToString("\n") { "• $it" }
        }

        if (cons.isEmpty()) {
            binding.consSection.visibility = View.GONE
        } else {
            binding.consSection.visibility = View.VISIBLE
            binding.txtConsList.text = cons.distinct().joinToString("\n") { "• $it" }
        }
    }

    private fun renderIngredients(product: Product, analysis: HalalAnalysis) {
        val raw = product.ingredientsText
        if (raw.isNullOrBlank()) {
            binding.ingredientsCard.visibility = View.GONE
            return
        }
        binding.ingredientsCard.visibility = View.VISIBLE
        binding.txtIngredients.text = buildHighlightedText(
            raw, analysis.haramTriggers, analysis.mushboohTriggers
        )
        binding.txtIngredients.visibility = View.VISIBLE
        binding.txtIngredientsHeader.text = getString(R.string.result_ingredients_header)
        binding.txtIngredientsHeader.visibility = View.VISIBLE
    }

    // -------------------------------------------------------------------------
    // Loading / Fehler
    // -------------------------------------------------------------------------
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentGroup.visibility = View.GONE
        binding.errorBox.visibility = View.GONE
    }

    private fun showNotFound(barcode: String) {
        binding.progressBar.visibility = View.GONE
        binding.contentGroup.visibility = View.GONE
        binding.errorBox.visibility = View.VISIBLE
        binding.txtError.text = getString(R.string.result_not_found, barcode)
        binding.btnTryOcr.visibility = View.VISIBLE
        binding.txtTryOcrHint.visibility = View.VISIBLE
        binding.btnTryOcr.setOnClickListener {
            startActivity(Intent(this, TextScannerActivity::class.java))
            finish()
        }
        binding.btnContribute.visibility = View.VISIBLE
        binding.btnContribute.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://world.openfoodfacts.org/cgi/product.pl?type=edit&code=$barcode")))
            } catch (_: Exception) {}
        }
        binding.btnSearchGoogle.visibility = View.VISIBLE
        binding.btnSearchGoogle.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.google.com/search?q=barcode+$barcode")))
            } catch (_: Exception) {}
        }
        binding.txtSourcesSearched.visibility = View.VISIBLE
    }

    private fun showError(msg: String) {
        binding.progressBar.visibility = View.GONE
        binding.contentGroup.visibility = View.GONE
        binding.errorBox.visibility = View.VISIBLE
        binding.txtError.text = getString(R.string.result_error, msg)
        binding.btnTryOcr.visibility = View.VISIBLE
        binding.txtTryOcrHint.visibility = View.VISIBLE
        binding.btnTryOcr.setOnClickListener {
            startActivity(Intent(this, TextScannerActivity::class.java))
            finish()
        }
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------
    private fun buildHighlightedText(
        text: String,
        haramTriggers: List<String>,
        mushboohTriggers: List<String>,
    ): SpannableStringBuilder {
        val sb = SpannableStringBuilder(text)
        val haramBg = 0x66E54B4B
        val mushBg  = 0x66E6A23C
        val white   = Color.WHITE

        fun highlight(triggers: List<String>, bgColor: Int) {
            val sorted = triggers.distinct().sortedByDescending { it.length }
            for (t in sorted) {
                val pattern = "(?<![\\p{L}\\p{N}])" + Regex.escape(t) + "(?![\\p{L}\\p{N}])"
                val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                for (match in regex.findAll(sb.toString())) {
                    sb.setSpan(BackgroundColorSpan(bgColor),
                        match.range.first, match.range.last + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    sb.setSpan(ForegroundColorSpan(white),
                        match.range.first, match.range.last + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    sb.setSpan(StyleSpan(android.graphics.Typeface.BOLD),
                        match.range.first, match.range.last + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        highlight(mushboohTriggers, mushBg)
        highlight(haramTriggers, haramBg)
        return sb
    }

    private fun darken(color: Int, factor: Float): Int {
        val r = ((color shr 16 and 0xFF) * factor).toInt()
        val g = ((color shr 8 and 0xFF) * factor).toInt()
        val b = ((color and 0xFF) * factor).toInt()
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    /** "en:milk-chocolate" -> "milk chocolate" */
    private fun stripPrefix(tag: String): String {
        val noLangPrefix = tag.substringAfter(':', tag)
        return noLangPrefix.replace('-', ' ').trim()
    }

    private fun titleCase(s: String): String {
        return s.split(' ').joinToString(" ") { word ->
            if (word.isEmpty()) word
            else word.substring(0, 1).uppercase(Locale.getDefault()) + word.substring(1)
        }
    }

    private fun formatG(value: Double?): String {
        if (value == null) return getString(R.string.nutrition_unknown)
        val s = if (value >= 10.0)
            String.format(Locale.getDefault(), "%.0f", value)
        else
            String.format(Locale.getDefault(), "%.1f", value)
        return getString(R.string.nutrition_value_g, s)
    }

    private fun formatKcal(value: Double?): String {
        if (value == null) return getString(R.string.nutrition_unknown)
        val s = String.format(Locale.getDefault(), "%.0f", value)
        return getString(R.string.nutrition_value_kcal, s)
    }

    companion object {
        const val EXTRA_BARCODE        = "barcode"
        const val EXTRA_OCR_TEXT       = "ocr_text"
        const val EXTRA_OCR_PHOTO_PATH = "ocr_photo_path"
    }
}
