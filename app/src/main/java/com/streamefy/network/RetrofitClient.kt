package com.streamefy.network

import android.content.Context
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.streamefy.component.base.MyApp
import com.streamefy.data.SharedPref
import com.streamefy.network.Constants.dummy_token
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    fun retrofit(context: Context): Retrofit {
        val gson = GsonBuilder().setLenient().create()
        var okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)

        val token = if (!SharedPref.getBoolean(Constants.isLogin)) "" else SharedPref.getString(Constants.TOKEN)

        okHttpClient.addInterceptor { chain ->
            var origin = chain.request()
            var newRequest = origin.newBuilder()
                .addHeader("accept", "text/plain")
                .addHeader("Content-Type", "application/json")
                .apply {
                    if (token?.isNotEmpty()!!) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }                .method(origin.method(), origin.body())
                .build()
            chain.proceed(newRequest)
        }
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY


        okHttpClient.addInterceptor(interceptor)
        var retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .baseUrl(ServerUrls.BASE_URL)
            .client(okHttpClient.build())
            .build()
        return retrofit
    }


}