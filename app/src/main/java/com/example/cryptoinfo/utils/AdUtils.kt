package com.example.cryptoinfo.utils

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdUtils {
    fun loadAds(context: Context, view: AdView) {
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            MobileAds.initialize(context)
        }
        val adRequest = AdRequest.Builder().build()
        view.loadAd(adRequest)
    }
}