package com.resuelveya.android.features.proveedor.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
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
    }

    override fun onResume() {
        super.onResume()
        loadSolicitudes()
    }

    private fun setupRecyclerView() {
        adapter = SolicitudRecibidaAdapter(
            onItemClick = { solicitud ->
                navigateToDetalle(solicitud)
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
                        // Guardar en Room para acceso offline respetando estados locales
                        syncManager.guardarContratosLocales(lista, esProveedor = true) {
                            cargarSoloLocales()
                        }
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

    private fun showEmptyState() {
        binding.rvSolicitudes.visibility = View.GONE
        binding.ivIconoVacio.visibility = View.VISIBLE
        binding.tvVacioSolicitudes.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToDetalle(solicitud: SolicitudResponse) {
        val bundle = bundleOf(
            "solicitudId" to (solicitud.id ?: -1L),
            "clienteNombre" to (solicitud.clienteNombre ?: ""),
            "servicioTitulo" to (solicitud.servicioTitulo ?: solicitud.descripcion ?: ""),
            "descripcion" to (solicitud.descripcion ?: ""),
            "fechaProgramada" to (solicitud.fechaProgramada ?: ""),
            "direccionAtencion" to (solicitud.direccionAtencion ?: ""),
            "precioAcordado" to (solicitud.precioAcordado?.toFloat() ?: 0f),
            "estado" to (solicitud.estado ?: "ACEPTADA")
        )
        findNavController().navigate(
            R.id.action_solicitudes_to_detalle,
            bundle
        )
    }

    companion object {
        private const val TAG = "SolicitudesRecibidasFrag"
    }
}