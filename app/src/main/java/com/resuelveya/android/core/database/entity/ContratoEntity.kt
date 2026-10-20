package com.resuelveya.android.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.resuelveya.android.features.catalogo.model.SolicitudResponse

@Entity(tableName = "contratos")
data class ContratoEntity(
    @PrimaryKey
    val id: Long,
    val servicioId: Long? = null,
    val servicioTitulo: String? = null,
    val tecnicoId: Long? = null,
    val tecnicoNombre: String? = null,
    val clienteId: Long? = null,
    val clienteNombre: String? = null,
    val descripcion: String? = null,
    val precioAcordado: Double? = null,
    val direccionAtencion: String? = null,
    val fechaProgramada: String? = null,
    val estado: String = "PENDIENTE",
    val syncStatus: String = "SYNCED",
    val esProveedor: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toSolicitudResponse(): SolicitudResponse {
        return SolicitudResponse(
            id = id,
            servicioId = servicioId,
            servicioTitulo = servicioTitulo,
            tecnicoId = tecnicoId,
            tecnicoNombre = tecnicoNombre,
            clienteId = clienteId,
            clienteNombre = clienteNombre,
            descripcion = descripcion,
            precioAcordado = precioAcordado,
            direccionAtencion = direccionAtencion,
            fechaProgramada = fechaProgramada,
            estado = estado
        )
    }

    companion object {
        fun fromSolicitudResponse(
            response: SolicitudResponse,
            esProveedor: Boolean,
            syncStatus: String = "SYNCED"
        ): ContratoEntity {
            return ContratoEntity(
                id = response.id ?: 0L,
                servicioId = response.servicioId,
                servicioTitulo = response.servicioTitulo,
                tecnicoId = response.tecnicoId,
                tecnicoNombre = response.tecnicoNombre,
                clienteId = response.clienteId,
                clienteNombre = response.clienteNombre,
                descripcion = response.descripcion,
                precioAcordado = response.precioAcordado,
                direccionAtencion = response.direccionAtencion,
                fechaProgramada = response.fechaProgramada,
                estado = response.estado ?: "PENDIENTE",
                syncStatus = syncStatus,
                esProveedor = esProveedor,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
