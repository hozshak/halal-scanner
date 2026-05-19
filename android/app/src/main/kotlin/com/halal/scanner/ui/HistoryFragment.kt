package com.halal.scanner.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.halal.scanner.R
import com.halal.scanner.databinding.FragmentHistoryBinding
import com.halal.scanner.databinding.ItemHistoryBinding
import com.halal.scanner.db.HistoryStore
import com.halal.scanner.halal.HalalStatus
import java.text.DateFormat
import java.util.Date

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val history by lazy { HistoryStore(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.btnClear.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.history_clear_confirm)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    history.clear(); refresh()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun refresh() {
        val entries = history.list()
        binding.emptyView.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.adapter = Adapter(entries) { entry ->
            startActivity(
                Intent(requireContext(), ResultActivity::class.java)
                    .putExtra(ResultActivity.EXTRA_BARCODE, entry.barcode)
            )
        }
    }

    private class Adapter(
        private val items: List<HistoryStore.Entry>,
        private val onClick: (HistoryStore.Entry) -> Unit,
    ) : RecyclerView.Adapter<Adapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position], onClick)
        override fun getItemCount() = items.size

        class VH(private val b: ItemHistoryBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(e: HistoryStore.Entry, onClick: (HistoryStore.Entry) -> Unit) {
                b.txtName.text = e.name ?: "Unbekannt"
                b.txtBrand.text = listOfNotNull(e.brand, e.barcode).joinToString(" · ")
                b.txtTime.text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(Date(e.scannedAt))
                val (color, label) = when (e.status) {
                    HalalStatus.HALAL -> 0xFF2EB872.toInt() to "HALAL"
                    HalalStatus.LIKELY_HALAL -> 0xFF5BA85B.toInt() to "vmtl. halal"
                    HalalStatus.MUSHBOOH -> 0xFFE6A23C.toInt() to "FRAGLICH"
                    HalalStatus.HARAM -> 0xFFE54B4B.toInt() to "HARAM"
                    HalalStatus.UNKNOWN -> 0xFF8A929E.toInt() to "?"
                }
                b.statusBadge.setBackgroundColor(color)
                b.statusLabel.text = label
                if (!e.imageUrl.isNullOrBlank()) {
                    b.thumb.visibility = View.VISIBLE
                    b.thumb.load(e.imageUrl)
                } else {
                    b.thumb.visibility = View.INVISIBLE
                }
                b.root.setOnClickListener { onClick(e) }
            }
        }
    }
}
