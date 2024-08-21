package com.streamefy.component.base

import android.app.Application
import com.streamefy.data.SharedPref
import com.streamefy.data.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
   companion object  var pref: SharedPref?=null
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
        }
        pref=  SharedPref(this)

    }
}