package com.example.cryptoinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class CoinViewModel : ViewModel() {

    private val _coins = MutableLiveData<List<Coin>>()
    val coins: LiveData<List<Coin>> get() = _coins

    private val _chartData = MutableLiveData<Map<String, List<Pair<Float, Float>>>>()
    val chartData: LiveData<Map<String, List<Pair<Float, Float>>>> get() = _chartData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        fetchCoins()
    }

    private fun fetchCoins() {
        viewModelScope.launch {
            try {
                val coinList = RetrofitInstance.coinApiService.getCoins("usd", 5)
                _coins.postValue(coinList)
                fetchAndDisplayAllCharts(coinList.map { it.id })
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun fetchAndDisplayAllCharts(coinIds: List<String>) {
        viewModelScope.launch {
            try {
                val charts = mutableMapOf<String, List<Pair<Float, Float>>>()
                val now = System.currentTimeMillis() / 1000
                val oneDayAgo = now - TimeUnit.DAYS.toSeconds(1)

                for (coinId in coinIds) {
                    val response = RetrofitInstance.coinApiService.getMarketChart(
                        coinId,
                        "usd",
                        oneDayAgo.toInt(),
                        now.toInt()
                    )
                    val chartDataList = response.prices.map {
                        Pair(it[0].toFloat(), it[1].toFloat())
                    }
                    charts[coinId] = chartDataList
                }
                _chartData.postValue(charts)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}