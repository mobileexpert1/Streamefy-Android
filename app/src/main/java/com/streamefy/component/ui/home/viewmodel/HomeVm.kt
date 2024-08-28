package com.streamefy.component.ui.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.data.SingleLiveEvent
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.ApiService
import com.streamefy.network.MyResource
import com.streamefy.utils.isNetworkAvailable
import kotlinx.coroutines.launch

class HomeVm(repo: ApiService) : ViewModel() {
    var homeLiveData = SingleLiveEvent<MyResource<String>>()

    fun get(context: Context) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                homeLiveData.value = MyResource.isLoading()

            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
            }
        }
    }

}