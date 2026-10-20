package com.resuelveya.android.core.database

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.resuelveya.android.core.database.entity.ContratoEntity
import com.resuelveya.android.core.network.RetrofitClient
import com.resuelveya.android.features.catalogo.model.SolicitudResponse
import com.resuelveya.android.features.proveedor.data.ProveedorApi
import java.util.concurrent.Executors

class ContratoSyncManager(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.contratoDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val proveedorApi: ProveedorApi = RetrofitClient.createService(ProveedorApi::class.java)

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun guardarContratosLocales(
        contratos: List<SolicitudResponse>,
        esProveedor: Boolean,
        onComplete: (() -> Unit)? = null
    ) {
        executor.execute {
            try {
                val entities = contratos.map { response ->
                    val existing = response.id?.let { dao.getContratoById(it) }
                    if (existing != null) {
                        // Preservar estado local si aún no se sincronizó o si está EN_PROCESO localmente
                        val nuevoEstado = if (existing.estado == "EN_PROCESO" && (response.estado == "ACEPTADA" || response.estado == null)) {
                            "EN_PROCESO"
                        } else {
                            response.estado ?: existing.estado
                        }
                        val sync = if (existing.syncStatus.startsWith("PENDING")) existing.syncStatus else "SYNCED"
                        existing.copy(
                            estado = nuevoEstado,
                            clienteNombre = response.clienteNombre ?: existing.clienteNombre,
                            servicioTitulo = response.servicioTitulo ?: existing.servicioTitulo,
                            descripcion = response.descripcion ?: existing.descripcion,
                            direccionAtencion = response.direccionAtencion ?: existing.direccionAtencion,
                            fechaProgramada = response.fechaProgramada ?: existing.fechaProgramada,
                            precioAcordado = response.precioAcordado ?: existing.precioAcordado,
                            syncStatus = sync,
                            updatedAt = System.currentTimeMillis()
                        )
                    } else {
                        ContratoEntity.fromSolicitudResponse(response, esProveedor = esProveedor, syncStatus = "SYNCED")
                    }
                }
                dao.insertAll(entities)
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando contratos locales", e)
            } finally {
                mainHandler.post { onComplete?.invoke() }
            }
        }
    }

    fun obtenerContratosLocales(
        esProveedor: Boolean,
        onResult: (List<ContratoEntity>) -> Unit
    ) {
        executor.execute {
            val lista = try {
                dao.getContratosByRol(esProveedor)
            } catch (e: Exception) {
                Log.e(TAG, "Error leyendo contratos locales", e)
                emptyList()
            }
            mainHandler.post { onResult(lista) }
        }
    }

    fun obtenerContratoPorId(
        id: Long,
        onResult: (ContratoEntity?) -> Unit
    ) {
        executor.execute {
            val contrato = try {
                dao.getContratoById(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error leyendo contrato por id $id", e)
                null
            }
            mainHandler.post { onResult(contrato) }
        }
    }

    fun iniciarContratoLocal(
        solicitudId: Long,
        onSuccess: (ContratoEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        executor.execute {
            val contrato = dao.getContratoById(solicitudId)
            val updated = if (contrato != null) {
                contrato.copy(
                    estado = "EN_PROCESO",
                    syncStatus = "SYNCED",
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                ContratoEntity(
                    id = solicitudId,
                    servicioId = null,
                    servicioTitulo = null,
                    tecnicoId = null,
                    tecnicoNombre = null,
                    clienteId = null,
                    clienteNombre = null,
                    descripcion = null,
                    direccionAtencion = null,
                    fechaProgramada = null,
                    precioAcordado = null,
                    estado = "EN_PROCESO",
                    esProveedor = true,
                    syncStatus = "SYNCED",
                    updatedAt = System.currentTimeMillis()
                )
            }
            dao.insert(updated)

            val finalEntity = dao.getContratoById(solicitudId) ?: updated
            mainHandler.post { onSuccess(finalEntity) }
        }
    }

    fun finalizarContratoLocal(
        solicitudId: Long,
        onSuccess: (ContratoEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        executor.execute {
            val contrato = dao.getContratoById(solicitudId)
            if (contrato == null) {
                mainHandler.post { onError("Contrato no encontrado en base de datos local") }
                return@execute
            }

            val syncStatus = if (isOnline()) "SYNCED" else "PENDING_FINALIZAR"
            val updated = contrato.copy(
                estado = "FINALIZADA",
                syncStatus = syncStatus,
                updatedAt = System.currentTimeMillis()
            )
            dao.insert(updated)

            if (isOnline()) {
                try {
                    val response = proveedorApi.completarSolicitud(solicitudId).execute()
                    if (!response.isSuccessful) {
                        dao.updateEstado(solicitudId, "FINALIZADA", "PENDING_FINALIZAR")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error de red al sincronizar finalización de contrato", e)
                    dao.updateEstado(solicitudId, "FINALIZADA", "PENDING_FINALIZAR")
                }
            }

            val finalEntity = dao.getContratoById(solicitudId) ?: updated
            mainHandler.post { onSuccess(finalEntity) }
        }
    }

    fun sincronizarPendientes(onFinished: (Int) -> Unit) {
        if (!isOnline()) {
            onFinished(0)
            return
        }

        executor.execute {
            val pendientes = dao.getPendingSyncContratos()
            var sincronizados = 0

            for (p in pendientes) {
                try {
                    when (p.syncStatus) {
                        "PENDING_INICIAR" -> {
                            val resp = proveedorApi.aceptarSolicitud(p.id).execute()
                            if (resp.isSuccessful) {
                                dao.updateEstado(p.id, "EN_PROCESO", "SYNCED")
                                sincronizados++
                            }
                        }
                        "PENDING_FINALIZAR" -> {
                            val resp = proveedorApi.completarSolicitud(p.id).execute()
                            if (resp.isSuccessful) {
                                dao.updateEstado(p.id, "FINALIZADA", "SYNCED")
                                sincronizados++
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sincronizando solicitud ${p.id}", e)
                }
            }

            mainHandler.post { onFinished(sincronizados) }
        }
    }

    companion object {
        private const val TAG = "ContratoSyncManager"
    }
}
