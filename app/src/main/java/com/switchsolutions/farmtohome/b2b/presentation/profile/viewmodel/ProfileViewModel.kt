package com.switchsolutions.farmtohome.b2b.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel



import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.api.RestApiClient
import com.switchsolutions.farmtohome.b2b.api.RetrofitApiManager
import com.switchsolutions.farmtohome.b2b.callbacks.ErrorDto
import com.switchsolutions.farmtohome.b2b.callbacks.Event
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.request.OrderRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.OrdersResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.singleorder.GetSingleOrderResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse
import com.switchsolutions.farmtohome.b2b.presentation.profile.data.response.PasswordResetResponse
import timber.log.Timber

class ProfileViewModel : ViewModel() {

    var callUpdatePasswordApi: MutableLiveData<Boolean> = MutableLiveData()

    private val apiUpdatePasswordSuccess = MutableLiveData<Event<PasswordResetResponse>>()
    val statusUpdatePasswordSuccess: MutableLiveData<Event<PasswordResetResponse>>
        get() = apiUpdatePasswordSuccess

    private val apiUpdatePasswordFailure = MutableLiveData<Event<ErrorDto>>()
    val statusUpdatePasswordFailure: MutableLiveData<Event<ErrorDto>>
        get() = apiUpdatePasswordFailure

    init {
        callUpdatePasswordApi.value = false
    }
    fun startObserver(obj: JsonObject) {
        callUpdatePasswordApi.value = true
        //call api here
        object : RetrofitApiManager<PasswordResetResponse>(AppLauncher.ApplicationContext) {
            init {
                callServer(RestApiClient.getClient(addHeaders = true).resetPassword(obj))
            }
            override fun onSuccess(t: PasswordResetResponse?) {
                callUpdatePasswordApi.value = false
                //saving user details
                apiUpdatePasswordSuccess.value = Event(t!!)
            }
            override fun onFailure(t: ErrorDto) {
                Timber.e(t.message)
                callUpdatePasswordApi.value = false
                apiUpdatePasswordFailure.value = Event(t)
            }
            override fun tokenRefreshed() {
                //nothing required in this case
            }
        }
    }
}