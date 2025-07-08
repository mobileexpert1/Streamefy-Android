package com.streamefy.component.base

import android.app.Application
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.streamefy.component.ui.video.PlayerHandler
import com.streamefy.data.SharedPref
import com.streamefy.data.appModule
import com.streamefy.network.NetworkReceiver
import com.streamefy.network.NetworkStatusListener
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level

class MyApp : Application(),NetworkReceiver.NetworkStatusListener {
//    val startTime = System.currentTimeMillis()
private lateinit var networkReceiver: NetworkReceiver
 var statusListener: NetworkStatusListener?=null
    companion object {
        lateinit var player: ExoPlayer
    }
    override fun onCreate() {
//        if (FirebaseApp.getApps(this).isEmpty()) {
//            FirebaseApp.initializeApp(this)
//            Log.e("firebasecrass", "Firebase initialized: ${FirebaseApp.getApps(this).isNotEmpty()}")
//        }
        super.onCreate()
        statusListener?.onNetworkStatusChanged(false)
        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
            printLogger(Level.DEBUG)
        }

        SharedPref.init(this@MyApp)

        networkReceiver = NetworkReceiver(this)
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)



    }

    fun reinitializeKoin() {
        // Stop the current Koin context
        stopKoin()

        // Reinitialize Koin with the same or new configuration
        startKoin {
            androidContext(this@MyApp)
            modules(appModule)  // You can change the modules if needed
            printLogger(Level.DEBUG)
        }
    }

    fun setNetworkStatusListener(listener:NetworkStatusListener){
        statusListener=listener
    }

    override fun onTerminate() {
        super.onTerminate()
        // Unregister the receiver to prevent memory leaks
            unregisterReceiver(networkReceiver)

    }
    override fun onNetworkAvailable() {
        Log.e("BaseFragment", "Application network available")
        statusListener?.onNetworkStatusChanged(true)

    }

    override fun onNetworkUnavailable() {
        Log.e("BaseFragment", "Application network available")
        statusListener?.onNetworkStatusChanged(false)
    }

}