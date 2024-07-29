package com.example.cryptoinfo.utils

import android.content.Context
import android.content.res.ColorStateList
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.cryptoinfo.R

object ColorUtils {
    fun setColorBasedOnChange(
        percentageChange: Double,
        context: Context,
        tvPercent: TextView,
        tvPrice: TextView
    ): Int {
        val colorResId = when {
            percentageChange < 0 -> R.color.app_red
            percentageChange > 0 -> R.color.app_green
            else -> R.color.app_gray
        }
        val color = ContextCompat.getColor(context, colorResId)
        tvPercent.backgroundTintList = ColorStateList.valueOf(color)
        tvPrice.setTextColor(color)
        return colorResId
    }
}