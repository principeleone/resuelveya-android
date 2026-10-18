package com.resuelveya.features.home

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class ServiciosPaginados(
    val content: List<ServicioDto> = emptyList(),
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val number: Int = 0
)

data class ServicioDto(
    val id: Long,
    val titulo: String,
    val descripcion: String?,
    val precioEstimado: Double?,
    val tiempoEstimado: String?,
    val categoriaNombre: String?,
    val tecnicoNombre: String?,
    val tecnicoCiudad: String?,
    val calificacionPromedio: Double?,
    val totalResenias: Int?,
    @SerializedName("tecnicoCalificacionPromedio")
    val calificacionTecnico: Double?
)

interface ServicioApi {
    @GET("api/v1/public/servicios")
    fun listarServicios(
        @Query("q") busqueda: String? = null,
        @Query("categoriaId") categoriaId: Long? = null,
        @Query("page") pagina: Int = 0,
        @Query("size") tamano: Int = 10,
        @Query("sort") ordenar: String? = null
    ): Call<ServiciosPaginados>
}