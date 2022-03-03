package com.switchsolutions.farmtohome.b2b.presentation.dashboard.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.api.RestApiClient
import com.switchsolutions.farmtohome.b2b.api.RetrofitApiManager
import com.switchsolutions.farmtohome.b2b.callbacks.ErrorDto
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.request.OrderRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.OrdersResponseModel
import timber.log.Timber

class DashboardViewModel : ViewModel() {

    var callSignInApi: MutableLiveData<Boolean> = MutableLiveData()

    var apiResponseSuccess: MutableLiveData<OrdersResponseModel> = MutableLiveData()
    var apiResponseFailure: MutableLiveData<ErrorDto> = MutableLiveData()


    init {
        callSignInApi.value = false
    }
    fun startObserver() {
        callSignInApi.value = true
        //call api here
        object : RetrofitApiManager<OrdersResponseModel>(AppLauncher.ApplicationContext) {
            init {
                callServer(RestApiClient.getClient2(addHeaders = true).getProcessingOrders(OrderRequestModel(1, 9940)))
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
}