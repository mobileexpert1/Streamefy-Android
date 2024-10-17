package com.streamefy.component.ui.pin_authentication


import com.google.gson.annotations.SerializedName

data class PinResponse(@SerializedName("response")
                       val response: String = "",
                       @SerializedName("error")
                       val error: Error,
                       @SerializedName("isSuccess")
                       val isSuccess: Boolean = false)


data class Error(
    @SerializedName("userMessage")
    val userMessage: String = "",
    @SerializedName("developerMessage")
    val developerMessage: String = "",
    @SerializedName("innerException")
    val innerException: String = "",
    @SerializedName("errorCode")
    val errorCode: String = "",
    @SerializedName("stackTrace")
    val stackTrace: String = "",
    @SerializedName("error")
    val error: String = ""
)