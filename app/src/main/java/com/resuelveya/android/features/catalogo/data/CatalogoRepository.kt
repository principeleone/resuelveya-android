package com.resuelveya.android.features.catalogo.data

import com.resuelveya.android.core.network.RetrofitClient
import com.resuelveya.android.features.catalogo.model.Categoria
import com.resuelveya.android.features.catalogo.model.PageResponse
import com.resuelveya.android.features.catalogo.model.ServicioPublico
import com.resuelveya.android.features.catalogo.model.SolicitudRequest
import com.resuelveya.android.features.catalogo.model.SolicitudResponse
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

    fun getTecnicos(
        categoriaId: Long? = null,
        nombre: String? = null,
        size: Int = 50,
        callback: Callback<PageResponse<TecnicoPublico>>
    ) {
        catalogoApi.getTecnicos(categoriaId, nombre, size).enqueue(callback)
    }

    fun getTecnicosPorCategoria(categoriaId: Long, callback: Callback<PageResponse<TecnicoPublico>>) {
        catalogoApi.getTecnicosPorCategoria(categoriaId).enqueue(callback)
    }

    fun getTecnicoCompleto(id: Long, callback: Callback<TecnicoDetalleResponse>) {
        catalogoApi.getTecnicoCompleto(id).enqueue(callback)
    }

    fun crearSolicitud(request: SolicitudRequest, callback: Callback<SolicitudResponse>) {
        catalogoApi.crearSolicitud(request).enqueue(callback)
    }

    fun getMisSolicitudes(callback: Callback<List<SolicitudResponse>>) {
        catalogoApi.getMisSolicitudes().enqueue(callback)
    }
}