package com.switchsolutions.farmtohome.b2b.presentation.dashboard.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.api.RestApiClient
import com.switchsolutions.farmtohome.b2b.api.RetrofitApiManager
import com.switchsolutions.farmtohome.b2b.callbacks.ErrorDto
import com.switchsolutions.farmtohome.b2b.callbacks.Event
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.request.OrderRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.OrdersResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.singleorder.GetSingleOrderResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse
import timber.log.Timber

class DashboardViewModel : ViewModel() {

    var callSignInApi: MutableLiveData<Boolean> = MutableLiveData()
    private val callSingleOrderApi = MutableLiveData<Event<Boolean>>()
    val statusSingleOrderApi: MutableLiveData<Event<Boolean>>
        get() = callSingleOrderApi

    var apiResponseSuccess: MutableLiveData<OrdersResponseModel> = MutableLiveData()
    var apiResponseFailure: MutableLiveData<ErrorDto> = MutableLiveData()
    private val apiSingleOrderSuccess = MutableLiveData<Event<GetSingleOrderResponseModel>>()
    val statusSingleOrderSuccess: MutableLiveData<Event<GetSingleOrderResponseModel>>
        get() = apiSingleOrderSuccess

    private val apiSingleOrderFailure = MutableLiveData<Event<LoginResponse>>()
    val statusSingleOrderFailure: MutableLiveData<Event<LoginResponse>>
        get() = apiSingleOrderFailure

    init {
        callSignInApi.value = false
        callSingleOrderApi.value = Event(false)
    }
    fun startObserver(cityId: Int, customerId: Int) {
        callSignInApi.value = true
        //call api here
        object : RetrofitApiManager<OrdersResponseModel>(AppLauncher.ApplicationContext) {
            init {
                callServer(RestApiClient.getClient2(addHeaders = true).getProcessingOrders(OrderRequestModel(cityId, customerId)))
            }
            override fun onSuccess(t: OrdersResponseModel?) {
                callSignInApi.value = false
                //saving user details
                apiResponseSuccess.value = t!!
            }
            override fun onFailure(t: ErrorDto) {
                Timber.e(t.message)
                callSignInApi.value = false
                apiResponseFailure.value = t
            }
            override fun tokenRefreshed() {
                //nothing required in this case
            }
        }
    }

    fun startSingleOrderObserver(reqId: Int) {
        callSingleOrderApi.value = Event(true)
        //call api here
        object : RetrofitApiManager<GetSingleOrderResponseModel>(AppLauncher.ApplicationContext) {
            init {
                callServer(RestApiClient.getClient2(addHeaders = true).getSingleOrder(reqId))
            }
            override fun onSuccess(t: GetSingleOrderResponseModel?) {
                callSingleOrderApi.value = Event(false)
                //saving user details
                apiSingleOrderSuccess.value = Event(t!!)
            }
            override fun onFailure(t: ErrorDto) {
                Timber.e(t.message)
                callSingleOrderApi.value = Event(false)
                apiResponseFailure.value = t
            }
            override fun tokenRefreshed() {
                //nothing required in this case
            }
        }
    }
}