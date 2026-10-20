package com.resuelveya.android.features.auth.model

import com.google.gson.annotations.SerializedName

data class FirebaseRegisterRequest(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("correo")
    val correo: String,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("direccion")
    val direccion: String? = null,

    @SerializedName("ciudad")
    val ciudad: String? = null,

    @SerializedName("rol")
    val rol: String
)

