package com.example.cryptoinfo

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("coins/markets")
    suspend fun getCoins(
        @Query("vs_currency") currency: String,
        @Query("per_page") perPage: Int,
    ): List<Coin>

    @GET("coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") id: String,
        @Query("vs_currency") vsCurrency: String,
        @Query("days") days: String
    ): MarketChartResponse
}