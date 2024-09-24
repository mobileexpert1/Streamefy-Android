package com.streamefy.component.ui.otp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.component.ui.otp.model.OTPRequest
import com.streamefy.component.ui.otp.model.OTPResponse
import com.streamefy.component.ui.otp.model.VerificationRequest
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

    var otpLiveData = SingleLiveEvent<MyResource<OTPResponse>>()
    var vericationData = SingleLiveEvent<MyResource<OTPResponse>>()

    fun getOtp(context: Context, optrequest: OTPRequest) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                otpLiveData.value = MyResource.isLoading()
                try {
                    var response = repo.otp(optrequest)
                    if (response.body()?.isSuccess!!) {
                        otpLiveData.value = MyResource.isSuccess(response.body())
                    } else {
                        //ShowError.handleError.handleError(ErrorCodeManager.NOT_FOUND)
                        context.showMessage(response.body()?.error?.userMessage.toString())
                        otpLiveData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NOT_FOUND))
                    }
                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
                    ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
                    otpLiveData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                otpLiveData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }
    fun otpVerification(context: Context, verirequest: VerificationRequest) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                vericationData.value = MyResource.isLoading()
                try {
                    var response = repo.verify(verirequest)
                    if (response.body()?.isSuccess!!) {
                        vericationData.value = MyResource.isSuccess(response.body())
                    } else {
                        context.showMessage(response.body()?.error?.userMessage.toString())
                       // ShowError.handleError.handleError(ErrorCodeManager.NOT_FOUND)
                        vericationData.value=MyResource.isError(response.body()?.error?.error!!)
                    }
                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
                    ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
                    vericationData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                vericationData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }
}