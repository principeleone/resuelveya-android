package com.resuelveya.network

import com.resuelveya.model.Especialidad
import retrofit2.http.GET
import com.resuelveya.model.PageResponse
import com.resuelveya.model.Tecnico
import retrofit2.Response
import retrofit2.http.Query
import com.resuelveya.model.LoginRequest
import com.resuelveya.model.LoginResponse
import com.resuelveya.model.TecnicoCompleto
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.POST
interface ApiService {

    @GET("api/v1/categorias")
    suspend fun getEspecialidades(): List<Especialidad>

    @GET("api/v1/public/tecnicos")
    suspend fun listarTecnicos(
        @Query("categoriaId") categoriaId: Long?,
        @Query("nombre") nombre: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<Tecnico>>

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/v1/tecnicos/{id}/completo")
    suspend fun getTecnicoCompleto(@Path("id") id: Long): TecnicoCompleto
}