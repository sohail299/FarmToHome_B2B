package com.switchsolutions.farmtohome.b2b.presentation.history.data.response

data class Data(
    val comments: String,
    val delivered_at: Any,
    val delivery_date: String,
    val id: Int,
    val products: List<Product>,
    val rejected_comments: Any,
    val status: Int
)