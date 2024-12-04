package com.streamefy

import android.app.ActivityManager
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.streamefy.component.base.BaseFragment
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.network.NetworkReceiver
import com.streamefy.utils.gone
import com.streamefy.utils.isNetworkAvailable
import com.streamefy.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    var navHostFragment: Fragment? = null
    private lateinit var navController: NavController
  lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var networkReceiver: NetworkReceiver
    lateinit var tvMessage:TextView
    var isNetwork=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        wakelock()
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationview)
        tvMessage=findViewById(R.id.tvNetworkMessaage)
        navController = (navHostFragment as NavHostFragment).navController
        if (isNetworkAvailable()) {
            getLocationFromIP()
        }

//        if (FirebaseApp.getApps(this).isEmpty()) {
//            FirebaseApp.initializeApp(this)
//
//
//        }else{
//            Log.e("mdckld","slmclsmc not initialize")
//        }
//        networkReceiver = NetworkReceiver(this)
//
//        // Register the receiver with a filter for connectivity changes
//        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//        registerReceiver(networkReceiver, filter)
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
             try {

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
            }catch (e:Exception){
            Log.e("cjbjdbc","exception $e")
        }
            }
        }
    }
    fun wakelock(){
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MainActivity::WakeLock"
            )
            wakeLock?.acquire()

    }
    fun exitApp() {
        if (!isFinishing && !isDestroyed) {
            // First, finish all activities in the task
            finishAffinity()
        }
//        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        activityManager.clearApplicationUserData()
        exitProcess(0)
//        finishAffinity()
//        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        activityManager.clearApplicationUserData()
//        exitProcess(0)
    }

    override fun onPause() {
        super.onPause()
//        wakeLock.let {
//            if (it.isHeld) {
//                it.release() // Release only if the WakeLock is currently held
//            }
//        }
//        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    override fun onDestroy() {
        super.onDestroy()
        wakeLock.let {
            if (it.isHeld) {
                it.release()
            }
        }
        //unregisterReceiver(networkReceiver)
    }
//    override fun onNetworkAvailable() {
//        isNetwork=true
//        BaseFragment.run {
//            isNetworkAvailable=isNetwork
//             }
//    }
//
//    override fun onNetworkUnavailable() {
//        isNetwork=false
//        BaseFragment.isNetworkAvailable=isNetwork
//        tvMessage.visible()
//        tvMessage.setText(getString(R.string.network_message))
//        lifecycleScope.launch {
//            delay(5000)
//            withContext(Dispatchers.Main){
//                tvMessage.gone()
//            }
//        }
//    }

    fun showNetwork(isNetworkAvailable: Boolean) {
        if (!isNetwork){
        tvMessage.visible()
        tvMessage.setText(getString(R.string.network_message))
        lifecycleScope.launch {
            delay(5000)
            withContext(Dispatchers.Main){
                tvMessage.gone()
            }
        }
    }}

}
