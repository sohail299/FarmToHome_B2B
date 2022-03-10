package com.switchsolutions.farmtohome.b2b.presentation.login.data.response

import android.content.Context
import com.google.gson.Gson
import com.switchsolutions.farmtohome.b2b.utils.SharedPrefUtils

data class LoginResponse(
    val accessToken: String,
    val data: Data,
    val expiresIn: Int,
    val message: String,
    val status: String,
    val statusCode: Int,
    val tokenType: String,
    val token: String
){
    companion object{
        fun getStoredInstance(context: Context): LoginResponse {
            var loginUserModel: LoginResponse
            val json = SharedPrefUtils.getInstance(context).getStringValue(SharedPrefUtils.USER_PROFILE, "")
            loginUserModel = if (json!!.isNotBlank()) {
                Gson().fromJson(json, LoginResponse::class.java)
            } else {
                LoginResponse("", Data("",
                    City("",0, ""),"","",0,Info("","","","","","",
                        ""),0,"","","",
                "","",0,0,"","",""), 0,
                    "", "", 0, "","")
            }
            return loginUserModel
        }
    }

}