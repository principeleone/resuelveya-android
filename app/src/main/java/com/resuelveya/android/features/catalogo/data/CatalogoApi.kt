package com.resuelveya.android.features.catalogo.data

import com.resuelveya.android.features.catalogo.model.Categoria
import com.resuelveya.android.features.catalogo.model.PageResponse
import com.resuelveya.android.features.catalogo.model.ServicioPublico
import com.resuelveya.android.features.catalogo.model.TecnicoDetalleResponse
import com.resuelveya.android.features.catalogo.model.TecnicoPublico
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CatalogoApi {

    @GET("api/v1/categorias")
    fun getCategorias(): Call<List<Categoria>>

    @GET("api/v1/public/servicios")
    fun buscarServicios(
        @Query("q") query: String,
        @Query("size") size: Int = 20
    ): Call<PageResponse<ServicioPublico>>

    @GET("api/v1/public/tecnicos")
    fun getTecnicosPorCategoria(
        @Query("categoriaId") categoriaId: Long,
        @Query("size") size: Int = 30
    ): Call<PageResponse<TecnicoPublico>>

    @GET("api/v1/tecnicos/{id}/completo")
    fun getTecnicoCompleto(
        @Path("id") id: Long
    ): Call<TecnicoDetalleResponse>
}