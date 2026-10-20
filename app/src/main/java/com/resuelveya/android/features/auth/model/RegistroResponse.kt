package com.resuelveya.android.features.auth.model

import com.google.gson.annotations.SerializedName

data class RegistroResponse(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("nombre")
    val nombre: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("fotoUrl")
    val fotoUrl: String? = null,

    @SerializedName("direccion")
    val direccion: String? = null,

    @SerializedName("distrito")
    val distrito: String? = null,

    @SerializedName("ciudad")
    val ciudad: String? = null,

    @SerializedName("rol")
    val rol: String? = null
)
