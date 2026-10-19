package com.resuelveya.android.features.tecnicos.model

data class Resenia(
    val id: Long,
    val calificacion: Int,
    val comentario: String?,
    val clienteId: Long?,
    val clienteNombre: String?,
    val clienteFotoUrl: String?,
    val tecnicoId: Long?,
    val createdAt: String?
)

