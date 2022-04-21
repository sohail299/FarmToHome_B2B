package com.switchsolutions.farmtohome.b2b.presentation.login.ui


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.MainActivity
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.api.RestApiClient
import com.switchsolutions.farmtohome.b2b.api.RetrofitApiManager
import com.switchsolutions.farmtohome.b2b.callbacks.ErrorDto
import com.switchsolutions.farmtohome.b2b.callbacks.Event
import com.switchsolutions.farmtohome.b2b.callbacks.HttpStatusCodes
import com.switchsolutions.farmtohome.b2b.databinding.ActivityLoginNewBinding
import com.switchsolutions.farmtohome.b2b.presentation.login.data.request.LoginRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.request.OTPVerificationRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.request.PasswordResetRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse
import com.switchsolutions.farmtohome.b2b.presentation.login.viewmodel.LogInViewModel
import com.switchsolutions.farmtohome.b2b.utils.NotificationUtil
import com.switchsolutions.farmtohome.b2b.utils.Utilities
import com.switchsolutions.farmtohome.b2b.utils.enums.Type
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity(),
    View.OnClickListener/*, ICallBackListener<LoginResponseModel>*/ {
    lateinit var binding: ActivityLoginNewBinding
    private lateinit var loginResponseModel: LoginResponse
    private lateinit var viewModel: LogInViewModel
    private val MY_PREFS_NAME = "FarmToHomeB2B"
    private lateinit var otp: String
    private lateinit var phone: String
    private val countryCode = "+92"
    private var mResendToken: ForceResendingToken? = null

    companion object {
        const val SIGNIN_FRAGMENT_TAG: String = "signInFrag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        // Inflate the layout for this fragment
        binding = ActivityLoginNewBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[LogInViewModel::class.java]
        setContentView(binding.root)

        viewModel.getFirebaseToken()
        startObservers()
        binding.loginLoginBtn.setOnClickListener(this)
        binding.loginForgotPasswordTv.setOnClickListener(this)
        binding.backButtonForgotPassword.setOnClickListener(this)
        binding.sendOtpBtn.setOnClickListener(this)
        binding.otpVerificationButton.setOnClickListener(this)
        binding.resetPasswordChangePasswordBtn.setOnClickListener(this)
        binding.showHidePasswordIcon.setOnClickListener(this)
        binding.sendRefreshSmsCall.setOnClickListener(this)
        binding.resetPasswordChangePasswordEt.setOnClickListener(this)
        binding.resetPasswordConfirmChangePasswordErrorEt.setOnClickListener(this)
        binding.resetPasswordShowHideIcon.setOnClickListener(this)
        binding.resetPasswordConfirmShowHideIcon.setOnClickListener(this)
        binding.imageViewOtpBack.setOnClickListener(this)
        binding.loginPhoneNumberEt.requestFocus()
    }


    private fun checkIfLoggedIn() {
        val userModel = LoginResponse.getStoredInstance(this)
        if (userModel.data.id != null
            && userModel.data.id != 0) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }
    override fun onClick(v: View?) {
        when (v?.id!!) {
            binding.loginLoginBtn.id -> viewModel.signInClicked()
            binding.loginForgotPasswordTv.id -> switchScreen(binding.layoutLogin, binding.layoutForgotPassword,"ForgotPassword")
            binding.backButtonForgotPassword.id -> switchScreen(binding.layoutForgotPassword, binding.layoutLogin,"")
            binding.otpVerificationButton.id -> viewModel.startOtpVerification()
            binding.resetPasswordChangePasswordBtn.id -> callChangePasswordApi()
            binding.sendOtpBtn.id -> viewModel.resetPasswordBtnClicked()
            binding.showHidePasswordIcon.id -> Utilities.instance!!.showAndHideIcon(binding.loginPasswordEt, binding.showHidePasswordIcon)
            binding.sendRefreshSmsCall.id -> resendVerificationCode(mResendToken)
            binding.resetPasswordShowHideIcon.id -> Utilities.instance!!.showAndHideIcon(binding.resetPasswordChangePasswordEt, binding.resetPasswordShowHideIcon)
            binding.resetPasswordConfirmShowHideIcon.id -> Utilities.instance!!.showAndHideIcon(binding.resetPasswordConfirmChangePasswordEt, binding.resetPasswordConfirmShowHideIcon)
            binding.imageViewOtpBack.id -> switchScreen(binding.layoutOtpVerification, binding.layoutForgotPassword,"")
        }
    }

    private fun setCountDownTimer() {
        binding.sendRefreshSmsCall.visibility = View.GONE
        object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.otpCountDownTimerLabel.text = "OTP expires in: " + millisUntilFinished / 1000 + " seconds"
            }

            override fun onFinish() {
                binding.otpCountDownTimerLabel.text = "Resend OTP"
                binding.sendRefreshSmsCall.visibility = View.VISIBLE
            }
        }.start()
    }
    private fun callChangePasswordApi() {
        binding.apply {
            if (resetPasswordChangePasswordEt.text.toString().trim { it <= ' ' }.isEmpty() ||
                resetPasswordChangePasswordEt.text.toString().trim { it <= ' ' }.length < 8
            ) {
                resetPasswordChangePasswordErrorEt.text = "Kindly Enter 8 Character Password First"
                resetPasswordChangePasswordErrorEt.visibility = View.VISIBLE
            } else if (resetPasswordChangePasswordEt.text.toString() != resetPasswordConfirmChangePasswordEt.text
                    .toString()
            ) {
                resetPasswordChangePasswordErrorEt.visibility = View.GONE
                resetPasswordConfirmChangePasswordErrorEt.text = "Password doesn't match the above entered password"
                resetPasswordConfirmChangePasswordErrorEt.visibility = View.VISIBLE
            } else {
                resetPasswordChangePasswordErrorEt.visibility = View.GONE
                resetPasswordConfirmChangePasswordErrorEt.visibility = View.GONE
                val changePasswordObject = JsonObject()
                changePasswordObject.addProperty("password", resetPasswordChangePasswordEt.text.toString())
                changePasswordObject.addProperty("msisdn", phone)
                changePasswordObject.addProperty("token", viewModel.token)
                changePasswordObject.addProperty("platform", "2")
                changePasswordObject.addProperty("fcmTokenAndroid", viewModel.FCM_TOKEN)
                resetPasswordChangePasswordBtn.startAnimation()
                sendResetPasswordRequest(changePasswordObject)
            }
        }
    }

    private fun sendResetPasswordRequest(changePasswordObject: JsonObject) {
        object : RetrofitApiManager<LoginResponse>(AppLauncher.ApplicationContext) {

            init {
                callServer(RestApiClient.getClient(addHeaders = false).changePassword(changePasswordObject))
            }

            override fun onSuccess(t: LoginResponse?) {
               // callSignInApi.value = false
                //saving user details
                //Event(t!!)
               // NotificationUtil.showShortToast(this, "Error Occurred", Type.DANGER)
                NotificationUtil.showLongToast(this@LoginActivity, "Password Updated", Type.SUCCESS)
                switchScreen(binding.layoutPasswordReset, binding.layoutLogin,"")
            }

            override fun onFailure(t: ErrorDto) {
                binding.resetPasswordConfirmChangePasswordErrorEt.text = t.message
                binding.resetPasswordConfirmChangePasswordErrorEt.visibility = View.VISIBLE
                binding.resetPasswordChangePasswordBtn.revertAnimation()
                binding.resetPasswordChangePasswordBtn.setBackgroundResource(R.drawable.rounded_edittext_bg)
                //Event(t)
            }
            override fun tokenRefreshed() {
                //nothing required in this case
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (binding.layoutForgotPassword.isVisible)
            switchScreen(binding.layoutForgotPassword, binding.layoutLogin,"")
        else
            finish()
    }
    private fun switchScreen(frontLayout: ConstraintLayout, backLayout: ConstraintLayout, screen: String){
        frontLayout.animate().translationY(0F)
            .alpha(0.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    //binding.layoutLogin.visibility = View.GONE
                }
            })
        frontLayout.visibility = View.GONE
        backLayout.visibility = View.VISIBLE
        backLayout.alpha = 0.0f
        backLayout.animate().duration = 500
        backLayout.animate()
            .translationY(1F)
            .alpha(1.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    //binding.layoutForgotPassword.visibility = View.VISIBLE
                }
            })
        if (screen == "OTP") {
            binding.otp1.requestFocus()
            binding.forgotPasswordPhoneNumberEt.setText("")
        }
        if (screen == "ForgotPassword"){
            binding.forgotPasswordPhoneNumberEt.requestFocus()
        }
    }

    private fun startObservers() {
        viewModel.getLoginRequestModel().observe(this, Observer {
            binding.loginRequestModel = it!!
        })
        viewModel.getOTPRequestModel().observe(this, Observer {
            binding.otpRequestModel = it!!
        })

        viewModel.getPassResetRequestModel().observe(this) {
            binding.passwordResetRequestModel = it!!
        }
        viewModel.signInEmailErrorStatus.observe(this, Observer {
            if (it!!) setEmailError()
        })

        viewModel.signInPhoneErrorStatus.observe(this, Observer {
            if (it!!){
                binding.errorMessageTextview.visibility = View.VISIBLE
                binding.errorMessageTextview.text = "Enter Phone Number"
                binding.loginPhoneNumberEt.requestFocus()
            }
        })

        viewModel.signInPassErrorStatus.observe(this, Observer {
            if (it!!) {
                binding.errorMessageTextview.visibility = View.VISIBLE
                binding.errorMessageTextview.text = "Enter Password"
                binding.loginPasswordEt.requestFocus()
            }
        })
        viewModel.otpPhoneErrorStatus.observe(this, Observer {
            if (it!!){
                binding.errorMessageTextviewOtp.visibility = View.VISIBLE
                binding.errorMessageTextviewOtp.text = "Enter Phone Number"
            }
        })
        viewModel.signInPasswordErrorStatus.observe(this, Observer {
            if (it!!) setPasswordError()
        })
        viewModel.clearAllInputErrors.observe(this, Observer {
            if (it!!) clearAllErrors()
        })

        viewModel.resetPhoneErrorStatus.observe(this, Observer {
            if (it!!) {
                binding.errorMessageTextviewOtp.visibility = View.VISIBLE
                binding.errorMessageTextviewOtp.text = "Invalid Phone Number"
            }
        })
        viewModel.callSignInApi.observe(this, Observer {
            if (it!!) {
                binding.errorMessageTextview.visibility = View.GONE
                binding.loginLoginBtn.startAnimation()
                //waitDialog = ProgressDialog.show(this, "", "Sign in")
            }
        })
        viewModel.callResetPassApi.observe(this, Observer {
            if (it!!) {
               // binding.errorMessageTextview.visibility = View.GONE
                binding.sendOtpBtn.startAnimation()
                //waitDialog = ProgressDialog.show(this, "", "Sign in")
            }
        })
        viewModel.statusSignInSuccess.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                binding.loginLoginBtn.revertAnimation()
                binding.loginLoginBtn.setBackgroundResource(R.drawable.rounded_edittext_bg)
                loginResponseModel = it
                //redirect to main landing screen
                if (loginResponseModel.data.type == 2) {
                    val editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
                    editor.putInt("User", loginResponseModel.data.id)
                    editor.putBoolean("isLoggedIn", true)
                    if (loginResponseModel.data.city!=null)
                    editor.putInt("cityId", loginResponseModel.data.city.id)
                    editor.putString("accessToken", loginResponseModel.accessToken)
                    editor.apply()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    binding.errorMessageTextview.visibility = View.VISIBLE
                    binding.errorMessageTextview.text = "Account does not exist"
                }
            }
        }

        viewModel.statusOtpVerificationBtn.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it)
                    binding.otpVerificationButton.startAnimation()
                else {
                    binding.otpVerificationButton.revertAnimation()
                    binding.otpVerificationButton.setBackgroundResource(R.drawable.rounded_edittext_bg)
                }

            }
        }

        viewModel.statusOtpTextError.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it)
                    Toast.makeText(this, "Enter Valid OTP", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.statusOtpVerificationCall.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (!it)
                    Toast.makeText(this@LoginActivity, "Wrong OTP", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.statusOpenPasswordResetScreen.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it)
                    switchScreen(binding.layoutOtpVerification,binding.layoutPasswordReset,"")

            }
        }

        viewModel.statusPassResetSuccess.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                loginResponseModel = it
                //redirect to main landing screen
                if (loginResponseModel.statusCode== 200) {
                    binding.errorMessageTextviewOtp.visibility = View.GONE

                    Log.i("TOKEN", loginResponseModel.token)
                    requestOtpFromFirebase(viewModel.phoneNum)
                }
            }
        }

        viewModel.statusFailure.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                binding.loginLoginBtn.revertAnimation()
                binding.loginLoginBtn.setBackgroundResource(R.drawable.rounded_edittext_bg)
                if ((it.statusCode == HttpStatusCodes.SC_UNAUTHORIZED) || (it.statusCode == HttpStatusCodes.SC_NO_CONTENT || (it.statusCode == HttpStatusCodes.SC_BAD_REQUEST))) {
                    binding.errorMessageTextview.visibility = View.VISIBLE
                    binding.errorMessageTextview.text = it.message
//                val builder = AlertDialog.Builder(context!!)
//                builder.setMessage(context?.getString(R.string.invalid_credentials))
//                        .setPositiveButton(context?.getString(R.string.ok)) { dialog, _ ->
//                            dialog.dismiss()
//                        }
//                builder.create().show()
                } else {
//                Toast.makeText(
//                    this, "An Error Occurred",
//                    Toast.LENGTH_LONG
//                ).show()
                    //NotificationUtil.showShortToast(this, "Error Occurred", Type.DANGER)
                }
            }

        }

        viewModel.statusOTPFailure.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                binding.sendOtpBtn.revertAnimation()
                binding.sendOtpBtn.setBackgroundResource(R.drawable.rounded_edittext_bg)
                if ((it.statusCode == HttpStatusCodes.SC_UNAUTHORIZED) || (it.statusCode == HttpStatusCodes.SC_NO_CONTENT || (it.statusCode == HttpStatusCodes.SC_BAD_REQUEST))) {
                    binding.errorMessageTextviewOtp.visibility = View.VISIBLE
                    binding.errorMessageTextviewOtp.text = it.message
//                val builder = AlertDialog.Builder(context!!)
//                builder.setMessage(context?.getString(R.string.invalid_credentials))
//                        .setPositiveButton(context?.getString(R.string.ok)) { dialog, _ ->
//                            dialog.dismiss()
//                        }
//                builder.create().show()
                } else {
//                Toast.makeText(
//                    this, "An Error Occurred",
//                    Toast.LENGTH_LONG
//                ).show()
                    //NotificationUtil.showShortToast(this, "Error Occurred", Type.DANGER)
                }
            }

        }
    }
    private fun resendVerificationCode(token: ForceResendingToken?) {
        setCountDownTimer()
        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(viewModel.phoneNum)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(verificationId: String,forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    Log.i("MYTAG", verificationId)
                    //verificationCode = verificationId
                    // Save the verification id somewhere
                    // ...
                    viewModel.verificationCode = verificationId
                    mResendToken = forceResendingToken

                    // The corresponding whitelisted code above should be used to complete sign-in.
                    //this.enableUserManuallyInputCode()
                }

                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    Log.i("MYTAG", phoneAuthCredential.toString())
                    // Sign in with the credential
                    // ...
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.i("MYTAG", e.message.toString())
                    // ...
                }
            }).setForceResendingToken(mResendToken!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun requestOtpFromFirebase(phoneNumber: String) {
        phone = phoneNumber
        viewModel.phoneNum = countryCode + phoneNumber.substring(1)



// Configure faking the auto-retrieval with the whitelisted numbers.
// Whenever verification is triggered with the whitelisted number,
// provided it is not set for auto-retrieval, onCodeSent will be triggered.
        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(viewModel.phoneNum)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(verificationId: String,forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    Log.i("MYTAG", verificationId)
                    //verificationCode = verificationId/
                    // Save the verification id somewhere
                    // ...
                    Toast.makeText(this@LoginActivity, "OTP sent to $phone", Toast.LENGTH_SHORT
                    ).show()
                    binding.sendOtpBtn.revertAnimation()
                    binding.sendOtpBtn.setBackgroundResource(R.drawable.rounded_edittext_bg)
                    Utilities.instance!!.moveOTPBack(binding.otp2, binding.otp1)
                    Utilities.instance!!.moveOTPBack(binding.otp3, binding.otp2)
                    Utilities.instance!!.moveOTPBack(binding.otp4, binding.otp3)
                    Utilities.instance!!.moveOTPBack(binding.otp5, binding.otp4)
                    Utilities.instance!!.moveOTPBack(binding.otp6, binding.otp5)
                    setCountDownTimer()
                    switchScreen(binding.layoutForgotPassword, binding.layoutOtpVerification,"OTP")
                    viewModel.verificationCode = verificationId
                    mResendToken = forceResendingToken

                    // The corresponding whitelisted code above should be used to complete sign-in.
                    //this.enableUserManuallyInputCode()
                }

                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    Log.i("MYTAG", phoneAuthCredential.toString())
                    // Sign in with the credential
                    // ...
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.i("MYTAG", e.message.toString())
                    binding.sendOtpBtn.revertAnimation()
                    binding.sendOtpBtn.setBackgroundResource(R.drawable.rounded_edittext_bg)
                    if (e.message == "We have blocked all requests from this device due to unusual activity. Try again later.") {
                        Toast.makeText(this@LoginActivity,
                            "Your number has been restricted due to too many attempts. Kindly try again later",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else Toast.makeText(
                        this@LoginActivity,
                        "Unable to send message at the given number",
                        Toast.LENGTH_SHORT
                    ).show()
                    // ...
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun setEmailError() {
        // NotificationUtil.showShortToast(context!!, getString(R.string.email_is_invalid), Type.DANGER)
        binding.loginPhoneNumberEt.error = "Invalid Phone Number"
    }

    private fun setPasswordError() {
        // NotificationUtil.showShortToast(context!!, getString(R.string.password_error), Type.DANGER)
        binding.loginPasswordEt.error = "invalid Password"
//        binding.tilUserPassword.isErrorEnabled = true
    }

    private fun clearAllErrors() {
//        binding.tilUserEmail.isErrorEnabled = false
//        binding.tilUserPassword.isErrorEnabled = false
    }

}
