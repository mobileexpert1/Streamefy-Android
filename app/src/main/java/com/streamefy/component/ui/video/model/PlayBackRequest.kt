package com.streamefy.component.ui.video.model

data class PlayBackRequest(
    var phoneNumber:String="",
    var mediaId:Int=0,
    var videoId:String="",
    var duration:String="",
)