package com.streamefy.component.ui.otp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.streamefy.data.SingleLiveEvent
import com.streamefy.network.ApiService
import com.streamefy.network.MyResource
import com.streamefy.utils.isNetworkAvailable

class OTPVM(repo: ApiService) : ViewModel() {

    var otpLiveData = SingleLiveEvent<MyResource<String>>()
    fun getOtp(context: Context) {
        if (context.isNetworkAvailable()) {
            otpLiveData.value = MyResource.isLoading()
            try {

            } catch (e: Exception) {
            }
        }
    }


}