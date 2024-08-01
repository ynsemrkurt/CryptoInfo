package com.example.cryptoinfo.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoinfo.data.model.Coin
import com.example.cryptoinfo.data.model.MarketChartResponse
import com.example.cryptoinfo.data.network.RetrofitInstance
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
                val charts = mutableMapOf<String, MarketChartResponse>()

                coinList.forEach { coin ->
                    fetchMarketChart(coin, charts)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun fetchMarketChart(coin: Coin, charts: MutableMap<String, MarketChartResponse>) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis() / 1000
                val oneDayAgo = now - TimeUnit.DAYS.toSeconds(1)

                val response = RetrofitInstance.coinApiService.getMarketChart(
                    coin.id,
                    "usd",
                    oneDayAgo.toInt(),
                    now.toInt()
                )
                val chartDataList = response.prices.filterIndexed { index, _ -> index % 10 == 0 }
                charts[coin.id] = MarketChartResponse(chartDataList)
                _chartData.postValue(charts)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}