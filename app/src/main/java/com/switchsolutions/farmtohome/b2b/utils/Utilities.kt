package com.switchsolutions.farmtohome.b2b.utils

import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import com.switchsolutions.farmtohome.b2b.R
import java.util.*

class Utilities {

    fun moveOTPBack(currentEditText: EditText, previousEditText: EditText) {
        currentEditText.setOnKeyListener(View.OnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
            try {
                if (event.action != KeyEvent.ACTION_DOWN)
                     true
                if (keyCode == KeyEvent.KEYCODE_DEL && currentEditText.getText().length == 0) {
                    val lengthTxt: Int = currentEditText.getText().length
                    if (lengthTxt == 0) {
                        previousEditText.requestFocus()
                    }
                }
            } catch (e: ArithmeticException) {
                e.printStackTrace()
            }
            false
        })
        previousEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 1) {
                    currentEditText.requestFocus()
                }
            }
        })
    }
    fun showAndHideIcon(editText: EditText, imageView: ImageView) {
        if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
            imageView.setBackgroundResource(R.drawable.password_show_icon)
            //Show Password
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            imageView.setBackgroundResource(R.drawable.password_hide_icon)

            //Hide Password
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
        }
        editText.setSelection(editText.text.length)
    }
    companion object {
        private var utilities: Utilities? = null
        val instance: Utilities?
            get() {
                if (utilities == null) utilities = Utilities()
                return utilities
            }
    }
}