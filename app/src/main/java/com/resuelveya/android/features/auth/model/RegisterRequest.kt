package com.resuelveya.android.features.auth.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("firebaseToken")
    val firebaseToken: String,

    @SerializedName("rol")
    val rol: String? = null,

    @SerializedName("nombre")
    val nombre: String? = null,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("direccion")
    val direccion: String? = null,

    @SerializedName("ciudad")
    val ciudad: String? = null
)