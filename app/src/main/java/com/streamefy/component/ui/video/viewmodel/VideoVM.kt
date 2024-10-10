package com.streamefy.component.ui.video.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.streamefy.component.ui.otp.model.OTPResponse
import com.streamefy.data.SingleLiveEvent
import com.streamefy.network.AuthService
import com.streamefy.network.MyResource

class VideoVM(var repo: AuthService) : ViewModel() {

    var _videoLiveData = SingleLiveEvent<MyResource<OTPResponse>>()
    val videoLiveData: LiveData<MyResource<OTPResponse>> get() = _videoLiveData

}