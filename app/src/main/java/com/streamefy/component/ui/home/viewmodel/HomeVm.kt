package com.streamefy.component.ui.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.component.ui.home.model.HomeResponse
import com.streamefy.component.ui.otp.model.OTPRequest
import com.streamefy.component.ui.otp.model.OTPResponse
import com.streamefy.data.SingleLiveEvent
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.ApiService
import com.streamefy.network.MyResource
import com.streamefy.utils.LogMessage
import com.streamefy.utils.isNetworkAvailable
import com.streamefy.utils.showMessage
import kotlinx.coroutines.launch

class HomeVm(var repo: ApiService) : ViewModel() {
    var homeLiveData = SingleLiveEvent<MyResource<HomeResponse>>()


    fun getUserVideos(
        context: Context, page: Int,
        itemsPerPage: Int,
        userPin: String,
        phoneNumber: String,
    ) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                homeLiveData.value = MyResource.isLoading()
                try {
                    var response = repo.getUserVideos(page,itemsPerPage,userPin,phoneNumber)
                    if (response.body()?.isSuccess!!) {
                        homeLiveData.value = MyResource.isSuccess(response.body())
                    } else {
                       // ShowError.handleError.handleError(ErrorCodeManager.NOT_FOUND)
                        context.showMessage(response.body()?.error?.userMessage.toString())
                        homeLiveData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NOT_FOUND))
                    }
                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
                    ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
                    homeLiveData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                homeLiveData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }

}