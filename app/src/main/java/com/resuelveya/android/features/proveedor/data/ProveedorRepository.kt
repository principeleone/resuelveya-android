package com.resuelveya.android.features.proveedor.data

import com.resuelveya.android.core.network.RetrofitClient
import com.resuelveya.android.features.proveedor.controller.CategoriaResponse
import com.resuelveya.android.features.proveedor.controller.ServicioProveedor
import com.resuelveya.android.features.proveedor.controller.ServicioRequest
import retrofit2.Callback

class ProveedorRepository {

    private val proveedorApi: ProveedorApi = RetrofitClient.createService(ProveedorApi::class.java)

    fun getMisServicios(callback: Callback<List<ServicioProveedor>>) {
        proveedorApi.getMisServicios().enqueue(callback)
    }

    fun crearServicio(request: ServicioRequest, callback: Callback<ServicioProveedor>) {
        proveedorApi.crearServicio(request).enqueue(callback)
    }

    fun actualizarServicio(
        id: Long,
        request: ServicioRequest,
        callback: Callback<ServicioProveedor>
    ) {
        proveedorApi.actualizarServicio(id, request).enqueue(callback)
    }

    fun eliminarServicio(id: Long, callback: Callback<Void>) {
        proveedorApi.eliminarServicio(id).enqueue(callback)
    }

    fun getCategorias(callback: Callback<List<CategoriaResponse>>) {
        proveedorApi.getCategorias().enqueue(callback)
    }

    fun getSolicitudesTecnico(estado: String? = null, callback: Callback<List<com.resuelveya.android.features.catalogo.model.SolicitudResponse>>) {
        proveedorApi.getSolicitudesTecnico(estado).enqueue(callback)
    }

    fun aceptarSolicitud(id: Long, callback: Callback<com.resuelveya.android.features.catalogo.model.SolicitudResponse>) {
        proveedorApi.aceptarSolicitud(id).enqueue(callback)
    }

    fun iniciarSolicitud(id: Long, callback: Callback<com.resuelveya.android.features.catalogo.model.SolicitudResponse>) {
        proveedorApi.iniciarSolicitud(id).enqueue(callback)
    }

    fun rechazarSolicitud(id: Long, callback: Callback<com.resuelveya.android.features.catalogo.model.SolicitudResponse>) {
        proveedorApi.rechazarSolicitud(id).enqueue(callback)
    }

    fun completarSolicitud(id: Long, callback: Callback<com.resuelveya.android.features.catalogo.model.SolicitudResponse>) {
        proveedorApi.completarSolicitud(id).enqueue(callback)
    }
}