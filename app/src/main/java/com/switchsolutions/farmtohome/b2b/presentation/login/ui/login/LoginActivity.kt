package com.switchsolutions.farmtohome.b2b.presentation.login.ui.login


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.switchsolutions.farmtohome.b2b.MainActivity
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.callbacks.HttpStatusCodes
import com.switchsolutions.farmtohome.b2b.databinding.ActivityLoginNewBinding
import com.switchsolutions.farmtohome.b2b.presentation.login.data.requestmodel.LoginRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.login.data.responsemodel.login.LoginResponse
import com.switchsolutions.farmtohome.b2b.presentation.login.data.viewmodel.LogInViewModel


class LoginActivity : AppCompatActivity(),
    View.OnClickListener/*, ICallBackListener<LoginResponseModel>*/ {
    lateinit var binding: ActivityLoginNewBinding
    private lateinit var loginRequestModel: LoginRequestModel
    private lateinit var loginResponseModel: LoginResponse
    private lateinit var viewModel: LogInViewModel
    private val MY_PREFS_NAME = "FarmToHomeBDO"
    //private lateinit var branchViewmodel: BranchViewModel


    companion object {
        const val SIGNIN_FRAGMENT_TAG: String = "signInFrag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        startObservers()
        binding.loginLoginBtn.setOnClickListener(this)
        binding.loginForgotPasswordTv.setOnClickListener(this)
        binding.backButtonForgotPassword.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id!!) {
            binding.loginLoginBtn.id -> viewModel.signInClicked()
            binding.loginForgotPasswordTv.id -> openPasswordScreen()
            binding.backButtonForgotPassword.id -> openLoginScreen()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (binding.layoutForgotPassword.isVisible)
            openLoginScreen()

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

    private fun startObservers() {
        viewModel.getLoginRequestModel().observe(this, Observer {
            loginRequestModel = it!!
            binding.loginRequestModel = loginRequestModel
        })
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
        viewModel.apiResponseSuccess.observe(this, Observer {
          //  if (waitDialog.isShowing) waitDialog.dismiss()
            binding.loginLoginBtn.revertAnimation()
            binding.loginLoginBtn.setBackgroundResource(R.drawable.rounded_edittext_bg)
            loginResponseModel = it
            //redirect to main landing screen
            if (loginResponseModel.data.type == 2) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
        viewModel.apiResponseFailure.observe(this, Observer {
            binding.loginLoginBtn.revertAnimation()
            binding.loginLoginBtn.setBackgroundResource(R.drawable.rounded_edittext_bg)
            if ((it?.statusCode == HttpStatusCodes.SC_UNAUTHORIZED) || (it?.statusCode == HttpStatusCodes.SC_NO_CONTENT)) {
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
        })
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
