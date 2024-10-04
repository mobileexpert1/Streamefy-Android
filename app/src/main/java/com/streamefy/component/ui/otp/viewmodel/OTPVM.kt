package com.streamefy.component.ui.otp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.component.ui.otp.model.OTPRequest
import com.streamefy.component.ui.otp.model.OTPResponse
import com.streamefy.component.ui.otp.model.VerificationRequest
import com.streamefy.data.KoinCompo
import com.streamefy.data.SingleLiveEvent
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.ApiService
import com.streamefy.network.AuthService
import com.streamefy.network.MyResource
import com.streamefy.utils.LogMessage
import com.streamefy.utils.isNetworkAvailable
import com.streamefy.utils.showMessage
import kotlinx.coroutines.launch

class OTPVM(var repo: AuthService) : ViewModel() {

    var _otpLiveData = SingleLiveEvent<MyResource<OTPResponse>>()
    val otpLiveData: LiveData<MyResource<OTPResponse>> get() = _otpLiveData

    var _vericationData = SingleLiveEvent<MyResource<OTPResponse>>()
    val vericationData: LiveData<MyResource<OTPResponse>> get() = _vericationData

    fun getOtp(context: Context, optrequest: OTPRequest) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                _otpLiveData.value = MyResource.isLoading()
                try {
                    var response = repo.otp(optrequest)
                    if (response.body()?.isSuccess!!) {
                        _otpLiveData.value = MyResource.isSuccess(response.body())
                    } else {
                        //ShowError.handleError.handleError(ErrorCodeManager.NOT_FOUND)
                        context.showMessage(response.body()?.error?.userMessage.toString())
                        _otpLiveData.value=MyResource.isError(response.body()?.error?.userMessage.toString())
                    }
                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
                  //  ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
                    _otpLiveData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                _otpLiveData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }
    fun otpVerification(context: Context, verirequest: VerificationRequest) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                _vericationData.value = MyResource.isLoading()
                try {
                    var response = repo.verify(verirequest)
                    Log.e("ckdbcjdbc","skmcksn ${response.body()?.isSuccess!!}")
                    if (response.body()?.isSuccess!!) {
                        _vericationData.value = MyResource.isSuccess(response.body())
                    } else {
                        context.showMessage(response.body()?.error?.userMessage.toString())
                       // ShowError.handleError.handleError(ErrorCodeManager.NOT_FOUND)
                        _vericationData.value=MyResource.isError(response.body()?.error?.userMessage.toString()!!)
                    }
                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
                   // ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
                    _vericationData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                _vericationData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }
}