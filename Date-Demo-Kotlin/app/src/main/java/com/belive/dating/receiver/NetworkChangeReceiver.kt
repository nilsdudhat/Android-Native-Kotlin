package com.belive.dating.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.InetSocketAddress
import java.net.Socket

interface NetworkChangeCallBack {
    fun isConnected(
        isConnected: Boolean,
        isPoorSignal: Boolean,
    )
}

class NetworkChangeReceiver() : BroadcastReceiver() {

    private var networkChangeCallBack: NetworkChangeCallBack? = null

    constructor(networkChangeCallBack: NetworkChangeCallBack) : this() {
        this.networkChangeCallBack = networkChangeCallBack
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager?.let {
            val network = it.activeNetwork ?: return false
            val capabilities = it.getNetworkCapabilities(network) ?: return false
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
                )
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Performs an actual internet reachability check by connecting to a reliable external server.
     */
    private fun isInternetReachable(): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53), 1500) // Google DNS server
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if ("android.net.conn.CONNECTIVITY_CHANGE" == intent.action) {
            networkChangeCallBack?.isConnected(isInternetAvailable(context), isInternetReachable())
        }
    }
}