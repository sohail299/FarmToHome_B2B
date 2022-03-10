package com.switchsolutions.farmtohome.b2b.data.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.MainActivity
import com.switchsolutions.farmtohome.b2b.api.RestApiClient
import com.switchsolutions.farmtohome.b2b.api.RetrofitApiManager
import com.switchsolutions.farmtohome.b2b.callbacks.ErrorDto
import com.switchsolutions.farmtohome.b2b.callbacks.Event
import com.switchsolutions.farmtohome.b2b.presentation.createorder.data.response.ProductResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse
import timber.log.BuildConfig
import timber.log.Timber

class ProductsApiViewModel : ViewModel() {

    var callProductsInApi: MutableLiveData<Boolean> = MutableLiveData()


    private val apiProductResponseSuccess = MutableLiveData<Event<ProductResponseModel>>()
    val statusProductResponseSuccess: MutableLiveData<Event<ProductResponseModel>>
        get() = apiProductResponseSuccess

    private val apiProductResponseFailure = MutableLiveData<Event<ErrorDto>>()
    val statusProductResponseFailure: MutableLiveData<Event<ErrorDto>>
        get() = apiProductResponseFailure

    init {
        callProductsInApi.value = false
    }
    fun startObserver(cityId: Int, customerId: Int) {
        callProductsInApi.value = true

        //call api here
        object : RetrofitApiManager<ProductResponseModel>(AppLauncher.ApplicationContext) {
            init {
                callServer(RestApiClient.getClient2(addHeaders = true).getProducts(cityId = cityId, customer_id = customerId))
            }
            override fun onSuccess(t: ProductResponseModel?) {
                callProductsInApi.value = false
                //saving user details
                apiProductResponseSuccess.value = Event(t!!)

            }
            override fun onFailure(t: ErrorDto) {
                Timber.e(t.message)
                callProductsInApi.value = false
                apiProductResponseFailure.value = Event(t)

            }
            override fun tokenRefreshed() {
                //nothing required in this case
            }
        }
    }
}