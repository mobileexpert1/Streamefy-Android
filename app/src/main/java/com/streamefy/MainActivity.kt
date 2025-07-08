package com.streamefy

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {
    var navHostFragment: Fragment? = null
    private lateinit var navController: NavController
    lateinit var wakeLock: PowerManager.WakeLock
    lateinit var tvMessage: TextView
    var isNetwork = true
    private val UPDATE_REQUEST_CODE = 100
    private lateinit var appUpdateManager: AppUpdateManager

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
//          get ipaddress for spacific country
            getLocationFromIP()
        }

        // Initialize AppUpdateManager
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdate()
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

            try {
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
                        }else {

                        }

                    } catch (e: Exception) {
                        Log.e("cjbjdbc", "exception $e")
                    }
                }
            }catch (e:Exception){

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

    // ✅ Auto update check for Google & Amazon Fire Stick
    private fun checkForUpdate() {
        if (isGooglePlayDevice()) {
            Log.e("call", "GOOGLE FIRE STICK!!!!")
            checkGooglePlayUpdate()
        } else {
            Log.e("call", "AMAZON FIRE STICK!!!!")
            // checkAmazonUpdate()
        }
    }

    // ✅ Google Fire Stick: Check for Google Play updates
    private fun checkGooglePlayUpdate() {
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

            Log.e("call", "Update Availability: ${appUpdateInfo.updateAvailability()}")
            Log.e("call", "Update Type Allowed: ${appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)}")

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                Log.e("call", "STATUS: Update Available! Redirecting...")

                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE, // 🔥 Change to IMMEDIATE for forced updates
                        this,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            } else {
                Log.e("call", "STATUS: No Update Available")
            }
        }.addOnFailureListener { exception ->
            Log.e("call", "STATUS: Failed to check for update", exception)
        }
    }

    // ✅ Amazon Fire Stick: Redirect to Amazon Appstore for update
    private fun checkAmazonUpdate() {
        val appPackageName = packageName
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.amazon.com/gp/mas/dl/android?p=$appPackageName")
        }
        startActivity(intent)
    }

    // 🛠️ Detect whether the device supports Google Play
    private fun isGooglePlayDevice(): Boolean {
        return try {
            packageManager.getPackageInfo("com.android.vending", 0) // Check Google Play Store package
            packageManager.getPackageInfo("com.google.android.gms", 0) // Check Google Play Services
            true // Device has Google Play
        } catch (e: Exception) {
            false // No Google Play (likely an Amazon Fire Stick)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e("call", "STATUS: Update failed!")
                Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
