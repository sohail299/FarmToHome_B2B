package com.switchsolutions.farmtohome.b2b.api


import com.google.gson.JsonObject
import com.switchsolutions.farmtohome.b2b.presentation.cart.response.OrderSubmissionResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.createorder.data.response.ProductResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.request.LoginRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.request.PasswordResetRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.request.OrderRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.OrdersResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.singleorder.GetSingleOrderResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.history.data.response.OrderHistoryResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.profile.data.response.PasswordResetResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiCommService {
    //Sign in api
    @POST("auth/signin")
    fun signInCall(@Body loginRequest: LoginRequestModel): Call<LoginResponse>

    //Password reset
    @POST("auth/app/password/reset")
    fun passwordReset(@Body resetPassword: PasswordResetRequestModel): Call<LoginResponse>

    //verify user
    @POST("user/verify")
    fun verifyUser(@Query("") resetPassword: PasswordResetRequestModel): Call<LoginResponse>

    //change/update password
    @POST("auth/app/password/change")
    fun changePassword(@Body resetPassword: JsonObject): Call<LoginResponse>

    //change password
    @POST("user/changepassword")
    fun resetPassword(@Body resetPassword: JsonObject): Call<PasswordResetResponse>


    @POST("btobcustomer/processingrequests")
    fun getProcessingOrders(@Body order: OrderRequestModel): Call<OrdersResponseModel>

    @POST("btobcustomer/previousrequests")
    fun getPreviousOrders(@Body order: OrderRequestModel): Call<OrderHistoryResponseModel>

    @POST("btobcustomer/createrequest")
    fun createOrderRequest(@Body order: JsonObject): Call<OrderSubmissionResponseModel>

    @GET("names/products/cityproductsofcustomer")
    fun getProducts(@Query("cityId") cityId : Int,
                    @Query("customer_id") customer_id : Int): Call<ProductResponseModel>

    @GET("btobrequestdetails")
    fun getSingleOrder(@Query ("request_id")reqId: Int): Call<GetSingleOrderResponseModel>
}