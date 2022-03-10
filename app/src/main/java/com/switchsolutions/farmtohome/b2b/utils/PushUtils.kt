package com.switchsolutions.farmtohome.b2b.utils

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse
import timber.log.Timber

class PushUtils {
    companion object {

        const val NOTIFICATION_TYPE_BUNDLE_KEY = "type"
        const val NOTIFICATION_DATA_OBJECT_BUNDLE_KEY = "dataObject"

        private fun grabFcmToken(): String{
            var token= ""
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
                     token = task.result
                    Log.d(
                        "MYTAG",
                        token
                    )
                }
            return token
        }


        fun saveTokenForPush(context: Context) {
            val loggedInUser = LoginResponse.getStoredInstance(AppLauncher.ApplicationContext)
            val requestModel = PushTokenUpdateModel(loggedInUser.data.id ?: -1,
                    loggedInUser.accessToken,
                    grabFcmToken())
            Timber.tag("FirebaseToken,PushUtils")
            Timber.e(grabFcmToken())
        }

        fun deleteTokenForPush(context: Context) {
            val loggedInUser = LoginResponse.getStoredInstance(AppLauncher.ApplicationContext)
            val requestModel = PushTokenUpdateModel(loggedInUser.data.id ?: -1,
                    loggedInUser.accessToken,
                    grabFcmToken(),
                    PushTokenUpdateModel.getStoredInstance().deviceIdTokenId)
            Timber.tag("FirebaseToken,PushUtils")
            Timber.e(grabFcmToken())
        }
    }
}