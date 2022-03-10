package com.switchsolutions.farmtohome.b2b.interfaces

import com.switchsolutions.farmtohome.b2b.presentation.history.data.response.Data

interface ShowOrderDetail {
    fun showOrderDetails(reqId: Int )
    fun startObserverForSingleOrder()
    fun showOrderHistory(data: Data)
}