package com.switchsolutions.farmtohome.b2b.presentation.profile.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.CartViewModel
import com.switchsolutions.farmtohome.b2b.CartViewModelFactory
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.databinding.FragmentProfileNewBinding
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse
import com.switchsolutions.farmtohome.b2b.presentation.login.ui.LoginActivity
import com.switchsolutions.farmtohome.b2b.presentation.login.viewmodel.LogInViewModel
import com.switchsolutions.farmtohome.b2b.presentation.profile.viewmodel.ProfileViewModel
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartDatabase
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartRepository
import com.switchsolutions.farmtohome.b2b.utils.SharedPrefUtils
import com.switchsolutions.farmtohome.b2b.utils.Utilities

class ProfileFragment : Fragment() {
    companion object {
        lateinit var binding : FragmentProfileNewBinding
        const val TAG = "profileFragment"

    }
    private lateinit var loggedInUser : LoginResponse
    private lateinit var viewModel: ProfileViewModel
    private lateinit var cartViewModel: CartViewModel
    private val MY_PREFS_NAME = "FarmToHomeB2B"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileNewBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        loggedInUser = LoginResponse.getStoredInstance(requireContext())
        binding.loginResponseModel = loggedInUser
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dao = CartDatabase.getInstance(requireContext()).cartDAO
        val repository = CartRepository(dao)
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, factory).get(CartViewModel::class.java)
        binding.apply {
    btnLogout.setOnClickListener {
        logout()
    }
    editProfileChangeProfileLabel.setOnClickListener {
        openResetPasswordScreen()
    }

    changePasswordButton.setOnClickListener {
        changeOldPassword()
    }
   imageView23.setOnClickListener {
        openProfileScreen()
    }
    oldPasswordShowIcon.setOnClickListener {
        Utilities.instance!!.showAndHideIcon(changePasswordOldPasswordEt, oldPasswordShowIcon)
    }
    newPasswordShowIcon.setOnClickListener {
        Utilities.instance!!.showAndHideIcon(changePasswordNewPasswordEt, newPasswordShowIcon)
    }
    confirmNewPasswordShowIcon.setOnClickListener {
        Utilities.instance!!.showAndHideIcon(confirmChangePasswordNewPasswordEt, confirmNewPasswordShowIcon)
    }
}
        viewModel.statusUpdatePasswordSuccess.observe(viewLifecycleOwner) { it ->
            it.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), "Password Updated", Toast.LENGTH_SHORT).show()
                openProfileScreen()
            }
        }

    }

    private fun openProfileScreen() {
        binding.apply {
            layoutPasswordResetProfile.animate()
                .translationY(0F)
                .alpha(0.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        //binding.layoutLogin.visibility = View.GONE
                    }
                })
            changePasswordOldPasswordEt.setText("")
            changePasswordNewPasswordEt.setText("")
            confirmChangePasswordNewPasswordEt.setText("")
            changePasswordButton.revertAnimation()
            changePasswordButton.setBackgroundResource(R.drawable.rounded_edittext_bg)

            layoutPasswordResetProfile.visibility = View.GONE
            layoutProfile.visibility = View.VISIBLE
            layoutProfile.alpha = 0.0f
            layoutProfile.animate().duration = 500
            layoutProfile.animate()
                .translationY(0F)
                .alpha(1.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        //binding.layoutForgotPassword.visibility = View.VISIBLE
                    }
                })
        }
    }

    private fun openResetPasswordScreen() {
            binding.layoutProfile.animate()
                .translationY(0F)
                .alpha(0.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        //binding.layoutLogin.visibility = View.GONE
                    }
                })
            binding.layoutProfile.visibility = View.GONE
            binding.layoutPasswordResetProfile.visibility = View.VISIBLE
            binding.layoutPasswordResetProfile.alpha = 0.0f
            binding.layoutPasswordResetProfile.animate().duration = 500
            binding.layoutPasswordResetProfile.animate()
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
    fun changeOldPassword() {
        binding.apply {
        if (changePasswordOldPasswordEt.text.toString().trim { it <= ' ' }.length < 8) {
            changePasswordOldPasswordEt.error = "Password cannot be less then 8 characters"
        } else if (changePasswordNewPasswordEt.text.toString().trim { it <= ' ' }.length < 8) {
            changePasswordNewPasswordEt.error = "Password cannot be less then 8 characters"
        } else if (changePasswordNewPasswordEt.text.toString() != confirmChangePasswordNewPasswordEt.text.toString()) {
            confirmChangePasswordNewPasswordEt.error = "Password doesn't match the above entered password"
        } else {
                val changePasswordObject = JsonObject()
                changePasswordObject.addProperty("password", changePasswordOldPasswordEt.text.toString())
                changePasswordObject.addProperty("newPassword", confirmChangePasswordNewPasswordEt.text.toString())
                changePasswordButton.startAnimation()
               viewModel.startObserver(changePasswordObject)

        }
    }
    }
    fun logout() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.signing_out_warning))
            .setPositiveButton(getString(R.string.sign_out)) { dialog, _ ->
                // logout user
                SharedPrefUtils.getInstance(AppLauncher.ApplicationContext).logoutUser()
                requireActivity().startActivity(Intent(activity, LoginActivity::class.java))
                dialog.dismiss()
                val editor = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE).edit()
                editor.putInt("badgeCount", 0)
                editor.putInt("customerId", 0) //0 is the default value.
                editor.apply()
                cartViewModel.clearAll()
                requireActivity().finish()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                // cancel logout process
                dialog.dismiss()
            }
        builder.show()
    }



}