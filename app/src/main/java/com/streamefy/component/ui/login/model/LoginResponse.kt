package com.streamefy.component.ui.login.model


import com.google.gson.annotations.SerializedName

data class Response(
    @SerializedName("expiration")
    val expiration: String = "",
    @SerializedName("accessToken")
    val accessToken: String = "",
    @SerializedName("refreshToken")
    val refreshToken: String = ""
)


data class LoginResponse(
    @SerializedName("response")
    val response: Response,
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


