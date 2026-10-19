package com.resuelveya.android.features.catalogo.data

import com.resuelveya.android.core.network.RetrofitClient
import com.resuelveya.android.features.catalogo.model.Categoria
import com.resuelveya.android.features.catalogo.model.PageResponse
import com.resuelveya.android.features.catalogo.model.ServicioPublico
import com.resuelveya.android.features.catalogo.model.TecnicoDetalleResponse
import com.resuelveya.android.features.catalogo.model.TecnicoPublico
import retrofit2.Callback

class CatalogoRepository {

    private val catalogoApi: CatalogoApi = RetrofitClient.createService(CatalogoApi::class.java)

    fun getCategorias(callback: Callback<List<Categoria>>) {
        catalogoApi.getCategorias().enqueue(callback)
    }

    fun buscarServicios(query: String, callback: Callback<PageResponse<ServicioPublico>>) {
        catalogoApi.buscarServicios(query).enqueue(callback)
    }

    fun getTecnicosPorCategoria(categoriaId: Long, callback: Callback<PageResponse<TecnicoPublico>>) {
        catalogoApi.getTecnicosPorCategoria(categoriaId).enqueue(callback)
    }

    fun getTecnicoCompleto(id: Long, callback: Callback<TecnicoDetalleResponse>) {
        catalogoApi.getTecnicoCompleto(id).enqueue(callback)
    }
}