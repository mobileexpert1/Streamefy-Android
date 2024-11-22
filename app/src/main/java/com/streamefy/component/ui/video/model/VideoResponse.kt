package com.streamefy.component.ui.video.model


import com.google.gson.annotations.SerializedName

data class Response(@SerializedName("eventId")
                    val eventId: Int = 0,
                    @SerializedName("nextVideo")
                    val nextVideo: NextVideo?=null,
                    @SerializedName("mediaId")
                    val mediaId: Int = 0,
                    @SerializedName("hlsUrl")
                    val hlsUrl: String = "")


data class VideoResponse(@SerializedName("response")
                         val response: Response,
                         @SerializedName("error")
                         val error: MError,
                         @SerializedName("isSuccess")
                         val isSuccess: Boolean = false)


data class NextVideo(@SerializedName("nextVideoThumbnail")
                     val nextVideoThumbnail: String = "",
                     @SerializedName("nextVideoId")
                     val nextVideoId: String = "")
//                     @SerializedName("nextVideoPlaybackDuration")
//                     val nextVideoPlaybackDuration: Int?=null)
data class MError(@SerializedName("userMessage")
                  val userMessage: String = "",
                  @SerializedName("developerMessage")
                  val developerMessage: String = "",
                  @SerializedName("error")
                  val error: String = "")

