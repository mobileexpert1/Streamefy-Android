package com.streamefy

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    var navHostFragment: Fragment? = null
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationview)
        navController = (navHostFragment as NavHostFragment).navController
        getLocationFromIP()
//        if (FirebaseApp.getApps(this).isEmpty()) {
//            FirebaseApp.initializeApp(this)
//
//
//        }else{
//            Log.e("mdckld","slmclsmc not initialize")
//        }

    }
    fun getDialingCode(countryIso: String?): Int {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        return phoneNumberUtil.getCountryCodeForRegion(countryIso)
    }

    private fun getLocationFromIP() {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()

            // Send a request to ipinfo.io to get IP-based location info (this does not require permissions)
            val request = Request.Builder()
                .url("https://ipinfo.io/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    // Parse the JSON response
                    val json = JSONObject(response.body!!.string())
                    val country = json.optString("country", "Unknown")

                    // Show location info based on IP
                    runOnUiThread {
                        var code=  getDialingCode(country)
                        SharedPref.setString(PrefConstent.COUNTRY_CODE,code.toString())
                        Log.d("hhhhthth", "$code local result $response")
                    }
                } else {
                    runOnUiThread {
                        Log.d("hhhhthth", "Failed to retrieve location ")
                    }
                }
            }
        }
    }
    fun exitApp() {
        finishAffinity()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.clearApplicationUserData()
        exitProcess(0)
    }

}
