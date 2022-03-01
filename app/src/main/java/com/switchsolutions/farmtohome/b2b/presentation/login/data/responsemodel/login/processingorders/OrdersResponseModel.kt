package com.switchsolutions.farmtohome.b2b.presentation.login.data.responsemodel.login.processingorders

data class OrdersResponseModel(
    val `data`: List<Data>,
    val message: String,
    val status: String,
    val statusCode: Int
)