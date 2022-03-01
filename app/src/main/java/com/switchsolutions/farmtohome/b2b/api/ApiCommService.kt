package com.switchsolutions.farmtohome.b2b.api


import com.google.gson.JsonObject
import com.switchsolutions.farmtohome.b2b.presentation.login.data.requestmodel.LoginRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.requestmodel.OrderRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.responsemodel.login.LoginResponse
import com.switchsolutions.farmtohome.b2b.presentation.login.data.responsemodel.login.processingorders.OrdersResponseModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface ApiCommService {

    //Sign in api
    @POST("auth/signin")
    fun signInCall(@Body loginRequest: LoginRequestModel): Call<LoginResponse>

    @POST("btobcustomer/processingrequests")
    fun getProcessingOrders(@Body order: OrderRequestModel): Call<OrdersResponseModel>
}