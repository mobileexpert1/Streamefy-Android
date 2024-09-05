package com.streamefy.component.ui.pin_authentication

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.data.SingleLiveEvent
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.ApiService
import com.streamefy.network.AuthService
import com.streamefy.network.MyResource
import com.streamefy.utils.isNetworkAvailable
import kotlinx.coroutines.launch

class PinVM(repo: AuthService) : ViewModel() {

    var pinData = SingleLiveEvent<MyResource<String>>()

    fun getPin(context: Context) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                pinData.value = MyResource.isLoading()
                /// call network
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
            }
        }
    }
}