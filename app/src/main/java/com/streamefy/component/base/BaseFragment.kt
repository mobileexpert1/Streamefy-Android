package com.streamefy.component.base

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
//import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.streamefy.BuildConfig
import com.streamefy.MainActivity
import com.streamefy.R
import com.streamefy.component.ui.networkui.NetDialog
import com.streamefy.network.NetworkReceiver
import com.streamefy.network.NetworkStatusListener

abstract class BaseFragment<B : ViewBinding> : Fragment(), NetworkStatusListener {
    lateinit var binding: B
    private var loaderView: View? = null

    fun is4KSupported(context: Context): Boolean {
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val modes = display.supportedModes
            for (mode in modes) {
                if (mode.physicalWidth >= 3840 && mode.physicalHeight >= 2160) {
                    return true
                }
            }
        }

        return false
    }

    companion object{
        var isNetworkAvailable=false
        lateinit var progressDialog:CircularProgressDialog
    }
    var dialog:NetDialog?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build())
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        (requireContext().applicationContext as MyApp).setNetworkStatusListener(this)
         dialog=NetDialog(requireActivity())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        progressDialog= CircularProgressDialog(requireContext())
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(inflater, bindView(), container, false)
        }

        return binding.root
    }


    private var loaderAnimator: ObjectAnimator? = null

    fun showCustomLoader() {
        if (loaderView == null) {
            loaderView = layoutInflater.inflate(R.layout.view_custom_loader, null)
            val rootView = requireActivity().findViewById<ViewGroup>(android.R.id.content)

            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutParams.gravity = Gravity.CENTER

            rootView.addView(loaderView, layoutParams)
            loaderView?.bringToFront()

            val loaderImage = loaderView?.findViewById<ImageView>(R.id.ivLoader)
            loaderAnimator = ObjectAnimator.ofFloat(loaderImage, View.ROTATION, 0f, 360f).apply {
                duration = 1000
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                start()
            }
        }

        loaderView?.visibility = View.VISIBLE
        loaderAnimator?.resume()
    }

    fun hideCustomLoader() {
        loaderAnimator?.pause()
        loaderView?.visibility = View.GONE
    }


    fun showProgress(){
        if (BuildConfig.FLAVOR == "streamefy") {
            //** for streamefy
            showCustomLoader()
        }else {
            //** for cupcake
            progressDialog.show()
        }
    }
    fun dismissProgress(){
        if (BuildConfig.FLAVOR == "streamefy") {
            //** for streamefy
            hideCustomLoader()
        }else {
            //** for cupcake
            progressDialog.dismiss()
        }
    }
    abstract fun bindView(): Int

    override fun onDestroyView() {
        super.onDestroyView()

    }

    fun throwerror(value:String){
        throw RuntimeException(value)
    }


    fun logException(e: Exception) {
        Log.e("BaseFragment", "Handled exception: ${e.message}", e)
    }

    override fun onPause() {
        super.onPause()

        if (BuildConfig.FLAVOR == "streamefy") {
            //** for streamefy
            hideCustomLoader()
        }else {
            //** for cupcake
            progressDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        MainActivity().showNetwork(isNetworkAvailable)
    }

    override fun onNetworkStatusChanged(isAvailable: Boolean) {
        Log.e("BaseFragment", "network available $isAvailable")
        netStatus()
        if (isAvailable){
            dialog?.dismiss()
            isNetworkAvailable=true
        }else{
            dialog?.show()
            isNetworkAvailable=false
        }
    }

    abstract fun netStatus()
}