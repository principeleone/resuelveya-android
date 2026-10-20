package com.resuelveya.android.features.proveedor.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.resuelveya.android.R
import com.resuelveya.android.core.database.ContratoSyncManager
import com.resuelveya.android.databinding.FragmentSolicitudesRecibidasBinding
import com.resuelveya.android.features.catalogo.model.SolicitudResponse
import com.resuelveya.android.features.proveedor.adapter.SolicitudRecibidaAdapter
import com.resuelveya.android.features.proveedor.data.ProveedorRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SolicitudesRecibidasFragment : Fragment() {

    private var _binding: FragmentSolicitudesRecibidasBinding? = null
    private val binding get() = _binding!!

    private val repository = ProveedorRepository()
    private lateinit var syncManager: ContratoSyncManager
    private lateinit var adapter: SolicitudRecibidaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSolicitudesRecibidasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        syncManager = ContratoSyncManager(requireContext())
        setupRecyclerView()
        loadSolicitudes()
    }

    private fun setupRecyclerView() {
        adapter = SolicitudRecibidaAdapter(
            onItemClick = { solicitud ->
                val bundle = Bundle().apply {
                    putLong("solicitudId", solicitud.id ?: -1L)
                    putString("clienteNombre", solicitud.clienteNombre.orEmpty())
                    putString("servicioTitulo", solicitud.servicioTitulo.orEmpty())
                    putString("descripcion", solicitud.descripcion.orEmpty())
                    putString("fechaProgramada", solicitud.fechaProgramada.orEmpty())
                    putString("direccionAtencion", solicitud.direccionAtencion.orEmpty())
                    putFloat("precioAcordado", solicitud.precioAcordado?.toFloat() ?: 0f)
                    putString("estado", solicitud.estado ?: "PENDIENTE")
                }
                findNavController().navigate(R.id.action_solicitudes_to_detalle, bundle)
            }
        )

        binding.rvSolicitudes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSolicitudes.adapter = adapter
    }

    private fun loadSolicitudes() {
        binding.progress.visibility = View.VISIBLE

        // 1. Cargar inmediatamente desde Room (modo offline-first)
        syncManager.obtenerContratosLocales(esProveedor = true) { locales ->
            if (_binding == null) return@obtenerContratosLocales
            if (locales.isNotEmpty()) {
                val respuestas = locales.map { it.toSolicitudResponse() }
                binding.rvSolicitudes.visibility = View.VISIBLE
                binding.ivIconoVacio.visibility = View.GONE
                binding.tvVacioSolicitudes.visibility = View.GONE
                adapter.updateData(respuestas)
            }
        }

        // 2. Si hay conexión, sincronizar pendientes y refrescar desde el backend
        if (syncManager.isOnline()) {
            syncManager.sincronizarPendientes { sincronizados ->
                if (sincronizados > 0 && context != null) {
                    Toast.makeText(requireContext(), "Se sincronizaron $sincronizados cambio(s) de estado", Toast.LENGTH_SHORT).show()
                }
                fetchRemoteSolicitudes()
            }
        } else {
            binding.progress.visibility = View.GONE
            syncManager.obtenerContratosLocales(esProveedor = true) { locales ->
                if (_binding == null) return@obtenerContratosLocales
                if (locales.isEmpty()) {
                    showEmptyState()
                } else {
                    Toast.makeText(requireContext(), "Modo offline: mostrando datos guardados en el dispositivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchRemoteSolicitudes() {
        repository.getSolicitudesTecnico(null, object : Callback<List<SolicitudResponse>> {
            override fun onResponse(
                call: Call<List<SolicitudResponse>>,
                response: Response<List<SolicitudResponse>>
            ) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE

                if (response.isSuccessful) {
                    val lista = response.body().orEmpty()
                    if (lista.isNotEmpty()) {
                        binding.rvSolicitudes.visibility = View.VISIBLE
                        binding.ivIconoVacio.visibility = View.GONE
                        binding.tvVacioSolicitudes.visibility = View.GONE
                        adapter.updateData(lista)
                        // Guardar en Room para acceso offline
                        syncManager.guardarContratosLocales(lista, esProveedor = true)
                    } else {
                        showEmptyState()
                    }
                } else {
                    cargarSoloLocales()
                }
            }

            override fun onFailure(call: Call<List<SolicitudResponse>>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                cargarSoloLocales()
                Log.e(TAG, "Error cargando solicitudes remotas", t)
            }
        })
    }

    private fun cargarSoloLocales() {
        syncManager.obtenerContratosLocales(esProveedor = true) { locales ->
            if (_binding == null) return@obtenerContratosLocales
            if (locales.isNotEmpty()) {
                val respuestas = locales.map { it.toSolicitudResponse() }
                binding.rvSolicitudes.visibility = View.VISIBLE
                binding.ivIconoVacio.visibility = View.GONE
                binding.tvVacioSolicitudes.visibility = View.GONE
                adapter.updateData(respuestas)
            } else {
                showEmptyState()
            }
        }
    }

    private fun iniciarContrato(id: Long) {
        binding.progress.visibility = View.VISIBLE
        syncManager.iniciarContratoLocal(
            solicitudId = id,
            onSuccess = { _ ->
                if (_binding == null) return@iniciarContratoLocal
                binding.progress.visibility = View.GONE
                val mensaje = if (syncManager.isOnline()) {
                    "Contrato iniciado con éxito"
                } else {
                    "Contrato iniciado (Guardado offline, se enviará al conectar)"
                }
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                cargarSoloLocales()
            },
            onError = { err ->
                if (_binding == null) return@iniciarContratoLocal
                binding.progress.visibility = View.GONE
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun finalizarContrato(id: Long) {
        binding.progress.visibility = View.VISIBLE
        syncManager.finalizarContratoLocal(
            solicitudId = id,
            onSuccess = { _ ->
                if (_binding == null) return@finalizarContratoLocal
                binding.progress.visibility = View.GONE
                val mensaje = if (syncManager.isOnline()) {
                    "Contrato finalizado con éxito"
                } else {
                    "Contrato finalizado (Guardado offline, se enviará al conectar)"
                }
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                cargarSoloLocales()
            },
            onError = { err ->
                if (_binding == null) return@finalizarContratoLocal
                binding.progress.visibility = View.GONE
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun aceptarSolicitud(id: Long) {
        binding.progress.visibility = View.VISIBLE
        repository.aceptarSolicitud(id, object : Callback<SolicitudResponse> {
            override fun onResponse(call: Call<SolicitudResponse>, response: Response<SolicitudResponse>) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Solicitud aceptada", Toast.LENGTH_SHORT).show()
                    loadSolicitudes()
                } else {
                    Toast.makeText(requireContext(), "No se pudo aceptar la solicitud", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SolicitudResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                Toast.makeText(requireContext(), "Error de red al aceptar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun rechazarSolicitud(id: Long) {
        binding.progress.visibility = View.VISIBLE
        repository.rechazarSolicitud(id, object : Callback<SolicitudResponse> {
            override fun onResponse(call: Call<SolicitudResponse>, response: Response<SolicitudResponse>) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                    loadSolicitudes()
                } else {
                    Toast.makeText(requireContext(), "No se pudo rechazar la solicitud", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SolicitudResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                Toast.makeText(requireContext(), "Error de red al rechazar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEmptyState() {
        binding.rvSolicitudes.visibility = View.GONE
        binding.ivIconoVacio.visibility = View.VISIBLE
        binding.tvVacioSolicitudes.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SolicitudesRecibidasFrag"
    }
}