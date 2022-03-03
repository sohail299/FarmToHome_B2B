package com.switchsolutions.farmtohome.b2b.api


import com.google.gson.JsonObject
import com.switchsolutions.farmtohome.b2b.presentation.cart.response.OrderSubmissionResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.createorder.data.response.ProductResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.request.LoginRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.request.PasswordResetRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.request.OrderRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.OrdersResponseModel
import retrofit2.Call
import retrofit2.http.*

interface ApiCommService {
    //Sign in api
    @POST("auth/signin")
    fun signInCall(@Body loginRequest: LoginRequestModel): Call<LoginResponse>

    @POST("auth/app/password/reset")
    fun passwordReset(@Body resetPassword: PasswordResetRequestModel): Call<LoginResponse>

    @POST("user/verify")
    fun verifyUser(@Query("") resetPassword: PasswordResetRequestModel): Call<LoginResponse>

    @POST("auth/app/password/change")
    fun changePassword(@Body resetPassword: JsonObject): Call<LoginResponse>

    @POST("btobcustomer/processingrequests")
    fun getProcessingOrders(@Body order: OrderRequestModel): Call<OrdersResponseModel>

    @POST("btobcustomer/createrequest")
    fun createOrderRequest(@Body order: JsonObject): Call<OrderSubmissionResponseModel>

    @GET("names/products/cityproductsofcustomer")
    fun getProducts(@Query("cityId") cityId : Int,
                    @Query("customer_id") customer_id : Int): Call<ProductResponseModel>
}