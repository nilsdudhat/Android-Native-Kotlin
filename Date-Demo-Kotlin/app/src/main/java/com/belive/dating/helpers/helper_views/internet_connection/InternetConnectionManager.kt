package com.belive.dating.helpers.helper_views.internet_connection

import android.util.Log
import com.google.android.material.snackbar.Snackbar

class InternetConnectionManager(
    private val noInternetSnackBar: Snackbar,
    private val poorInternetSnackBar: Snackbar,
    private val internetRestoredSnackBar: Snackbar,
    private val onInternetAvailableForFirstTime: () -> Unit,
    private val onInternetConfigurationChanged: (Boolean) -> Unit,
) {

    private var isInternetAvailable: Boolean = true
    private var isPoorInternet: Boolean = false
    private var isFirstTimeInternetCallbackReceived: Boolean = true

    fun onConnectionStatusChanged(isConnected: Boolean, isPoorSignal: Boolean) {
        Log.d("InternetConnectionManager", "onConnectionStatusChanged: isConnected=$isConnected, isPoorSignal=$isPoorSignal")
        when {
            !isConnected -> handleNoInternet()
            isPoorSignal -> handlePoorInternet()
            else -> handleGoodInternet()
        }
    }

    private fun handleNoInternet() {
        Log.d("InternetConnectionManager", "handleNoInternet")
        if (isInternetAvailable) {
            isInternetAvailable = false
            noInternetSnackBar.show()
            onInternetConfigurationChanged(false)
        }
    }

    private fun handlePoorInternet() {
        Log.d("InternetConnectionManager", "handlePoorInternet")
        if (!isPoorInternet) {
            isPoorInternet = true
            poorInternetSnackBar.show()
            onInternetConfigurationChanged(true)
        }
    }

    private fun handleGoodInternet() {
        Log.d("InternetConnectionManager", "handleGoodInternet")
        if (isPoorInternet) {
            isPoorInternet = false
            poorInternetSnackBar.dismiss()
        }

        if (!isInternetAvailable) {
            isInternetAvailable = true
            noInternetSnackBar.dismiss()
            internetRestoredSnackBar.show()
        }

        if (isFirstTimeInternetCallbackReceived) {
            isFirstTimeInternetCallbackReceived = false
            onInternetAvailableForFirstTime()
        } else {
            onInternetConfigurationChanged(true)
        }
    }
}