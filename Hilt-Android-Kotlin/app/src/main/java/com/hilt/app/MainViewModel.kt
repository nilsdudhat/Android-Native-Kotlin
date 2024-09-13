package com.hilt.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// @HiltViewModel will make models to be
// created using Hilt's model factory
// @Inject annotation used to inject all
// dependencies to view model class
@HiltViewModel
class MainViewModel @Inject constructor(
    private val cryptocurrencyRepository: CryptocurrencyRepository
) : ViewModel() {
    private val cryptocurrencyEmitter = MutableLiveData<List<Cryptocurrency>>()
    val cryptoCurrency: LiveData<List<Cryptocurrency>> = cryptocurrencyEmitter
    init {
        loadCryptocurrency()
    }

    // getting cryptocurrencies list using
    // repository and passing it into live data
    private fun loadCryptocurrency() {
        cryptocurrencyEmitter.value = cryptocurrencyRepository.getCryptoCurrency()
    }
}