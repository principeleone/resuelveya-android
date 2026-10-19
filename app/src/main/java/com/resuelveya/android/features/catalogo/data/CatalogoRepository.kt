package com.resuelveya.android.features.catalogo.data

import com.resuelveya.android.core.network.RetrofitClient
import com.resuelveya.android.features.catalogo.model.Categoria
import retrofit2.Callback

class CatalogoRepository {

    private val catalogoApi: CatalogoApi = RetrofitClient.createService(CatalogoApi::class.java)

    fun getCategorias(callback: Callback<List<Categoria>>) {
        catalogoApi.getCategorias().enqueue(callback)
    }
}