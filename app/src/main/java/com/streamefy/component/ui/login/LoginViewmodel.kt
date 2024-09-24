package com.streamefy.component.ui.login

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.component.ui.login.model.LoginResponse
import com.streamefy.data.SingleLiveEvent
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.ApiService
import com.streamefy.network.AuthService
import com.streamefy.network.MyResource
import com.streamefy.utils.LogMessage.logeMe
import com.streamefy.utils.isNetworkAvailable
import com.streamefy.utils.showMessage
import kotlinx.coroutines.launch

class LoginViewmodel(var repo: AuthService) : ViewModel() {


    var loginLiveData = SingleLiveEvent<MyResource<LoginResponse>>()

    fun login(context: Context, login: LoginRequest) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                loginLiveData.value = MyResource.isLoading()
                try {
                    var response = repo.login(login)
                    if (response.body()?.isSuccess!!) {
                        loginLiveData.value = MyResource.isSuccess(response.body())
                    } else {
                        context.showMessage(response.body()?.error?.userMessage.toString())
                    }
                } catch (e: Exception) {
                    logeMe(e.toString())
                    ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
            }
        }
    }
}