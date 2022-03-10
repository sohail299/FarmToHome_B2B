package com.switchsolutions.farmtohome.b2b.presentation.history.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.api.RestApiClient
import com.switchsolutions.farmtohome.b2b.api.RetrofitApiManager
import com.switchsolutions.farmtohome.b2b.callbacks.ErrorDto
import com.switchsolutions.farmtohome.b2b.callbacks.Event
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.request.OrderRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.singleorder.GetSingleOrderResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.history.data.response.OrderHistoryResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse
import timber.log.Timber

class OrderHistoryViewModel : ViewModel() {

    var callSignInApi: MutableLiveData<Boolean> = MutableLiveData()

    var apiResponseSuccess: MutableLiveData<OrderHistoryResponseModel> = MutableLiveData()
    var apiResponseFailure: MutableLiveData<ErrorDto> = MutableLiveData()
    private val apiSingleOrderSuccess = MutableLiveData<Event<GetSingleOrderResponseModel>>()
    val statusSingleOrderSuccess: MutableLiveData<Event<GetSingleOrderResponseModel>>
        get() = apiSingleOrderSuccess

    private val apiSingleOrderFailure = MutableLiveData<Event<LoginResponse>>()
    val statusSingleOrderFailure: MutableLiveData<Event<LoginResponse>>
        get() = apiSingleOrderFailure

    init {
        callSignInApi.value = false
    }
    fun startObserver(cityId: Int, customerId: Int) {
        callSignInApi.value = true
        //call api here
        object : RetrofitApiManager<OrderHistoryResponseModel>(AppLauncher.ApplicationContext) {
            init {
                callServer(RestApiClient.getClient2(addHeaders = true).getPreviousOrders(OrderRequestModel(cityId, customerId)))
            }
            override fun onSuccess(t: OrderHistoryResponseModel?) {
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

}