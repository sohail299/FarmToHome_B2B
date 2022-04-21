package com.switchsolutions.farmtohome.b2b.presentation.login.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.BuildConfig
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.api.RestApiClient
import com.switchsolutions.farmtohome.b2b.api.RetrofitApiManager
import com.switchsolutions.farmtohome.b2b.callbacks.ErrorDto
import com.switchsolutions.farmtohome.b2b.callbacks.Event
import com.switchsolutions.farmtohome.b2b.presentation.login.data.request.LoginRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.request.OTPVerificationRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.request.PasswordResetRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse
import com.switchsolutions.farmtohome.b2b.utils.SharedPrefUtils
import com.switchsolutions.farmtohome.b2b.utils.ValidationUtil

class LogInViewModel : ViewModel() {

    lateinit var FCM_TOKEN: String
    lateinit var otp: String
    lateinit var verificationCode: String
    lateinit var token: String
    lateinit var phoneNum: String
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val loginRequestModel: MutableLiveData<LoginRequestModel> = MutableLiveData()
    private val otpRequestModel: MutableLiveData<OTPVerificationRequestModel> = MutableLiveData()
    private val resetPasswordRequestModel: MutableLiveData<PasswordResetRequestModel> = MutableLiveData()


    var signInEmailErrorStatus: MutableLiveData<Boolean> = MutableLiveData()
    var otpPhoneErrorStatus: MutableLiveData<Boolean> = MutableLiveData()
    var signInPhoneErrorStatus: MutableLiveData<Boolean> = MutableLiveData()
    var signInPassErrorStatus: MutableLiveData<Boolean> = MutableLiveData()
    var resetPhoneErrorStatus: MutableLiveData<Boolean> = MutableLiveData()
    var signInPasswordErrorStatus: MutableLiveData<Boolean> = MutableLiveData()
    var clearAllInputErrors: MutableLiveData<Boolean> = MutableLiveData()
    var callSignInApi: MutableLiveData<Boolean> = MutableLiveData()
    var callResetPassApi: MutableLiveData<Boolean> = MutableLiveData()

    private val otpTextError = MutableLiveData<Event<Boolean>>()
    val statusOtpTextError: MutableLiveData<Event<Boolean>>
        get() = otpTextError

    private val apiSignInSuccess = MutableLiveData<Event<LoginResponse>>()
    val statusSignInSuccess: MutableLiveData<Event<LoginResponse>>
        get() = apiSignInSuccess

    private val otpVerificationBtn = MutableLiveData<Event<Boolean>>()
    val statusOtpVerificationBtn: MutableLiveData<Event<Boolean>>
        get() = otpVerificationBtn

    private val otpVerfificationCall = MutableLiveData<Event<Boolean>>()
    val statusOtpVerificationCall: MutableLiveData<Event<Boolean>>
        get() = otpVerfificationCall

    private val openPasswordResetScreen = MutableLiveData<Event<Boolean>>()
    val statusOpenPasswordResetScreen: MutableLiveData<Event<Boolean>>
        get() = openPasswordResetScreen

    private val apiPassResetSuccess = MutableLiveData<Event<LoginResponse>>()
    val statusPassResetSuccess: MutableLiveData<Event<LoginResponse>>
        get() = apiPassResetSuccess

    private val apiFailure = MutableLiveData<Event<ErrorDto>>()
    val statusFailure: MutableLiveData<Event<ErrorDto>>
        get() = apiFailure

    private val apiOTPFailure = MutableLiveData<Event<ErrorDto>>()
    val statusOTPFailure: MutableLiveData<Event<ErrorDto>>
        get() = apiOTPFailure

    init {
        loginRequestModel.value = if (BuildConfig.DEBUG) LoginRequestModel("", "","",",","","")
        else LoginRequestModel("", "","","","","")
        resetPasswordRequestModel.value = if (BuildConfig.DEBUG) PasswordResetRequestModel("")
        else PasswordResetRequestModel("")
        otpRequestModel.value = if (BuildConfig.DEBUG) OTPVerificationRequestModel("", "","","","","")
        else OTPVerificationRequestModel("", "","","","","")
        signInEmailErrorStatus.value = false
        otpPhoneErrorStatus.value = false
        signInPhoneErrorStatus.value = false
        signInPassErrorStatus.value = false
        resetPhoneErrorStatus.value = false
        signInPasswordErrorStatus.value = false
        clearAllInputErrors.value = true
        callSignInApi.value = false
    }


    fun getLoginRequestModel(): MutableLiveData<LoginRequestModel> {
        return loginRequestModel
    }

    fun getOTPRequestModel(): MutableLiveData<OTPVerificationRequestModel> {
        return otpRequestModel
    }

    fun getPassResetRequestModel(): MutableLiveData<PasswordResetRequestModel> {
        return resetPasswordRequestModel
    }

    fun signInClicked() {
        clearAllInputErrors.value = false
        //Validating data
        loginRequestModel.value?.msisdn = loginRequestModel.value?.msisdn?.trim() ?: ""
        if (!ValidationUtil.isNotEmpty(loginRequestModel.value?.msisdn?.trim() ?: ""))
        {
            signInPhoneErrorStatus.value = true
            return
        }
        if (!ValidationUtil.isNotEmpty(loginRequestModel.value?.password?.trim() ?: ""))
        {
            signInPassErrorStatus.value = true
            return
        }
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
                sharedPref.setValue(SharedPrefUtils.USER_PROFILE, Gson().toJson(t))
                statusSignInSuccess.value = Event(t!!)
            }

            override fun onFailure(t: ErrorDto) {
                callSignInApi.value = false
                statusFailure.value = Event(t)
            }

            override fun tokenRefreshed() {
                //nothing required in this case
            }
        }
    }

    fun startOtpVerification() {
        otpRequestModel.value?.otp1 = otpRequestModel.value?.otp1?.trim() ?: ""
        otpRequestModel.value?.otp2 = otpRequestModel.value?.otp2?.trim() ?: ""
        otpRequestModel.value?.otp3 = otpRequestModel.value?.otp3?.trim() ?: ""
        otpRequestModel.value?.otp4 = otpRequestModel.value?.otp4?.trim() ?: ""
        otpRequestModel.value?.otp5 = otpRequestModel.value?.otp5?.trim() ?: ""
        otpRequestModel.value?.otp6 = otpRequestModel.value?.otp6?.trim() ?: ""
        loginRequestModel.value?.msisdn = loginRequestModel.value?.msisdn?.trim() ?: ""
            if (otpRequestModel.value?.otp1.toString().isEmpty()) {
                otpTextError.value = Event(true)
//                otp1.setError("Add OTP Number")
//                otp1.requestFocus()
            } else if (otpRequestModel.value?.otp2.toString().isEmpty()) {
                otpTextError.value = Event(true)
//                otp2.setError("Add OTP Number")
//                otp2.requestFocus()
            } else if (otpRequestModel.value?.otp3.toString().isEmpty()) {
                otpTextError.value = Event(true)
//                otp3.setError("Add OTP Number")
//                otp3.requestFocus()
            } else if (otpRequestModel.value?.otp4.toString().isEmpty()) {
                otpTextError.value = Event(true)
//                otp4.setError("Add OTP Number")
//                otp4.requestFocus()
            } else if (otpRequestModel.value?.otp5.toString().isEmpty()) {
                otpTextError.value = Event(true)
//                otp5.setError("Add OTP Number")
//                otp5.requestFocus()
            } else if (otpRequestModel.value?.otp6.toString().isEmpty()) {
                otpTextError.value = Event(true)
//                otp6.setError("Add OTP Number")
//                otp6.requestFocus()
            } else {
                otpVerificationBtn.value = Event(true)
                otp = (otpRequestModel.value?.otp1.toString() +
                        otpRequestModel.value?.otp2.toString() +
                        otpRequestModel.value?.otp3.toString() +
                        otpRequestModel.value?.otp4.toString() +
                        otpRequestModel.value?.otp5.toString() +
                        otpRequestModel.value?.otp6.toString())
                try {
                    val credential =
                        PhoneAuthProvider.getCredential(verificationCode, otp)
                    SignInWithPhone(credential)
                } catch (e: IllegalArgumentException) {
                    otpVerificationBtn.value = Event(false)
                }
            }
    }

    private fun SignInWithPhone(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(OnCompleteListener { task: Task<AuthResult?> ->

                otpVerfificationCall.value = Event(true)
                if (task.isSuccessful) {
                    otpVerfificationCall.value = Event(true)
                    openPasswordResetScreen.value = Event(true)
                }
                else {
                    otpVerificationBtn.value = Event(false)
                    otpVerfificationCall.value = Event(false)
                }
            })
    }

    fun getFirebaseToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    Log.w(
                        "MYTAG",
                        "getInstanceId failed",
                        task.exception
                    )
                }
                // Get new Instance ID token
                val token = task.result
                Log.d(
                    "MYTAG",
                    token
                )
                FCM_TOKEN = token
            }
    }

    fun resetPasswordBtnClicked() {
        clearAllInputErrors.value = false
        //Validating data
        resetPasswordRequestModel.value?.msisdn = resetPasswordRequestModel.value?.msisdn?.trim() ?: ""
        if (!ValidationUtil.isNotEmpty(resetPasswordRequestModel.value?.msisdn?.trim() ?: ""))
        {
            otpPhoneErrorStatus.value = true
            return
        }
        if (!ValidationUtil.isPhoneNumberValid(resetPasswordRequestModel.value?.msisdn!!) && !ValidationUtil.isANumber(
                resetPasswordRequestModel.value?.msisdn!!)) {

            resetPhoneErrorStatus.value = true
            return
        }
        callResetPassApi.value = true
        phoneNum = resetPasswordRequestModel.value?.msisdn?.trim() ?: ""

        //call api here
        object : RetrofitApiManager<LoginResponse>(AppLauncher.ApplicationContext) {

            init {
                callServer(RestApiClient.getClient(addHeaders = false).passwordReset(
                    PasswordResetRequestModel(resetPasswordRequestModel.value?.msisdn ?: "")
                ))
            }

            override fun onSuccess(t: LoginResponse?) {
                callResetPassApi.value = false
                //saving user details
                //verificationCode = t!!.token
                token = t!!.token
                apiPassResetSuccess.value = Event(t)
            }

            override fun onFailure(t: ErrorDto) {
                callSignInApi.value = false
                apiOTPFailure.value = Event(t)
            }

            override fun tokenRefreshed() {
                //nothing required in this case
            }
        }
    }
}