package com.streamefy.component.ui.projects.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.component.ui.pin_authentication.model.ResetPinRequest
import com.streamefy.component.ui.pin_authentication.model.ResetPinResponse
import com.streamefy.component.ui.projects.model.ProjectRequest
import com.streamefy.component.ui.projects.model.ProjectResponse
import com.streamefy.data.SingleLiveEvent
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.ApiService
import com.streamefy.network.MyResource
import com.streamefy.utils.LogMessage
import com.streamefy.utils.isNetworkAvailable
import kotlinx.coroutines.launch

class ProjectsVM(var repo: ApiService) : ViewModel() {
    var _projectLiveData = SingleLiveEvent<MyResource<ProjectResponse>>()
    val projectLiveData: LiveData<MyResource<ProjectResponse>> get() = _projectLiveData

    fun getProject(context: Context, request: ProjectRequest) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                _projectLiveData.value = MyResource.isLoading()
                try {
                    var response = repo.getProject(request)
                        _projectLiveData.value = MyResource.isSuccess(response.body())

                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
                    _projectLiveData.value =
                        MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                _projectLiveData.value =
                    MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }

    // reset pin
    var _resetData = SingleLiveEvent<MyResource<ResetPinResponse>>()
    var resetData :LiveData<MyResource<ResetPinResponse>> =_resetData
    fun resetPin(
        context: Context,
        userPin: ResetPinRequest,
    ) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                _resetData.value = MyResource.isLoading()
                try {
                    var response = repo.resetPin(userPin)
                    if (response.body()?.isSuccess!!) {
                        _resetData.value = MyResource.isSuccess(response.body())
                    } else {
                        ShowError.handleError.message(response.body()?.error?.userMessage.toString())
                        _resetData.value = MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NOT_FOUND))
                    }
                } catch (e: Exception) {
                    ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
                    _resetData.value = MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                _resetData.value =
                    MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }

}