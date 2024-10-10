package com.streamefy.component.ui.video.model


import com.google.gson.annotations.SerializedName

data class VideoPlaback(@SerializedName("response")
                        val response: String = "",
                        @SerializedName("error")
                        val error: Error,
                        @SerializedName("isSuccess")
                        val isSuccess: Boolean = false)


data class Error(@SerializedName("userMessage")
                 val userMessage: String = "",
                 @SerializedName("developerMessage")
                 val developerMessage: String = "",
                 @SerializedName("error")
                 val error: String = "",
                 @SerializedName("errorLocation")
                 val errorLocation: String = "")


