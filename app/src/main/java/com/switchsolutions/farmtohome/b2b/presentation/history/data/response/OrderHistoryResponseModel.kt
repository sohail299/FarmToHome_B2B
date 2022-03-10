package com.switchsolutions.farmtohome.b2b.presentation.history.data.response

data class OrderHistoryResponseModel(
    val `data`: List<Data>,
    val message: String,
    val status: String,
    val statusCode: Int
)