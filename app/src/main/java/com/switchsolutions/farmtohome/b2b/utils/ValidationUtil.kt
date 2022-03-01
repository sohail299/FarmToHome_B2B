package com.switchsolutions.farmtohome.b2b.utils

import java.util.regex.Pattern

class ValidationUtil {
    companion object {

        private val EMAIL_PATTERN : String = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        private val NUMBER_PATTERN : String = "^(03[0-6])[0-9]{8}\$"

        fun isEmailValid(email : String) : Boolean{
            val pattern = Pattern.compile(EMAIL_PATTERN)
            return pattern.matcher(email).matches()
        }

        fun isPasswordValid(password : String) : Boolean{
            return password.length > 5
        }

        fun isPhoneNumberValid(msisdn : String) : Boolean{
           // return msisdn.length in 10..12
            val pattern = Pattern.compile(NUMBER_PATTERN)
            return msisdn.isNotBlank() && pattern.matcher(msisdn).matches()
        }

        fun isUserNameValid(userName : String) : Boolean{
            return userName.isNotBlank()
        }

        fun arePasswordsEqual(password:String, confirmPassword: String) : Boolean{
            return password.equals(confirmPassword)
        }

        fun isANumber(str: String): Boolean {
            val pattern = Pattern.compile(NUMBER_PATTERN)
            return str.isNotBlank() && pattern.matcher(str).matches()
        }

        fun isVerificationCodeValid(str: String): Boolean {
            return str.length > 4
        }


    }
}