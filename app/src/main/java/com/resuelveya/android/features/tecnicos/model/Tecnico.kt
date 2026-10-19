package com.resuelveya.android.features.tecnicos.model

data class Tecnico(
    val id: Long,
    val nombre: String,
    val fotoUrl: String?,
    val distrito: String?,
    val ciudad: String?,
    val presentacion: String?,
    val aniosExperiencia: Int?,
    val calificacionPromedio: Double?,
    val totalResenias: Int?,
    val especialidadId: Long?,
    val especialidadNombre: String?
)
