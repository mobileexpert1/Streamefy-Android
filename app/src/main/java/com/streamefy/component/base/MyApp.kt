package com.streamefy.component.base

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.streamefy.data.SharedPref
import com.streamefy.data.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApp : Application() {
//    val startTime = System.currentTimeMillis()

    override fun onCreate() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
            Log.e("firebasecrass", "Firebase initialized: ${FirebaseApp.getApps(this).isNotEmpty()}")
        }
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
            printLogger(Level.DEBUG)
        }

        SharedPref.init(this@MyApp)

//        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
//            // Log the exception to Crashlytics
//            FirebaseCrashlytics.getInstance().recordException(throwable)
//            Log.e("appdede", "Uncaught exception: ${throwable.message}", throwable)
//            // Optionally rethrow the exception to let the app crash
//            throw throwable//  RuntimeException("Application cll")
//        }


    }
}