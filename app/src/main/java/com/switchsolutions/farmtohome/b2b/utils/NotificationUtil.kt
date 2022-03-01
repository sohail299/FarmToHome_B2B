package com.switchsolutions.farmtohome.b2b.utils

import android.content.Context
import com.switchsolutions.farmtohome.b2b.utils.enums.Cue
import com.switchsolutions.farmtohome.b2b.utils.enums.Duration
import com.switchsolutions.farmtohome.b2b.utils.enums.Type

class NotificationUtil {

    companion object {

        private const val textSize : Int = 13

        fun showShortToast(context : Context, msg : String, type : Type){
            Cue.init()!!
                    .with(context)
                    .setMessage(msg)
                    .setDuration(Duration.SHORT)
                    .setType(type)
                    .setTextSize(textSize)
                    .show()
        }

        fun showShortToast(context : Context, msg : Int, type : Type){
            Cue.init()!!
                    .with(context)
                    .setMessage(context.getString(msg))
                    .setDuration(Duration.SHORT)
                    .setType(type)
                    .setTextSize(textSize)
                    .show()
        }

        fun showLongToast(context : Context, msg : String, type : Type){
            Cue.init()!!
                    .with(context)
                    .setMessage(msg)
                    .setDuration(Duration.LONG)
                    .setType(type)
                    .setTextSize(textSize)
                    .show()
        }

        fun showLongToast(context : Context, msg : Int, type : Type){
            Cue.init()!!
                    .with(context)
                    .setMessage(context.getString(msg))
                    .setDuration(Duration.LONG)
                    .setType(type)
                    .setTextSize(textSize)
                    .show()
        }
    }
}