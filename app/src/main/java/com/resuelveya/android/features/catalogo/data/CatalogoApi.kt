package com.resuelveya.android.features.catalogo.data

import com.resuelveya.android.features.catalogo.model.Categoria
import retrofit2.Call
import retrofit2.http.GET

interface CatalogoApi {

    @GET("api/v1/categorias")
    fun getCategorias(): Call<List<Categoria>>
}