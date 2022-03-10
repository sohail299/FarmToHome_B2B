package com.switchsolutions.farmtohome.b2b.presentation.history.data.response

data class Product(
    val imgUrl: String,
    val is_removed: Int,
    val label: String,
    val quantity: Int,
    val unit: String,
    val value: Int
)