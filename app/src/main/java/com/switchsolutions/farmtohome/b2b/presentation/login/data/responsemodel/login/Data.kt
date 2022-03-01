package com.switchsolutions.farmtohome.b2b.presentation.login.data.responsemodel.login

data class Data(
    val address: String,
    val city: City,
    val deliveryCharges: String,
    val email: String,
    val id: Int,
    val info: Info,
    val isActive: Int,
    val msisdn: String,
    val name: String,
    val packingCharges: String,
    val profileImage: Any,
    val sector: Any,
    val type: Int,
    val verification: Int,
    val wallet: String,
    val xCoordinates: String,
    val yCoordinates: String
)