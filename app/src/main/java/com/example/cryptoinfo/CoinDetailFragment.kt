package com.example.cryptoinfo

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.cryptoinfo.databinding.FragmentCoinDetailBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CoinDetailFragment : Fragment() {

    private lateinit var binding: FragmentCoinDetailBinding
    private val args: CoinDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCoinDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val coin = args.coin
        val marketChartResponse = args.marketChartResponse
        val colorResId = setColorBasedOnChange(coin.priceChangePercentage24h)

        setViews(coin)
        showChart(marketChartResponse.prices, colorResId)

        binding.ivGoDetail.setOnClickListener {
            findNavController().popBackStack()
        }

        loadImageAndSetColor(coin.image)
    }

    private fun showChart(chartData: List<List<Float>>, @ColorRes chartColor: Int) {
        val lineChart: LineChart = binding.lineChart

        val entries = chartData.map { Entry(it[0], it[1]) }
        val dataSet = LineDataSet(entries, null)
        val textColor = android.R.attr.textColor.getColorFromTheme()

        val chartColorValue = ContextCompat.getColor(binding.root.context, chartColor)
        dataSet.color = chartColorValue
        dataSet.setFillColor(chartColorValue)
        dataSet.fillAlpha = 100

        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
        dataSet.setDrawFilled(true)

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.apply {
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(value.toLong() * 1000))
                }
            }
            granularity = 3600f
            labelRotationAngle = -45f
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setTextColor(textColor)
        }

        lineChart.axisLeft.apply {
            setTextColor(textColor)
        }

        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false

        // Add animation
        lineChart.animateX(1000)

        lineChart.invalidate()
    }

    // Extension function to get color from theme
    private fun Int.getColorFromTheme(): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(this, typedValue, true)
        return typedValue.data
    }


    private fun setColorBasedOnChange(percentageChange: Double): Int {
        val colorResId = when {
            percentageChange < 0 -> R.color.app_red
            percentageChange > 0 -> R.color.app_green
            else -> R.color.app_gray
        }
        val color = ContextCompat.getColor(requireContext(), colorResId)
        binding.tvPercent.backgroundTintList = ColorStateList.valueOf(color)
        binding.tvPrice.setTextColor(color)
        return colorResId
    }

    private fun setViews(coin: Coin) {
        with(binding) {
            tvSymbol.text = getString(R.string.coin_usd, coin.symbol)
            tvPrice.text = getString(R.string.dollar_format, coin.currentPrice.toString())
            tvName.text = coin.name
            tvHighPrice.text = getString(R.string.high_24h_format, coin.high24h.toString())
            tvLowPrice.text = getString(R.string.low_24h_format, coin.low24h.toString())
            tvCirculatingSupply.text =
                getString(R.string.circ_supply_format, coin.circulatingSupply.toString())
            tvTotalVolume.text = getString(R.string.tot_volume_format, coin.totalVolume.toString())
            tvMarketCap.text = getString(R.string.market_cap_format, coin.marketCap.toString())
            tvMarketCapRank.text =
                getString(R.string.market_cap_rank_format, coin.marketCapRank.toString())
            tvPercent.text =
                getString(R.string.per_format, coin.priceChangePercentage24h.toString())
        }
    }

    private fun loadImageAndSetColor(url: String) {
        Glide.with(this)
            .load(url)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    val bitmap = resource.toBitmap()
                    Palette.from(bitmap).generate { palette ->
                        val dominantColor = palette?.getDominantColor(Color.BLACK) ?: Color.BLACK
                        binding.tvName.backgroundTintList = ColorStateList.valueOf(dominantColor)
                    }
                    binding.ivSymbol.setImageDrawable(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // TODO
                }
            })
    }
}