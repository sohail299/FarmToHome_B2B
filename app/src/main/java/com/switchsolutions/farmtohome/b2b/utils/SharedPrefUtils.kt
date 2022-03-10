package com.switchsolutions.farmtohome.b2b.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.City
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.Data
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.Info
import com.switchsolutions.farmtohome.b2b.presentation.login.data.response.LoginResponse


class SharedPrefUtils {

    var mContext: Context? = null
    private var mSharedPreferences: SharedPreferences
    private var mSharedPreferencesEditor: SharedPreferences.Editor

    private constructor(context: Context) {
        mContext = context
        mSharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        mSharedPreferencesEditor = mSharedPreferences.edit()
    }

    /**
     * Creates single instance of SharedPreferenceUtils
     *
     * @param context context of Activity or Service
     * @return Returns instance of SharedPreferenceUtils
     */
    companion object {
        private var mSharedPreferenceUtils: SharedPrefUtils? = null

        //keys
        const val USER_PROFILE = "userProfile"
        const val DEVICE_TOKEN_ID_FIREBASE = "devicetokenIdfirebasess"

        @Synchronized
        fun getInstance(context: Context): SharedPrefUtils {

            if (mSharedPreferenceUtils == null) {
                mSharedPreferenceUtils = SharedPrefUtils(context.applicationContext)
            }
            return mSharedPreferenceUtils!!
        }
    }

    /**
     * Stores String value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    fun setValue(key: String, value: String) {
        mSharedPreferencesEditor.putString(key, value)
        mSharedPreferencesEditor.commit()
    }


    /**Logout user**/
    fun logoutUser(){
        setValue(USER_PROFILE, Gson().toJson(
            LoginResponse("", Data("",
            City("",0, ""),"","",0, Info("","","","","","",
                ""),0,"","","",
            "","",0,0,"","",""), 0,
            "", "", 0, "","")
        ))
    }
    /**
     * Stores int value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    fun setValue(key: String, value: Int) {
        mSharedPreferencesEditor.putInt(key, value)
        mSharedPreferencesEditor.commit()
    }

    /**
     * Stores Double value in String format in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    fun setValue(key: String, value: Double) {
        setValue(key, java.lang.Double.toString(value))
    }

    /**
     * Stores long value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    fun setValue(key: String, value: Long) {
        mSharedPreferencesEditor.putLong(key, value)
        mSharedPreferencesEditor.commit()
    }

    /**
     * Stores boolean value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    fun setValue(key: String, value: Boolean) {
        mSharedPreferencesEditor.putBoolean(key, value)
        mSharedPreferencesEditor.commit()
    }

    /**
     * Retrieves String value from preference
     *
     * @param key          key of preference
     * @param defaultValue default value if no key found
     */
    fun getStringValue(key: String, defaultValue: String): String? {
        return mSharedPreferences.getString(key, defaultValue)
    }

    /**
     * Retrieves int value from preference
     *
     * @param key          key of preference
     * @param defaultValue default value if no key found
     */
    fun getIntValue(key: String, defaultValue: Int): Int {
        return mSharedPreferences.getInt(key, defaultValue)
    }

    /**
     * Retrieves long value from preference
     *
     * @param key          key of preference
     * @param defaultValue default value if no key found
     */
    fun getLongValue(key: String, defaultValue: Long): Long {
        return mSharedPreferences.getLong(key, defaultValue)
    }

    /**
     * Retrieves boolean value from preference
     *
     * @param keyFlag      key of preference
     * @param defaultValue default value if no key found
     */
    fun getBoolanValue(keyFlag: String, defaultValue: Boolean): Boolean {
        return mSharedPreferences.getBoolean(keyFlag, defaultValue)
    }

    /**
     * Removes key from preference
     *
     * @param key key of preference that is to be deleted
     */
    fun removeKey(key: String) {
        mSharedPreferencesEditor.remove(key)
        mSharedPreferencesEditor.commit()
    }

    /**
     * Clears all the preferences stored
     */
    fun clear() {
        mSharedPreferencesEditor.clear().commit()
    }
}