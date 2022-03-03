package com.switchsolutions.farmtohome.b2b.api


import com.switchsolutions.farmtohome.b2b.MainActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RestApiClient {
    companion object {

        private val retrofitWithHeaders : Retrofit by lazy {
            val okHttpClientBuild = OkHttpClient().newBuilder()
            okHttpClientBuild.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor())
            okHttpClientBuild.addInterceptor(getHeaderInterceptor())
            val okHttpClient = okHttpClientBuild.build()

            Retrofit.Builder()
                .baseUrl("http://farmtohome.com.pk/api/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        private val retrofitWithOutHeaders : Retrofit by lazy {
            val okHttpClientBuild = OkHttpClient().newBuilder()
            okHttpClientBuild.connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(httpLoggingInterceptor())
            val okHttpClient = okHttpClientBuild.build()

            Retrofit.Builder()
                    .baseUrl("http://farmtohome.com.pk/api/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }

        private val retrofitWithHeaders2 : Retrofit by lazy {
            val okHttpClientBuild = OkHttpClient().newBuilder()
            okHttpClientBuild.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor())
            okHttpClientBuild.addInterceptor(getHeaderInterceptor())
            val okHttpClient = okHttpClientBuild.build()

            Retrofit.Builder()
                .baseUrl("http://admintest.farmtohome.com.pk/api/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        private val retrofitWithOutHeaders2 : Retrofit by lazy {
            val okHttpClientBuild = OkHttpClient().newBuilder()
            okHttpClientBuild.connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(httpLoggingInterceptor())
            val okHttpClient = okHttpClientBuild.build()

            Retrofit.Builder()
                    .baseUrl("http://admintest.farmtohome.com.pk/api/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
        private fun getHeaderInterceptor(): Interceptor {
            return Interceptor { chain ->
                val token = "123"
                Timber.tag("Token").d(token)
                if (token != null) {
                    val request = chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                    chain.proceed(request)
                } else {
                    chain.proceed(chain.request())
                }
            }
        }

        private fun httpLoggingInterceptor(): HttpLoggingInterceptor {
            val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
                Timber.tag("RAW_RESPONSE").d(message)
            })
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            return interceptor
        }

        fun getClient(addHeaders : Boolean) : ApiCommService {
            return if(addHeaders){
                retrofitWithHeaders.create(ApiCommService::class.java)
            } else{
                retrofitWithOutHeaders.create(ApiCommService::class.java)
            }
        }

        fun getClient2(addHeaders : Boolean) : ApiCommService {
            return if(addHeaders){
                retrofitWithHeaders2.create(ApiCommService::class.java)
            } else{
                retrofitWithOutHeaders2.create(ApiCommService::class.java)
            }
        }
    }
}