package com.resuelveya.android.features.catalogo.model

import com.google.gson.annotations.SerializedName

data class ServicioPublico(
    @SerializedName("id")
    val id: Long,

    @SerializedName("titulo")
    val titulo: String,

    @SerializedName("descripcion")
    val descripcion: String? = null,

    @SerializedName("precioEstimado")
    val precioEstimado: Double = 0.0,

    @SerializedName("tiempoEstimado")
    val tiempoEstimado: String? = null,

    @SerializedName("categoriaId")
    val categoriaId: Long? = null,

    @SerializedName("categoriaNombre")
    val categoriaNombre: String? = null,

    @SerializedName("tecnicoId")
    val tecnicoId: Long? = null,

    @SerializedName("tecnicoNombre")
    val tecnicoNombre: String? = null,

    @SerializedName("tecnicoFotoUrl")
    val tecnicoFotoUrl: String? = null,

    @SerializedName("tecnicoDistrito")
    val tecnicoDistrito: String? = null,

    @SerializedName("tecnicoCiudad")
    val tecnicoCiudad: String? = null,

    @SerializedName("tecnicoCalificacionPromedio")
    val tecnicoCalificacionPromedio: Double? = null
)
