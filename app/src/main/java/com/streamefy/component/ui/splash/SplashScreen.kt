package com.streamefy.component.ui.splash

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.window.SplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.databinding.FragmentSplashScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : BaseFragment<FragmentSplashScreenBinding>(){
    override fun bindView(): Int =R.layout.fragment_splash_screen
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            delay(500)
            navigateToHome()
                findNavController().navigate(R.id.homefragment)
        }

    }
    private fun navigateToHome() {
        val navOptions = androidx.navigation.NavOptions.Builder()
            .setPopUpTo(R.id.splashScreen, true)
            .build()

        findNavController().navigate(R.id.homefragment, null, navOptions)
    }
}