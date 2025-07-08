package com.streamefy.component.ui.projects.model.remove


import com.google.gson.annotations.SerializedName

data class RemoveProjectRequest(
    @SerializedName("projectId")
    val projectId: Int,
    @SerializedName("phoneNumber")
    val phoneNumber: String
)