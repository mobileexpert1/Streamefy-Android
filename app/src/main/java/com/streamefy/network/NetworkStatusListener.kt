package com.streamefy.network

interface NetworkStatusListener {
    fun onNetworkStatusChanged(isAvailable: Boolean)
}