package com.switchsolutions.farmtohome.b2b.presentation.login.data.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.BuildConfig
import com.switchsolutions.farmtohome.b2b.api.RestApiClient
import com.switchsolutions.farmtohome.b2b.api.RetrofitApiManager
import com.switchsolutions.farmtohome.b2b.callbacks.ErrorDto
import com.switchsolutions.farmtohome.b2b.presentation.login.data.requestmodel.LoginRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.responsemodel.login.LoginResponse
import com.switchsolutions.farmtohome.b2b.utils.SharedPrefUtils
import com.switchsolutions.farmtohome.b2b.utils.ValidationUtil

class LogInViewModel : ViewModel() {

    private val loginRequestModel: MutableLiveData<LoginRequestModel> = MutableLiveData()
    var signInEmailErrorStatus: MutableLiveData<Boolean> = MutableLiveData()
    var signInPasswordErrorStatus: MutableLiveData<Boolean> = MutableLiveData()
    var clearAllInputErrors: MutableLiveData<Boolean> = MutableLiveData()
    var callSignInApi: MutableLiveData<Boolean> = MutableLiveData()
    var apiResponseSuccess: MutableLiveData<LoginResponse> = MutableLiveData()
    var apiResponseFailure: MutableLiveData<ErrorDto> = MutableLiveData()

    init {
        loginRequestModel.value = if (BuildConfig.DEBUG) LoginRequestModel("", "","",",","","")
        else LoginRequestModel("", "","","","","")
        signInEmailErrorStatus.value = false
        signInPasswordErrorStatus.value = false
        clearAllInputErrors.value = true
        callSignInApi.value = false
    }

    fun getLoginRequestModel(): MutableLiveData<LoginRequestModel> {
        return loginRequestModel
    }

    fun signInClicked() {
        clearAllInputErrors.value = false
        //Validating data
        loginRequestModel.value?.msisdn = loginRequestModel.value?.msisdn?.trim() ?: ""
        if (!ValidationUtil.isPhoneNumberValid(loginRequestModel.value?.msisdn!!) && !ValidationUtil.isANumber(
                loginRequestModel.value?.msisdn!!
            )
        ) {
            signInEmailErrorStatus.value = true
            return
        }
        if (!ValidationUtil.isPasswordValid(loginRequestModel.value?.password!!)) {
            signInPasswordErrorStatus.value = true
            return
        }
        callSignInApi.value = true

        //call api here
        object : RetrofitApiManager<LoginResponse>(AppLauncher.ApplicationContext) {

            init {
                callServer(RestApiClient.getClient(addHeaders = false).signInCall(LoginRequestModel(loginRequestModel.value?.msisdn ?: "", loginRequestModel.value?.password ?: "", "2","1","1","1")))
            }

            override fun onSuccess(t: LoginResponse?) {
                callSignInApi.value = false
                //saving user details
                val sharedPref = SharedPrefUtils.getInstance(AppLauncher.ApplicationContext)
                sharedPref.setValue(SharedPrefUtils.USER_PROFILE, Gson().toJson(t?.data?.name))
                apiResponseSuccess.value = t!!
            }

            override fun onFailure(t: ErrorDto) {
                callSignInApi.value = false
                apiResponseFailure.value = t
            }

            override fun tokenRefreshed() {
                //nothing required in this case
            }
        }
    }
}