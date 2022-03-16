package com.switchsolutions.farmtohome.b2b

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse
import com.switchsolutions.farmtohome.b2b.presentation.login.ui.LoginActivity

class LaunchActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userModel = LoginResponse.getStoredInstance(this)
        if (userModel.data.id != null
            && userModel.data.id != 0) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        else
        {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        }

    }
}