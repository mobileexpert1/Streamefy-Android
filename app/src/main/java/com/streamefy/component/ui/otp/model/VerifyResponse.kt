package com.streamefy.component.ui.otp.model


import com.google.gson.annotations.SerializedName

data class Response(
    @SerializedName("message")
    val message: String = "",
    @SerializedName("email")
    val email: String = "",
    @SerializedName("isPrimaryuser")
    val isPrimaryuser: Boolean = false
)


data class VerifyResponse(
    @SerializedName("response")
    val response: Response,
    @SerializedName("error")
    val error: mError? = null,
    @SerializedName("isSuccess")
    val isSuccess: Boolean = false
)

data class mError(
    @SerializedName("error")
    val error: String = "",
    @SerializedName("errorLocation")
    val errorLocation: String = "",
    @SerializedName("userMessage")
    val userMessage: String = ""
)

