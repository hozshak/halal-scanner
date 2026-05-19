package com.halal.scanner.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
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
                showOcrResult(ocrText)
            }
            else -> finish()
        }
    }

    private fun showOcrResult(text: String) {
        val analysis = IngredientDatabase.analyze(text, emptyList())

        binding.progressBar.visibility = View.GONE
        binding.contentGroup.visibility = View.VISIBLE
        binding.errorBox.visibility = View.GONE

        binding.txtProductName.text = getString(R.string.result_ocr_product_label)
        binding.txtBrand.text = ""
        binding.txtBrand.visibility = View.GONE
        binding.productImage.visibility = View.GONE

        // Pseudo-Product anlegen damit renderIngredients funktioniert
        val pseudo = Product(
            barcode = "",
            name = null, brand = null, imageUrl = null,
            ingredientsText = text, ingredientsLanguage = null,
            labels = emptyList(), countries = emptyList(),
            novaGroup = null, nutriScore = null,
        )
        renderStatus(analysis)
        renderIngredients(pseudo, analysis)
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

        binding.txtReasons.text = analysis.reasons.joinToString("\n\n") { "• $it" }
    }

    private fun renderIngredients(product: Product, analysis: HalalAnalysis) {
        val raw = product.ingredientsText
        if (raw.isNullOrBlank()) {
            binding.txtIngredients.visibility = View.GONE
            binding.txtIngredientsHeader.visibility = View.GONE
            return
        }
        // Markiere haram/mushbooh-Trigger im Text mit eckigen Klammern
        val triggers = (analysis.haramTriggers + analysis.mushboohTriggers).distinct()
            .sortedByDescending { it.length }
        var marked: String = raw
        for (t in triggers) {
            // Case-insensitive replace - via String-Extension um Kotlin-Overload-Resolution-
            // Issue mit Regex.replace zu umgehen.
            val regex = Regex(Regex.escape(t), RegexOption.IGNORE_CASE)
            marked = marked.replace(regex) { match -> "⚠️${match.value}⚠️" }
        }
        binding.txtIngredients.text = marked
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
    }

    private fun showError(msg: String) {
        binding.progressBar.visibility = View.GONE
        binding.contentGroup.visibility = View.GONE
        binding.errorBox.visibility = View.VISIBLE
        binding.txtError.text = getString(R.string.result_error, msg)
    }

    companion object {
        const val EXTRA_BARCODE = "barcode"
        const val EXTRA_OCR_TEXT = "ocr_text"
    }
}
