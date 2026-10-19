package com.resuelveya.android.core.network

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("status")
    val status: Int? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("path")
    val path: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)
