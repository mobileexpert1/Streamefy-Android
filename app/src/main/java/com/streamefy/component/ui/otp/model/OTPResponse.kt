package com.streamefy.component.ui.otp.model


import com.google.gson.annotations.SerializedName

data class OTPResponse(
    @SerializedName("response")
    val response: String = "",
    @SerializedName("error")
    val error: error? = null,
    @SerializedName("isSuccess")
    val isSuccess: Boolean = false
)


data class error(
    @SerializedName("error")
    val error: String = "",
    @SerializedName("errorLocation")
    val errorLocation: String = "",
    @SerializedName("userMessage")
    val userMessage: String = ""
)