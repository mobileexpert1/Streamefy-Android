package com.streamefy.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast

class NetworkReceiver(private val listener: NetworkStatusListener) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (isNetworkAvailable(context)) {
            listener.onNetworkAvailable()
        } else {
            listener.onNetworkUnavailable()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    interface NetworkStatusListener {
        fun onNetworkAvailable()
        fun onNetworkUnavailable()
    }
}