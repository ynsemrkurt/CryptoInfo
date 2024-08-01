package com.example.cryptoinfo.ui.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.cryptoinfo.R
import com.example.cryptoinfo.data.model.Coin
import com.example.cryptoinfo.databinding.FragmentCoinDetailBinding
import com.example.cryptoinfo.utils.AdUtils
import com.example.cryptoinfo.utils.ColorUtils
import com.example.cryptoinfo.utils.getColorFromTheme
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
    private var colorResId: Int = 0

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
        colorResId =
            ColorUtils.getColorBasedOnChange(coin.priceChangePercentage24h, requireContext())
        ColorUtils.applyColorToTextViews(colorResId, binding.tvPercent, binding.tvPrice)

        AdUtils.loadAds(requireContext(), binding.adView)
        setViews(coin)
        showChart(marketChartResponse.prices, colorResId)

        binding.ivGoDetail.setOnClickListener {
            findNavController().popBackStack()
        }

        loadImage(coin.image)
    }

    private fun showChart(chartData: List<List<Float>>, @ColorRes chartColor: Int) {
        val lineChart: LineChart = binding.lineChart

        val entries = chartData.map { Entry(it[0], it[1]) }
        val dataSet = LineDataSet(entries, null)
        val textColor = android.R.attr.textColor.getColorFromTheme(requireContext())

        dataSet.color = chartColor
        dataSet.setFillColor(chartColor)
        dataSet.fillAlpha = 100

        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
        dataSet.setDrawFilled(true)

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.apply {
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                    ).format(Date(value.toLong() * 1000))
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

        lineChart.animateX(1000)

        lineChart.invalidate()
    }

    private fun setViews(coin: Coin) = with(binding) {
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

    private fun loadImage(url: String) {
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
                        onImageLoaded(resource, dominantColor)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Not used
                }
            })
    }

    private fun onImageLoaded(resource: Drawable, dominantColor: Int) {
        applyGradientToTextView(binding.tvSymbol, dominantColor, colorResId)
        binding.tvName.backgroundTintList = ColorStateList.valueOf(dominantColor)
        binding.ivSymbol.setImageDrawable(resource)
    }


    private fun applyGradientToTextView(textView: TextView, startColor: Int, endColor: Int) {
        val shader = LinearGradient(
            0f, 0f, textView.width.toFloat(), textView.textSize,
            intArrayOf(startColor, endColor),
            null,
            Shader.TileMode.CLAMP
        )
        textView.paint.shader = shader
        textView.invalidate()
    }
}