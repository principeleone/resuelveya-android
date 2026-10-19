package com.resuelveya.android.features.catalogo.model

import com.google.gson.annotations.SerializedName

data class SolicitudRequest(
    @SerializedName("servicioId")
    val servicioId: Long,

    @SerializedName("tecnicoId")
    val tecnicoId: Long,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("direccionAtencion")
    val direccionAtencion: String,

    @SerializedName("latitud")
    val latitud: Double? = -12.046374,

    @SerializedName("longitud")
    val longitud: Double? = -77.042793,

    @SerializedName("fechaProgramada")
    val fechaProgramada: String,

    @SerializedName("precioAcordado")
    val precioAcordado: Double
)

data class SolicitudResponse(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("servicioId")
    val servicioId: Long? = null,

    @SerializedName("servicioTitulo")
    val servicioTitulo: String? = null,

    @SerializedName("tecnicoId")
    val tecnicoId: Long? = null,

    @SerializedName("tecnicoNombre")
    val tecnicoNombre: String? = null,

    @SerializedName("clienteId")
    val clienteId: Long? = null,

    @SerializedName("clienteNombre")
    val clienteNombre: String? = null,

    @SerializedName("descripcion")
    val descripcion: String? = null,

    @SerializedName("direccionAtencion")
    val direccionAtencion: String? = null,

    @SerializedName("fechaProgramada")
    val fechaProgramada: String? = null,

    @SerializedName("precioAcordado")
    val precioAcordado: Double? = null,

    @SerializedName("estado")
    val estado: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null
)
