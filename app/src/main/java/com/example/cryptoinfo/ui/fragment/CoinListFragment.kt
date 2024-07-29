package com.example.cryptoinfo.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.cryptoinfo.R
import com.example.cryptoinfo.data.model.Coin
import com.example.cryptoinfo.data.model.MarketChartResponse
import com.example.cryptoinfo.databinding.FragmentCoinListBinding
import com.example.cryptoinfo.ui.viewmodel.CoinViewModel
import com.example.cryptoinfo.ui.adapter.CoinAdapter

class CoinListFragment : Fragment() {

    private val viewModel: CoinViewModel by viewModels()
    private lateinit var binding: FragmentCoinListBinding
    private lateinit var adapter: CoinAdapter
    private var coinList = listOf<Coin>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCoinListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeCoins()
        observeError()

        binding.etSearch.addTextChangedListener { text ->
            filterCoins(text?.trim().toString())
        }
    }

    private fun setupRecyclerView() {
        adapter = CoinAdapter(viewModel) { coin, chart ->
            val marketChartResponse = MarketChartResponse(chart ?: emptyList())
            val action =
                CoinListFragmentDirections.goToCoinDetailFragment(coin, marketChartResponse)
            findNavController().navigate(
                action,
                NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build()
            )
        }
        binding.rvCoins.adapter = adapter
    }

    private fun observeCoins() {
        viewModel.coins.observe(viewLifecycleOwner) { coins ->
            coinList = coins
            adapter.submitList(coins)
        }
    }

    private fun observeError() {
        viewModel.error.observe(viewLifecycleOwner) { error ->
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.error))
                .setMessage(getString(R.string.unexpected_error, error))
                .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun filterCoins(query: String) {
        val filteredList = if (query.isEmpty()) {
            coinList
        } else {
            coinList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.symbol.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filteredList)
    }
}