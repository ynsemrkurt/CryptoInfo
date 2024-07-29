package com.example.cryptoinfo.data.network

import com.example.cryptoinfo.data.model.Coin
import com.example.cryptoinfo.data.model.MarketChartResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("coins/markets")
    suspend fun getCoins(
        @Query("vs_currency") currency: String,
        @Query("per_page") perPage: Int,
    ): List<Coin>

    @GET("coins/{id}/market_chart/range")
    suspend fun getMarketChart(
        @Path("id") id: String,
        @Query("vs_currency") vsCurrency: String,
        @Query("from") from: Int,
        @Query("to") to: Int
    ): MarketChartResponse
}