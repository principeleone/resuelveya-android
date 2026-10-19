package com.resuelveya.android.features.proveedor.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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

        setupRecyclerView()
        loadSolicitudes()
    }

    private fun setupRecyclerView() {
        adapter = SolicitudRecibidaAdapter(
            onItemClick = { solicitud ->
                Toast.makeText(
                    requireContext(),
                    "${solicitud.clienteNombre ?: "Cliente"} - ${solicitud.estado ?: "PENDIENTE"}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onAceptarClick = { solicitud ->
                solicitud.id?.let { id -> aceptarSolicitud(id) }
            },
            onRechazarClick = { solicitud ->
                solicitud.id?.let { id -> rechazarSolicitud(id) }
            }
        )

        binding.rvSolicitudes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSolicitudes.adapter = adapter
    }

    private fun loadSolicitudes() {
        binding.progress.visibility = View.VISIBLE
        binding.rvSolicitudes.visibility = View.GONE
        binding.ivIconoVacio.visibility = View.GONE
        binding.tvVacioSolicitudes.visibility = View.GONE

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
                    } else {
                        showEmptyState()
                    }
                } else {
                    showEmptyState()
                }
            }

            override fun onFailure(call: Call<List<SolicitudResponse>>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                showEmptyState()
                Log.e(TAG, "Error cargando solicitudes recibidas", t)
            }
        })
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