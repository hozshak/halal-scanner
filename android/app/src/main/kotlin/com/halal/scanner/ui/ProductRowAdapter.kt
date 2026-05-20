package com.halal.scanner.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.halal.scanner.R
import com.halal.scanner.halal.HalalStatus

/**
 * Allgemeiner Adapter für gespeicherte Produkte (Watchlist + Compare-Selection).
 * Zeigt Bild, Name, Marke + farbigen Status-Punkt rechts.
 */
class ProductRowAdapter(
    private var items: List<Item>,
    private val onClick: (Item) -> Unit,
) : RecyclerView.Adapter<ProductRowAdapter.VH>() {

    data class Item(
        val barcode: String,
        val name: String?,
        val brand: String?,
        val imageUrl: String?,
        val status: HalalStatus,
        val selected: Boolean = false,
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_watchlist_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    fun replace(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val image: ImageView = view.findViewById(R.id.itemImage)
        private val name: TextView  = view.findViewById(R.id.itemName)
        private val brand: TextView = view.findViewById(R.id.itemBrand)
        private val dot: TextView   = view.findViewById(R.id.itemStatusDot)

        fun bind(item: Item) {
            name.text  = item.name  ?: itemView.context.getString(R.string.result_no_name)
            brand.text = item.brand ?: ""
            if (!item.imageUrl.isNullOrBlank()) {
                image.load(item.imageUrl)
            } else {
                image.setImageDrawable(null)
            }
            val bgRes = when (item.status) {
                HalalStatus.HALAL, HalalStatus.LIKELY_HALAL -> R.drawable.circle_green
                HalalStatus.HARAM -> R.drawable.circle_red
                HalalStatus.MUSHBOOH -> R.drawable.circle_orange
                HalalStatus.UNKNOWN -> R.drawable.circle_dark_outline
            }
            dot.setBackgroundResource(bgRes)
            itemView.alpha = if (item.selected) 0.5f else 1.0f
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
