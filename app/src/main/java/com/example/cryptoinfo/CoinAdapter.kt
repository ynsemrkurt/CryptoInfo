package com.example.cryptoinfo

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cryptoinfo.databinding.ItemCoinBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.Locale

class CoinAdapter(
    private val viewModel: CoinViewModel,
    private val onCoinClick: (Coin, List<List<Float>>?) -> Unit
) : ListAdapter<Coin, CoinAdapter.CoinViewHolder>(CoinDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val binding = ItemCoinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoinViewHolder(binding, viewModel, onCoinClick)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = getItem(position)
        holder.bind(coin)
    }

    class CoinViewHolder(
        private val binding: ItemCoinBinding,
        private val viewModel: CoinViewModel,
        private val onCoinClick: (Coin, List<List<Float>>?) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var chart: List<List<Float>>? = null

        fun bind(coin: Coin) {
            val context = binding.root.context

            val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            val percentageChange = coin.priceChangePercentage24h
            val formattedChange = String.format(Locale.getDefault(), "%.2f", percentageChange)
            val colorResId = setColorBasedOnChange(percentageChange)

            binding.root.startAnimation(fadeIn)

            with(binding) {
                tvSymbol.text = coin.symbol
                tvPrice.text = context.getString(
                    R.string.dollar_format,
                    coin.currentPrice.toString()
                )
                tvPercent.text = context.getString(R.string.per_format, formattedChange)
                Glide.with(context).load(coin.image).into(ivCoin)
            }

            viewModel.chartData.observeForever { chartDataMap ->
                chart = chartDataMap[coin.id]?.prices?.filterIndexed { index, _ -> index % 10 == 0 }
                chart?.let {
                    showChart(it, colorResId)
                }
            }

            itemView.setOnClickListener {
                onCoinClick(coin, chart)
            }
        }

        private fun setColorBasedOnChange(percentageChange: Double): Int {
            val colorResId =
                if (percentageChange < 0) {
                    R.color.app_red
                } else if (percentageChange > 0) {
                    R.color.app_green
                } else {
                    R.color.app_gray
                }
            val color = ContextCompat.getColor(binding.root.context, colorResId)
            with(binding) {
                tvPercent.backgroundTintList = ColorStateList.valueOf(color)
                tvPrice.setTextColor(color)
            }
            return colorResId
        }

        private fun showChart(chartData: List<List<Float>>, @ColorRes chartColor: Int) {
            val lineChart: LineChart = binding.lineChart

            val entries = chartData.map { Entry(it[0], it[1]) }
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
                valueFormatter = IndexAxisValueFormatter(chartData.map { it[0].toString() })
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
                axisMinimum = chartData.minOfOrNull { it[0] } ?: 0f
                axisMaximum = chartData.maxOfOrNull { it[0] } ?: 0f
                spaceMin = 0f
                spaceMax = 0f
            }
            lineChart.axisLeft.apply {
                axisMinimum = chartData.minOfOrNull { it[1] } ?: 0f
                axisMaximum = chartData.maxOfOrNull { it[1] } ?: 0f
                spaceMin = 0f
                spaceMax = 0f
            }

            lineChart.legend.isEnabled = false
            lineChart.description.isEnabled = false

            lineChart.invalidate()
        }
    }
}

class CoinDiffCallback : DiffUtil.ItemCallback<Coin>() {
    override fun areItemsTheSame(oldItem: Coin, newItem: Coin): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Coin, newItem: Coin): Boolean {
        return oldItem == newItem
    }
}