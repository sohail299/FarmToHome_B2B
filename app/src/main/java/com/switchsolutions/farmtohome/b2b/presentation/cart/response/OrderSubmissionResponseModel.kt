package com.switchsolutions.farmtohome.b2b.presentation.cart.response

data class OrderSubmissionResponseModel(
    val created_request_id: Int,
    val message: String,
    val status: String,
    val statusCode: Int
)