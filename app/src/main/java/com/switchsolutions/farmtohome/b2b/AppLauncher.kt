package com.switchsolutions.farmtohome.b2b

import android.content.Context
import androidx.multidex.MultiDexApplication
import timber.log.Timber

class AppLauncher : MultiDexApplication() {

    companion object {
        lateinit var ApplicationContext : Context
    }

    override fun onCreate() {
        super.onCreate()
        ApplicationContext = this
        Timber.plant(Timber.DebugTree())
        Timber.tag("B2B")
    }


}