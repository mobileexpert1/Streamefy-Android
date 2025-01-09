package com.streamefy.component.ui.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.component.ui.home.model.HomeResponse
import com.streamefy.component.ui.video.model.PlayBackRequest
import com.streamefy.component.ui.video.model.VideoPlaback
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
    var _homeLiveData = SingleLiveEvent<MyResource<HomeResponse>>()
    fun getUserVideos(
        context: Context, page: Int,
        itemsPerPage: Int,
        userPin: String,
        projectId: Int,
        phoneNumber: String,
    ) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                _homeLiveData.value = MyResource.isLoading()
                try {
                    var response = repo.getUserVideos(page,itemsPerPage,userPin,projectId,phoneNumber)

                    if (response.body()?.isSuccess!!) {
                        _homeLiveData.value = MyResource.isSuccess(response.body())
                    } else {
                        context.showMessage(response.body()?.error?.userMessage.toString())
                        _homeLiveData.value=MyResource.isError(response.body()?.error?.userMessage.toString())
                    }
                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
                    _homeLiveData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                _homeLiveData.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }


    var _videoduraion = SingleLiveEvent<MyResource<VideoPlaback>>()

    fun saveDuration(
        context: Context,
        request: PlayBackRequest
    ) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                _videoduraion.value = MyResource.isLoading()
                try {
                    var response = repo.saveDuration(request)
                    if (response.body()?.isSuccess!!) {
                        _videoduraion.value = MyResource.isSuccess(response.body())
                    } else {
                        _videoduraion.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NOT_FOUND))
                    }
                } catch (e: Exception) {
                    _videoduraion.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                _videoduraion.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }

}