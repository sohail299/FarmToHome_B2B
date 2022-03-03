package com.switchsolutions.farmtohome.b2b

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import timber.log.Timber

class AppLauncher : MultiDexApplication() {

    companion object {
        lateinit var ApplicationContext : Context
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        ApplicationContext = this
        Timber.plant(Timber.DebugTree())
        Timber.tag("B2B")
    }


}