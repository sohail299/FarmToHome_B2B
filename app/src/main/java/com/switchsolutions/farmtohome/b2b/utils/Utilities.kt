package com.switchsolutions.farmtohome.b2b.utils

import android.app.Service
import android.content.Context
import android.os.Process
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import java.lang.ArithmeticException
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Utilities {

    fun MoveOTPBack(currentEditText: EditText, previousEditText: EditText) {
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

    companion object {
        private var utilities: Utilities? = null
        val instance: Utilities?
            get() {
                if (utilities == null) utilities = Utilities()
                return utilities
            }
    }
}