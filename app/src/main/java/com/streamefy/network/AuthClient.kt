package com.streamefy.network

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object AuthClient {
    fun retrofit(context: Context): Retrofit {

        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())


        val gson = GsonBuilder().setLenient().create()
        var okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)

        val token = if (!SharedPref.getBoolean(PrefConstent.ISLOGIN)) "" else SharedPref.getString(
            PrefConstent.TOKEN
        )

        Log.e("sjkdnskjnf", "sknbcksj$token")

        okHttpClient.addInterceptor { chain ->
            var origin = chain.request()
            var newRequest = origin.newBuilder()
                .addHeader("accept", "text/plain")
                .addHeader("Content-Type", "application/json")
                .apply {
                    if (token?.isNotEmpty()!!) {
                        addHeader("Authorization", "Bearer ${token}")
                    }
                }.method(origin.method, origin.body)
                .build()
            chain.proceed(newRequest)
        }
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY


        okHttpClient.addInterceptor(interceptor)
        var retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .baseUrl(ServerUrls.BASE_AUTH_URL)
            .client(okHttpClient.build())
            .build()
        return retrofit
    }

}