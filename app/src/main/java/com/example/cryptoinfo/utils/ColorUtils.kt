package com.example.cryptoinfo.utils

import android.content.Context
import android.content.res.ColorStateList
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.cryptoinfo.R

object ColorUtils {

    fun getColorBasedOnChange(percentageChange: Double, context: Context): Int {
        return when {
            percentageChange < 0 -> R.color.app_red
            percentageChange > 0 -> R.color.app_green
            else -> R.color.app_gray
        }.let { ContextCompat.getColor(context, it) }
    }

    fun applyColorToTextViews(
        color: Int,
        tvPercent: TextView,
        tvPrice: TextView
    ) {
        tvPercent.backgroundTintList = ColorStateList.valueOf(color)
        tvPrice.setTextColor(color)
    }
}