package com.streamefy.component.ui.splash

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.window.SplashScreen
import androidx.core.animation.doOnEnd
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
import com.streamefy.utils.customAlfa
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashScreen : BaseFragment<FragmentSplashScreenBinding>() {
    override fun netStatus() {}
    override fun bindView(): Int = R.layout.fragment_splash_screen
    var isLogin = false
    var isResetPin = false
    var realnumer = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isLogin = SharedPref.getBoolean(PrefConstent.ISLOGIN)
        isResetPin = SharedPref.getBoolean(PrefConstent.ISRESET_PIN)
        realnumer = SharedPref.getString(PrefConstent.REALNUMBER).toString()

        binding.apply {

//            splashLayout.customAlfa(0f, 1f, 4000)
           // ivLauncher.customAlfa(0f, 1f, 4000)

            ObjectAnimator.ofFloat(splashLayout, "alpha", 0f, 1f).also {
                it.duration = 4000
                it.doOnEnd { lifecycleScope.launch {
                    delay(2000)
                    ObjectAnimator.ofFloat(splashLayout, "alpha", 1f, 0f).also { inner ->
                        inner.duration = 2000
                        inner.doOnEnd {
                          //  navigateToHome()
                        }
                        inner.start()
                    }
                }
                }
                it.start()
            }

            ObjectAnimator.ofFloat(ivLauncher, "alpha", 0f, 1f).also {
                it.duration = 4000
                it.doOnEnd { lifecycleScope.launch {
                    delay(2000)
                    ObjectAnimator.ofFloat(ivLauncher, "alpha", 1f, 0f).also { inner ->
                        inner.duration = 2000
                        inner.doOnEnd {
                            navigateToHome()
                        }
                        inner.start()
                    }
                    }
                }
                it.start()
            }

        }

//        lifecycleScope.launch {
//            delay(4000)
//            binding.apply {
//                splashLayout.customAlfa(1f, 0f,2000)
//               // ivLauncher.customAlfa(1f, 0f,2000)
//                val fadeout = ObjectAnimator.ofFloat(ivLauncher, "alpha", 1f, 0f)
//                fadeout.duration = 2000
//                fadeout.doOnEnd {
//                  //  withContext(Dispatchers.Main) {
////                logException()
//                        navigateToHome()
//                  //  }
//                }
//                fadeout.start()
//               // ivLauncher.animation=fadeout
//            }
//           // delay(1000)
//
//        }

//         causeNullPointerCrash()
    }

    private fun navigateToHome() {
        Log.e("sjndjsn", "realnumer $realnumer sknks $isLogin reset pin $isResetPin")
        if (isLogin) {
            // if (isResetPin){
            var bundle = Bundle()
            bundle.putBoolean(PrefConstent.ISHOME, true)
            findNavController().navigate(R.id.projectfragment, bundle)
//            }
//            else {
//                findNavController().navigate(R.id.homefragment)
//            }
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