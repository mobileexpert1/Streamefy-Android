package com.streamefy.component.ui.splash

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.window.SplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.MyApp
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentSplashScreenBinding
import com.streamefy.network.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : BaseFragment<FragmentSplashScreenBinding>() {
    override fun netStatus() {}
    override fun bindView(): Int = R.layout.fragment_splash_screen
    var isLogin = false
    var realnumer=""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isLogin = SharedPref.getBoolean(PrefConstent.ISLOGIN)
        realnumer = SharedPref.getString(PrefConstent.REALNUMBER).toString()
        lifecycleScope.launch {
            delay(2000)
            logException()
        }

//         causeNullPointerCrash()
    }

    private fun navigateToHome() {
        Log.e("sjndjsn", "realnumer $realnumer sknks $isLogin")
        if (isLogin) {
            findNavController().navigate(R.id.homefragment)
        } else {
            findNavController().navigate(R.id.loginFragment)
        }
    }

    fun logException() {
        try {
            navigateToHome()
        } catch (e: Exception) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(e) // Log the exception
            throw RuntimeException("Splash screen navigation")
        }
    }


    private fun causeNullPointerCrash() {
        val nullObject: String? = null
        // This will cause a NullPointerException
        try {
            val length = nullObject!!.length
        } catch (e: Exception) {

//            val crashlytics = FirebaseCrashlytics.getInstance()
//            crashlytics.recordException(e) // Log the exception
//            throw RuntimeException("Splash Error 5")

//            throwerror("Splash new")
        }
    }
}