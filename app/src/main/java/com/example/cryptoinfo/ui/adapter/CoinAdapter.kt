package com.example.cryptoinfo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cryptoinfo.R
import com.example.cryptoinfo.data.model.Coin
import com.example.cryptoinfo.databinding.ItemCoinBinding
import com.example.cryptoinfo.utils.ColorUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.Locale

class CoinAdapter(
    private val onCoinClick: (Coin, List<List<Float>>?) -> Unit
) : ListAdapter<Coin, CoinAdapter.CoinViewHolder>(CoinDiffCallback()) {

    private var chartDataMap: Map<String, List<List<Float>>> = emptyMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val binding = ItemCoinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoinViewHolder(binding, onCoinClick)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = getItem(position)
        holder.bind(coin, chartDataMap[coin.id])
    }

    fun setChartData(chartData: Map<String, List<List<Float>>>) {
        chartDataMap = chartData
        notifyItemRangeChanged(0, itemCount)
    }

    class CoinViewHolder(
        private val binding: ItemCoinBinding,
        private val onCoinClick: (Coin, List<List<Float>>?) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val context = binding.root.context

        fun bind(coin: Coin, chartData: List<List<Float>>?) {
            val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            binding.root.startAnimation(fadeIn)

            val percentageChange = coin.priceChangePercentage24h
            val formattedChange = String.format(Locale.getDefault(), "%.2f", percentageChange)
            val colorResId =
                ColorUtils.getColorBasedOnChange(coin.priceChangePercentage24h, context)
            ColorUtils.applyColorToTextViews(colorResId, binding.tvPercent, binding.tvPrice)

            setCoinInfo(coin, formattedChange)
            chartData?.let {
                showChart(it, colorResId)
            }

            itemView.setOnClickListener {
                onCoinClick(coin, chartData)
            }
        }

        private fun setCoinInfo(coin: Coin, formattedChange: String) {
            with(binding) {
                tvSymbol.text = coin.symbol
                tvPrice.text =
                    context.getString(R.string.dollar_format, coin.currentPrice.toString())
                tvPercent.text = context.getString(R.string.per_format, formattedChange)
                Glide.with(context).load(coin.image).into(ivCoin)
            }
        }

        private fun showChart(chartData: List<List<Float>>, @ColorRes chartColor: Int) {
            val lineChart: LineChart = binding.lineChart
            val entries = chartData.map { Entry(it[0], it[1]) }
            val dataSet = LineDataSet(entries, null).apply {
                color = chartColor
                setFillColor(chartColor)
                fillAlpha = 100
                setDrawValues(false)
                setDrawCircles(false)
                setDrawFilled(true)
            }

            lineChart.data = LineData(dataSet)
            configureChartAppearance(lineChart, chartData)
        }

        private fun configureChartAppearance(lineChart: LineChart, chartData: List<List<Float>>) {
            lineChart.apply {
                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(chartData.map { it[0].toString() })
                    granularity = 1f
                    setDrawLabels(false)
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                }
                axisRight.isEnabled = false
                axisLeft.apply {
                    setDrawLabels(false)
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                }
                setPadding(0, 0, 0, 0)
                setDrawBorders(false)
                setTouchEnabled(false)
                xAxis.apply {
                    axisMinimum = chartData.minOfOrNull { it[0] } ?: 0f
                    axisMaximum = chartData.maxOfOrNull { it[0] } ?: 0f
                    spaceMin = 0f
                    spaceMax = 0f
                }
                axisLeft.apply {
                    axisMinimum = chartData.minOfOrNull { it[1] } ?: 0f
                    axisMaximum = chartData.maxOfOrNull { it[1] } ?: 0f
                    spaceMin = 0f
                    spaceMax = 0f
                }
                legend.isEnabled = false
                description.isEnabled = false
                invalidate()
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
}