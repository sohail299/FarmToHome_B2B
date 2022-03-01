package com.switchsolutions.farmtohome.b2b.utils.enums

import android.view.Gravity
import android.widget.Toast
import android.view.LayoutInflater
import android.widget.TextView
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.switchsolutions.farmtohome.b2b.R

class Cue {
    private var context: Context? = null
    var message: String? = null
        private set
    var textSize = 14
        private set
    var gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        private set
    var textGravity = Gravity.CENTER
    var duration: Duration? = null
        private set
    var fontFace = ""
        private set
    var type: Type? = null
        private set
    var cornerRadius = 8
        private set
    var borderWidth = 1
        private set
    var padding = 16
        private set
    private var custom_background_color = ColorRes.primary_background_color
    private var custom_border_color = ColorRes.primary_border_color
    private var custom_text_color = ColorRes.primary_text_color
    var isHideToast = false
    fun setPadding(padding: Int): Cue {
        this.padding = padding
        return this
    }

    fun setBorderWidth(borderWidth: Int): Cue {
        this.borderWidth = borderWidth
        return this
    }

    fun setCornerRadius(cornerRadius: Int): Cue {
        this.cornerRadius = cornerRadius
        return this
    }

    fun setFontFace(fontFaceString: String): Cue {
        fontFace = fontFaceString
        return this
    }

    fun setCustomFontColor(bgcolor: Int, txtcolor: Int, bordercolor: Int): Cue {
        custom_background_color = bgcolor
        custom_border_color = bordercolor
        custom_text_color = txtcolor
        return this
    }

    fun setTextSize(textSize: Int): Cue {
        this.textSize = textSize
        return this
    }

    fun with(context: Context?): Cue {
        this.context = context
        return this
    }

    fun setType(type: Type?): Cue {
        this.type = type
        return this
    }

    fun setMessage(message: String?): Cue {
        this.message = message
        return this
    }

    fun setGravity(gravity: Int): Cue {
        this.gravity = gravity
        return this
    }

    fun setDuration(duration: Duration?): Cue {
        this.duration = duration
        return this
    }

    fun show() {
        if (context == null) return  // do nothing if context is now null (avoid NullPointerException)
        val toast = Toast(context!!.applicationContext)
        val view = LayoutInflater.from(context).inflate(R.layout.content_custome_toast, null, false)
        val custom_text = view.findViewById<TextView>(R.id.custom_text)
        custom_text.gravity = textGravity
        custom_text.text = message
        //        custom_text.setTextSize(textSize);
        custom_text.setPadding(padding, padding, padding, padding)
        if (!fontFace.isEmpty()) {
            val typeface = Typeface.createFromAsset(context!!.assets, fontFace)
            custom_text.typeface = typeface
        }
        getShape(type, custom_text)
        toast.duration = if (duration == Duration.LONG) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        toast.setGravity(gravity, 0, 120)
        toast.view = view
        toast.show()
        if (isHideToast) {
            view.setOnClickListener { v: View? -> toast.cancel() }
        }
    }

    private fun getShape(type: Type?, custom_text: TextView) {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadius = cornerRadius.toFloat()
        when (type) {
            Type.SUCCESS -> {
                shape.setColor(ColorRes.success_background_color)
                shape.setStroke(borderWidth, ColorRes.success_border_color)
                custom_text.setTextColor(ColorRes.success_text_color)
            }
            Type.SECONDARY -> {
                shape.setColor(ColorRes.secondary_background_color)
                shape.setStroke(borderWidth, ColorRes.secondary_border_color)
                custom_text.setTextColor(ColorRes.secondary_text_color)
            }
            Type.DANGER -> {
                shape.setColor(ColorRes.danger_background_color)
                shape.setStroke(borderWidth, ColorRes.danger_border_color)
                custom_text.setTextColor(ColorRes.danger_text_color)
            }
            Type.WARNING -> {
                shape.setColor(ColorRes.warning_background_color)
                shape.setStroke(borderWidth, ColorRes.warning_border_color)
                custom_text.setTextColor(ColorRes.warning_text_color)
            }
            Type.INFO -> {
                shape.setColor(ColorRes.info_background_color)
                shape.setStroke(borderWidth, ColorRes.info_border_color)
                custom_text.setTextColor(ColorRes.info_text_color)
            }
            Type.LIGHT -> {
                shape.setColor(ColorRes.light_background_color)
                shape.setStroke(borderWidth, ColorRes.light_border_color)
                custom_text.setTextColor(ColorRes.light_text_color)
            }
            Type.DARK -> {
                shape.setColor(ColorRes.dark_background_color)
                shape.setStroke(borderWidth, ColorRes.dark_border_color)
                custom_text.setTextColor(ColorRes.dark_text_color)
            }
            Type.CUSTOM -> {
                shape.setColor(custom_background_color)
                shape.setStroke(borderWidth, custom_border_color)
                custom_text.setTextColor(custom_text_color)
            }
            else -> {
                shape.setColor(ColorRes.primary_background_color)
                shape.setStroke(borderWidth, ColorRes.primary_border_color)
                custom_text.setTextColor(ColorRes.primary_text_color)
            }
        }
        custom_text.background = shape
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var cue: Cue? = null
        fun init(): Cue? {
            if (cue == null) cue = Cue()
            return cue
        }
    }
}