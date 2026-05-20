package com.halal.scanner.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.halal.scanner.R
import com.halal.scanner.databinding.ActivityAllergensBinding
import com.halal.scanner.db.AllergenPrefs

/**
 * Multi-Select-Liste der häufigen Allergene. Auswahl wird in AllergenPrefs
 * gespeichert und im Result-Screen zur Markierung benutzt.
 */
class AllergensActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAllergensBinding
    private lateinit var prefs: AllergenPrefs
    private val selected = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllergensBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AllergenPrefs(this)
        selected.addAll(prefs.selected())

        for (key in AllergenPrefs.ALL) {
            val chip = Chip(this).apply {
                text = labelFor(key)
                isCheckable = true
                isChecked = selected.contains(key)
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selected.add(key) else selected.remove(key)
                }
            }
            binding.allergensChips.addView(chip)
        }

        binding.btnAllergensBack.setOnClickListener { finish() }
        binding.btnAllergensSave.setOnClickListener {
            prefs.setSelected(selected)
            finish()
        }
    }

    private fun labelFor(key: String): String {
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
        return if (resId != 0) getString(resId) else key.replace('-', ' ')
    }
}
