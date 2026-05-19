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
import com.halal.scanner.data.OpenFoodFactsClient
import com.halal.scanner.data.Product
import com.halal.scanner.databinding.ActivityResultBinding
import com.halal.scanner.db.HistoryStore
import com.halal.scanner.halal.HalalAnalysis
import com.halal.scanner.halal.HalalStatus
import com.halal.scanner.halal.IngredientDatabase
import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val client = OpenFoodFactsClient()
    private val history by lazy { HistoryStore(this) }

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
                        is OpenFoodFactsClient.Result.Found -> showProduct(r.product)
                        is OpenFoodFactsClient.Result.NotFound -> showNotFound(barcode)
                        is OpenFoodFactsClient.Result.Error -> showError(r.message)
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

    private fun showOcrResult(text: String, photoPath: String?) {
        val analysis = IngredientDatabase.analyze(text, emptyList())

        binding.progressBar.visibility = View.GONE
        binding.contentGroup.visibility = View.VISIBLE
        binding.errorBox.visibility = View.GONE

        binding.txtProductName.text = getString(R.string.result_ocr_product_label)
        binding.txtBrand.text = ""
        binding.txtBrand.visibility = View.GONE
        binding.productImage.visibility = View.GONE  // kleine Kachel aus

        // Großes OCR-Foto in dedizierter ImageView anzeigen
        if (!photoPath.isNullOrBlank() && File(photoPath).exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(photoPath)
                if (bitmap != null) {
                    binding.ocrPhotoView.setImageBitmap(bitmap)
                    binding.ocrPhotoView.visibility = View.VISIBLE
                } else {
                    android.util.Log.w("ResultActivity", "Bitmap decode returned null for: $photoPath")
                }
            } catch (e: Exception) {
                android.util.Log.e("ResultActivity", "Photo load failed", e)
            }
        }

        renderStatus(analysis)

        // Spannable mit Farb-Highlights direkt auf dem OCR-Text
        val spannable = buildHighlightedText(text, analysis.haramTriggers, analysis.mushboohTriggers)
        binding.txtIngredients.text = spannable
        binding.txtIngredients.visibility = View.VISIBLE
        binding.txtIngredientsHeader.text = getString(R.string.result_extracted_text_header)
        binding.txtIngredientsHeader.visibility = View.VISIBLE
    }

    /**
     * Erzeugt SpannableString in dem haram-Trigger ROT und mushbooh-Trigger ORANGE
     * unterlegt werden. Statt der hässlichen ⚠️-Klammern.
     */
    private fun buildHighlightedText(
        text: String,
        haramTriggers: List<String>,
        mushboohTriggers: List<String>,
    ): SpannableStringBuilder {
        val sb = SpannableStringBuilder(text)
        val haramBg = 0x66E54B4B
        val mushBg = 0x66E6A23C
        val white = Color.WHITE

        fun highlight(triggers: List<String>, bgColor: Int) {
            // Längste zuerst damit überlappende Treffer korrekt markiert werden
            val sorted = triggers.distinct().sortedByDescending { it.length }
            for (t in sorted) {
                // Wort-Grenzen damit "ham" nicht in "Hamburg" markiert wird
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
        // Haram zuletzt damit es ggf. Mushbooh überschreibt
        highlight(haramTriggers, haramBg)
        return sb
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentGroup.visibility = View.GONE
        binding.errorBox.visibility = View.GONE
    }

    private fun showProduct(product: Product) {
        val analysis = IngredientDatabase.analyze(
            text = product.ingredientsText.orEmpty(),
            labels = product.labels
        )
        history.add(product, analysis.status)

        binding.progressBar.visibility = View.GONE
        binding.contentGroup.visibility = View.VISIBLE
        binding.errorBox.visibility = View.GONE

        binding.txtProductName.text = product.name ?: getString(R.string.result_no_name)
        binding.txtBrand.text = product.brand ?: ""
        binding.txtBrand.visibility = if (product.brand.isNullOrBlank()) View.GONE else View.VISIBLE

        if (!product.imageUrl.isNullOrBlank()) {
            binding.productImage.visibility = View.VISIBLE
            binding.productImage.load(product.imageUrl)
        } else {
            binding.productImage.visibility = View.GONE
        }

        renderStatus(analysis)
        renderIngredients(product, analysis)
    }

    private fun renderStatus(analysis: HalalAnalysis) {
        val (label, color, emoji) = when (analysis.status) {
            HalalStatus.HALAL -> Triple(getString(R.string.status_halal), 0xFF2EB872.toInt(), "✓")
            HalalStatus.LIKELY_HALAL -> Triple(getString(R.string.status_likely_halal), 0xFF5BA85B.toInt(), "✓")
            HalalStatus.MUSHBOOH -> Triple(getString(R.string.status_mushbooh), 0xFFE6A23C.toInt(), "?")
            HalalStatus.HARAM -> Triple(getString(R.string.status_haram), 0xFFE54B4B.toInt(), "✗")
            HalalStatus.UNKNOWN -> Triple(getString(R.string.status_unknown), 0xFF8A929E.toInt(), "?")
        }
        binding.statusBadge.setBackgroundColor(color)
        binding.txtStatusLabel.text = "$emoji  $label"

        binding.txtReasons.text = analysis.reasonResIds.joinToString("\n\n") { "• " + getString(it) }
    }

    private fun renderIngredients(product: Product, analysis: HalalAnalysis) {
        val raw = product.ingredientsText
        if (raw.isNullOrBlank()) {
            binding.txtIngredients.visibility = View.GONE
            binding.txtIngredientsHeader.visibility = View.GONE
            return
        }
        binding.txtIngredients.text = buildHighlightedText(
            raw, analysis.haramTriggers, analysis.mushboohTriggers
        )
        binding.txtIngredients.visibility = View.VISIBLE
        binding.txtIngredientsHeader.visibility = View.VISIBLE
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
                startActivity(
                    Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://world.openfoodfacts.org/cgi/product.pl?type=edit&code=$barcode"))
                )
            } catch (_: Exception) {}
        }
        binding.btnSearchGoogle.visibility = View.VISIBLE
        binding.btnSearchGoogle.setOnClickListener {
            try {
                startActivity(
                    Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://www.google.com/search?q=barcode+$barcode"))
                )
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

    companion object {
        const val EXTRA_BARCODE = "barcode"
        const val EXTRA_OCR_TEXT = "ocr_text"
        const val EXTRA_OCR_PHOTO_PATH = "ocr_photo_path"
    }
}
