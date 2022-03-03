package com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response

data class OrdersResponseModel(
    val `data`: List<Data>,
    val message: String,
    val status: String,
    val statusCode: Int
)