package com.resuelveya.android.features.auth.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("firebaseToken")
    val firebaseToken: String,

    @SerializedName("rol")
    val rol: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("telefono")
    val telefono: String,

    @SerializedName("direccion")
    val direccion: String,

    @SerializedName("ciudad")
    val ciudad: String
)