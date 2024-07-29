package com.example.cryptoinfo.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://api.coingecko.com/api/v3/"
    private const val API_KEY = "CG-4m5NyFPDGM3665dSMGadbJFV"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("accept", "application/json")
                .addHeader("x-cg-demo-api-key", API_KEY)
                .build()
            chain.proceed(request)
        }
        .build()

    val coinApiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}