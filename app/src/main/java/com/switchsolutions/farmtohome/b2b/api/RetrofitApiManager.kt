package com.switchsolutions.farmtohome.b2b.api

import android.content.Context
import com.google.gson.Gson
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.callbacks.ErrorDto
import com.switchsolutions.farmtohome.b2b.callbacks.HttpStatusCodes
import com.switchsolutions.farmtohome.b2b.callbacks.IOnTokenRefreshed
import com.switchsolutions.farmtohome.b2b.utils.ConnectionDetector
import com.switchsolutions.farmtohome.b2b.utils.NotificationUtil
import com.switchsolutions.farmtohome.b2b.utils.enums.Type
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


abstract class RetrofitApiManager<T>(context: Context) : Callback<T>, IOnTokenRefreshed {

    private var mContext: Context = context
    private var TAG = this.javaClass.simpleName

    init {
        Timber.tag(TAG)
    }

    fun <A : Call<T>> callServer(t: A) {
        if (ConnectionDetector(mContext).isConnectingToInternet()) {
            if (!t.isExecuted) t.enqueue(this)
        } else {
            NotificationUtil.showShortToast(mContext, mContext.getString(R.string.no_internet), Type.DANGER)
            responseFailure(ErrorDto(mContext.getString(R.string.no_internet), 0))
        }
    }

    override fun onResponse(call: Call<T>?, response: Response<T>?) {
        //check for null response
        if (response?.isSuccessful!! && response.body() != null) {
            //checking for every code
            when {
                response.code() == HttpStatusCodes.SC_OK -> responseSuccess(response.body())
                response.code() == HttpStatusCodes.SC_CREATED -> responseSuccess(response.body())
                response.code() == HttpStatusCodes.SC_NO_CONTENT -> {
                    Timber.d(mContext.getString(R.string.no_content))
                    responseFailure(ErrorDto(mContext.getString(R.string.no_content), response.code()))
                }
                response.code() == HttpStatusCodes.SC_UNAUTHORIZED -> {
                    Timber.d(mContext.getString(R.string.unauthrozed))
//                    responseFailure(ErrorDto(mContext.getString(R.string.unauthrozed), HttpStatusCodes.SC_UNAUTHORIZED))
                }
                else -> responseFailure(ErrorDto(response.message(), response.code()))
            }
        }else{
            if(response.code() == HttpStatusCodes.SC_UNAUTHORIZED && (!call?.request()?.url?.toString()?.startsWith("http://farmtohome.com.pk/api/")!!)) {
                Timber.d(mContext.getString(R.string.unauthrozed))
//                    responseFailure(ErrorDto(mContext.getString(R.string.unauthrozed), HttpStatusCodes.SC_UNAUTHORIZED))
            }else{
                try {
                    responseFailure(Gson().fromJson(response.errorBody()?.string()!!, ErrorDto::class.java))
                } catch (e: Exception) {
                    Timber.e(e)
                    responseFailure(ErrorDto(mContext.getString(R.string.error_occurred), 0))
                }
            }
        }
    }

    override fun onFailure(call: Call<T>?, t: Throwable?) {
        t?.printStackTrace()
        if(ConnectionDetector(mContext).isConnectingToInternet()){
            responseFailure(ErrorDto(mContext.getString(R.string.error_occurred), 0))
        }
        responseFailure(ErrorDto(mContext.getString(R.string.no_internet), 0))
    }

    override fun onTokenRefreshed(){
        tokenRefreshed()
    }

    abstract fun onSuccess(t: T?)

    abstract fun onFailure(t: ErrorDto)

    abstract fun tokenRefreshed()

    private fun responseSuccess(t : T?){
        onSuccess(t)
    }

    private fun responseFailure(t: ErrorDto) {
        onFailure(t)
    }
}