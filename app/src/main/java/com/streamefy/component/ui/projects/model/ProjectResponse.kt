package com.streamefy.component.ui.projects.model


import com.google.gson.annotations.SerializedName

data class ProjectResponse(
    @SerializedName("response")
    val response: List<ResponseItem>?,
    @SerializedName("error")
    val error: error? = null,
    @SerializedName("isSuccess")
    val isSuccess: Boolean = false
)


data class ResponseItem(
    @SerializedName("thumbnail")
    val thumbnail: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("mediaCount")
    val mediaCount: Int = 0,
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("createDate")
    val createDate: String = "",
    var isLast:Boolean=false
)

data class error(
    @SerializedName("error")
    val error: String = "",
    @SerializedName("errorLocation")
    val errorLocation: String = "",
    @SerializedName("userMessage")
    val userMessage: String = ""
)

