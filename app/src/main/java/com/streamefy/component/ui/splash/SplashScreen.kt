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
import com.streamefy.BuildConfig
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.MyApp
import com.streamefy.component.ui.login.LoginViewmodel
import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentSplashScreenBinding
import com.streamefy.network.Constants
import com.streamefy.network.MyResource
import com.streamefy.utils.customAlfa
import com.streamefy.utils.imageLoadonLayout
import com.streamefy.utils.loadUrl
import com.streamefy.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashScreen : BaseFragment<FragmentSplashScreenBinding>() {
    override fun netStatus() {}
    override fun bindView(): Int = R.layout.fragment_splash_screen
    var isLogin = false
    var isResetPin = false
    var realnumer = ""
    var admin_email = ""
    var admin_password = ""

    val viewmodel: LoginViewmodel by viewModel()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isLogin = SharedPref.getBoolean(PrefConstent.ISLOGIN)
        isResetPin = SharedPref.getBoolean(PrefConstent.ISRESET_PIN)
        realnumer = SharedPref.getString(PrefConstent.REALNUMBER).toString()

        admin_email = BuildConfig.Admin_email
        admin_password = BuildConfig.Password
        binding.apply {

            viewmodel.login(requireActivity(), LoginRequest(admin_email, admin_password))
            observe()
//            splashLayout.customAlfa(0f, 1f, 4000)
           // ivLauncher.customAlfa(0f, 1f, 4000)

            ObjectAnimator.ofFloat(splashLayout, "alpha", 0f, 1f).also {
                it.duration = 2000
                it.doOnEnd { lifecycleScope.launch {
                    delay(1000)
                    ObjectAnimator.ofFloat(splashLayout, "alpha", 1f, 0f).also { inner ->
                        inner.duration = 1000
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
                it.duration = 2000
                it.doOnEnd { lifecycleScope.launch {
                    delay(1000)
                    ObjectAnimator.ofFloat(ivLauncher, "alpha", 1f, 0f).also { inner ->
                        inner.duration = 1000
                        inner.doOnEnd {
                        }
                        inner.start()
                    }
                    delay(500)
                    navigateToHome()

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


    fun observe() {
        viewmodel.loginLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    ///loading
                  //  progressDialog.show()
                }

                is MyResource.isSuccess -> {
                    try {

                        var data = it.data?.response

                        Log.e("sncskn", "skncsknv $data")
                        SharedPref.setString(PrefConstent.TOKEN, "")
                        data?.run {
                            SharedPref.setString(PrefConstent.TOKEN, accessToken)
                            SharedPref.setString(PrefConstent.REFRESH_TOKEN, refreshToken)
                            SharedPref.setString(PrefConstent.APP_LOGO, data.logo)
                            if (data.backgroundTheme=="LIGHT"){
                                SharedPref.setBoolean(PrefConstent.IS_DARK, false)
                            }else{
                                SharedPref.setBoolean(PrefConstent.IS_DARK, true)
                            }
                            data.backgroundImage.run {
                                SharedPref.setString(PrefConstent.AUTH_BACKGROUND, data.backgroundImage)
                            }
                        }
                      //  progressDialog.dismiss()
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        throw RuntimeException("login getotp")
                    }
                }

                is MyResource.isError -> {
                   // progressDialog.dismiss()
                }

                else -> {}
            }
        }
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