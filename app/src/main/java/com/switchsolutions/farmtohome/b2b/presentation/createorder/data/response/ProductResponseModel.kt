package com.switchsolutions.farmtohome.b2b.presentation.createorder.data.response

data class ProductResponseModel(
    val data: List<Data>,
    val message: String,
    val status: String,
    val statusCode: Int
)