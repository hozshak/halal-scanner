package com.halal.scanner.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.halal.scanner.databinding.FragmentWatchlistBinding
import com.halal.scanner.db.BookmarkStore

class WatchlistFragment : Fragment() {
    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!
    private lateinit var store: BookmarkStore
    private lateinit var adapter: ProductRowAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWatchlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        store = BookmarkStore(requireContext())
        adapter = ProductRowAdapter(emptyList()) { item ->
            startActivity(
                Intent(requireContext(), ResultActivity::class.java)
                    .putExtra(ResultActivity.EXTRA_BARCODE, item.barcode)
            )
        }
        binding.watchlistList.layoutManager = LinearLayoutManager(requireContext())
        binding.watchlistList.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val entries = store.list()
        if (entries.isEmpty()) {
            binding.watchlistEmpty.visibility = View.VISIBLE
            binding.watchlistList.visibility = View.GONE
        } else {
            binding.watchlistEmpty.visibility = View.GONE
            binding.watchlistList.visibility = View.VISIBLE
            adapter.replace(entries.map {
                ProductRowAdapter.Item(
                    barcode = it.barcode,
                    name = it.name,
                    brand = it.brand,
                    imageUrl = it.imageUrl,
                    status = it.status,
                )
            })
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
