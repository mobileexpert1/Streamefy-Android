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
    override fun bindView(): Int = R.layout.fragment_splash_screen
    var isLogin = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isLogin = SharedPref.getBoolean(PrefConstent.ISLOGIN)
        lifecycleScope.launch {
            delay(2000)
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        val navOptions = androidx.navigation.NavOptions.Builder()
            .setPopUpTo(R.id.splashScreen, true)
            .build()
        Log.e("sjndjsn", "sknks $isLogin")
        if (isLogin) {
            SharedPref.setBoolean(PrefConstent.ISAUTH,false)
            findNavController().navigate(R.id.homefragment, null, navOptions)
        } else {
            findNavController().navigate(R.id.loginFragment, null, navOptions)
//            var bundle=Bundle()
//            bundle.putString(PrefConstent.VIDEO_URL,"")
//            findNavController().navigate(R.id.videofragment,bundle)
////            findNavController().navigate(R.id.dynamicscreen)
        }
    }
}