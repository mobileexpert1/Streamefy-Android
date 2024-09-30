package com.streamefy.component.ui.pin_authentication

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.component.ui.home.model.HomeResponse
import com.streamefy.data.SingleLiveEvent
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.ApiService
import com.streamefy.network.AuthService
import com.streamefy.network.MyResource
import com.streamefy.utils.LogMessage
import com.streamefy.utils.isNetworkAvailable
import kotlinx.coroutines.launch

class PinVM(var repo: ApiService) : ViewModel() {

    var pinData = SingleLiveEvent<MyResource<HomeResponse>>()

    fun setPin(
        context: Context, page: Int,
        itemsPerPage: Int,
        userPin: String,
        phoneNumber: String,
    ) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                pinData.value = MyResource.isLoading()
                try {
                    var response = repo.getUserVideos(page, itemsPerPage, userPin, phoneNumber)
                    if (response.body()?.isSuccess!!) {
                        pinData.value = MyResource.isSuccess(response.body())
                    } else {
                        ShowError.handleError.message(response.body()?.error?.userMessage.toString())
                        pinData.value =
                            MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NOT_FOUND))
                    }
                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
                  //  ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
                    pinData.value = MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                pinData.value =
                    MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }
}