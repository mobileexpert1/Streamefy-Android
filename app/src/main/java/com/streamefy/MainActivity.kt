package com.streamefy

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    var navHostFragment: Fragment? = null
    private lateinit var navController: NavController
    val permission = "android.permission.READ_PHONE_STATE"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationview)
        navController = (navHostFragment as NavHostFragment).navController
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            val countryIso = getCountryIsoCode()
            Log.d("Country Iso", "Country ISO Code: $countryIso")
        } else {
            // Request permission
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
        }
//        if (FirebaseApp.getApps(this).isEmpty()) {
//            FirebaseApp.initializeApp(this)
//
//
//        }else{
//            Log.e("mdckld","slmclsmc not initialize")
//        }


        //crashApp()
    }

    fun exitApp() {
        finishAffinity()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.clearApplicationUserData()
        exitProcess(0)
    }


    fun crashApp() {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log("Testing Crashlytics")
        throw RuntimeException("Test Crash") // Force a crash
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                val countryIso = getCountryIsoCode()
                Log.d("Country Iso", "Country ISO Code: $countryIso")
            } else {
                // Permission denied
                Log.d("Country Iso", "Permission denied")
            }
        }
    }

    fun getCountryIsoCode(): String? {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryIso = telephonyManager.networkCountryIso
        return if (countryIso.isNullOrEmpty()) {
            val locale = Locale.getDefault()
            locale.country
        } else {
            countryIso.toUpperCase()
        }
    }


}