package com.streamefy

import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.media.MediaHandler
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
    lateinit var tvMessage: TextView
    var isNetwork = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mediaHandler = MediaHandler(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        wakelock()
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationview)
        tvMessage = findViewById(R.id.tvNetworkMessaage)
        navController = (navHostFragment as NavHostFragment).navController
        if (isNetworkAvailable()) {
//            get ipaddress for spacific country
            getLocationFromIP()
        }
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
                            val code = getDialingCode(country)
                            SharedPref.setString(PrefConstent.COUNTRY_CODE, code.toString())
                        }
                    }

                } catch (e: Exception) {
                    Log.e("cjbjdbc", "exception $e")
                }
            }
        }
    }

    private lateinit var mediaHandler: MediaHandler

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        return when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                true
            }

            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                true
            }

            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }

    //    wake lock to prevent screen off
    private fun wakelock() {
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
        exitProcess(0)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    fun showNetwork(isNetworkAvailable: Boolean) {
        if (!isNetwork) {
            tvMessage.visible()
            tvMessage.text = getString(R.string.network_message)
            lifecycleScope.launch {
                delay(5000)
                withContext(Dispatchers.Main) {
                    tvMessage.gone()
                }
            }
        }
    }

}
