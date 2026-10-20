package com.resuelveya.android.features.auth.model

import com.google.gson.annotations.SerializedName

data class RegistroRequest(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("telefono")
    val telefono: String,

    @SerializedName("rol")
    val rol: String
)
