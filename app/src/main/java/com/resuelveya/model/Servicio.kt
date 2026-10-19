package com.resuelveya.model

data class Servicio(
    val id: Long,
    val titulo: String,
    val descripcion: String?,
    val precioEstimado: Double?,
    val tiempoEstimado: String?,
    val activo: Boolean?,
    val categoriaId: Long?,
    val categoriaNombre: String?,
    val tecnicoId: Long?,
    val tecnicoNombre: String?,
    val createdAt: String?
)

