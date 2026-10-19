package com.resuelveya.android.features.proveedor.data

import com.resuelveya.android.features.proveedor.controller.CategoriaResponse
import com.resuelveya.android.features.proveedor.controller.ServicioProveedor
import com.resuelveya.android.features.proveedor.controller.ServicioRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProveedorApi {

    @GET("api/v1/tecnico/servicios")
    fun getMisServicios(): Call<List<ServicioProveedor>>

    @POST("api/v1/tecnico/servicios")
    fun crearServicio(
        @Body request: ServicioRequest
    ): Call<ServicioProveedor>

    @DELETE("api/v1/tecnico/servicios/{id}")
    fun eliminarServicio(
        @Path("id") id: Long
    ): Call<Void>

    @GET("api/v1/categorias")
    fun getCategorias(): Call<List<CategoriaResponse>>
}
