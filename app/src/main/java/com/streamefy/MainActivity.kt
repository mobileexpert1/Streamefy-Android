package com.streamefy

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    var navHostFragment: Fragment? = null
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationview)
        navController = (navHostFragment as NavHostFragment).navController

    }

    fun exitApp() {
        finishAffinity()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.clearApplicationUserData()
        exitProcess(0)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.e("dscmkldanc","saklcn")
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_ENTER -> {
                // Handle key event
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

}