package com.streamefy.component.ui.pin_authentication.model


import com.google.gson.annotations.SerializedName

data class Error(@SerializedName("userMessage")
                 val userMessage: String = "",
                 @SerializedName("developerMessage")
                 val developerMessage: String = "",
                 @SerializedName("error")
                 val error: String = "",
                 @SerializedName("errorLocation")
                 val errorLocation: String = "")


data class ResetPinResponse(@SerializedName("response")
                            val response: String = "",
                            @SerializedName("error")
                            val error: Error,
                            @SerializedName("isSuccess")
                            val isSuccess: Boolean = false)


