package com.switchsolutions.farmtohome.b2b.utils

import com.google.gson.Gson
import com.switchsolutions.farmtohome.b2b.AppLauncher

data class PushTokenUpdateModel(
        val userId: Int? = null,
        val userGuid: String? = null,
        val userDeviceToken: String? = null,
        val deviceIdTokenId: Int? = null
){
    companion object {
        fun getStoredInstance() : PushTokenUpdateModel {
            val pushTokenUpdateModel: PushTokenUpdateModel
            val json = SharedPrefUtils.getInstance(AppLauncher.ApplicationContext).getStringValue(SharedPrefUtils.DEVICE_TOKEN_ID_FIREBASE, "")
            pushTokenUpdateModel = if (json!!.isNotBlank()) {
                Gson().fromJson(json, PushTokenUpdateModel::class.java)
            } else {
                PushTokenUpdateModel(0, "", "", 0)
            }
            return pushTokenUpdateModel
        }

        fun addOrUpdateModel(pushModel : PushTokenUpdateModel){
            //saving user push details
            val sharedPref = SharedPrefUtils.getInstance(AppLauncher.ApplicationContext)
            sharedPref.setValue(SharedPrefUtils.DEVICE_TOKEN_ID_FIREBASE, Gson().toJson(pushModel))
        }
    }
}
