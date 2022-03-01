package com.switchsolutions.farmtohome.b2b.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectionDetector {

    private val _context: Context

    constructor (context: Context) {
        this._context = context
    }

    /**
     * Checking for all possible internet providers
     */
    fun isConnectingToInternet(): Boolean {
        val connectivity = _context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info = connectivity.allNetworkInfo
            info?.indices?.filter { info[it].state == NetworkInfo.State.CONNECTED }?.forEach { return true }
        }
        return false
    }
}