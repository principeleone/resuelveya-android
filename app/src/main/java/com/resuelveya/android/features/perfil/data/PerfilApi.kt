package com.resuelveya.android.features.perfil.data

import com.resuelveya.android.features.perfil.model.PerfilResponse
import com.resuelveya.android.features.perfil.model.PerfilUpdateRequest
import com.resuelveya.android.features.perfil.model.TecnicoPerfilUpdateRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface PerfilApi {

    @GET("api/v1/perfil/me")
    fun getMiPerfil(): Call<PerfilResponse>

    @PUT("api/v1/perfil/me")
    fun updatePerfil(
        @Body request: PerfilUpdateRequest
    ): Call<PerfilResponse>

    @PUT("api/v1/tecnico/perfil")
    fun updateTecnicoPerfil(
        @Body request: TecnicoPerfilUpdateRequest
    ): Call<Void>
}