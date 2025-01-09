package com.streamefy.component.ui.otp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.component.ui.otp.model.OTPRequest
import com.streamefy.component.ui.otp.model.OTPResponse
import com.streamefy.component.ui.otp.model.VerificationRequest
import com.streamefy.component.ui.otp.model.VerifyResponse
import com.streamefy.data.SingleLiveEvent
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.AuthService
import com.streamefy.network.MyResource
import com.streamefy.utils.LogMessage
import com.streamefy.utils.isNetworkAvailable
import com.streamefy.utils.showMessage
import kotlinx.coroutines.launch

class OTPVM(var repo: AuthService) : ViewModel() {

    var _otpLiveData = SingleLiveEvent<MyResource<OTPResponse>>()
    val otpLiveData: LiveData<MyResource<OTPResponse>> get() = _otpLiveData

    var _vericationData = SingleLiveEvent<MyResource<VerifyResponse>>()
    val vericationData: LiveData<MyResource<VerifyResponse>> get() = _vericationData

    fun getOtp(context: Context, request: OTPRequest) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                _otpLiveData.value = MyResource.isLoading()
                try {
                    val response = repo.otp(request)
                    if (response.body()?.isSuccess!!) {
                        _otpLiveData.value = MyResource.isSuccess(response.body())
                    } else {
                        context.showMessage(response.body()?.error?.userMessage.toString())
                        _otpLiveData.value=MyResource.isError(response.body()?.error?.userMessage.toString())
                    }
                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
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
                    val response = repo.verify(verirequest)
                    if (response.body()?.isSuccess!!) {
                        _vericationData.value = MyResource.isSuccess(response.body())
                    } else {
                        context.showMessage(response.body()?.error?.userMessage.toString())
                        _vericationData.value=MyResource.isError(response.body()?.error?.userMessage.toString())
                    }
                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
                    _vericationData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                _vericationData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }
}