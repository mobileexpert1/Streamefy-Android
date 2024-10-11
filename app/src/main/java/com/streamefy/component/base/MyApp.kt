package com.streamefy.component.base

import android.app.Application
import android.util.Log
import com.streamefy.data.SharedPref
import com.streamefy.data.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApp : Application() {
    val startTime = System.currentTimeMillis()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
            printLogger(Level.DEBUG)
        }
        Log.e("MyApp", "Koin initialized in ${System.currentTimeMillis() - startTime} ms")

        SharedPref.init(this@MyApp)
        Log.e("MyApp", "SharedPref initialized in ${System.currentTimeMillis() - startTime} ms")
    }
}