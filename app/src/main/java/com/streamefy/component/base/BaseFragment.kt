package com.streamefy.component.base

import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.streamefy.MainActivity
import com.streamefy.component.ui.networkui.NetDialog
import com.streamefy.network.NetworkReceiver
import com.streamefy.network.NetworkStatusListener

abstract class BaseFragment<B : ViewBinding> : Fragment(), NetworkStatusListener {
    lateinit var binding: B


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
//        setRetainInstance(true)
        (requireContext().applicationContext as MyApp).setNetworkStatusListener(this)
         dialog=NetDialog(requireActivity())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        binding = DataBindingUtil.inflate(inflater, bindView(), container, false)
        progressDialog= CircularProgressDialog(requireContext())
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(inflater, bindView(), container, false)
        }

        return binding.root
    }

     fun showProgress(){
         progressDialog.show()
     }
    fun dismissProgress(){
        progressDialog.dismiss()
    }
    abstract fun bindView(): Int

    override fun onDestroyView() {
        super.onDestroyView()
        //unregister listener here
//        onBackPressedCallback.isEnabled = false
//        onBackPressedCallback.remove()
    }

    fun throwerror(value:String){
        throw RuntimeException(value)
    }


    fun logException(e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        throw RuntimeException("Base class")
        Log.e("BaseFragment", "Handled exception: ${e.message}", e)
    }

    override fun onPause() {
        super.onPause()
        progressDialog?.dismiss()
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