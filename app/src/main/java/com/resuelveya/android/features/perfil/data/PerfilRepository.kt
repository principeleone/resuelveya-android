package com.resuelveya.android.features.perfil.data

import com.resuelveya.android.core.network.RetrofitClient
import com.resuelveya.android.features.perfil.model.PerfilResponse
import com.resuelveya.android.features.perfil.model.PerfilUpdateRequest
import com.resuelveya.android.features.perfil.model.TecnicoPerfilUpdateRequest
import retrofit2.Callback

class PerfilRepository {

    private val api: PerfilApi = RetrofitClient.createService(PerfilApi::class.java)

    fun getMiPerfil(callback: Callback<PerfilResponse>) {
        api.getMiPerfil().enqueue(callback)
    }

    fun updatePerfil(request: PerfilUpdateRequest, callback: Callback<PerfilResponse>) {
        api.updatePerfil(request).enqueue(callback)
    }

    fun updateTecnicoPerfil(request: TecnicoPerfilUpdateRequest, callback: Callback<Void>) {
        api.updateTecnicoPerfil(request).enqueue(callback)
    }
}