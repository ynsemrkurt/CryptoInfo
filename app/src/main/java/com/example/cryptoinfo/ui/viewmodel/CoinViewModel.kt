package com.example.cryptoinfo.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoinfo.data.model.Coin
import com.example.cryptoinfo.data.model.MarketChartResponse
import com.example.cryptoinfo.data.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class CoinViewModel : ViewModel() {

    private val _coins = MutableLiveData<List<Coin>>()
    val coins: LiveData<List<Coin>> get() = _coins

    private val _chartData = MutableLiveData<Map<String, MarketChartResponse>>()
    val chartData: LiveData<Map<String, MarketChartResponse>> get() = _chartData

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
                val charts = mutableMapOf<String, MarketChartResponse>()
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
                        listOf(it[0], it[1])
                    }
                    charts[coinId] = MarketChartResponse(chartDataList)
                }
                _chartData.postValue(charts)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}