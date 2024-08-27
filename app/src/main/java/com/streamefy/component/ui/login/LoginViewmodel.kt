package com.streamefy.component.ui.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.data.SingleLiveEvent
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.ApiService
import com.streamefy.network.MyResource
import com.streamefy.utils.isNetworkAvailable
import kotlinx.coroutines.launch

class LoginViewmodel(repo: ApiService) : ViewModel() {

    val fullNameRegex = Regex("^[a-zA-Z]+\\s+[a-zA-Z]+(\\s+[a-zA-Z]+)?$")

    var loginLiveData = SingleLiveEvent<MyResource<String>>()

    fun login(context: Context) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                loginLiveData.value=MyResource.isLoading()
            }else{
                ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
            }
        }
    }


    fun isValidFullName(fullName: String): Boolean {
        return true
    }

    fun isValidPhoneNumberLength(phoneNumber: String): Boolean {
        if (phoneNumber.length < 7) {
            return false
        } else if (phoneNumber.length > 15) {
            return false
        } else {
            return true
        }
    }

}