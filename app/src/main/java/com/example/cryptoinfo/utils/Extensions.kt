package com.example.cryptoinfo.utils

import android.content.Context
import android.util.TypedValue

fun Int.getColorFromTheme(context: Context): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(this, typedValue, true)
    return typedValue.data
}