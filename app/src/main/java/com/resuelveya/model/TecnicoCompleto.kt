package com.resuelveya.model

data class TecnicoCompleto(
    val id: Long,
    val nombre: String,
    val email: String?,
    val telefono: String?,
    val fotoUrl: String?,
    val direccion: String?,
    val distrito: String?,
    val ciudad: String?,
    val presentacion: String?,
    val aniosExperiencia: Int?,
    val calificacionPromedio: Double?,
    val yapeNumero: String?,
    val plinNumero: String?,
    val titularPago: String?,
    val especialidadId: Long?,
    val especialidadNombre: String?,
    val servicios: List<Servicio>?,
    val resenias: List<Resenia>?
)
