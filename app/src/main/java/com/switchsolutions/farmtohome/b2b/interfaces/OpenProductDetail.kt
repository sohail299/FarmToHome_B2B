package com.switchsolutions.farmtohome.b2b.interfaces

import com.switchsolutions.farmtohome.b2b.presentation.createorder.data.response.Data

interface OpenProductDetail {
    fun showProductDetail(data: Data)
}