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
    val error: Any = "",
    @SerializedName("isSuccess")
    val isSuccess: Boolean = false
)


