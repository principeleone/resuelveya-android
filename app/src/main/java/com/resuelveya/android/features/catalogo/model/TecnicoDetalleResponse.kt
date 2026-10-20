package com.resuelveya.android.features.catalogo.model

import com.google.gson.annotations.SerializedName

data class TecnicoDetalleResponse(
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
    val especialidadNombre: String? = null,

    @SerializedName("servicios")
    val servicios: List<ServicioTarifaItem>? = null,

    @SerializedName("resenias")
    val resenias: List<ReseniaItem>? = null
)

data class ServicioTarifaItem(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("titulo")
    val titulo: String? = null,

    @SerializedName("descripcion")
    val descripcion: String? = null,

    @SerializedName("precioEstimado")
    val precioEstimado: Double? = null,

    @SerializedName("tiempoEstimado")
    val tiempoEstimado: String? = null,

    @SerializedName("activo")
    val activo: Boolean? = true,

    @SerializedName("categoriaId")
    val categoriaId: Long? = null,

    @SerializedName("categoriaNombre")
    val categoriaNombre: String? = null
)

data class ReseniaItem(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("calificacion")
    val calificacion: Int? = 5,

    @SerializedName("comentario")
    val comentario: String? = null,

    @SerializedName("clienteNombre")
    val clienteNombre: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null
)
