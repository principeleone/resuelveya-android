package com.resuelveya.android.features.perfil.model

import com.google.gson.annotations.SerializedName

data class PerfilResponse(
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
    val rol: String? = null,

    // Campos exclusivos de rol TECNICO
    @SerializedName("presentacion")
    val presentacion: String? = null,

    @SerializedName("aniosExperiencia")
    val aniosExperiencia: Int? = null,

    @SerializedName("calificacionPromedio")
    val calificacionPromedio: Double? = null,

    @SerializedName("yapeNumero")
    val yapeNumero: String? = null,

    @SerializedName("plinNumero")
    val plinNumero: String? = null,

    @SerializedName("titularPago")
    val titularPago: String? = null,

    @SerializedName("especialidadId")
    val especialidadId: Long? = null,

    @SerializedName("especialidadNombre")
    val especialidadNombre: String? = null
)

data class PerfilUpdateRequest(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("telefono")
    val telefono: String?,

    @SerializedName("fotoUrl")
    val fotoUrl: String? = null,

    @SerializedName("direccion")
    val direccion: String?,

    @SerializedName("distrito")
    val distrito: String?,

    @SerializedName("ciudad")
    val ciudad: String?
)

data class TecnicoPerfilUpdateRequest(
    @SerializedName("presentacion")
    val presentacion: String?,

    @SerializedName("aniosExperiencia")
    val aniosExperiencia: Int?,

    @SerializedName("especialidadId")
    val especialidadId: Long? = null,

    @SerializedName("yapeNumero")
    val yapeNumero: String?,

    @SerializedName("plinNumero")
    val plinNumero: String?,

    @SerializedName("titularPago")
    val titularPago: String?,

    @SerializedName("fotoUrl")
    val fotoUrl: String? = null
)