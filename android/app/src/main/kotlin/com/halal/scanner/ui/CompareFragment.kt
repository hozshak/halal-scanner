package com.halal.scanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.halal.scanner.R
import com.halal.scanner.databinding.FragmentCompareBinding
import com.halal.scanner.db.BookmarkStore

class CompareFragment : Fragment() {
    private var _binding: FragmentCompareBinding? = null
    private val binding get() = _binding!!
    private lateinit var store: BookmarkStore
    private lateinit var adapter: ProductRowAdapter

    private val selectedBarcodes = mutableSetOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCompareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        store = BookmarkStore(requireContext())

        adapter = ProductRowAdapter(emptyList()) { item ->
            if (selectedBarcodes.contains(item.barcode)) {
                selectedBarcodes.remove(item.barcode)
            } else {
                if (selectedBarcodes.size >= 2) {
                    Toast.makeText(requireContext(), R.string.compare_max_2, Toast.LENGTH_SHORT).show()
                    return@ProductRowAdapter
                }
                selectedBarcodes.add(item.barcode)
            }
            refresh()
        }
        binding.compareList.layoutManager = LinearLayoutManager(requireContext())
        binding.compareList.adapter = adapter

        binding.btnDoCompare.setOnClickListener {
            Toast.makeText(requireContext(), R.string.compare_coming_soon, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val entries = store.list()
        binding.txtCompareCounter.text = selectedBarcodes.size.toString()
        binding.btnDoCompare.isEnabled = selectedBarcodes.size == 2

        if (entries.isEmpty()) {
            binding.compareEmpty.visibility = View.VISIBLE
            binding.compareList.visibility = View.GONE
            binding.compareListTitle.visibility = View.GONE
        } else {
            binding.compareEmpty.visibility = View.GONE
            binding.compareList.visibility = View.VISIBLE
            binding.compareListTitle.visibility = View.VISIBLE
            adapter.replace(entries.map {
                ProductRowAdapter.Item(
                    barcode = it.barcode,
                    name = it.name,
                    brand = it.brand,
                    imageUrl = it.imageUrl,
                    status = it.status,
                    selected = selectedBarcodes.contains(it.barcode),
                )
            })
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
