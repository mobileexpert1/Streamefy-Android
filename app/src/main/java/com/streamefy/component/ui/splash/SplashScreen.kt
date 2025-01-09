package com.streamefy.component.ui.splash

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.streamefy.BuildConfig
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.ui.login.LoginViewmodel
import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentSplashScreenBinding
import com.streamefy.network.MyResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
//            get application details from server
            viewmodel.login(requireActivity(), LoginRequest(admin_email, admin_password))
            observe()

//            add transition
            ObjectAnimator.ofFloat(splashLayout, "alpha", 0f, 1f).also {
                it.duration = 2000
                it.doOnEnd { lifecycleScope.launch {
                    delay(1000)
                    ObjectAnimator.ofFloat(splashLayout, "alpha", 1f, 0f).also { inner ->
                        inner.duration = 1000
                        inner.doOnEnd {
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

    }


    private fun observe() {
        viewmodel.loginLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                }

                is MyResource.isSuccess -> {
                    try {

                        val data = it.data?.response

                        SharedPref.setString(PrefConstent.TOKEN, "")
                        data?.run {
                            SharedPref.setString(PrefConstent.TOKEN, accessToken)
                            SharedPref.setString(PrefConstent.REFRESH_TOKEN, refreshToken)
                            SharedPref.setString(PrefConstent.APP_LOGO, data.logo)
//                            implement theme setting
                            if (data.backgroundTheme=="LIGHT"){
                                SharedPref.setBoolean(PrefConstent.IS_DARK, false)
                            }else{
                                SharedPref.setBoolean(PrefConstent.IS_DARK, true)
                            }
                            data.backgroundImage.run {
                                SharedPref.setString(PrefConstent.AUTH_BACKGROUND, data.backgroundImage)
                            }
                        }
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        throw RuntimeException("login getotp")
                    }
                }

                is MyResource.isError -> {
                }

                else -> {}
            }
        }
    }

    private fun navigateToHome() {
        if (isLogin) {
            val bundle = Bundle()
            bundle.putBoolean(PrefConstent.ISHOME, true)
            findNavController().navigate(R.id.projectfragment, bundle)
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