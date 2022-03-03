package com.switchsolutions.farmtohome.b2b.presentation.cart.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.api.RestApiClient
import com.switchsolutions.farmtohome.b2b.api.RetrofitApiManager
import com.switchsolutions.farmtohome.b2b.callbacks.ErrorDto
import com.switchsolutions.farmtohome.b2b.presentation.cart.response.OrderSubmissionResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.cart.ui.CartFragment
import timber.log.BuildConfig
import timber.log.Timber

class SubmitCartViewModel : ViewModel() {


    var callCartSubmitApi: MutableLiveData<Boolean> = MutableLiveData()
    var apiResponseSuccess: MutableLiveData<OrderSubmissionResponseModel> = MutableLiveData()
    var apiResponseFailure: MutableLiveData<ErrorDto> = MutableLiveData()

    init {
        callCartSubmitApi.value = false
    }


    fun startObserver() {
        callCartSubmitApi.value = true
        //call api here
        object : RetrofitApiManager<OrderSubmissionResponseModel>(AppLauncher.ApplicationContext) {
            init {
                callServer(RestApiClient.getClient(addHeaders = true).createOrderRequest(CartFragment.placeOrderJson))
            }
            override fun onSuccess(t: OrderSubmissionResponseModel?) {
                callCartSubmitApi.value = false
                //saving user details
                apiResponseSuccess.value = t!!
            }
            override fun onFailure(t: ErrorDto) {
                Timber.e(t.message)
                callCartSubmitApi.value = false
                apiResponseFailure.value = t
            }
            override fun tokenRefreshed() {
                //nothing required in this case
            }
        }
    }
}