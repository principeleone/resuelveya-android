package com.resuelveya.android.features.tecnicos.network

import com.resuelveya.android.features.tecnicos.model.Especialidad
import retrofit2.http.GET
import com.resuelveya.android.features.tecnicos.model.PageResponse
import com.resuelveya.android.features.tecnicos.model.Tecnico
import retrofit2.Response
import retrofit2.http.Query
import com.resuelveya.android.features.tecnicos.model.LoginRequest
import com.resuelveya.android.features.tecnicos.model.LoginResponse
import com.resuelveya.android.features.tecnicos.model.TecnicoCompleto
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