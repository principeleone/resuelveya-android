package com.resuelveya.android.features.proveedor.controller

import com.google.gson.annotations.SerializedName

data class ServicioProveedor(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("titulo")
    val titulo: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("precioEstimado")
    val precioEstimado: Double,

    @SerializedName("tiempoEstimado")
    val tiempoEstimado: String,

    @SerializedName("categoriaId")
    val categoriaId: Long? = null,

    @SerializedName("categoriaNombre")
    val categoriaNombre: String? = null,

    @SerializedName("activo")
    val activo: Boolean = true,

    @SerializedName("tecnicoId")
    val tecnicoId: Long? = null,

    @SerializedName("tecnicoNombre")
    val tecnicoNombre: String? = null
)

data class ServicioRequest(
    @SerializedName("titulo")
    val titulo: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("precioEstimado")
    val precioEstimado: Double,

    @SerializedName("tiempoEstimado")
    val tiempoEstimado: String,

    @SerializedName("categoriaId")
    val categoriaId: Long,

    @SerializedName("activo")
    val activo: Boolean = true
)

data class CategoriaResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String? = null
)