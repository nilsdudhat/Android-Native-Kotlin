package com.hilt.app

interface CryptocurrencyRepository {
    fun getCryptoCurrency(): List<Cryptocurrency>
}