package com.switchsolutions.farmtohome.b2b.presentation.login.data.responsemodel.login

data class LoginResponse(
    val accessToken: String,
    val data: Data,
    val expiresIn: Int,
    val message: String,
    val status: String,
    val statusCode: Int,
    val tokenType: String
)