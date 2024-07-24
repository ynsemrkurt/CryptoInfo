package com.example.cryptoinfo

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cryptoinfo.databinding.ItemCoinBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class CoinAdapter(
    private var coins: List<Coin>,
    private val viewModel: CoinViewModel
) : RecyclerView.Adapter<CoinAdapter.CoinViewHolder>() {

    init {
        viewModel.chartData.observeForever {
            notifyDataSetChanged()
        }
    }

    fun updateCoins(newCoins: List<Coin>) {
        coins = newCoins
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val binding = ItemCoinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoinViewHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = coins[position]
        holder.bind(coin)
    }

    override fun getItemCount() = coins.size

    class CoinViewHolder(
        private val binding: ItemCoinBinding,
        private val viewModel: CoinViewModel
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(coin: Coin) {
            val percentageChange = coin.priceChangePercentage24h
            val formattedChange = String.format("%.2f", percentageChange)
            val colorResId = setColorBasedOnChange(percentageChange)
            val chart = viewModel.chartData.value?.get(coin.id)

            with(binding) {
                tvSymbol.text = coin.symbol
                tvPrice.text = "$${coin.currentPrice}"
                tvPercent.text = root.context.getString(R.string.per_format, formattedChange)
                Glide.with(root.context).load(coin.image).into(ivCoin)
            }
            chart?.let {
                showChart(it, colorResId)
            }
        }

        private fun setColorBasedOnChange(percentageChange: Double): Int {
            val colorResId =
                if (percentageChange < 0) {
                    R.color.app_red
                } else {
                    R.color.app_green
                }
            val color = ContextCompat.getColor(binding.root.context, colorResId)
            with(binding) {
                tvPercent.backgroundTintList = ColorStateList.valueOf(color)
                tvPrice.setTextColor(color)
            }
            return colorResId
        }

        private fun showChart(chartData: List<Pair<Float, Float>>, @ColorRes chartColor: Int) {
            val lineChart: LineChart = binding.lineChart

            val filteredData = chartData.filterIndexed { index, _ -> index % 10 == 0 }

            val entries = filteredData.map { Entry(it.first, it.second) }
            val dataSet = LineDataSet(entries, null)

            dataSet.color = ContextCompat.getColor(binding.root.context, chartColor)
            dataSet.setFillColor(ContextCompat.getColor(binding.root.context, chartColor))
            dataSet.fillAlpha = 100

            dataSet.setDrawValues(false)
            dataSet.setDrawCircles(false)
            dataSet.setDrawFilled(true)

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            lineChart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(filteredData.map { it.first.toString() })
                granularity = 1f
                setDrawLabels(false)
                setDrawGridLines(false)
                setDrawAxisLine(false)
            }

            lineChart.axisRight.isEnabled = false
            lineChart.axisLeft.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
                setDrawAxisLine(false)
            }

            lineChart.setPadding(0, 0, 0, 0)
            lineChart.setDrawBorders(false)
            lineChart.setTouchEnabled(false)

            lineChart.xAxis.apply {
                axisMinimum = filteredData.minOfOrNull { it.first } ?: 0f
                axisMaximum = filteredData.maxOfOrNull { it.first } ?: 0f
                spaceMin = 0f
                spaceMax = 0f
            }
            lineChart.axisLeft.apply {
                axisMinimum = filteredData.minOfOrNull { it.second } ?: 0f
                axisMaximum = filteredData.maxOfOrNull { it.second } ?: 0f
                spaceMin = 0f
                spaceMax = 0f
            }

            lineChart.legend.isEnabled = false
            lineChart.description.isEnabled = false

            lineChart.invalidate()
        }
    }
}
