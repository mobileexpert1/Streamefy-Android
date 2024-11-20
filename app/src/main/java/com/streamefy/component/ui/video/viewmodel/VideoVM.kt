package com.streamefy.component.ui.video.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamefy.component.ui.video.model.PlayBackRequest
import com.streamefy.component.ui.video.model.VideoPlaback
import com.streamefy.component.ui.video.model.VideoResponse
import com.streamefy.data.SingleLiveEvent
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.ApiService
import com.streamefy.network.MyResource
import com.streamefy.utils.LogMessage
import com.streamefy.utils.isNetworkAvailable
import com.streamefy.utils.showMessage
import kotlinx.coroutines.launch

class VideoVM(var repo: ApiService) : ViewModel() {

    var _videoLiveData = SingleLiveEvent<MyResource<VideoResponse>>()
    val videoLiveData: LiveData<MyResource<VideoResponse>> get() = _videoLiveData

    fun getVideo(
        context: Context,
        nextVideoId:String
    ) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                _videoLiveData.value = MyResource.isLoading()
                try {
                    var response = repo.getVideo(nextVideoId)
                    response.body()?.run {
                        if (response.body()!!.isSuccess) {
                            _videoLiveData.value = MyResource.isSuccess(response.body())
                           // var data=response.body()?.response
//                            if (data!=null){
//                                videoUrl=data.hlsUrl
//                                nextVideoId=data.nextVideo.nextVideoId
//                                mediaId=data.mediaId
//                                videoThumb=data.nextVideo.nextVideoThumbnail
//                                //videoDuration=data.nextVideo.nextVideoPlaybackDuration.toString()
//                            }else{
//                                context.showMessage("Video not found")
//                            }

                        } else {
                            context.showMessage(response.body()?.error?.userMessage.toString())
                            _videoLiveData.value=MyResource.isError(
                                ErrorCodeManager.getErrorMessage(
                                    ErrorCodeManager.NOT_FOUND))
                        }
                    }
                } catch (e: Exception) {
                    LogMessage.logeMe(e.toString())
                    // ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
                    _videoLiveData.value=MyResource.isError(
                        ErrorCodeManager.getErrorMessage(
                            ErrorCodeManager.UNKNOWN_ERROR))
                    ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                _videoLiveData.value=MyResource.isError(
                    ErrorCodeManager.getErrorMessage(
                        ErrorCodeManager.NETWORK_ISSUE))

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
                        // ShowError.handleError.handleError(ErrorCodeManager.NOT_FOUND)
                        //  context.showMessage(response.body()?.error?.userMessage.toString())
                        _videoduraion.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NOT_FOUND))
                    }
                } catch (e: Exception) {
                    //  LogMessage.logeMe(e.toString())
                    // ShowError.handleError.handleError(ErrorCodeManager.UNKNOWN_ERROR)
                    _videoduraion.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.UNKNOWN_ERROR))

                }
            } else {
                ShowError.handleError.handleError(ErrorCodeManager.NETWORK_ISSUE)
                _videoduraion.value=MyResource.isError(ErrorCodeManager.getErrorMessage(ErrorCodeManager.NETWORK_ISSUE))

            }
        }
    }
}