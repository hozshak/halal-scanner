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
import com.halal.scanner.db.BookmarkStore
import com.halal.scanner.db.HistoryStore
import com.halal.scanner.db.ScanStatsStore
import com.halal.scanner.halal.HalalAnalysis
import com.halal.scanner.halal.HalalStatus
import com.halal.scanner.halal.IngredientDatabase
import kotlinx.coroutines.launch
import java.util.Locale

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val client = OpenFoodFactsClient()
    private val history by lazy { HistoryStore(this) }
    private val bookmarks by lazy { BookmarkStore(this) }
    private val stats by lazy { ScanStatsStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScanAgain.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
            finish()
        }
        binding.btnBack.setOnClickListener { finish() }

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
        renderProsCons(analysis, product.nutriments, product.novaGroup)
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
