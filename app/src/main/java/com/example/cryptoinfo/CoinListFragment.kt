package com.example.cryptoinfo

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.cryptoinfo.databinding.FragmentCoinListBinding

class CoinListFragment : Fragment() {

    private val viewModel: CoinViewModel by viewModels()
    private lateinit var binding: FragmentCoinListBinding
    private lateinit var adapter: CoinAdapter

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
    }

    private fun setupRecyclerView() {
        adapter = CoinAdapter(emptyList(), viewModel) { coin, chart ->
            val marketChartResponse = MarketChartResponse(chart ?: emptyList())
            val action = CoinListFragmentDirections.goToCoinDetailFragment(coin, marketChartResponse)
            findNavController().navigate(action)
        }
        binding.rvCoins.adapter = adapter
    }

    private fun observeCoins() {
        viewModel.coins.observe(viewLifecycleOwner) { coins ->
            adapter.updateCoins(coins)
            viewModel.fetchAndDisplayAllCharts(coins.map { it.id })
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
}