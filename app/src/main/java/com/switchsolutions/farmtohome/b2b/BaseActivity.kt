package com.switchsolutions.farmtohome.b2b

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.switchsolutions.farmtohome.b2b.presentation.login.ui.login.LoginActivity
import timber.log.Timber

class BaseActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

//        val token = FirebaseMessaging.getInstance().token
//
//        Log.i("MYTAG" , token.result.toString())
        val i = Intent(this, LoginActivity::class.java)
        if (intent.extras != null) {
            i.putExtras(intent.extras!!)
        }
        startActivity(i)
        finish()
    }
}