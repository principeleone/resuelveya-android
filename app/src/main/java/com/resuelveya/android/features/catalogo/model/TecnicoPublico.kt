package com.resuelveya.android.features.catalogo.model

import com.google.gson.annotations.SerializedName

data class TecnicoPublico(
    @SerializedName("id")
    val id: Long,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("fotoUrl")
    val fotoUrl: String? = null,

    @SerializedName("distrito")
    val distrito: String? = null,

    @SerializedName("ciudad")
    val ciudad: String? = null,

    @SerializedName("presentacion")
    val presentacion: String? = null,

    @SerializedName("aniosExperiencia")
    val aniosExperiencia: Int? = null,

    @SerializedName("calificacionPromedio")
    val calificacionPromedio: Double? = null,

    @SerializedName("totalResenias")
    val totalResenias: Int? = null,

    @SerializedName("especialidadId")
    val especialidadId: Long? = null,

    @SerializedName("especialidadNombre")
    val especialidadNombre: String? = null
)
