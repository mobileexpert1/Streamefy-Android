package com.streamefy.component.ui.projects.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.component.ui.otp.model.OTPRequest
import com.streamefy.component.ui.otp.model.OTPResponse
import com.streamefy.component.ui.otp.model.VerifyResponse
import com.streamefy.component.ui.projects.model.ProjectRequest
import com.streamefy.component.ui.projects.model.ProjectResponse
import com.streamefy.data.SingleLiveEvent
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.ApiService
import com.streamefy.network.MyResource
import com.streamefy.utils.LogMessage
import com.streamefy.utils.isNetworkAvailable
import com.streamefy.utils.showMessage
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
                    if (response.body()?.isSuccess!!) {
                        _projectLiveData.value = MyResource.isSuccess(response.body())
                    } else {
                        //ShowError.handleError.handleError(ErrorCodeManager.NOT_FOUND)
                        if (response.body()?.error?.userMessage.toString() != "No primary projects found for the user.") {
                            context.showMessage(response.body()?.error?.userMessage.toString())
                        }
                        _projectLiveData.value =
                            MyResource.isError(response.body()?.error?.userMessage.toString())
                    }
                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
                    //  ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
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


}