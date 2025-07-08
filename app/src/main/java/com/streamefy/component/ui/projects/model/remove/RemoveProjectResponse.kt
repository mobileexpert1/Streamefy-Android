package com.streamefy.component.ui.projects.model.remove


import com.google.gson.annotations.SerializedName

data class RemoveProjectResponse(
    @SerializedName("error")
    val error: Error,
    @SerializedName("isSuccess")
    val isSuccess: Boolean,
    @SerializedName("response")
    val response: String
) {
    data class Error(
        @SerializedName("developerMessage")
        val developerMessage: String,
        @SerializedName("error")
        val error: String,
        @SerializedName("errorLocation")
        val errorLocation: String,
        @SerializedName("userMessage")
        val userMessage: String
    )
}