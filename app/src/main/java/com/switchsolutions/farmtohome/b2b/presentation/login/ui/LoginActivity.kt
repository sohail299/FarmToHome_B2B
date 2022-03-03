package com.switchsolutions.farmtohome.b2b.presentation.login.ui


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
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
import java.util.concurrent.TimeUnit


class LoginActivity : AppCompatActivity(),
    View.OnClickListener/*, ICallBackListener<LoginResponseModel>*/ {
    lateinit var binding: ActivityLoginNewBinding
    private lateinit var loginRequestModel: LoginRequestModel
    private lateinit var otpRequestModel: OTPVerificationRequestModel
    private lateinit var passResetRequestModel: PasswordResetRequestModel
    private lateinit var loginResponseModel: LoginResponse
    private lateinit var viewModel: LogInViewModel
    private val MY_PREFS_NAME = "FarmToHomeB2B"
    private lateinit var otp: String


    //private lateinit var branchViewmodel: BranchViewModel


    companion object {
        const val SIGNIN_FRAGMENT_TAG: String = "signInFrag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        // Inflate the layout for this fragment
        binding = ActivityLoginNewBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(LogInViewModel::class.java)
        setContentView(binding.root)
        val preferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        var name = preferences.getInt("User", 0)
        var isLoggedIn = preferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewModel.getFirebaseToken()
        startObservers()
        binding.loginLoginBtn.setOnClickListener(this)
        binding.loginForgotPasswordTv.setOnClickListener(this)
        binding.backButtonForgotPassword.setOnClickListener(this)
        binding.forgotPasswordResetBtn.setOnClickListener(this)
        binding.otpVerificationButton.setOnClickListener(this)
        binding.resetPasswordChangePasswordBtn.setOnClickListener(this)
    }



    override fun onClick(v: View?) {
        when (v?.id!!) {
            binding.loginLoginBtn.id -> viewModel.signInClicked()
            binding.loginForgotPasswordTv.id -> openPasswordScreen()
            binding.backButtonForgotPassword.id -> openLoginScreen()
            binding.otpVerificationButton.id -> viewModel.startOtpVerification()
            binding.resetPasswordChangePasswordBtn.id -> callChangePasswordApi()
            binding.forgotPasswordResetBtn.id -> viewModel.resetPasswordBtnClicked()

        }
    }

    private fun callChangePasswordApi() {
        binding.apply {
            if (resetPasswordChangePasswordEt.getText().toString().trim { it <= ' ' }.length < 1 ||
                resetPasswordChangePasswordEt.getText().toString().trim { it <= ' ' }.length < 8
            ) {
                resetPasswordChangePasswordErrorEt.setText("Kindly Enter 8 Character Password First")
                resetPasswordChangePasswordErrorEt.setVisibility(View.VISIBLE)
            } else if (resetPasswordChangePasswordEt.getText().toString() != resetPasswordConfirmChangePasswordEt.getText()
                    .toString()
            ) {
                resetPasswordConfirmChangePasswordErrorEt.setText("Password doesn't match the above entered password")
                resetPasswordConfirmChangePasswordErrorEt.setVisibility(View.VISIBLE)
            } else {
                resetPasswordChangePasswordErrorEt.setVisibility(View.GONE)
                resetPasswordChangePasswordEt.setVisibility(View.GONE)
                val changePasswordObject = JsonObject()
                changePasswordObject.addProperty("password", resetPasswordChangePasswordEt.getText().toString())
                changePasswordObject.addProperty("msisdn", "03365814305")
                changePasswordObject.addProperty("token", viewModel.verificationCode)
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
                Event(t!!)
            }

            override fun onFailure(t: ErrorDto) {

                Event(t)
            }

            override fun tokenRefreshed() {
                //nothing required in this case
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        if (binding.layoutForgotPassword.isVisible)
            openLoginScreen()
        else
            finish()

    }
    private fun openPasswordScreen() {
        binding.layoutLogin.animate()
            .translationY(0F)
            .alpha(0.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    //binding.layoutLogin.visibility = View.GONE
                }
            })
        binding.layoutLogin.visibility = View.GONE
        binding.layoutForgotPassword.visibility = View.VISIBLE
        binding.layoutForgotPassword.alpha = 0.0f
        binding.layoutForgotPassword.animate().duration = 500
        binding.layoutForgotPassword.animate()
            .translationY(0F)
            .alpha(1.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    //binding.layoutForgotPassword.visibility = View.VISIBLE
                }
            })
       // binding.layoutLogin.visibility = View.GONE
        //binding.layoutForgotPassword.visibility = View.VISIBLE
    }
    private fun openLoginScreen() {
            binding.layoutForgotPassword.animate().translationY(0F)
            .alpha(0.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    //binding.layoutLogin.visibility = View.GONE
                }
            })
        binding.layoutForgotPassword.visibility = View.GONE
        binding.layoutLogin.visibility = View.VISIBLE
        binding.layoutLogin.alpha = 0.0f
        binding.layoutLogin.animate().duration = 500
        binding.layoutLogin.animate()
            .translationY(1F)
            .alpha(1.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    //binding.layoutForgotPassword.visibility = View.VISIBLE
                }
            })
    }


    private fun openPasswordResetScreen() {
        binding.layoutOtpVerification.animate().translationY(0F)
            .alpha(0.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    //binding.layoutLogin.visibility = View.GONE
                }
            })
        binding.layoutOtpVerification.visibility = View.GONE
        binding.layoutPasswordReset.visibility = View.VISIBLE
        binding.layoutPasswordReset.alpha = 0.0f
        binding.layoutPasswordReset.animate().duration = 500
        binding.layoutPasswordReset.animate()
            .translationY(1F)
            .alpha(1.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    //binding.layoutForgotPassword.visibility = View.VISIBLE
                }
            })
    }

    private fun openOtpScreen() {
        binding.layoutForgotPassword.animate().translationY(0F)
            .alpha(0.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    //binding.layoutLogin.visibility = View.GONE
                }
            })
        binding.layoutForgotPassword.visibility = View.GONE
        binding.layoutOtpVerification.visibility = View.VISIBLE
        binding.layoutOtpVerification.alpha = 0.0f
        binding.layoutOtpVerification.animate().duration = 500
        binding.layoutOtpVerification.animate()
            .translationY(1F)
            .alpha(1.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    //binding.layoutForgotPassword.visibility = View.VISIBLE
                }
            })
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
        viewModel.signInPasswordErrorStatus.observe(this, Observer {
            if (it!!) setPasswordError()
        })
        viewModel.clearAllInputErrors.observe(this, Observer {
            if (it!!) clearAllErrors()
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
                binding.forgotPasswordResetBtn.startAnimation()
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
                    editor.putInt("cityId", loginResponseModel.data.city.id)
                    editor.putString("accessToken", loginResponseModel.accessToken)
                    editor.apply()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
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

        viewModel.statusOpenPasswordResetScreen.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it)
                    openPasswordResetScreen()

            }
        }

        viewModel.statusPassResetSuccess.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                binding.forgotPasswordResetBtn.revertAnimation()
                binding.forgotPasswordResetBtn.setBackgroundResource(R.drawable.rounded_edittext_bg)
                loginResponseModel = it
                //redirect to main landing screen
                if (loginResponseModel.statusCode== 200) {
                    openOtpScreen()
                    Log.i("TOKEN", loginResponseModel.token)
                    requestOtpFromFirebase("03365814305")
                }
            }
        }

        viewModel.statusFailure.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                binding.loginLoginBtn.revertAnimation()
                binding.loginLoginBtn.setBackgroundResource(R.drawable.rounded_edittext_bg)
                if ((it.statusCode == HttpStatusCodes.SC_UNAUTHORIZED) || (it.statusCode == HttpStatusCodes.SC_NO_CONTENT)) {
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
    }

    private fun requestOtpFromFirebase(phoneNumber: String) {
        val phoneNum = "+923365814305"
        val testVerificationCode = "123456"

// Whenever verification is triggered with the whitelisted number,
// provided it is not set for auto-retrieval, onCodeSent will be triggered.
        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNum)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(verificationId: String,forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    Log.i("MYTAG", verificationId)
                    //verificationCode = verificationId
                    // Save the verification id somewhere
                    // ...

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
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


//    private fun storeBranches() {
//        val daoBranch = BranchDatabase.getInstance(this).branchDAO
//        val repositoryBranch = BranchRespository(daoBranch)
//        val factoryBranch = BranchViewModelFactory(repositoryBranch)
//        branchViewmodel = ViewModelProvider(this, factoryBranch).get(BranchViewModel::class.java)
//        binding.branchViewModel = branchViewmodel
//        if (loginResponseModel.data!!.siteBranches.isNotEmpty()) {
//            branchViewmodel.clearAll()
//        }
//        for (element in loginResponseModel.data!!.siteBranches) {
//            branchViewmodel.insertBranch(
//                BranchEntityClass(
//                    0,
//                    element.value!!,
//                    element.label!!,
//                    element.code!!
//                )
//            )
//        }
//    }

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
